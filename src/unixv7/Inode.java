package unixv7;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class Inode {
	
	public static final int INODE_SIZE = 64;
	public static final int DIRINFO_SIZE = 16;
	
	public int di_mode; //ushort
	public short di_nlink;
	public short di_uid;
	public short di_gid;
	public int di_size;
	public byte[] di_addr = new byte[40];
	public int di_atime;
	public int di_mtime;
	public int di_ctime;
	
	public boolean isDirectory;
	
	private Map<String, Integer> f_table;
	
	public static List<Inode> parse(BlockDevice bd, int b_size) {
		bd.setIndex(2 * BlockDevice.BLOCK_SIZE);
		List<Inode> inodes = new ArrayList<Inode>();
		inodes.add(new Inode()); // dummy
		
		for (int i = 0; i < (BlockDevice.BLOCK_SIZE * (b_size-2) / INODE_SIZE); ++i) {		
		//for (int i = 0; i < 2; ++i) {						
			Inode inode = new Inode();
			inode.di_mode = bd.readUShort();
			inode.di_nlink = bd.readShort();
			inode.di_uid = bd.readShort();
			inode.di_gid = bd.readShort();
			inode.di_size = bd.readInt();
			for (int j = 0; j < inode.di_addr.length; ++j) {
				inode.di_addr[j] = (byte)(bd.readChar() & 0xff);
			}
			inode.di_atime = bd.readInt();
			inode.di_mtime = bd.readInt();
			inode.di_ctime = bd.readInt();			
			inode.isDirectory = ((inode.di_mode >> 12) & 7) == 4;
			inodes.add(inode);
		}		
		return inodes;
	}
	
	public void createIndex(BlockDevice bd) {
		f_table = new HashMap<String, Integer>();
		boolean remaining = true;
		for (int i = 0; i < (di_addr.length / 3); i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			//System.out.printf("offset = 0x%x\n", offset);
			
			for (int j = 0; j < (BlockDevice.BLOCK_SIZE / Inode.DIRINFO_SIZE); ++j) { // j < 32
				if (j >= ((di_size - ((i/3) *BlockDevice.BLOCK_SIZE)) / Inode.DIRINFO_SIZE)) {
					remaining = false;
					break;
				}
				int p = j * Inode.DIRINFO_SIZE + offset;
				bd.setIndex(p);
				int fi_num = bd.readUShort();
				p += 2;
				int end = bd.seekZero();
				String s = new String(bd.getBytes(p, end));
				//System.out.printf("%04x: %s\n", fi_num, s);
				f_table.put(s,  fi_num);
			}			
			if (!remaining) break;
		}
	}
	
	public void showTable() {
		System.out.print("\nBlocks: ");
		for (int i = 0; i < (di_addr.length / 3); i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			if (i * BlockDevice.BLOCK_SIZE > di_size) break;
			System.out.printf("0x%x ", offset);
		}
		System.out.println();
		for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
			System.out.printf("%s : %d\n", entry.getKey(), entry.getValue());
		}
	}
	
	public int getTargetInode(String path) {
		return f_table.containsKey(path) ? f_table.get(path) : -1;
	}
	
	
	public void show() {
		System.out.printf("di_mode = %04x\n", (short)di_mode);
		System.out.printf("di_nlink = %04x\n", di_nlink);
		System.out.printf("di_uid = %04x\n", di_uid);
		System.out.printf("di_gid = %04x\n", di_gid);
		System.out.printf("di_size = %08x\n", di_size);
		System.out.println("di_addr");
		for (int i = 0; i < di_addr.length; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", di_addr[i]);
		}
		System.out.println();
		System.out.printf("di_atime = %08x\n", di_atime);
		System.out.printf("di_mtime = %08x\n", di_mtime);
		System.out.printf("di_ctime = %08x\n", di_ctime);
		System.out.println();
	}
	
	public byte[] extract(BlockDevice bd) {
		return extractDirect(bd);
	}
	
	private byte[] extractDirect(BlockDevice bd) {
		byte[] buf = new byte[di_size];
		int p = 0;
		int remainsize = di_size;
		for (int i = 0; i < (di_addr.length / 3); i += 3) {
			int offset = (di_addr[i] & 0xff | (di_addr[i+1] & 0xff) << 8 | (di_addr[i+2] & 0xff) << 16) * BlockDevice.BLOCK_SIZE;
			if (remainsize < BlockDevice.BLOCK_SIZE) {
				byte[] tmp = bd.getBytes(offset, offset+remainsize);
				System.arraycopy(tmp, 0, buf, p, tmp.length);
				p += tmp.length;
				break;
			} else {
				byte[] tmp = bd.getBytes(offset,  offset+BlockDevice.BLOCK_SIZE);
				System.arraycopy(tmp, 0, buf, p, tmp.length);
				p += tmp.length;
				remainsize -= BlockDevice.BLOCK_SIZE;
			}			
		}			
		return buf;
	}

}
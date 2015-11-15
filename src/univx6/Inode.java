package univx6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Inode {
	
	public static final int INODE_SIZE = 32;
	public static final int DIRINFO_SIZE = 16;
	public int i_mode;
	public char i_nlink;
	public char i_uid;
	public char i_gid;
	public char i_size0;
	public int i_size1; // pointer
	public int i_addr[] = new int[8];
	public int i_atime[] = new int[2];
	public int i_mtime[] = new int[2];
	
	public boolean isDirectory;
	public boolean isLarge;
	public int size;
	private Map<String, Integer> f_table;
	
	
	public static List<Inode> parse(BlockDevice bd, int b_size) {
		//System.out.println("b_size = " + b_size);
		bd.setIndex(2 * BlockDevice.BLOCK_SIZE);		
		List<Inode> inodes = new ArrayList<Inode>();
		inodes.add(new Inode()); // dummy
		
		for (int i = 0; i < (BlockDevice.BLOCK_SIZE * b_size / INODE_SIZE); ++i) {
			Inode inode = new Inode();
			inode.i_mode = bd.readInt();
			inode.i_nlink = bd.readChar();
			inode.i_uid = bd.readChar();
			inode.i_gid = bd.readChar();
			inode.i_size0 = bd.readChar();
			inode.i_size1 = bd.readInt();
			for (int j = 0; j < inode.i_addr.length; ++j) {
				inode.i_addr[j] = bd.readInt();
			}
			for (int j = 0; j < inode.i_atime.length; ++j) {
				inode.i_atime[j] = bd.readInt();
			}
			for (int j = 0; j < inode.i_mtime.length; ++j) {
				inode.i_mtime[j] = bd.readInt();
			}
			
			inode.isDirectory = ((inode.i_mode >>> 14) & 1) == 1;
			inode.isLarge = ((inode.i_mode >>> 12) & 1) == 1;
			inode.size = (inode.i_size0 & 0xff) << 16 | inode.i_size1 & 0xffff;
			
			inodes.add(inode);
		}
		
		return inodes;
	}
	
	public void createIndex(BlockDevice bd) {
		f_table = new HashMap<String, Integer>();
		
		boolean remaining = true;
		for (int i = 0; i < i_addr.length; ++i) {
			int offset = i_addr[i] * BlockDevice.BLOCK_SIZE;
			//System.out.printf("offset: %x\n", offset);
			for (int j = 0; j < (BlockDevice.BLOCK_SIZE / Inode.DIRINFO_SIZE); ++j) { // j < 32
				if (j >= ((size - (i*BlockDevice.BLOCK_SIZE)) / Inode.DIRINFO_SIZE)) {
				//if (j >= (size / Inode.DIRINFO_SIZE)) {						
					remaining = false;
					//System.out.println("Break!!!!");
					break;
				}
				int p = j * Inode.DIRINFO_SIZE + offset; // refer to head address of each directory info
				bd.setIndex(p);				
				int fi_num = bd.readInt();
				p += 2; // increment for file inode number
				int end = bd.seekZero();
				String s = new String(bd.getBytes(p, end));
				//System.out.printf("%04x: %s\n", fi_num, s);
				f_table.put(s, fi_num);
			}
			
			if (!remaining) break;
			//System.out.println("remaining!!!");
		}
	}
	
	public void showTable() {
		System.out.print("Blocks: ");
		for (int i = 0; i < i_addr.length; ++i) {
			if (i * BlockDevice.BLOCK_SIZE > size) break;
			System.out.printf("0x%x ", i_addr[i] * BlockDevice.BLOCK_SIZE);			
		}
		System.out.println();
		
		for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
			System.out.printf("%s : %d\n", entry.getKey(), entry.getValue());
		}
	}
	
	public int getTargetInode(String path) {
		return (f_table.containsKey(path)) ? f_table.get(path) : -1;
	}
	
	public void show() {
		System.out.printf("i_mode = %04x\n", (short)i_mode);
		System.out.printf("i_nlink = %02x\n", (byte)i_nlink);
		System.out.printf("i_nuid = %02x\n", (byte)i_uid);
		System.out.printf("i_ngid = %02x\n", (byte)i_gid);
		System.out.printf("i_size0 = %02x\n", (byte)i_size0);
		System.out.printf("i_size1 = %04x\n", (short)i_size1);
		System.out.println("i_addr");
		for (int i = 0; i < 8; ++i) {
			System.out.printf("%04x ", (short)i_addr[i]);
		}
		System.out.println();
		System.out.println("i_atime");
		for (int i = 0; i < 2; ++i) {
			System.out.printf("%04x ", (short)i_atime[i]);
		}
		System.out.println();
		System.out.println("i_mtime");
		for (int i = 0; i < 2; ++i) {
			System.out.printf("%04x ", (short)i_mtime[i]);
		}		
	}
	
	public byte[] extract(BlockDevice bd) {
		if (isLarge) {
			
		} else {
			return extractDirect(bd);
		}
				
		return null;
	}
	
	private byte[] extractDirect(BlockDevice bd) {
		byte[] buf = new byte[size];
		int p = 0;
		int remainsize = size;
		for (int i = 0; i < i_addr.length; ++i) {
			int offset = i_addr[i] * BlockDevice.BLOCK_SIZE;
			System.out.printf("offset[%d] = 0x%x\n", i, offset);
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

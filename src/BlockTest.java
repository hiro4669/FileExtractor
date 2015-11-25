import java.util.List;

import unixv7.BlockDevice;
import unixv7.FilSys;
import unixv7.RandomBlockDevice;
import unixv7.V7Extractor;


public class BlockTest {
	
	private BlockDevice bd;
	private RandomBlockDevice rbd;
	
	public static void main(String ...args) {		
		BlockTest test = new BlockTest();
		test.process("rp06.disk");
		test.processRandom("rp06.disk");
	}
	
	public void process(String disk) {
		try {
			bd = new BlockDevice(disk);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FilSys filsys = FilSys.parse(bd);
		//filsys.show();
		List<unixv7.Inode> inodes = unixv7.Inode.parse(bd, filsys.s_isize);
		int dummy = 0;
		for (int i = 0; i < inodes.size(); ++i) {
			unixv7.Inode inode = inodes.get(i);
			if (inode.isDirectory) {
				if (++dummy == 1) {
					inode.createIndex(bd);
					inode.showTable();
					break;
				}
			}
		}
		
	}
	
	public void processRandom(String disk) {
		try {
			rbd = new RandomBlockDevice(disk);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);			
		}
		
		System.out.println("\n-----------------");		
		rbd.setIndex(1 * BlockDevice.BLOCK_SIZE);
		System.out.printf("%08x\n", rbd.readInt());
		System.out.printf("%04x\n", rbd.readShort());
		System.out.println("-------------------");
		
		FilSys filsys = FilSys.parse(rbd);
		//filsys.show();
		
		System.out.println("------- dir -------");
		List<unixv7.Inode> inodes = unixv7.Inode.parse(bd, filsys.s_isize);
		int dummy = 0;
		for (int i = 0; i < inodes.size(); ++i) {
			unixv7.Inode inode = inodes.get(i);
			if (inode.isDirectory) {
				if (++dummy == 1) {
					inode.createIndex(rbd);
					inode.showTable();
					break;
				}
			}
		}

		
	}
	
	

}

package unixv7;

import java.io.FileOutputStream;
import java.util.List;

public class V7Extractor {
	
	private BlockDevice bd;
	private List<Inode> inodes;
	
	public static void main(String[] args) {
		String targetName = "";
		String disk = "";
		switch(args.length) {
		case 0: {
			targetName = "/test/a.out";
			disk = "rp06.disk";
			break;
		}
		case 1: {
			disk = "rp06.disk";
			targetName = args[0];
			break;
		}
		case 2: {
			disk = args[0];
			targetName = args[1];
			break;
		}
		default: {
			System.exit(1);
		}
		}
		
		V7Extractor v7e = new V7Extractor();
		v7e.process(disk);
		//v7e.extract("/hoge.txt");
		//v7e.extract("/test/hoge.txt");
		//v7e.extract("/test/hello.s");
		v7e.extract(targetName);
		
	}
	
	public void process(String disk) {
		try {
			bd = new BlockDevice(disk);
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
		
		FilSys filsys = FilSys.parse(bd);
		inodes = Inode.parse(bd, filsys.s_isize);
		int dummy = 0;
		for (int i = 0; i < inodes.size(); ++i) {
			Inode inode = inodes.get(i);
			if (inode.isDirectory) {
				//if (++dummy == 1) {
					//System.out.printf("offset = 0x%x\n", i * 0x40 + 0x400);
					inode.createIndex(bd);
					//inodes.get(i).show();
					//inode.showTable();					
					//break;
				//}
			}
		}		
	}
	
	public void extract(String path) {
		System.out.println("try to find " + path);
		Inode parent = inodes.get(2); // root node
		Inode target = null;
		String[] paths = path.substring(1, path.length()).split("/");
		for (String str : paths) {
			int num = parent.getTargetInode(str);
			if (num == -1) {
				System.out.println("cannot find inode for " + str);
				System.exit(1);
			}
			target = inodes.get(num);
			if (target.isDirectory) {
				parent = target;
			}
		}		
		System.out.println("size = " + target.di_size);
		
		byte[] data = target.extract(bd);
		
		try {
			FileOutputStream fout = new FileOutputStream(paths[paths.length-1]);
			fout.write(data, 0, data.length);
			fout.flush();
			fout.close();
			System.out.println("Write data to > " + paths[paths.length-1]);
		} catch (Exception e) {
			e.printStackTrace();
		}				
	}
}

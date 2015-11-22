package unixv7;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class V7Extractor {
	
	private BlockDevice bd;
	private List<Inode> inodes;
	private List<String> allFiles;
	
	public static void main(String[] args) {
		String targetName = "";
		String disk = "";
		boolean all = false;
		switch(args.length) {
		case 0: {
			//targetName = "/test/a.out";
			//targetName = "/test/result"; // blow 5012 byte
			//targetName = "/test/result2";  
			//targetName = "/fboot";  
			//targetName = "/unix";
			all = true;
			disk = "rp06.disk";
			break;
		}
		case 1: {
			disk = "rp06.disk";
			all = true;
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
		if (all) {
			v7e.extractAll();
		} else {
			v7e.extract(targetName);
		}
				
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
	
	private void extract(Inode node, String pname) {		
		if (node.isDirectory) {
			Map<String, Integer> f_table = node.getTable();
			for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
				String name = entry.getKey();
				int num = entry.getValue();
				if (num > 2 && !name.equals(".") && !name.equals("..")) {
					extract(inodes.get(num), pname + "/" + name);										
				}				
			}
		} else {
			if (node.isRegular) {
				allFiles.add(pname);
			}
		}
	}
	
	public void extractAll() {
		allFiles = new ArrayList<String>();
		Inode root = inodes.get(2);
		extract(root, "");
		for (String s : allFiles) {
			extract(s);
		}
		
	}
	
	public void extract(String path) {
		System.out.println("try to find " + path);
		Inode parent = inodes.get(2); // root node
		Inode target = null;
		int inode_num = 0;
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
			} else {
				//System.out.println("num = " + num);
				inode_num = num;
			}
		}		
		// offset of the target i_node
		//System.out.printf("inode offset = 0x%x\n", (inode_num - 1) * 0x40 + 0x400);
		//System.out.println("size = " + target.di_size);
		//target.show();
		
		//target.extract(bd);
		
		//To extract
		byte[] data = target.extract(bd);
		System.out.println("data length = " + data.length);
		String distDir = "output";
		int pos = path.substring(1,  path.length()).lastIndexOf("/");
		if (pos != -1) {
			String dirName = distDir + path.substring(0, pos+1);
			File dir = new File(dirName);
			if (!dir.exists()) {
				System.out.println("mkdir.. " + dirName);
				dir.mkdirs();
			}
		}
		
		
		try {
			FileOutputStream fout = new FileOutputStream(distDir + path);
			fout.write(data, 0, data.length);
			fout.flush();
			fout.close();
			System.out.println("Write data to > " + distDir + path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
}

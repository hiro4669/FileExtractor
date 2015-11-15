import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;


public class Test {
	
	public static void main(String[] args) {
		try {
			new Test().doit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void doit() throws Exception {
		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		BufferedInputStream bin = new BufferedInputStream(new FileInputStream("v6root"));
		byte buf[] = new byte[1024];
		int count = 0;
		while ((count = bin.read(buf, 0, buf.length)) != -1) {
			bout.write(buf, 0, count);
		}
		
		byte[] data = bout.toByteArray();
		System.out.println(data.length);
		
		/*
		for (int i = 0x200; i < 0x400; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", data[i]);
		}
		*/
		
		// parse super block
		byte[] superblock = new byte[0x200];
		System.arraycopy(data, 0x200, superblock, 0, 0x200);
		FilSys filsys = new FilSys(superblock);
		filsys.parse();
		
		// parse inode
		System.out.println("\ninode bock size = " + filsys.s_isize);
		byte[] rawdata = new byte[0x20];
		int i = 0;
		System.arraycopy(data, i * 0x20 + 0x400, rawdata, 0, 0x20);
		
		Inode inode = new Inode(rawdata);
		inode.parse();
		inode.show();
		System.out.println();
		System.out.println("isDirectory = " + inode.isDirectory);
		System.out.println("isLarge = " + inode.isLarge);
		int size = (inode.i_size0 & 0xff) << 16 | inode.i_size1 & 0xffff;
		System.out.println("size = " + size);
		
		int b_block = inode.i_addr[0];
		int b_addr = b_block * 0x200;
		System.out.printf("b_block = 0x%x\n", b_block);
		System.out.printf("b_addr  = 0x%x\n", b_addr);
		
		boolean remaining = true;
		for (int j = 0; j < 32; ++j) {
			//System.out.println("----------------------");
			//System.out.println("j = " + j);
			if (j >= (size / 16)) {
				remaining = false;
				break;
			}
			int index = j * 16 + b_addr;
			int n_num = data[index++] & 0xff | (data[index++] & 0xff) << 8;
//			System.out.println("n_num = " + n_num);
			int begin = index;			
			for (int k = begin; k < begin + 14; ++k) {
//				System.out.printf("%x ", data[k]);
				if (data[k] == 0) {
//					System.out.print("----end");
					index = k;
					break;
				}
			}
//			System.out.println();
//			System.out.printf("begin = %x\n", begin);
//			System.out.printf("end = %x\n", index);
//			System.out.println("diff = " + (index - begin));
			byte sbuf[] = new byte[index-begin];
			System.arraycopy(data, begin, sbuf, 0, sbuf.length);
			System.out.println(new String(sbuf));
			inode.addTable(new String(sbuf), n_num);
			/*
			for (int k = begin; k < index; ++k) {
				System.out.printf("str = %x\n", (byte)data[k]);
			}
			*/			
		}
				
		if (remaining) {
			// inode_i_addr should be incremented
		}
		
		inode.showTable();
		
		
		
		
	}

}

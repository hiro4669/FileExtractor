
public class FilSys {
	
	public int s_isize;
	public int s_fsize;
	public int s_nfree;
	public int s_free[] = new int[100];
	public int s_ninode;
	public int s_inode[] = new int[100];
	public char s_flock;
	public char s_ilock;
	public char s_fmod;
	public char s_ronly;
	public int s_time[] = new int[2];
	public int pad[] = new int[50];
	
	private int index;
	private byte[] data;
	
	private byte[] tb = {(byte)0x91, (byte)0x0a};
	
	public FilSys(byte[] data) {
		this.data = data;
		index = 0;
	}
	
	
	private int readInt() {
		return (int)(data[index++] & 0xff | (data[index++] & 0xff) << 8);		
	}
	
	private char readChar() {
		return (char)data[index++];
	}
	
	private void show() {
		System.out.printf("s_isize = %04x\n", (short)s_isize);
		System.out.printf("s_fsize = %04x\n", (short)s_fsize);
		System.out.printf("s_nfree = %04x\n", (short)s_nfree);
		System.out.print("s_free");
		for (int i = 0; i < 100; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_free[i]);
		}
		System.out.println();
		System.out.printf("s_ninode = %04x\n", (short)s_ninode);
		
		System.out.print("s_inode");
		for (int i = 0; i < 100; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_inode[i]);
		}
		System.out.println();
		System.out.printf("s_flock = %02x\n", (byte)s_flock);
		System.out.printf("s_ilock = %02x\n", (byte)s_ilock);
		System.out.printf("s_fmod = %02x\n", (byte)s_fmod);
		System.out.printf("s_ronly = %02x\n", (byte)s_ronly);
		
		System.out.print("s_time");
		for (int i = 0; i < 2; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)s_time[i]);
		}
		System.out.print("pad");
		for (int i = 0; i < 48; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)pad[i]);
		}
	}
	
	public void parse() {
		index = 0;
		/*
		System.out.printf("0x%x\n", readInt(data));
		System.out.println(index);
		*/
		
		s_isize = readInt();
		s_fsize = readInt();
		s_nfree = readInt();
		for (int i = 0; i < 100; ++i) {
			s_free[i] = readInt();
		}
		s_ninode = readInt();
		for (int i = 0; i < 100; ++i) {
			s_inode[i] = readInt();
		}
		s_flock = readChar();
		s_ilock = readChar();
		s_fmod = readChar();
		s_ronly = readChar();
		for (int i = 0; i < 2; ++i) {
			s_time[i] = readInt();
		}
		
		for (int i = 0; i < 48; ++i) {
			pad[i] = readInt();
		}
		show();
		
		/*
		for (int i = 0; i < 48; ++i) {
			if (i % 8 == 0) System.out.println();
			System.out.printf("%04x ", (short)pad[i]);
		}
		*/
		
		
		
		
		
		
		/*
		System.out.println("\n");
		for (int i = 0; i < 2; ++i) {
			System.out.printf("%x ", tb[i]);
		}
		*/
		
//		System.out.printf("%04x ", (short)((tb[1] &0xff) << 8 | tb[0] & 0xff));
		//System.out.printf("%04x ", (short)(tb[0] & 0xff | (tb[1] & 0xff) << 8));
		//System.out.printf("index = 0x%x\n", 0x200 + index + 100);
		
		
		/*
		for (int i = 0; i < data.length; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", data[i]);
		}
		*/
	}
	

}

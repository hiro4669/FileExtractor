import java.util.HashMap;
import java.util.Map;


public class Inode {
	
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
	
	private byte[] rawdata;
	private int index;
	private Map<String, Integer> f_table = new HashMap<String, Integer>();
	
	public Inode(byte[] rawdata) {
		this.rawdata = rawdata;
		index = 0;
	}
	
	public void addTable(String file, int inode_num) {
		f_table.put(file, inode_num);
	}
	
	public void showTable() {
		for (Map.Entry<String, Integer> entry : f_table.entrySet()) {
			System.out.printf("%s : %d\n", entry.getKey(), entry.getValue());
		}
	}
	
	private int readInt() {
		return (int)(rawdata[index++] & 0xff | (rawdata[index++] & 0xff) << 8);		
	}
	
	private char readChar() {
		return (char)rawdata[index++];
	}
	
	public void show() {
		for (int i = 0; i < 0x20; ++i) {
			if (i % 16 == 0) System.out.println();
			System.out.printf("%02x ", rawdata[i]);
		}
		System.out.println();
		
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
	
	public void parse() {
		i_mode = readInt();
		i_nlink = readChar();
		i_uid = readChar();
		i_gid = readChar();
		i_size0 = readChar();
		i_size1 = readInt(); // caution!!
		for (int i = 0; i < 8; ++i) {
			i_addr[i] = readInt();
		}
		for (int i = 0; i < 2; ++i) {
			i_atime[i] = readInt();
		}
		for (int i = 0; i < 2; ++i) {
			i_mtime[i] = readInt();
		}
		
		isDirectory = ((i_mode >>> 14) & 1) == 1;
		isLarge = ((i_mode >>> 12) & 1) == 1;		
	}

}

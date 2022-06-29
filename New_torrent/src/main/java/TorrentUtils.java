import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
public class TorrentUtils {
	private static MessageDigest crypt;

	static {
		try {
			crypt = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("this is literally not supposed to happen under any circumstance, wtf");
			e.printStackTrace();
		}
	}
	
	public static byte[] get_SHA1(byte[] arr) {
		crypt.reset();
		crypt.update(arr);
		return crypt.digest();
	}

	private static ByteBuffer bb = ByteBuffer.allocate(1024);
	
	private static void realloc(int last_size, boolean save) {
		if (bb.capacity() < last_size) {
			int newSize = Math.max(last_size, bb.capacity() * 2);
			
			if (save) {
				byte[] curCont = Arrays.copyOf(bb.array(), bb.array().length);
				int sz = bb.position();
				bb = ByteBuffer.allocate(newSize);
				bb.put(curCont, 0, sz);
			} else {
				bb = ByteBuffer.allocate(newSize);
			}
		}
	}
	
	public enum Type {
		REQUEST (5),
		PIECE (6);
		
		public final byte num;
		Type (int n) {
			num = (byte)n;
		}
	}
	
	public static synchronized void send_message(Type type, byte[] cont, SocketChannel sock) throws IOException {
		realloc(cont.length + 5, false);
		
		bb.clear();
		bb.putInt(cont.length);
		bb.put(type.num);
		bb.put(cont);
		
		bb.limit(bb.position());
		bb.position(0);
	
		sock.write(bb);
		
		bb.limit(bb.capacity());
		
	}
	
	public static class Message {
		Type typ;
		byte[] cont;
	}
	
	public static synchronized Message recieve_message(SocketChannel sock) throws IOException {
		bb.clear();
		int len = sock.read(bb);
		if (len <= 4) {
			return null;
		}

		bb.position(0);
		
		int count = bb.getInt();
		byte type_byte = bb.get();
		Type typ = null;
		
		for (Type t : Type.values()) {
			if (type_byte == t.num) { typ = t; break; }
		}
		
		if (typ == null) {
			System.out.println("Unrecognized message type " + type_byte);
			return null;
		}
	
	
		if (len < count) {
			while (true) {
				bb.position(len);
				realloc(count + 5, true);
				
				int readMore = sock.read(bb);
				
				if (readMore <= 0) { break; }
				len += readMore;
				if (len >= count + 5) { break; }
			}
			
		}
		
		Message ret = new Message();
		ret.cont = Arrays.copyOfRange(bb.array(), 5, 5 + count);
		ret.typ = typ;
		return ret;
	}
}

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import com.dampcake.bencode.Bencode;

public class Utils {
	static final Bencode bencode = new Bencode(StandardCharsets.UTF_8, true);
	private static MessageDigest crypt;
	static int DEFAULT_PIECE_SIZE = 1 << 16; //Default piece size

	static {
		try {
			crypt = MessageDigest.getInstance("SHA-1");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("this is literally not supposed to happen under any circumstance, wtf");
			e.printStackTrace();
		}
	}
	
	public static synchronized byte[] getSHA1(byte[] arr, int len) {
		crypt.reset();
		crypt.update(arr, 0, len);
		
		return crypt.digest();
	}
	
	public static byte[] getSHA1(byte[] arr) {
		return getSHA1(arr, arr.length);
	}
	
	public static String SHA1toHex(byte[] sha) {
		Formatter formatter = new Formatter();
		for (byte b : sha) {
			formatter.format("%02x", b);
		}
		
		String hex = formatter.toString();
		formatter.close();
		
		return hex;
	}

	private static ByteBuffer bb = ByteBuffer.allocate(1024);
	
	private static void realloc(int atleast, boolean saveContent) {
		if (bb.capacity() < atleast) {
			int newSize = Math.max(atleast, bb.capacity() * 2);
			
			if (saveContent) {
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
		NEWPIECE (4),
		REQUEST (5),
		PIECE (6),
		AVAILABILITY (7),
		
		;
		
		public final byte num;
		Type (int n) {
			num = (byte)n;
		}
	}
	
	public static synchronized void sendMessage(Type type, byte[] cont, SocketChannel sock) throws IOException {
		realloc(cont.length + 5, false);
		
		bb.clear();
		bb.putInt(cont.length);
		bb.put(type.num);
		bb.put(cont);
		
		bb.flip();

		sock.write(bb);
		//System.out.println("sending " + bb.limit() + "bytes to " + sock.getRemoteAddress());
		bb.limit(bb.capacity());
	}
	
	public static class Message {
		Type typ;
		byte[] cont;
	}
	

	public static synchronized Message[] recvMessage(SocketChannel sock) throws IOException {
		bb.clear();
		ArrayList<Message> queue = new ArrayList<>();
		
		sock.socket().setSoTimeout(500);
		int len = sock.read(bb);
		if (len == 0) { return null; }
		
		// prints here
		if (len <= 4) {
			return null;
		}

		// System.out.println("recv msg: len is " + len);
		bb.position(0);
		
		
		// try to split this huge chunk of SHIT into a bunch of messages
		int leftUnsplit = len - 5;
		
		while (leftUnsplit > 0) {
			int contLen = bb.getInt(); // how long is the current message?

			byte btyp = bb.get(); // message type
			Type typ = null;
			
			for (Type t : Type.values()) {
				// cry about it
				if (btyp == t.num) { typ = t; break; } 
			}
			
			if (typ == null) {
				System.out.printf("Unrecognized type enum from %s (%s); ignoring message%n", sock.getRemoteAddress(), btyp);
				System.out.println("	(length: " + contLen + ")");
				return null;
			}
			
			// if the message length is more than what we've read, then
			// we're probably missing something... keep reading more
			if (len < contLen) {
				while (true) {
					bb.position(len);
					realloc(contLen + 5, true);
					
					int readMore = sock.read(bb);
					
					if (readMore <= 0) { break; }
					len += readMore;
					leftUnsplit += readMore;
					if (len >= contLen + 5) { break; }
				}
			}
			
			leftUnsplit -= contLen;
			//System.out.println("Read msg #%s, left unsplit: %s".formatted(queue.size(), leftUnsplit));
			
			Message ret = new Message();
			ret.cont = Arrays.copyOfRange(bb.array(), 5, 5 + contLen); // TODO: this fucking sucks
			ret.typ = typ;
			
			queue.add(ret);
		}
		
		return queue.toArray(new Message[0]);
	}
	public static byte[] torrent_encode(String fn) throws IOException {
		Path path = Paths.get(fn);
		byte[] fBytes;

		try {
			fBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		HashMap<String, Object> benmap = new HashMap<>();
		HashMap<String, Object> info = new HashMap<>();

		int pieces = (int)Math.ceil((double)fBytes.length / DEFAULT_PIECE_SIZE);
		ByteArrayOutputStream bout = new ByteArrayOutputStream(pieces * 20);

		for (int i = 0; i < fBytes.length; i += DEFAULT_PIECE_SIZE) {
			MessageDigest crypt;
			try {
				crypt = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				System.out.println("what are you, fuckin retarded?");
				e.printStackTrace();
				return null;
			}
			crypt.reset();
			crypt.update(fBytes, i, Math.min(DEFAULT_PIECE_SIZE, fBytes.length - i));

			bout.writeBytes(crypt.digest());
		}

		info.put("pieces", ByteBuffer.wrap(bout.toByteArray()));
		info.put("length", fBytes.length);
		info.put("name", path.getFileName().toString());
		info.put("piece length", DEFAULT_PIECE_SIZE);

		benmap.put("info", info);
		benmap.put("created by", "Yui Hirasawa");
		benmap.put("creation date", 0);

		byte[] out = bencode.encode(benmap);
		Path outPath = Paths.get(fn + ".torrent");
		Files.write(outPath, out);

		byte[] infoBytes = bencode.encode(info);

		return Utils.getSHA1(infoBytes); // we will use the SHA of info for our handshake
	}

}

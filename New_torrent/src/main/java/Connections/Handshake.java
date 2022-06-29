package Connections;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class Handshake {
	static Random rand = new Random();
	static final int PEER_ID_LENGTH = 20;
	static final String PROTOCOL_STRING = "BitTorrent protocol";
	static final String PEER_ID = "-BT0000-";
	static final int RESERVED_LENGTH = 8;
	static final int INFO_HASH_LENGTH = 20;
	static final byte[] Reserved = (new String("\000").repeat(8).getBytes());

	public static byte[] responce_check(byte[] resp) {
		byte len = resp[0];

		// unacceptable: data makes no sense
		if (len > (resp.length - 1) || len == 0) {
			throw new IllegalArgumentException( String.format(
					"Invalid handshake received (received header length '%d' makes no sense)",
					len
				));
		}
		
		if (resp.length < (49 + len - PEER_ID_LENGTH)) {
			throw new IllegalArgumentException( String.format(
				"Invalid handshake received (expected length of at least %d; received %d)",
				49 + len - PEER_ID_LENGTH,
				resp.length
			));
		}
		
		String protocol = new String(Arrays.copyOfRange(resp, 1, PROTOCOL_STRING.length() + 1));
		
		if (!protocol.equals(PROTOCOL_STRING)) {
			return null;
		}

		return Arrays.copyOfRange(resp, 1 + len + RESERVED_LENGTH,
				1 + len + RESERVED_LENGTH + INFO_HASH_LENGTH);
	}
	
	public static ByteBuffer get_handshake(byte[] sha) {
		assert sha.length == 20;
		
		ByteArrayOutputStream buf = new ByteArrayOutputStream();
		try {
			buf.write((byte) PROTOCOL_STRING.length());
			buf.write(PROTOCOL_STRING.getBytes());
			buf.write(Reserved);
			buf.write(sha);
			buf.write(PEER_ID.getBytes());
			for (int i = 0; i < (PEER_ID_LENGTH - PEER_ID.length()); i++) {
				buf.write((byte)(rand.nextInt(10) + '0'));
			}
		} catch (Exception ex) {
			System.out.println("How could this happen?");
			ex.printStackTrace();
			return null;
		}
		
		return ByteBuffer.wrap(buf.toByteArray());
	}
	
}
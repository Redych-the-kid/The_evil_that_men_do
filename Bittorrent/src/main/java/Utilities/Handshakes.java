package Utilities;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Random;

public class Handshakes {
	static Random random = new Random(); //didn't need peer id,so I just fill it with a random
	static final int PEER_ID_LENGTH = 20;
	static final String PROTOCOL_STRING = "BitTorrent protocol";
	static final String PEER_ID = "-BT0000-";
	static final int RESERVED_LENGTH = 8;
	static final int INFO_HASH_LENGTH = 20;
	static final byte[] RESERVED = ("\000".repeat(8).getBytes());
	

	public static byte[] validateResponse(byte[] resp) {
		byte len = resp[0];
		
		// This is unacceptable!
		if (len > (resp.length - 1) || len == 0) {
			throw new IllegalArgumentException( String.format(
					"Invalid handshake received (received header length '%d' makes no sense)",
					len
				));
		}

		//Peer ID's aren't implemented so why bother!
		if (resp.length < (49 + len - PEER_ID_LENGTH)) {
			throw new IllegalArgumentException( String.format(
				"Invalid handshake received (expected length of at least %d; received %d)",
				49 + len - PEER_ID_LENGTH,
				resp.length
			));
		}
		
		String protocol_name = new String(Arrays.copyOfRange(resp, 1, PROTOCOL_STRING.length() + 1));

		//We don't support other protocols except that from the manual
		if (!protocol_name.equals(PROTOCOL_STRING)) {
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
			buf.write(RESERVED);
			buf.write(sha);
			buf.write(PEER_ID.getBytes());
			for (int i = 0; i < (PEER_ID_LENGTH - PEER_ID.length()); i++) {
				buf.write((byte)(random.nextInt(0, 9) + '0'));
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		return ByteBuffer.wrap(buf.toByteArray());
	}
	
}
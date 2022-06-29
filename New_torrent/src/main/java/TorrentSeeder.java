import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.dampcake.bencode.Bencode;

import Connections.Handshake;

public class TorrentSeeder {
	static List<SocketChannel> accepted = new ArrayList<>();
	static List<SocketChannel> handshaken = new ArrayList<>();
	static Map<SocketChannel, Boolean> need_handshake = new HashMap<>();
	
	static final int PIECE_SIZE = 16384;
	static final Bencode bencode = new Bencode(true);
	
	static int file_size = 0;
	static int pieces = 0;
	
	private static byte[] encode(String fn) throws IOException {
		Path path = Paths.get(fn);
		byte[] file_bytes;

		try {
			file_bytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		HashMap<String, Object> benmap = new HashMap<>();
		HashMap<String, Object> info = new HashMap<>();
		
		int pieces = (int)Math.ceil((double)file_bytes.length / PIECE_SIZE);
		ByteArrayOutputStream byte_out = new ByteArrayOutputStream(pieces * 20);
		
		for (int i = 0; i < file_bytes.length; i += PIECE_SIZE) {
			MessageDigest crypt;
			try {
				crypt = MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				System.out.println("How could this happen?!");
				e.printStackTrace();
				return null;
			}
			crypt.reset();
			crypt.update(file_bytes, i, Math.min(16384, file_bytes.length - i));
			
			byte_out.writeBytes(crypt.digest());
			
		}
		
		info.put("pieces", ByteBuffer.wrap(byte_out.toByteArray()));
		info.put("length", file_bytes.length);
		info.put("name", path.getFileName().toString());
		info.put("piece length", PIECE_SIZE);

		benmap.put("info", info);
		benmap.put("created by", "Yui");
		benmap.put("creation date", 0); // ew
		
		file_size = file_bytes.length;
		TorrentSeeder.pieces = pieces;
		byte[] out = bencode.encode(benmap);
		Path output = Paths.get(fn + ".torrent");
		Files.write(output, out);

		byte[] infoBytes = bencode.encode(info);

		return TorrentUtils.get_SHA1(infoBytes);
	}

	private static void accept_connection(SocketChannel con) throws IOException {
		System.out.println("New client: accepting...");
		con.configureBlocking(false);
		handshaken.add(con);
	}
	
	private static byte[] get_pieces(RandomAccessFile fl, int start, int end) throws IOException {
		fl.seek(start * PIECE_SIZE);
		int read_end = (int)Math.min(end * PIECE_SIZE, fl.length());
		int read_start = Math.min(read_end, start * PIECE_SIZE);
		int to_read = read_end - read_start;
		if (to_read <= 0) { return new byte[0]; }
		byte[] buf = new byte[to_read];
		fl.readFully(buf, 0, to_read);
		System.out.println("Sending file piece "+ start + " Size:" + to_read);
		return buf;
	}
	
	private static boolean doHandshake(byte[] ourSHA, SocketChannel con) throws IOException {
		if (!con.finishConnect()) {
			return false;
		}
		ByteBuffer buf = ByteBuffer.allocate(64);
		int read = con.read(buf);
		if (read == -1) {
			con.close();
			handshaken.remove(con);
			System.out.println("Reading error!");
			return false;
		}
		byte[] sha = Handshake.responce_check(buf.array());
		if (!Arrays.equals(sha, ourSHA)) {
			con.close();
			handshaken.remove(con);
			System.out.println("Different hashes!Nothing personal,kid...");
			return false;
		}
		
		System.out.println("Handshake complete!Now accepting...");
		handshaken.remove(con);
		accepted.add(con);
		need_handshake.put(con, true);
		return true;
	}

	public static void start(int port, String fn) throws IOException {
		Thread.currentThread().setName("Torrent - Seeding");
		System.out.println("Seeding: " + fn);
		byte[] OSHA;
		try {
			OSHA = encode(fn); // violation
		} catch (IOException ex) {
			System.out.println("Failed to create .torrent file!" + ex.getMessage());
			return;
		}

		if (OSHA == null) {
			System.out.println("Like how?");
			return;
		}

		ServerSocketChannel serverChannel;
		Selector selector;
		
		RandomAccessFile file = new RandomAccessFile(fn, "r");
		
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(port));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.out.println("Socket binding failure!");
			e.printStackTrace();
			file.close();
			return;
		}

		System.out.println("Seeding socket has been opened!");
		while (true) {
			selector.select();
			Iterator<SelectionKey> iter = selector.selectedKeys().iterator();

			while (iter.hasNext()) {
				SelectionKey key = iter.next();
				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable())
					acception: {
						SocketChannel newCl = serverChannel.accept();
						if (newCl == null) {
							break acception;
						} // exit early

						accept_connection(newCl);
						newCl.register(selector, SelectionKey.OP_READ);
						//continue;
					}

				if (key.isReadable()) ReadState: {
					// got something to read

					SocketChannel cl = (SocketChannel) key.channel();
					
					handshaking: {
						
						if (!handshaken.contains(cl)) {
							break handshaking;
						}
						
						boolean ok = false;
						try {
							ok = doHandshake(OSHA, cl);
						} catch (Exception ex) {
							ex.printStackTrace();
						}
	
						if (!ok) {
							continue;
						}
						
						cl.register(selector, SelectionKey.OP_WRITE);
						continue;
					}

					TorrentUtils.Message msg = TorrentUtils.recieve_message(cl);
					if (msg == null) { break ReadState; }
					
					if (msg.typ == TorrentUtils.Type.REQUEST)
						PieceRequestState: {
							if (!accepted.contains(cl)) {
								break PieceRequestState;
							}

							ByteBuffer pcDat = ByteBuffer.wrap(msg.cont);
							
							int start = pcDat.getInt(), end = pcDat.getInt();

							if (end > pieces) {
								System.out.println("FUCK YOU " + end + "/" + pieces);
								break PieceRequestState;
							}
							byte[] fileCont = get_pieces(file, start, end);
							TorrentUtils.send_message(TorrentUtils.Type.PIECE, fileCont, cl);
						}
				}

				if (key.isWritable()) {
					SocketChannel cl = (SocketChannel) key.channel();
					if (need_handshake.get(cl) != null) {
						ByteBuffer buf = Handshake.get_handshake(OSHA);
						cl.write(buf);
						System.out.println("responded with our own handshake");
						need_handshake.remove(cl);
					}
				}
			}
			
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println("JAVA!WHY I CAN'T SLEEP AT THIS TIME?");
			}
		}
	}
}

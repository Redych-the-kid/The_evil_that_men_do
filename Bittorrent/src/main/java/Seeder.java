import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import Utilities.Handshakes;


/*
  A Server part of a torrent application
 */
public class Seeder {
	List<SocketChannel> handshaken_clients = new ArrayList<>(); // We have accepted them already
	List<SocketChannel> handshake_to_clients = new ArrayList<>(); // We are waiting for a handshake with em'
	
	Map<Integer, Boolean> pieces_available = new HashMap<>(); //Check if we already have the piece
	Map<SocketChannel, Boolean> need_4_handshake = new HashMap<>(); //Do we need 2 send them a handshake?
	
	static final Bencode bencode = new Bencode(true);
	
	static int DEFAULT_PIECE_SIZE = 1 << 16; //Default piece size
	int piece_size = DEFAULT_PIECE_SIZE; //Piece size(damn)
	int file_size = 0;
	int pieces_counter = 0;
	
	public void add_to_available(int num) {
		pieces_available.put(num, true);
		handshaken_clients.forEach((SocketChannel ch) -> {
			byte[] buf = ByteBuffer.allocate(4)
					.putInt(num)
					.array();
			try {
				System.out.println("notify: new piece");
				Utils.sendMessage(Utils.Type.NEWPIECE, buf, ch);
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	/*
		Adds a new client
	 */
	private void accept_connection(SocketChannel connection) throws IOException {
		System.out.println("New client: accepting...");
		connection.configureBlocking(false);
		handshake_to_clients.add(connection);
	}

	/*
		Get the piece from the file by the offset
	 */
	private byte[] get_piece(RandomAccessFile file, int start, int end) throws IOException {
		
		// pieces check
		for (int i = start; i < end; i++) {
			if (!pieces_available.containsKey(i)) { //Leecher requested the impossible
				return null;
			}
		}

		// Move to the piece that we need to send
		file.seek(start * piece_size);

		//Counting how many bytes we need 2 read(cause the last piece can be less than piece_size
		int read_end = (int)Math.min(end * piece_size, file.length());
		int read_start = Math.min(read_end, start * piece_size);
		int to_read = read_end - read_start;
		if (to_read <= 0) { return new byte[0]; }

		//Reading the piece to buffer
		byte[] buf = new byte[to_read];
		file.readFully(buf, 0, to_read);
		
		System.out.println("sending file piece " + start * piece_size + "+" + read_end + " size of " + to_read);

		return buf;
	}

	/*
	  Checks if everything's alright with a handshake
	 */
	private boolean check_handshake(byte[] SHA, SocketChannel connection) throws IOException {
		//No established connection == no handshake!
		if (!connection.finishConnect()) {
			return false;
		}

		ByteBuffer buf = ByteBuffer.allocate(64); // Like in manual
		
		int read;

		try {
			read = connection.read(buf);
		} catch (SocketException e) {
			//Somehow we got this exception
			System.out.println("Socket exception for " + connection.getRemoteAddress());
			return false;
		}

		//Failed to handshake - kill em'!
		if (read == -1) {
			connection.close();
			handshake_to_clients.remove(connection);
			System.out.println("Reading error!Expected something that is not -1!");
			return false;
		}

		byte[] sha = Handshakes.validateResponse(buf.array());
		//Check in hashes are matching
		if (!Arrays.equals(sha, SHA)) {
			connection.close();
			handshake_to_clients.remove(connection);
			assert sha != null;
			System.out.println("Handshake denied!Two hashes are too different!(" + Utils.SHA1toHex(sha) + " compared to " + Utils.SHA1toHex(SHA) + ")");
			return false;
		}
		
		System.out.println("Handshake validation complete for " + connection.getRemoteAddress() + "!Accepting now...");
		handshake_to_clients.remove(connection);
		handshaken_clients.add(connection);
		need_4_handshake.put(connection, true);
		return true;
	}

	/*
		Sending a handshake to the socket
	 */
	private void send_handshake(SocketChannel channel, byte[] SHA) throws IOException {
		ByteBuffer buf = Handshakes.get_handshake(SHA);
		channel.write(buf);
		System.out.println("Responded with a handshake");
		need_4_handshake.remove(channel);

		//Send the data to them, so they know what pieces do we have
		BitSet availDat = new BitSet();
		for (int piece : pieces_available.keySet()) {
			availDat.set(piece, true);
		}
		
		System.out.println("Set of the avaliable pieces (" + availDat.cardinality() + " len) has been sent");

		new java.util.Timer().schedule(new java.util.TimerTask() {
		            @Override
		            public void run() {
		            	try {
							Utils.sendMessage(Utils.Type.AVAILABILITY, availDat.toByteArray(), channel);
						} catch (IOException e) {
							e.printStackTrace();
						}
		            }
		        }, 500
		);
	}

	/*
		Parser for the torrent client
	 */
	public HashMap<String, Object> parse_torrent(String filename) {
		Path path = Paths.get(filename);
		byte[] fBytes;
		try {
			fBytes = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		Map<String, Object> dict = bencode.decode(fBytes, Type.DICTIONARY);
		HashMap<String, Object> info = (HashMap<String, Object>) dict.get("info");
	    if (info == null) {
	    	throw new IllegalArgumentException("Torrent file didn't contain info.");
	    }

	    // see which pieces we actually have
	    String piece_file_name = new String( ((ByteBuffer)info.get("name")).array() );
	    Path torrent_path = path.getParent()
	    		.resolve("downloads")
	    		.resolve(piece_file_name);
	    try {
	    	File file = torrent_path.toFile();
	    	file.getParentFile().mkdirs();
	    	file.createNewFile();
	    }
	    catch (FileAlreadyExistsException ignore) { }
	    catch (IOException e) {
	    	System.out.println("Failed to create file for seeding: " + e.getMessage());
			e.printStackTrace();
		}

	    RandomAccessFile file;

		try {
			file = new RandomAccessFile(torrent_path.toString(), "r");
		} catch (IOException e) {
			System.out.println("Failed to open file for seeding: " + e.getMessage());
			return null;
		}

	    file_size = ((Long)info.get("length")).intValue();
	    pieces_counter = (int)Math.ceil((double) file_size / ((Long)info.get("piece length")).intValue());

	    ByteBuffer pieces = (ByteBuffer)info.get("pieces");

	    int piece_length = ((Long)info.get("piece length")).intValue();
	    byte[] buf = new byte[piece_length]; //buf for piece
		byte[] sha_buf = new byte[20]; // buf for reading every piece SHA from the torrent file

	    try {
	    	for (int i = 0; i < pieces_counter; i++) {
				//Check if EOF
	    		if (file.length() <= (long) i * piece_length) {
	    			break;
	    		}

				//Searching and reading a piece
		    	file.seek((long) i * piece_length);
		    	int read = file.read(buf);

				//getting SHA's
		    	byte[] sha = Utils.getSHA1(buf, read);
		    	pieces.get(i * 20, sha_buf, 0, 20);

				//Compare SHA's from torrent and file
		    	if (Arrays.equals(sha_buf, sha)) {
		    		add_to_available(i);
		    	} else {
		    		System.out.println("hash mismatch for piece #" + i);
		    		System.out.println(Utils.SHA1toHex(sha) + " vs " + Utils.SHA1toHex(sha_buf));
		    	}
		    }
	    } catch (IOException ex) {
	    	System.out.println("Failed to retrieve correct pieces for seeding: " + ex.getMessage());
	    	return null;
	    }
	    return info;
	}

	/*
		Main attraction for Seeder
	 */
	public void start(int port, String torrent_filename) throws IOException {
		Thread.currentThread().setName("Seeder thread");
		System.out.println("Seeding a file named " + torrent_filename);
		HashMap<String, Object> info = parse_torrent(torrent_filename);
		if (info == null) {
			System.out.println("Failed to retain the information!");
			return;
		}
		
		byte[] i_bytes = bencode.encode(info); // information to byte array
	    byte[] OSHA = Utils.getSHA1(i_bytes); //Original SHA
	
		String fn = new String( ((ByteBuffer)info.get("name")).array() );
		ServerSocketChannel serverChannel;
		Selector selector;
		
		Path path = Path.of(torrent_filename).getParent().resolve("downloads");
		
		RandomAccessFile file;
		try {
			file = new RandomAccessFile(path.resolve(fn).toString(), "r");
		} catch (FileNotFoundException ex) {
			System.out.println("Failed to seed the file named " + fn + "!File not found!");
			throw new NullPointerException();
		}
		//Opening a seeding socket
		try {
			serverChannel = ServerSocketChannel.open();
			serverChannel.socket().bind(new InetSocketAddress(port));
			serverChannel.configureBlocking(false);
			selector = Selector.open();
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		} catch (IOException e) {
			System.out.println("Couldnt bind to this socket");
			e.printStackTrace();
			file.close();
			return;
		}

		System.out.println("Server socket has been opened!Now accepting...");
		while (true) {
			selector.select();
			for (SelectionKey key : selector.selectedKeys()) {
				//Why
				if (!key.isValid()) {
					continue;
				}

				if (key.isAcceptable()) // Now we can accept
					Accepting:{
						SocketChannel new_channel = serverChannel.accept();
						if (new_channel == null) {
							break Accepting;
						}

						accept_connection(new_channel);
						new_channel.register(selector, SelectionKey.OP_READ);
					}

				if (key.isReadable()) Reading:{ //Oh, we can get a handshake!
					SocketChannel channel = (SocketChannel) key.channel();
					Handshaking:
					{
						//We don't need those handshakes...
						if (!handshake_to_clients.contains(channel)) {
							break Handshaking;
						}

						boolean ok = false;
						try {
							ok = check_handshake(OSHA, channel);
						} catch (Exception ex) {
							ex.printStackTrace();
							handshake_to_clients.remove(channel);
						}
						//Ignore this connection...
						if (!ok) {
							continue;
						}
						channel.register(selector, SelectionKey.OP_WRITE);
						continue;
					}

					//Messaging time!
					Utils.Message[] messages = Utils.recvMessage(channel);
					if (messages == null) {
						break Reading;
					}

					for (Utils.Message msg : messages) {
						if (msg.typ == Utils.Type.REQUEST)
							Requesting:{
								//We don't need those handshakes... x2
								if (!handshaken_clients.contains(channel)) {
									break Requesting;
								}
								ByteBuffer piece_data = ByteBuffer.wrap(msg.cont);
								int start = piece_data.getInt(), end = piece_data.getInt();
								byte[] send_piece = get_piece(file, start, end);
								if (send_piece == null) {
									System.out.println("File piece denied " + start + " - " + end);
									break Requesting;
								}
								Utils.sendMessage(Utils.Type.PIECE, send_piece, channel);
							}
					}
				}

				//Writing to a connection
				if (key.isWritable()) {
					SocketChannel channel = (SocketChannel) key.channel();
					//Before writing we need to handshake anyways
					if (need_4_handshake.get(channel) != null) {
						send_handshake(channel, OSHA);
					}
				}
			}
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				System.out.println("How I am supposed to sleep in JAVA!!!!!!!!");
			}
		}
	}
}

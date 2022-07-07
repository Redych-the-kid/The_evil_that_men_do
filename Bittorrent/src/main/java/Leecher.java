import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import Utilities.Connection;
import Utilities.Handshakes;

/*
	A client part of a torrent application
 */
public class Leecher {
	static final Bencode bencode = new Bencode(StandardCharsets.UTF_8, true);
	static ThreadPoolExecutor executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
	
	public void on_piece_received(byte[] cont, int num) {
		write_to_file(cont, num, write_file);
	}

	/*
		We are writing in a file rn!
	 */
	private synchronized void write_to_file(byte[] dat, int piece, RandomAccessFile fl) {
		try {
			fl.seek(piece * piece_length);
			fl.write(dat);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
		int curPc = last_written.addAndGet(piece_to_fetch);

		if (curPc == total_pieces) {
			try {
				fl.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/*
		Wait State enum:
		HANDSHAKE - We are handshking with a socket rn
		AVAILABILITY - Getting an availability data from the socket rn
		READY - We are ready for a request
		PIECE - We are waiting for a piece that was requested earlier
	 */
	public enum State {
		WAIT_HANDSHAKE,
		WAIT_AVAILABILITY,
		WAIT_READY,
		WAIT_PIECE,
	}

	/*
		Getters and setters for the socket states
	 */
	public State get_state(SocketChannel socket) {
		return states.get(socket);
	}

	public void set_state(SocketChannel socket, State to) {
		states.put(socket, to);
	}


	/*
		Requesting a piece
	 */
	private void request_piece(SocketChannel connection) {
		int the_piece = get_request_piece(connection);
		waiting_pieces.put(connection, the_piece);
		if (the_piece == -1) {
			set_state(connection, State.WAIT_READY);
			waiting_pieces.put(connection, null);
			return;
		}
		
		ByteBuffer buf = ByteBuffer.allocate(8);

		buf.putInt(the_piece);
		buf.putInt(Math.min(total_pieces, the_piece + piece_to_fetch));

		//Requesting the pieces
		try {
			System.out.printf("Requesting piece №%s from %s%n", the_piece, connection.getRemoteAddress());
			Utils.sendMessage(Utils.Type.REQUEST, buf.array(), connection);
			
			set_state(connection, State.WAIT_PIECE);
		
			response_to_piece.put(connection, (Utils.Message msg) -> {
				on_piece_received(msg.cont, the_piece);
				new java.util.Timer().schedule(new java.util.TimerTask() {
				            @Override
							//request the piece again
				            public void run() {
				            	request_piece(connection);
				            }
				        }, 
				        (int)(Math.random() * 400) 
				);
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
		Functions to reply based on the status:
		on_availability_received - if we receive AVAILABILITY
		on_connection - if we are connected
		on_close - if we lost connection
	 */
	private void on_availability_received(SocketChannel connection) {
		System.out.println("Availability received!");
		executor.submit(() -> {
			var pieces = availability.get(connection);
			if (pieces == null) { throw new NullPointerException("HOW!!!!!!!!"); }
			set_state(connection, State.WAIT_READY);
			return null;
		});
	}
	
	private void on_connection(SocketChannel connection) throws IOException {
		System.out.println("Established connection with a " + connection.getRemoteAddress());
		//Preparing to get available pieces list
		availability.put(connection, new HashMap<> ());
		set_state(connection, State.WAIT_AVAILABILITY);
	}

	private void on_close(SocketChannel chan) {
		var wait = waiting_pieces.get(chan);
		if (wait != null) {
			//put back the piece that we wanted
			wanted_pieces.add(wait);
		}
	}

	/*
		Reading something from the socket
	 */
	private boolean socket_read(SocketChannel channel) throws Exception {
		byte[] seededSHA;
		if (get_state(channel) == State.WAIT_HANDSHAKE) {
			ByteBuffer buf = ByteBuffer.allocate(512);
			int read = channel.read(buf);
			if (read == -1) { System.out.println("Reading from the socket error!"); return true; }

			if (read < 20) {
				throw new Exception("Bad handshake (length: %s, expected: %s)".formatted(read, 20));
			}
			
			seededSHA = Handshakes.validateResponse(buf.array());
			//If not matching-going away!
			if (!Arrays.equals(seededSHA, HSHA)) {
				return false;
			}
			on_connection(channel);
			return true;
		}
		
		if (get_state(channel) == State.WAIT_READY) {
			//Request a piece when ready
			request_piece(channel);
		}

		//Messaging time!
		Utils.Message[] msges;
		try {
			msges = Utils.recvMessage(channel);
		} catch (Exception e) {
			//Socket is dead!Maybe...
			return false;
		}
		
		if (msges == null) { return true; }

		//Message handling(like in Seeder)
		for (Utils.Message msg : msges) {
			//It's a PIECE!
			if (msg.typ == Utils.Type.PIECE) {
				if (response_to_piece.get(channel) != null) {
					response_to_piece.get(channel).accept(msg);
				}
				return true;
			}
			//Receive the available pieces list!
			if (msg.typ == Utils.Type.AVAILABILITY) {
				int expected_length = (int) Math.ceil(total_pieces / 8);
				if (msg.cont.length < expected_length) {
					throw new Exception(
							"Availability data is incorrect!"
						);
				}

				BitSet bitset = BitSet.valueOf(msg.cont);
				bitset.stream().forEach((int bitPos) -> {
					availability.get(channel).put(bitPos, true);
				});
				if (get_state(channel) == State.WAIT_AVAILABILITY) {
					set_state(channel, State.WAIT_READY);
					on_availability_received(channel);
				}
			}

			//Oh, someone has got a new piece!
			if (msg.typ == Utils.Type.NEWPIECE) {
				int piece = ByteBuffer.wrap(msg.cont).getInt();
				availability.get(channel).put(piece, true);
			}
		}
		return true;
	}


	/*
		Main attraction for a Leecher
	 */
	public void start(String torrent, List<String> server_list) throws IOException {
		Thread.currentThread().setName("Leecher thread");
		System.out.println("Downloading " + torrent);
		for (String s : server_list) {
			System.out.println(" from " + s);
		}
		
		Path path = Paths.get(torrent);
		byte[] read;
	
	    try {
			read = Files.readAllBytes(path);
		} catch (IOException e) {
			System.out.println("Failed to read file named " + path + "!");
			return;
		}
	    
	    Map<String, Object> dict = bencode.decode(read, Type.DICTIONARY);

		HashMap<String, Object> info = (HashMap<String, Object>) dict.get("info");
	    if (info == null) {
	    	throw new IllegalArgumentException("Torrent file didn't contain info.");
	    }
	    
	    byte[] infoBytes = bencode.encode(info);
	    
	    HSHA = Utils.getSHA1(infoBytes);
	    
		piece_length = ((Long)info.get("piece length")).intValue();
		total_pieces = (int)Math.ceil(
			(double)
				((Long)info.get("length")).intValue() / piece_length
		);
		
		for (int i = 0; i < total_pieces; i++) {
			wanted_pieces.add(i);
		}
		
	    //Sockets initialisation!
	    for (String addr : server_list) {
	    	var m = regex.matcher(addr);
	    	String ip = null;
	    	int port = 0;
	    	while (m.find()) {
	    		ip = m.group(1);
	    		port = Integer.parseInt(m.group(2));
	    	}
	    	
	    	if (ip == null) {
	    		System.out.println("Failed to parse IP " + addr);
	    		continue;
	    	}
	    	
	    	try {
				socks.put(addr, new Connection(ip, port));
			} catch (UnknownHostException e) {
				System.out.println("Unknown host exception caught for the address " + addr);
			} catch (IOException e) {
				System.out.println("Couldn't open socket for " + addr + ": " + e.getMessage());
			}
	    }

	    String fPath = path.getParent()
	    		.resolve("downloads")
	    		.resolve(new String( ((ByteBuffer)info.get("name")).array() ))
	    		.toString();

		write_file = new RandomAccessFile(fPath, "rw");
	   
	    //Handshake time!
	    byte[] hs = Objects.requireNonNull(Handshakes.get_handshake(HSHA)).array();
	   
	    Selector selector = Selector.open();
	    
	    socks.forEach((address, connection) -> {
	    	try {
	    		connection.get_bytes().configureBlocking(false);
				connection.get_bytes().register(selector, SelectionKey.OP_WRITE | SelectionKey.OP_READ);
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	try {
				connection.write_bytes(hs);
				set_state(connection.get_bytes(), State.WAIT_HANDSHAKE);
			} catch (IOException e) {
				e.printStackTrace();
			} 
	    });

	    while (true) {
			selector.select();

			for (SelectionKey key : selector.selectedKeys()) {
				if (!key.isValid()) {
					continue;
				}

				SocketChannel chan = (SocketChannel) key.channel();

				if (key.isReadable()) {
					try {
						boolean ok = socket_read(chan);
						if (!ok) {
							System.out.println("Reader disconnection...");
							on_close(chan);
							chan.close();
							key.cancel();
							break;
						}
					} catch (Exception e) {
						System.out.println("Socket reading exception!Disconnecting!");
						e.printStackTrace();
						on_close(chan);
						chan.close();
						key.cancel();
						break;
					}
				}
			}
	    }
	}
	
	private synchronized int get_request_piece(SocketChannel channel) {
		var available = availability.get(channel);
		Iterator<Integer> iterator = wanted_pieces.descendingIterator();
		while (iterator.hasNext()) {
			int num = iterator.next();
			if (available.get(num) != null) {
				iterator.remove();
				return num;
			}
		}
		return -1;
	}

	static final int piece_to_fetch = 1;
	private int total_pieces = 0;
	private int piece_length = 0;
	private byte[] HSHA; // SHA we'll use for the handshake
	private RandomAccessFile write_file; //File to write our recieved pieces
	private final HashMap<String, Connection> socks = new HashMap<>(); //Sockets
	private final HashMap<SocketChannel, Consumer<Utils.Message>> response_to_piece = new HashMap<>(); //What you gonna do when the piece is calling?
	private final HashMap<SocketChannel, Integer> waiting_pieces = new HashMap<>(); //What pieces are we waiting for
	private final HashMap<SocketChannel, HashMap<Integer, Boolean>> availability = new HashMap<>(); //Array of what pieces are avaliable rn
	private final LinkedList<Integer> wanted_pieces = new LinkedList<>(); // We want that pieces so we call for them
	private final HashMap<SocketChannel, State> states = new HashMap<>(); //Manage the states of the channels
	static Pattern regex = Pattern.compile("(\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?):(\\d{5})"); //Found on stackoverflow to simplify my cruel life
	static AtomicInteger last_written = new AtomicInteger(0); // the latest written piece
	
}

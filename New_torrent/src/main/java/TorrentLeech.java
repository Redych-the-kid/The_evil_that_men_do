import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.dampcake.bencode.Bencode;
import com.dampcake.bencode.Type;

import Connections.Connection;
import Connections.Handshake;
public class TorrentLeech {
	static final Bencode bencode = new Bencode(StandardCharsets.UTF_8, true);
	private static final HashMap<String, Connection> socksets = new HashMap<>();
	
	static Pattern ip_pattern = Pattern.compile("(\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?\\.\\d{1,3}?):(\\d{5})");
	
	static AtomicInteger needed = new AtomicInteger(0);
	static AtomicInteger last_written = new AtomicInteger(0);
	
	static final int PIECE_FETCH = 1;
	
	static ThreadPoolExecutor executor =  (ThreadPoolExecutor) Executors.newFixedThreadPool(4);
	static ThreadPoolExecutor writer =  (ThreadPoolExecutor) Executors.newFixedThreadPool(1);
	
	static Callable function = null;
	
	private static void on_connection(String addr, Connection con, Map<String, Object> dict, RandomAccessFile wr) {
		HashMap<String, Object> info = (HashMap<String, Object>) dict.get("info");	
		
		int piece_length = ((Long)info.get("piece length")).intValue();
		int total_pieces = (int)Math.ceil(
				(double)
					((Long)info.get("length")).intValue() / piece_length
			);
		
		System.out.println("SHA has been accepted!Connecting!");

		function = () -> {
			while (needed.get() < total_pieces) {
				ByteBuffer buf = ByteBuffer.allocate(8);
				
				int need = needed.getAndAdd(PIECE_FETCH);
				buf.putInt(need);
				buf.putInt(Math.min(total_pieces, need + PIECE_FETCH));
				
				try {
					TorrentUtils.send_message(TorrentUtils.Type.REQUEST, buf.array(), con.get_socket());
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				TorrentUtils.Message msg = TorrentUtils.recieve_message(con.get_socket());
				
				if (msg == null) {
					System.out.println("Recieved message was empty...Message address: " + addr);
					return null;
				}
				writer.submit(() -> {
					try {
						wr.seek(need * piece_length);
						wr.write(msg.cont);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					
					int curPc = last_written.addAndGet(PIECE_FETCH);
					
					if (curPc == total_pieces) {
						try {
							wr.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				});
			}
				
			return null;
		};
	
		executor.submit(function);
	}
	
	public static void start(String torrent, List<String> svs) throws IOException {
		Thread.currentThread().setName("Torrent - Client");
		System.out.println("Downloading " + torrent);
		for (String s : svs) {
			System.out.println(" from " + s);
		}
		
		Path path = Paths.get(torrent);
		byte[] read;
	
	    try {
			read = Files.readAllBytes(path);
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
	    
	    Map<String, Object> dict = bencode.decode(read, Type.DICTIONARY);
	    HashMap<String, Object> info = (HashMap<String, Object>) dict.get("info");
	    if (info == null) {
	    	throw new IllegalArgumentException("Torrent file didn't contain info.");
	    }
	    
	    byte[] infoBytes = bencode.encode(info);
	    
	    // SHA we'll use for the handshake
	    byte[] handshake_SHA = TorrentUtils.get_SHA1(infoBytes);
	   
	    // initialize sockets for each server
	    for (String addr : svs) {
	    	Matcher m = ip_pattern.matcher(addr);
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
				socksets.put(addr, new Connection(ip, port));
			} catch (UnknownHostException e) {
				System.out.println("Unknown host exception caught for address " + addr);
			} catch (IOException e) {
				System.out.println("Couldn't open socket for " + addr + ": " + e.getMessage());
			}
	    	
	    	
	    }

	    File fld = new File(path.getParent().resolve("received").toString());
	    fld.mkdirs();

		String fPath = path.getParent()
				.resolve("received")
				.resolve(new String( ((ByteBuffer)info.get("name")).array() ))
				.toString();

	    RandomAccessFile flIn = new RandomAccessFile(fPath, "rw");

	    // handshake with everyone
	    byte[] handshake_bytes = Objects.requireNonNull(Handshake.get_handshake(handshake_SHA)).array();

	    socksets.forEach((addr, con) -> {
	    	byte[] seededSHA;

	    	try {
	    		System.out.println("Sending handshake");
    			con.write_bytes(handshake_bytes);
    			byte[] handshake = con.read();
    			seededSHA = Handshake.responce_check(handshake);
	    	} catch (IOException ex) {
	    		System.out.println("Exception while handshaking with " + addr);
	    		ex.printStackTrace();
	    		return;
	    	}

    		// wrong SHA given; peace out
    		if (!Arrays.equals(seededSHA, handshake_SHA)) {
    			socksets.remove(addr);
    			System.out.println("Error!Bad SHA!");
    			return;
    		}

    		on_connection(addr, con, dict, flIn);
    	});

	}
}

package Connections;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;

public class Connection {
	private SocketChannel sock = null;
	private final ByteBuffer buf = ByteBuffer.allocate(512);

	public Connection(String ip, int port) throws IOException {
		set_socket(SocketChannel.open(new InetSocketAddress(ip, port)));
	}
	
	public void write_bytes(byte[] bytes) throws IOException {
		get_socket().write(ByteBuffer.wrap(bytes));
	}
	
	public byte[] read() throws IOException {
		int read = get_socket().read(buf);
		if (read == -1) {
			return null;
		}
		
		return Arrays.copyOf(buf.array(), read);
	}

	public SocketChannel get_socket() {
		return sock;
	}

	public void set_socket(SocketChannel sock) {
		this.sock = sock;
	}
}

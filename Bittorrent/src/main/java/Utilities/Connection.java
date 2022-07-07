package Utilities;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Connection {
	private SocketChannel socket = null;

	public Connection(String ip, int port) throws IOException {
		set_bytes(SocketChannel.open(new InetSocketAddress(ip, port)));
	}
	
	public void write_bytes(byte[] bytes) throws IOException {
		get_bytes().write(ByteBuffer.wrap(bytes));
	}

	public SocketChannel get_bytes() {
		return socket;
	}

	public void set_bytes(SocketChannel sock) {
		this.socket = sock;
	}
}

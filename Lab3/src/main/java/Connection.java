import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;



/*
	Connection class to make a connection with a peer.We give port, name and ip and connection list,
	and it waits 15 seconds to be sure that peer is online and then creates socket and puts it in the connections list.
 */
public class Connection implements Runnable
{
	private final ConcurrentHashMap<String, SocketChannel> connections; // Connections
	private final String name; // Peer name
	private final String host_ip; // Peer host ip
	private final int port; // Peer

	public Connection(int port, String name, String host_ip,
					  ConcurrentHashMap<String, SocketChannel> connections)
	{
		this.connections = connections;
		this.port = port;
		this.name = name;
		this.host_ip = host_ip;
	}


	public void run()
	{
		try
		{
			Thread.sleep(15000); // ~15 seconds
			SocketChannel socket = SocketChannel.open(new InetSocketAddress(host_ip, port));
			connections.put(name, socket);
		}
		catch(IOException | InterruptedException e)
		{
			e.printStackTrace();
		}
	}

}

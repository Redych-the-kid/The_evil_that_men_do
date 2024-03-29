import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Connection class to make a connection with a peer.We give port, name and ip and connection list,
 * and it waits 15 seconds to be sure that peer is online and then creates a socket and puts it in the connections list.
 * If it fails to connect, it puts a server_name in the dead_connections list
 */
public class Connection implements Runnable {
    private final ConcurrentHashMap<String, SocketChannel> connections; // Connections
    private final ConcurrentLinkedQueue<String> dead_connections;
    private final String name; // Peer name
    private final String host_ip; // Peer host ip
    private final int port; // Peer

    private final CountDownLatch counter;

    /**
     * Construction method for the Connection class
     *
     * @param port             The number of the port
     * @param name             The name of the server that we are connecting to
     * @param host_ip          Server host address
     * @param connections      Connections hashmap that we are using to store the connections in the current session
     * @param countdown        Countdown latch to make sure that every connection will be finished before the client starts
     * @param dead_connections Dead connections list, so we can reconnect later if something goes wrong
     */
    public Connection(int port, String name, String host_ip,
                      ConcurrentHashMap<String, SocketChannel> connections, CountDownLatch countdown, ConcurrentLinkedQueue<String> dead_connections) {
        this.connections = connections;
        this.port = port;
        this.name = name;
        this.host_ip = host_ip;
        this.counter = countdown;
        this.dead_connections = dead_connections;
    }

    /**
     * Starts the connection process
     */
    public void run() {
        try {
            Thread.sleep(15000); // ~15 seconds
            SocketChannel socket = SocketChannel.open(new InetSocketAddress(host_ip, port));
            connections.put(name, socket);
        } catch (IOException | InterruptedException e) {
            System.out.println("Failed to connect to " + name);
            //Push to the dead_connections, so we can reconnect later
            dead_connections.add(name);
        } finally {
            counter.countDown();
        }
    }
}

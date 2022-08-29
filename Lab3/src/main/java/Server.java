import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Selector server for Peer.It accepts,reads and writes the response.IN ONE THREAD!Cool, huh?
 */
public class Server implements Runnable {
    private final int port;
    private final String ip;

    /**
     * Construction method for Server class
     *
     * @param port      The number of the server port
     * @param server_ip The host address for the server
     */
    public Server(int port, String server_ip) {
        this.port = port;
        this.ip = server_ip;
    }

    private static final ConcurrentHashMap<String, String> dht_instance = new ConcurrentHashMap<>(); // I LOVE static because it made it so simple...

    /**
     * Starts the server
     */
    public void run() {
        try {
            Selector selector = Selector.open();
            ServerSocketChannel ss_channel = ServerSocketChannel.open();
            ss_channel.bind(new InetSocketAddress(ip, port));
            ss_channel.configureBlocking(false);
            ss_channel.register(selector, SelectionKey.OP_ACCEPT);
            while (true) {
                //Accepting connections
                selector.select();
                Set<SelectionKey> keys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = keys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isAcceptable()) {
                        accept(selector, ss_channel);
                    }
                    if (key.isReadable()) {
                        response(key);
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            System.out.println("Server has thrown an exception!");
            e.printStackTrace();
        }
    }

    /**
     * Put method for server. Used by the client-side of the program
     *
     * @param key   DHT key
     * @param value DHT value
     * @return Returns true on success and false on failure
     */
    public static boolean put(String key, String value) {
        //if we put successfully, then print the containment(debug plus to be sure that everything is fine)
        if (!Objects.equals(dht_instance.put(key, value), "null")) {
            System.out.println("DHT after this put: " + dht_instance);
            return true;
        } else {
            return false;
        }
    }

    //Accepts the connection to our server-side
    private static void accept(Selector selector, ServerSocketChannel ss_channel) throws IOException {
        SocketChannel client = ss_channel.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
    }

    //Responds to the request sent by another peer
    private static void response(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(Client.command_limit);
        SocketChannel client = (SocketChannel) key.channel();
        int num_read = client.read(buffer);
        if (num_read == -1) {
            client.close();
            key.cancel();
            System.out.println("Client has left lol");
        } else {
            byte[] data = new byte[num_read];
            System.arraycopy(buffer.array(), 0, data, 0, num_read);
            String message = new String(data);
            String[] parsed_message = message.split(" ");
            String type = parsed_message[0];
            switch (type) {
                case "put": //We need to put?Okay...
                    System.out.println("Put operation has been requested...");
                    //read the message from above and then parse it
                    String p_key = parsed_message[1];
                    String p_value = parsed_message[2];
                    //Putting to our table and sending our result back to client
                    boolean result = put(p_key, p_value);
                    buffer.flip();
                    buffer.put(String.valueOf(result).getBytes());
                    buffer.flip();
                    client.write(buffer);
                    break;

                case "get": //Get...Fine, I guess...
                    System.out.println("Get operation has been requested...");
                    //Read key, get value and sent the result
                    String g_key = parsed_message[1];
                    String g_value = get(g_key);
                    if (g_value == null) {
                        g_value = "null";
                    }
                    buffer.flip();
                    buffer.put(g_value.getBytes());
                    buffer.flip();
                    client.write(buffer);
                    break;
                case "delete":    //Perform Delete Operation
                    System.out.println("Delete operation has been requested...");

                    //Read key, get stupid results...
                    String tobedeleted = parsed_message[1];
                    boolean delete_result = del(tobedeleted);

                    //Write back the result
                    buffer.flip();
                    buffer.put(String.valueOf(delete_result).getBytes());
                    buffer.flip();
                    client.write(buffer);
                    break;
            }
        }
    }

    /**
     * Get method for server. Used by the client-side of the program
     *
     * @param key DHT key
     * @return DHT value on success and null on failure
     */
    public static String get(String key) {
        return dht_instance.get(key);
    }

    /**
     * Delete method for server. Used by the client-side of the program
     *
     * @param key DHT key
     * @return true on success and false on failure
     */
    public static boolean del(String key) {
        return dht_instance.remove(key, dht_instance.get(key));
    }
}

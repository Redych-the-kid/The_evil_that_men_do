import java.io.IOException;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * This is a peer class.It acts like a client and a server at the same time.It reads server list(he is here too) and then he sends connections to others.
 * He starts server thread and then waiting for connections to finish their jobs.And finally he starts his client side
 */

public class Peer {
    private static boolean binded = false; // If server_socket is binded
    private static final ConcurrentHashMap<String, SocketChannel> sockets = new ConcurrentHashMap<>(); // Concurrent because multiple connection threads are rushing
    private static final ConcurrentHashMap<String, Integer> socket_ports = new ConcurrentHashMap<>(); // To reconnect
    private static int s_port; // Port of the server
    public static String server_name; // Name of the server
    private static final String server_ip = "localhost"; //Assuming that the peers are in the same network

    private static final ConcurrentLinkedQueue<String> dead_connections = new ConcurrentLinkedQueue<>();

    private static void print_help() {
        System.out.println("Usage: peer.java + {server_name}(from config file)");
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Too many or too few arguments!");
            print_help();
            return;
        }
        System.out.println("Connecting to servers, please wait ~15 seconds...");
        CountDownLatch countdown;
        try {
            List<Thread> connections = new ArrayList<>(); // This is for CountDownLatch
            server_name = args[0];
            System.out.println("Server name: " + server_name);
            /*
                I'm too lazy to write server names and ports in command line, so I used properties instead!
                And IT'S NOT DEPRECATED!YAY!
             */
            Parser parser = new Parser();
            int server_count = Integer.parseInt(parser.get_properties("SERVER_COUNTER"));
            countdown = new CountDownLatch(server_count - 1); // We don't count our server mkay?
            for (int i = 0; i < server_count; i++) {
                String name = parser.get_properties("SERVER_NAME_" + i);
                if (!Objects.equals(name, "server" + i)) {
                    throw new IOException();
                }
                int port = Integer.parseInt(parser.get_properties("SERVER_PORT_" + i));
                if (name.equalsIgnoreCase(server_name)) // If it's our server-bind to server_socket!
                {
                    if (!binded) // Double-check
                    {
                        s_port = port;
                        binded = true;
                    }
                } else {
                    //If it's not ours,then connect to them!
                    Thread connection_thread = new Thread(new Connection(port, name, server_ip, sockets, countdown, dead_connections));
                    connections.add(connection_thread);
                    //Add to port table to be able to reconnect later
                    socket_ports.put(name, port);
                }
            }
            if (!binded) {
                System.out.println("PLEASE! provide correct server name or server count!");
                return;
            }
            //Launch the Server side so server can accept!
            Thread server_thread = new Thread(new Server(server_name, s_port, server_ip, sockets, dead_connections, socket_ports));
            server_thread.start();

            //client can't perform operations if he is not in DHT network,so...
            Iterator<Thread> thread_iterator = connections.iterator();
            while (thread_iterator.hasNext()) {
                Thread thread = thread_iterator.next();
                thread.start();
                thread_iterator.remove();
            }
            countdown.await();

            //We got connections-release the Client-side!
            Thread client_thread = new Thread(new Client(server_name, s_port, sockets, socket_ports, server_ip, dead_connections));
            client_thread.start();
        } catch (IOException | InterruptedException e) {
            System.out.println("Peer has thrown an exception!");
            e.printStackTrace();
        }
    }

}

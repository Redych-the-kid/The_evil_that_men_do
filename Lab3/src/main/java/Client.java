import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;

/**
 * Client side of the peer.He knows where to send and receive messages(get,put and delete + results).
 * You can type 3 types of the commands(letter case is ignored):
 * 1.Put [Key] [Value] - puts an element to DHT.Returns "Success" message if everything is fine of "Failure" message if not.
 * 2.Get [Key] - gets the element from DHT.If it contains null, then it returns "Value not found!" message.
 * 3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.
 * 4.Exit - just quits from the app and tries to save the backup of the hashtable
 * 5.Reconnect - attempts to reconnect to other peers lol
 * 6.Connect - attempts to notify connection peers that you exist
 * 7.Rehash - rehashes the table to get rid of ghost entries
 * Note: maximum length of a message is 1024(including get, put and delete words plus space symbols)
 */
public class Client implements Runnable {

    private static ConcurrentHashMap<String, SocketChannel> connections = null; // Just to know where and to whom I need to send or receive if my HT has nothing...
    private static ConcurrentHashMap<String, Integer> ports = null;
    private static ConcurrentLinkedQueue<String> dead_connections = null;
    private static final int name_limit = 300;
    private static final int value_limit = 719;
    public static final int command_limit = 1024;
    private static String ip = "localhost";
    private static boolean rehash;
    private final String client_name;
    private final int port;

    private static final ConcurrentLinkedQueue<String> connect_to = new ConcurrentLinkedQueue<>();

    /**
     * Construction method for Client class
     *
     * @param connections      Hashmap of server that we are connected to
     * @param server_ports     Hashmap of ports that we are connected/should be connected to
     * @param server_ip        Our host address
     * @param dead_connections Queue of connections that we failed to make
     */
    public Client(String name, int port, ConcurrentHashMap<String, SocketChannel> connections, ConcurrentHashMap<String, Integer> server_ports, String server_ip, ConcurrentLinkedQueue<String> dead_connections) {
        this.client_name = name;
        this.port = port;
        Client.connections = connections;
        ports = server_ports;
        ip = server_ip;
        Client.dead_connections = dead_connections;
        first_connect(client_name, port);
    }

    /**
     * Starts the client-side of the app
     */
    private void print_help() {
        System.out.println("Usage:");
        System.out.println("You can type 3 types of the commands(letter case is ignored):");
        System.out.println("1.Put [Key] [Value] - puts an element to DHT.Returns \"Success\" message if everything is fine of \"Failure\" message if not.");
        System.out.println("2.Get [Key] - gets the element from DHT.If it contains null, then it returns \"Value not found!\" message.");
        System.out.println("3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.");
        System.out.println("4.Exit - just quits from the app and tries to save the backup of the hashtable");
        System.out.println("5.Reconnect - attempts to reconnect to other peers lol");
        System.out.println("6.Connect - attempts to notify connection peers that you exist");
        System.out.println("7.Rehash - rehashes the table to get rid of ghost entries");
        System.out.println("Note: maximum length of a message is " + command_limit + "(including get, put and delete words plus space symbols)");
    }

    public void run() {
        String type;
        while (true) {
            if (dead_connections.size() != 0) {
                System.out.println("You've failed to connect to " + dead_connections.size() + " servers!Please type \"reconnect\" to reconnect to them!");
            }
            if (connect_to.size() != 0) {
                System.out.println("You've failed to make contact with " + connect_to.size() + " servers!Please type \"connect\" to connect to them!");
            }
            if (rehash) {
                System.out.println("You should rehash all entries in this server by using \"rehash\" command!");
            }
            System.out.println("Now you can type!");

            //Reading the command line
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            if (command.length() > command_limit) {
                System.out.println("ERROR!LEN IS MORE THAN " + command_limit + "!Please write less");
                continue;
            }
            //"Parsing" the command line
            String[] parsed_command = command.split(" ");
            type = parsed_command[0].toLowerCase(); // Ignore case is cool, but it saves me many letters of code
            //YandereDev moment
            switch (type) {

                case "put": // Performing put operation(see the description above for more)
                    if (!dead_connections.isEmpty() || !connect_to.isEmpty()) {
                        break;
                    }
                    if (parsed_command.length != 3) {
                        System.out.println("Put args too short or too long!");
                        break;
                    }
                    //Get name and value
                    String name = parsed_command[1];
                    if (name.length() > name_limit) {
                        System.out.println("Key is too big!");
                        break;
                    }
                    String value = parsed_command[2];
                    if (value.length() > value_limit) {
                        System.out.println("Value is too big!");
                        break;
                    }
                    String hash = socket_hash(name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (connections.get(hash) == null) {
                        boolean result = Server.put(name, value);
                        if (result) {
                            System.out.println("Success");
                        } else {
                            System.out.println("Failure");
                        }
                    } else {
                        //Ok,send this to server that probably can add to his DHT
                        String message = name + " " + value;
                        send_to_other_peer(hash, type, message);
                    }

                    break;

                case "get":    // Performing get operation(see the description above for more)
                    if (!dead_connections.isEmpty() || !connect_to.isEmpty() || rehash) {
                        break;
                    }
                    if (parsed_command.length != 2) {
                        System.out.println("Get args too short or too long!");
                        break;
                    }
                    String get_name = parsed_command[1];
                    if (get_name.length() > name_limit) {
                        System.out.println("Key is too big!");
                        break;
                    }
                    String get_hash = socket_hash(get_name);
                    //Hash function determines where to get

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (connections.get(get_hash) == null) {
                        String result = Server.get(get_name);
                        if (result != null) {
                            System.out.println("The value is: " + result);
                        } else {
                            System.out.println("Value not found!");
                        }
                    } else {
                        send_to_other_peer(get_hash, type, get_name);
                    }

                    break;
                case "delete":    // Performing delete operation(see the description above for more)
                    if (!dead_connections.isEmpty() || !connect_to.isEmpty() || rehash) {
                        break;
                    }
                    if (parsed_command.length != 2) {
                        System.out.println("delete args too short or too long!");
                        break;
                    }

                    String delete_name = parsed_command[1];
                    if (delete_name.length() > name_limit) {
                        System.out.println("Key is too big!");
                        break;
                    }
                    String delete_hash = socket_hash(delete_name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (connections.get(delete_hash) == null) {
                        boolean result = Server.del(delete_name);
                        if (result) {
                            System.out.println("Success");
                        } else {
                            System.out.println("Failure");
                        }
                    } else {
                        send_to_other_peer(delete_hash, type, delete_name);
                    }

                    break;
                case "help": //Prints command list so client knows what to type
                    print_help();
                    break;
                case "exit": // Exits and saves the backup table
                    System.out.println("Exiting...");
                    Server.write_table(); // Calling a server to write down the table in a .bak file
                    System.exit(0);
                case "reconnect": // Trying to bring back the dead connections to life...
                    List<Thread> reconnections = new ArrayList<>();
                    CountDownLatch countdown = new CountDownLatch(dead_connections.size());
                    for (String reconnect_name : dead_connections) {
                        Runnable reconnection = () -> {
                            if (reconnect_by_name(reconnect_name)) {
                                dead_connections.remove(reconnect_name);
                            }
                            countdown.countDown();
                        };
                        Thread reconnection_thread = new Thread(reconnection);
                        reconnections.add(reconnection_thread);
                    }
                    Iterator<Thread> t_iterator = reconnections.iterator();
                    while (t_iterator.hasNext()) {
                        Thread thread = t_iterator.next();
                        thread.start();
                        t_iterator.remove();
                    }
                    try {
                        countdown.await();
                    } catch (InterruptedException e) {
                        System.out.println("WHY CAN'T I COUNT DOWN, MASTER!?");
                    }
                    break;
                case "connect":
                    if (!dead_connections.isEmpty()) {
                        break;
                    }
                    System.out.println("Trying to connect to existing DHT network...");
                    countdown = new CountDownLatch(connect_to.size());
                    List<Thread> connects = new ArrayList<>();
                    for (String connect_name : connect_to) {
                        Runnable connection = () -> {
                            System.out.println("Connecting to " + connect_name);
                            String message = String.format("%s %s", client_name, port);
                            if (send_to_other_peer(connect_name, "connect", message)) {
                                connect_to.remove(connect_name);
                            }
                            countdown.countDown();
                        };
                        Thread connect_thread = new Thread(connection);
                        connects.add(connect_thread);
                    }
                    Iterator<Thread> iterator = connects.iterator();
                    while (iterator.hasNext()) {
                        Thread thread = iterator.next();
                        thread.start();
                        iterator.remove();
                    }
                    try {
                        countdown.await();
                    } catch (InterruptedException e) {
                        System.out.println("WHY CAN'T I COUNT DOWN, MASTER!?");
                    }
                    break;
                case "rehash": {
                    if (!dead_connections.isEmpty() || !connect_to.isEmpty()) {
                        break;
                    }
                    Server.rehash();
                    break;
                }
                default:
                    System.out.println("Wrong command!Type \"help\" to get the command list!");
                    break;
            }
        }
    }

    //"Borrowed" from StackOverflow...It's better than string.hashcode() so why not...
    public static String socket_hash(String Key) {
        if (connections != null) {
            int hash = 7;
            for (char c : Key.toCharArray()) {
                hash = hash * 31 + (int) c;
            }
            hash = Math.abs(hash);
            return "server" + hash % (connections.size() + 1);
        }
        return null;
    }

    public static void set_rehash(boolean val) {
        rehash = val;
    }

    //Reconnection function
    private static boolean reconnect_by_name(String server_name) {
        boolean result = false;
        if (ports != null) {
            for (int i = 0; i < 3; ++i) { // We have 3 attempts to reconnect
                try {
                    System.out.println("Trying to reconnect to " + server_name + "...please wait");
                    Thread.sleep(5000); // Waiting 5 secs
                    SocketChannel retry = SocketChannel.open(new InetSocketAddress(ip, ports.get(server_name)));
                    connections.put(server_name, retry);
                    result = true;
                    break;
                } catch (IOException | InterruptedException e) {
                    System.out.println("Failed to reconnect to " + server_name + "!Trying again!");
                }
            }
        }
        return result;
    }

    private static void first_connect(String client_name, int port) {
        System.out.println("Trying to connect to existing DHT network...");
        CountDownLatch count = new CountDownLatch(connections.size());
        List<Thread> connects = new ArrayList<>();
        for (Map.Entry<String, SocketChannel> entry : connections.entrySet()) {
            Runnable connection = () -> {
                System.out.println("Connecting to " + entry.getKey());
                String message = String.format("%s %s", client_name, port);
                if (!send_to_other_peer(entry.getKey(), "connect", message)) {
                    connect_to.add(entry.getKey());
                }
                count.countDown();
            };
            Thread connect_thread = new Thread(connection);
            connects.add(connect_thread);
        }
        Iterator<Thread> iterator = connects.iterator();
        while (iterator.hasNext()) {
            Thread thread = iterator.next();
            thread.start();
            iterator.remove();
        }
        try {
            count.await();
        } catch (InterruptedException e) {
            System.out.println("WHY CAN'T I COUNT DOWN, MASTER!?");
        }
        connect_to.addAll(dead_connections);
    }

    //Sends the request to other peer and get the response
    public static boolean send_to_other_peer(String socket_name, String type, String message) {
        try {
            SocketChannel socket = connections.get(socket_name);
            //We need those to send/get messages
            ByteBuffer buffer = ByteBuffer.wrap((type + " " + message).getBytes());
            //Write type and the message to server
            socket.write(buffer);
            //Read the response
            buffer.flip();
            int read_count = socket.read(buffer);
            if (read_count == -1) { // Oh no!Peer is missing!Trying to get access one more time...
                System.out.println("Server is probably down rn!Trying to reconnect...");
                boolean reconnected = reconnect_by_name(socket_name);
                if (!reconnected) {
                    System.out.println("Reconnection failed!Try again later!"); // Damn son...
                } else {
                    System.out.println("Reconnection success!"); // Yay, we can finally send the request. DO IT!
                    send_to_other_peer(socket_name, type, message);
                }
                return false;
            }
            buffer.flip();
            byte[] b = new byte[read_count];
            buffer.get(b, 0, buffer.limit());
            String response = new String(b).trim();
            //parse it depending on what message type we sent
            if (type.equals("get")) {
                if (!response.equals("null")) {
                    System.out.println("The value is: " + response);
                } else {
                    System.out.println("Value not found!");
                }
            } else if (type.equals("put") || type.equals("delete") || type.equals("connect")) {
                if (response.equals("true")) {
                    System.out.println("Success");
                } else {
                    System.out.println("Failure");
                }
            }
        } catch (IOException e) {
            System.out.println("Client has thrown an exception!");
            e.printStackTrace();
            return false;
        }
        return true;
    }
}

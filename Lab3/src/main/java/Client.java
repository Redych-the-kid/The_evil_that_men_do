import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


/*
	Client side of the peer.He knows where to send and receive messages(get,put and delete + results).
	You can type 3 types of the commands(letter case is ignored):
	1.Put [Key] [Value] - puts an element to DHT.Returns "Success" message if everything is fine of "Failure" message if not.
	2.Get [Key] - gets the element from DHT.If it contains null, then it returns "Value not found!" message.
	3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.
	Note: maximum length of a message is 1024(including get, put and delete words plus space symbols)
 */
public class Client implements Runnable {

    private final ConcurrentHashMap<String, SocketChannel> connections; // Just to know where and to whom I need to send or receive if my HT has nothing...

    public Client(ConcurrentHashMap<String, SocketChannel> connections) {
        this.connections = connections;
    }

    private void print_help() {
        System.out.println("Usage:");
        System.out.println("You can type 3 types of the commands(letter case is ignored):");
        System.out.println("1.Put [Key] [Value] - puts an element to DHT.Returns \"Success\" message if everything is fine of \"Failure\" message if not.");
        System.out.println("2.Get [Key] - gets the element from DHT.If it contains null, then it returns \"Value not found!\" message.");
        System.out.println("3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.");
        System.out.println("Note: maximum length of a message is 1024(including get, put and delete words plus space symbols)");
    }

    public void run() {
        String type;
        while (true) {
            System.out.println("Now you can type!");

            //Reading the command line
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();
            if (command.length() > 1024) {
                System.out.println("ERROR!LEN IS MORE THAN 1024!Please write less");
                continue;
            }
            //"Parsing" the command line
            String[] parsed_command = command.split(" ");
            type = parsed_command[0].toLowerCase(); // Ignore case is cool, but it saves me many letters of code

            //YandereDev moment
            switch (type) {

                case "put": // Performing put operation(see the description above for more)

                    if (parsed_command.length != 3) {
                        System.out.println("Put args too short or too long!");
                        break;
                    }

                    //Get name and value
                    String name = parsed_command[1];
                    String value = parsed_command[2];

                    //Hash function determines where to put
                    SocketChannel socket_by_hash = socket_hash(name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (socket_by_hash == null) {
                        boolean result = Server.put(name, value);
                        if (result) {
                            System.out.println("Success");
                        } else {
                            System.out.println("Failure");
                        }
                    } else {
                        //Ok,send this to server that probably can add to his DHT
                        String message = name + " " + value;
                        send_to_other_peer(socket_by_hash, type, message);
                    }

                    break;

                case "get":    // Performing get operation(see the description above for more)
                    if (parsed_command.length != 2) {
                        System.out.println("Get args too short or too long!");
                        break;
                    }
                    String get_name = parsed_command[1];

                    //Hash function determines where to get
                    socket_by_hash = socket_hash(get_name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (socket_by_hash == null) {
                        String result = Server.get(get_name);
                        if (result != null) {
                            System.out.println("The value is: " + result);
                        } else {
                            System.out.println("Value not found!");
                        }
                    } else {
                        send_to_other_peer(socket_by_hash, type, get_name);
                    }

                    break;
                case "delete":    // Performing delete operation(see the description above for more)
                    if (parsed_command.length != 2) {
                        System.out.println("delete args too short or too long!");
                        break;
                    }

                    String delete_name = parsed_command[1];

                    //Hash function determines where to get
                    socket_by_hash = socket_hash(delete_name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (socket_by_hash == null) {
                        boolean result = Server.del(delete_name);
                        if (result) {
                            System.out.println("Success");
                        } else {
                            System.out.println("Failure");
                        }
                    } else {
                        send_to_other_peer(socket_by_hash, type, delete_name);
                    }

                    break;
                case "help": //Prints command list so client knows what to type
                    print_help();
                    break;
                default:
                    System.out.println("Wrong command!Type \"help\" to get the command list!");
                    break;
            }
        }
    }

    //Simplest hash function eva.Not perfect, but it's doing okay I guess...
    public SocketChannel socket_hash(String Key) {
        String hashValue = "server" + Math.abs((Key.hashCode()) % (connections.size() + 1));
        return connections.get(hashValue);
    }

    //Send the request to other peer and get the response
    public void send_to_other_peer(SocketChannel socket, String type, String message) {
        try {
            //We need those to send/get messages
            ByteBuffer buffer = ByteBuffer.wrap((type + " " + message).getBytes());
            //Write type and the message to server
            socket.write(buffer);
            //Read the response
            buffer.flip();
            int read_count = socket.read(buffer);
            if (read_count == -1) {
                System.out.println("Server is probably down rn!");
                //We can remove server here,but it will break hash function so no
                return;
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
            } else if (type.equals("put") || type.equals("delete")) {
                if (response.equals("true")) {
                    System.out.println("Success");
                } else {
                    System.out.println("Failure");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

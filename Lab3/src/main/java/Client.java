import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;


/*
	Client side of the peer.He knows where to send and receive messages(get,put and delete + results).
	You can type 3 types of the commands(letter case is ignored):
	1.Put [Key] [Value] - puts an element to DHT.Returns "Success" message if everything is fine of "Failure" message if not.
	2.Get [Key] - gets the element from DHT.If it contains null, then it returns "Value not found!" message.
	3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.
 */
public class Client implements Runnable {

    private final ConcurrentHashMap<String, Socket> connections; // Just to know where and to whom I need to send or receive if my HT has nuthin...

    public Client(ConcurrentHashMap<String, Socket> connections) {
        this.connections = connections;
    }

    private void print_help() {
        System.out.println("Usage:");
        System.out.println("You can type 3 types of the commands(letter case is ignored):");
        System.out.println("1.Put [Key] [Value] - puts an element to DHT.Returns \"Success\" message if everything is fine of \"Failure\" message if not.");
        System.out.println("2.Get [Key] - gets the element from DHT.If it contains null, then it returns \"Value not found!\" message.");
        System.out.println("3.Delete [Key] - deletes a key from DHT.Return is the same as from Put operation.");
    }

    public void run() {
        String type;
        while (true) {
            System.out.println("Now you can type!");

            //Reading the command line
            Scanner scanner = new Scanner(System.in);
            String command = scanner.nextLine();

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
                    Socket socket_by_hash = socket_hash(name);

                    //If null,then it is in our server_side.Why?In connections, we have like n - 1 servers(we do not connect to ourselves),and our name isn't there!
                    if (socket_by_hash == null) {
                        boolean result = ClientHandler.put(name, value);
                        if (result){
                            System.out.println("Success");
                        }
                        else{
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
                        String result = ClientHandler.get(get_name);
                        if(result != null){
                            System.out.println("The value is: " + result);
                        }
                        else{
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
                        boolean result = ClientHandler.del(delete_name);
                        if (result){
                            System.out.println("Success");
                        }
                        else{
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

    //Simplest hash function eva.Not perfect, but it's doin' okay I guess...
    public Socket socket_hash(String Key) {
        String hashValue = "server" + Math.abs((Key.hashCode()) % (connections.size() + 1));
        return connections.get(hashValue);
    }

    //Send the request to other peer and get the response
    public void send_to_other_peer(Socket socket, String type, String message) {
        try {
            //We need those to send/get messages
            DataInputStream input = new DataInputStream(socket.getInputStream());
            DataOutputStream output = new DataOutputStream(socket.getOutputStream());

            //Write type and the message to server
            output.writeUTF(type);
            output.writeUTF(message);

            //Read the response
            String result = input.readUTF();

            //parse it depending on what message type we sent
            if (type.equals("get")) {
                if(!result.equals("null")){
                    System.out.println("The value is: " + result);
                }
                else{
                    System.out.println("Value not found!");
                }
            }
            else if(type.equals("put") || type.equals("delete")){
                if (result.equals("true")) {
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

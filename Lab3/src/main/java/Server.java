import java.net.ServerSocket;
import java.net.Socket;

/*
    Multithread Server for peer.It accepts connection and then passes client to handler.
 */
public class Server implements Runnable{
    private final ServerSocket s_socket; // Server socket
    public Server(ServerSocket socket){
        this.s_socket = socket;
    }

    public void run(){
        try {
            while (true){
                //Accepting connections
                Socket client = s_socket.accept();

                //Now pass it to the handler because we can't read from one stream from multiple clients in one thread
                Thread client_handler = new Thread(new ClientHandler(client));
                client_handler.start();
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}

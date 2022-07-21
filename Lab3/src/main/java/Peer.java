import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


/*
	This is a peer class.It acts like a client and a server at the same time.It reads server list(he is here too) and then he sends connections to others.
	He starts server thread and then waiting for connections to finish their jobs.And finally he starts his client side
 */
public class Peer
{
	private static boolean binded = false; // If server_socket is binded
	private static final ConcurrentHashMap<String, Socket> sockets = new ConcurrentHashMap<>(); // Concurrent because multiple connection threads are rushin'
	private static ServerSocket s_socket; // Server socket
	public static String server_name; // Name of the server
	private static final String server_ip = "localhost"; //Assuming that the peers are in the same network

	public static void main(String[] args)
	{
		System.out.println("Connecting to servers, please wait ~15 seconds...");
		try
		{
			List<Thread> connections = new ArrayList<>(); // This is for thread.join()
			server_name = args[0];

			/*
				I'm too lazy to write server names in args and then write many lines of code just to parse them correctly,
				so I googled Java XML tutorial and hit the first link in YT.
			 */
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse("src/config.xml");
			NodeList server_nodes = document.getElementsByTagName("Server");

			for (int i = 0; i < server_nodes.getLength(); i++)
			{
				Node s = server_nodes.item(i);

				if (s.getNodeType() == Node.ELEMENT_NODE)
				{
					Element server = (Element) s;

					String name = server.getElementsByTagName("ServerName").item(0).getTextContent();
					int port = Integer.parseInt(server.getElementsByTagName("ServerPort").item(0).getTextContent());

					if(name.equalsIgnoreCase(server_name)) // If it's our server-bind to server_socket!
					{
						if(!binded) // Double-check
						{
							try 
							{
								s_socket = new ServerSocket(port);
								binded = true;
							} catch (IOException e)
							{
								e.printStackTrace();
							}
						}
					}
					else 
					{
							//If it's not ours,than connect to them!
							Thread connection_thread = new Thread(new Connection(port, name, server_ip, sockets));
							connection_thread.start();
							connections.add(connection_thread);
					}
				}
			}

			//Launch the Server side so server can accept!
			Thread server_thread = new Thread(new Server(s_socket));
			server_thread.start();

			//client can't perform operations if he is not in DHT network,so...
			Iterator<Thread> thread_iter = connections.iterator();
			while (thread_iter.hasNext()){
				Thread thread = thread_iter.next();
				thread.join();
				thread_iter.remove();
			}

			//We got connections-release the Client-side!
			Thread client_thread = new Thread(new Client(sockets));
			client_thread.start();
		}
		catch(IOException | ParserConfigurationException | InterruptedException | SAXException e)
		{
			e.printStackTrace();
		} 
	}

}

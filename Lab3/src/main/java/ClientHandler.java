import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/*
	Handler class for Multi-threaded server.We listen to one client and respond to his messages.
 */
public class ClientHandler implements Runnable
{
	private final Socket client; // Socket for the client that we're listening to
	public static ConcurrentHashMap<String, String> distributedHashTable = new ConcurrentHashMap<>(); // I LOVE static because it made it so simple...
	public ClientHandler(Socket client)
	{
		this.client = client;
	}

	public void run()
	{
		try
		{
			// we receive the message and send the response for it
			DataInputStream receive = new DataInputStream(client.getInputStream());
			DataOutputStream send = new DataOutputStream(client.getOutputStream());

			while(true)
			{
				try
				{
					//read the message type
					String type = receive.readUTF();

					switch(type)
					{
					case "put": //We need to put?Okay...
						System.out.println("Put operation has been requested...");

						//read the message from above and then parse it
						String message = receive.readUTF();
						String[] parsed_message = message.split(" ");

						//Putting to our table and sending our result back to client
						boolean result = put(parsed_message[0],parsed_message[1]);
						send.writeUTF(String.valueOf(result));
						break;

					case "get": //Get...Fine, I guess...
						System.out.println("Get operation has been requested...");
						//Read key, get value and sent the result
						String key = receive.readUTF();
						String value = get(key);
						send.writeUTF(Objects.requireNonNullElse(value, "null"));
						break;

					case "delete":	//Perform Delete Operation
						System.out.println("Delete operation has been requested...");

						//Read key, get stupid results...
						String tobedeleted = receive.readUTF();
						boolean delete_result = del(tobedeleted);

						//Write back the result
						send.writeUTF(String.valueOf(delete_result));
						break;
					}

				}
				catch(IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

	/*
		Synchronised operations wrappers just to be sure that everything will be fine, cause multiple instances are accessing the table
	 */
	public static synchronized boolean put(String key, String value)
	{
		//if we put successfully, then print the containment(debug plus to be sure that everything is fine)
		if(!Objects.equals(distributedHashTable.put(key, value), "null"))
		{
			System.out.println("DHT after this put: "+distributedHashTable);
			return true;
		}
		else
		{
				return false;
		}
	}

	public static String get(String key)
	{
		return distributedHashTable.get(key);
	}

	public static synchronized boolean del(String key)
	{
		return distributedHashTable.remove(key, distributedHashTable.get(key));
	}
}
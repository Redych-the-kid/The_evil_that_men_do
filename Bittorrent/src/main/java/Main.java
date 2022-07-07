import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {

	/*
		Prints a usage instructions:
		Notes:full path will be like: /home/nadeshiko/file_folder/file.type
	 */
	private static void manual() {
		System.out.println("""
				Usage:\s
					creating a torrent file: torrent.java + create + {some_file_full_path}
					run the torrent application: torrent.java + run + {torrent_file_path} + {seeding_port} + {ip:port list for leeching}
				"""
		);
	}

	static List<String> servers = new ArrayList<>(); //Servers for leeching
		public static void main(String[] args) {
		if (args.length == 0) { //Where is my args?
			manual();
			return;
		}
		boolean running = false; //Just to check if we succeed to run the Runner class or not
		int port = 0;
		if (args[0].equals("create")) {
			String seedFile = args[1]; //test.png for example
			try {
				byte[] sha = Utils.torrent_encode(seedFile);
				assert sha != null;
				System.out.println("Torrent file was successfully created!Now give it to someone!");
			} catch (Exception ex) {
				System.out.println("Failed to encode" + seedFile + ".torrent");
				System.out.println(ex.getMessage());
				ex.printStackTrace();
			}
			return;
		}
		//Main attraction
		else if (args[0].equals("run")) {
			if (args.length < 3) { //Too few 4 runner
				manual();
				return;
			}
			String leechFile = args[1]; // torrent file(test.png.torrent for example)
			try {
				port = Integer.parseInt(args[2]);
			} catch (Exception e) {
				System.out.println("Failed to parse the seeder port!");
				manual();
			}
			servers.addAll(Arrays.asList(args).subList(3, args.length));
			try {
				Runner swarm = new Runner();
				swarm.start(leechFile, port, servers);
			} catch (Exception e) {
				System.out.println("An exception was caught in Main!");
				e.printStackTrace();
				return;
			}
			running = true;
		}
		if (!running) {
			//No create?No run?
			manual();
		}
    }
}
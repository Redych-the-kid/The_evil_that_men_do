import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
	
	private static void printUsage() {
		System.out.println("Usage: \n" +
				"For seed: torrent.java seed [file_to_seed] port\n" +
				"For leech: torrent.java leech [torrent_file] [ip:port list to leech]\n"
		);
	}
	
	static List<String> servers = new ArrayList<>();
	static String seed_file = null;
	static String leech_file = null;

	public static void main(String[] args) {
		
		if (args.length == 0) {
			printUsage();
			return;
		}
		
		boolean is_leech = false;
		boolean is_seed = false;
		int port = 0;
	
		if (args[0].equals("seed")) {
			seed_file = args[1];
			if(null == seed_file){
				System.out.println("Error getting seed file name!");
				return;
			}
			try {
				port = Integer.parseInt(args[2]);
			} catch(Exception e) {
				System.out.println("Couldn't parse port number.");
				printUsage();
				return;
			}
			is_seed = true;
		} else if (args[0].equals("leech")) {
			leech_file = args[1];
			if(null == leech_file){
				System.out.println("Error getting leech file name!");
				return;
			}
			servers.addAll(Arrays.asList(args).subList(2, args.length));
			is_leech = true;
		} else{
			System.out.println("WHAT ARE YOU?????????");
			printUsage();
		}
		
		if (!is_seed && !is_leech) {
			printUsage();
			return;
		}
	
		if (is_leech && servers.size() == 0) {
			System.out.println("Missing server IPs.");
			printUsage();
			return;
		}
			
		try {
			if (is_leech) {
				TorrentLeech.start(leech_file, servers);
			} else{
				TorrentSeeder.start(port, seed_file);
			}
		} catch (Exception e) {
			System.out.println("main - caught exception");
			e.printStackTrace();
		}
    }

}
import java.io.IOException;
import java.util.List;

/*
	A torrent client runner
	We have two separate threads for client(leecher) and a server(seeder) parts of the program
 */

public class Runner {
	private final Leecher leecher = new Leecher() {
		@Override
		public void on_piece_received(byte[] cont, int num) {
			on_downloaded(cont, num);
			super.on_piece_received(cont, num);
		}
	};
	
	private final Seeder seeder = new Seeder();
	
	public void start(String metainfo, int port, List<String> servers){
		
		//Check if we really need a Leecher thread here
		if (servers.size() > 0) {
			// start a Leecher thread
			new Thread(() -> {
				try {
					leecher.start(metainfo, servers);
				} catch (IOException e) {
					System.out.println("Error in the leecher(client) thread!");
					e.printStackTrace();
				}
			}).start();
		} else {
			System.out.println("Seeding only mode activated");
		}
		
		// start a Seeder thread
		new Thread(() -> {
			try {
				seeder.start(port, metainfo);
			} catch (IOException e) {
				System.out.println("Error in the seeder(server) thread!");
				e.printStackTrace();
			}
		}).start();
	}
	
	// Connection between seeder and leecher
	private void on_downloaded(byte[] cont, int num) {
		seeder.add_to_available(num);
	}
}

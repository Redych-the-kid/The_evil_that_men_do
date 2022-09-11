import java.io.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serialisation utility to make backups of our table content
 */
public class Serialisator {
    private final ConcurrentHashMap<String, String> map;
    private final String server_name;

    private FileWriter writer = null;

    private BufferedReader reader = null;

    /**
     * Construction method for a Serialisator class
     *
     * @param server_name The name of the server
     * @param map         The table that we are working with
     */
    public Serialisator(String server_name, ConcurrentHashMap<String, String> map) {
        this.server_name = server_name;
        this.map = map;
    }

    /**
     * Writes the content of the table to server_name.bak file
     */
    public void write_table() {
        try {
            File f = new File(server_name + ".bak");
            writer = new FileWriter(f);
            for (ConcurrentHashMap.Entry<String, String> entry : map.entrySet()) {
                writer.write(entry.getKey() + ":" + entry.getValue());
                writer.write("\n");
            }
            if (map.isEmpty()) {
                writer.write("");
            }
        } catch (IOException e) {
            System.out.println("Failed to backup the table!");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Reads the content of the table from server_name.bak file if it exists
     */
    public void read_table() {
        try {
            File f = new File(server_name + ".bak");
            if (!f.isFile()) {
                System.out.println("No backup table were found!");
                return;
            }
            reader = new BufferedReader(new FileReader(f));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] partition = line.split(":");
                if (partition.length != 2) {
                    continue;
                }
                String key = partition[0].trim();
                String value = partition[1].trim();
                if (!key.equals("") && !value.equals("")) {
                    map.put(key, value);
                }
            }
        } catch (IOException e) {
            System.out.println("Failed to read the backup table!");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
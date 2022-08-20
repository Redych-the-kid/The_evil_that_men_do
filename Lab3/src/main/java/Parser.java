import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

class Parser {
    /*
        Parser utility class to parse the properties file(servers)
     */
    public String get_properties(String key) throws IOException {
        Properties properties = new Properties();
        String value = null;
        try {
            InputStream stream = this.getClass().getResourceAsStream("servers");
            properties.load(stream);
            value = properties.getProperty(key);
        } catch (IOException e){
            System.out.println("Failed to parse config file!");
            e.printStackTrace();
        }
        if(value == null){
            System.out.println("Failed to read the properties!");
            throw new IOException();
        }
        return value;
    }
}

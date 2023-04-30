import java.io.FileNotFoundException;
import java.io.IOException;

public class Driver {
    public static void main(String[] args) throws FileNotFoundException, IOException {
        MapGUI map = new MapGUI("triplog.csv");
    }
    
}
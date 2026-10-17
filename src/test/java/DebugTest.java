import com.airdata.service.CSVDataLoader;
import com.airdata.model.AirlineDataset;
import com.airdata.model.Flight;
import java.io.File;

public class DebugTest {
    public static void main(String[] args) throws Exception {
        CSVDataLoader loader = new CSVDataLoader();
        AirlineDataset ds = loader.loadFromFile(new File("data/sample_airline_data.csv"));
        Flight f = ds.getFlights().get(0);
        System.out.println("Flight ID: " + f.getFlightId());
        System.out.println("Airline: " + f.getAirline().getName());
        System.out.println("Origin: " + f.getOrigin().getIataCode());
        System.out.println("Dest: " + f.getDestination().getIataCode());
    }
}

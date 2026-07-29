package systemdata;

import java.util.ArrayList;

import project.Airport;
import project.Flight;

public class FlightSchedule {

	public static ArrayList<Flight> flights = new ArrayList<Flight>();


	//----------------------HELPER METHODS------------------------------------------
	
    public static void add(Flight f) {
        flights.add(f);
    }

    public static ArrayList<Flight> all() {
        return flights;
    }

    public static int size() {
        return flights.size();
    }

    public static boolean isEmpty() {
        return flights.isEmpty();
    }

    public static void clear() {
        flights.clear();
    }


    public static ArrayList<Flight> sortedByDeparture(ArrayList<Flight> arr) {
    	//vraca novu sortiranu listu po uzoru na original
    	
        ArrayList<Flight> sorted = new ArrayList<>(arr);
        sorted.sort((f1, f2) -> {
			int minutes1 = f1.getDepHour() * 60 + f1.getDepMinute();
			int minutes2 = f2.getDepHour() * 60 + f2.getDepMinute();
			return Integer.compare(minutes1, minutes2);
		});
        return sorted;
    }
    
    public static ArrayList<Flight> departingFrom(ArrayList<Airport> airports) { //odluka je da se simuliraju samo letovi ciji from Airport je cekiran,
    																			// ovo je filter metoda
        ArrayList<Flight> result = new ArrayList<>();
        for (Flight f : flights) {
            if (airports.contains(f.getFrom())) {
                result.add(f);
            }
        }
        result = sortedByDeparture(result);
        return result;
    }
}

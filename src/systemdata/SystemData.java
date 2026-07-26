package systemdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import project.Airport;
import project.Flight;


//ponovo dodati komentari

public class SystemData {

	public static ArrayList<Airport> airports = new ArrayList<Airport>();
	public static ArrayList<Flight> flights = new ArrayList<Flight>();

	private static ArrayList<Airport> checkedAirports = new ArrayList<Airport>();
	private static Map<String, Airport> map = new HashMap<>();

	private static ArrayList<Flight> activeFlights = null;

	public static void store(ArrayList<Object> arr) {
		for (Object obj : arr) {
			if (obj instanceof Airport) {
				airports.add((Airport) obj);
			} else if (obj instanceof Flight) {
				flights.add((Flight) obj);
			}
		}
	}

	public static ArrayList<Airport> getAirports() {
		return airports;
	}

	public static ArrayList<Airport> getCheckedAirports() {
		return checkedAirports;
	}

	public static void addToCheckedAirport(Airport a) {
		checkedAirports.add(a);
	}

	public static void removeFromCheckedAirport(Airport a) {
		checkedAirports.remove(a);
	}

	public static void mapAirport(Airport airport) {
		if (airport == null) {
			// ERROR
		}

		String code = airport.getCode();

		if (map.containsKey(code)) {
			// ERROR
		}

		map.put(code, airport);
	}

	public static Airport getAirport(String code) {
		Airport a = map.get(code);
		if (a == null) {
			// ERROR nepostojeci aerodrom
		}
		return a;
	}

	public static ArrayList<Flight> sortByDepartureTime(ArrayList<Flight> arr) {
		ArrayList<Flight> sorted = new ArrayList<>(arr);
		sorted.sort((f1, f2) -> {
			int minutes1 = f1.getDepHour() * 60 + f1.getDepMinute();
			int minutes2 = f2.getDepHour() * 60 + f2.getDepMinute();
			return Integer.compare(minutes1, minutes2);
		});
		return sorted;
	}

	public static ArrayList<Flight> initActiveFlights() {
		ArrayList<Flight> result = new ArrayList<>();
		for (Flight f : flights) {
			if (checkedAirports.contains(f.getFrom())) {
				result.add(f);
			}
		}
		return result;
	}

	public static ArrayList<Flight> getActiveFlights() {
		if (activeFlights == null) {
			activeFlights = new ArrayList<Flight>();
			synchronized (activeFlights) {
				activeFlights.addAll(initActiveFlights());
			}
		}
		return activeFlights;
	}

	public static void addActiveFlight(Flight f) {
		synchronized (getActiveFlights()) {
			getActiveFlights().add(f);
		}
	}

	public static boolean removeActiveFlight(Flight f) {
		synchronized (getActiveFlights()) {
			return getActiveFlights().remove(f);
		}
	}

	public static Flight removeActiveFlightAt(int index) {
		synchronized (getActiveFlights()) {
			return getActiveFlights().remove(index);
		}
	}

	public static Flight getActiveFlight(int index) {
		synchronized (getActiveFlights()) {
			return getActiveFlights().get(index);
		}
	}

	public static int activeFlightsSize() {
		synchronized (getActiveFlights()) {
			return getActiveFlights().size();
		}
	}

	public static boolean activeFlightsIsEmpty() {
		synchronized (getActiveFlights()) {
			return getActiveFlights().isEmpty();
		}
	}

	public static void resetActiveFlights() {
		activeFlights = null;
	}
	
	
	private static final ArrayList<Flight> inFlight = new ArrayList<>();

	public static void addInFlight(Flight f) {
		synchronized (inFlight) {
			inFlight.add(f);
		}
	}

	public static void removeInFlight(Flight f) {
		synchronized (inFlight) {
			inFlight.remove(f);
		}
	}

	public static ArrayList<Flight> getInFlight() {
		synchronized (inFlight) {
			return new ArrayList<>(inFlight);
		}
	}
	
	public static void clearInFlight() {
	    synchronized (inFlight) {
	        inFlight.clear();
	    }
	}

	public static void resetAirportsBusy() {
	    for (Airport a : airports) {
	        synchronized (a) {
	            a.setBusy(false);
	            a.notifyAll();
	        }
	    }
	}
	
	public static boolean isInFlightEmpty() {
	    synchronized (inFlight) {
	        return inFlight.isEmpty();
	    }
	}

	
}

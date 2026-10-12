package systemdata;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import exceptions.DuplicateAirportCodeException;
import exceptions.UnknownAirportCodeException;
import project.Airport;

public class AirportRegistry {

	private static ArrayList<Airport> airports = new ArrayList<Airport>(); //spisak svih aerodroma
	private static Map<String, Airport> byCode = new HashMap<>();  		//koristi se za brzu proveru da li je aerodorom postojeci ili ne 


    private AirportRegistry() {}  

    public static void add(Airport a) throws DuplicateAirportCodeException {
        if (byCode.containsKey(a.getCode())) {
            throw new DuplicateAirportCodeException(a.getCode());
        }
        airports.add(a);
        byCode.put(a.getCode(), a);
    }

    public static Airport get(String code) throws UnknownAirportCodeException {
        Airport a = byCode.get(code);
        if (a == null) throw new UnknownAirportCodeException(code);
        return a;
    }

    public static boolean contains(String code) { return byCode.containsKey(code); }
    public static ArrayList<Airport> all() { return airports; }
    public static int size() { return airports.size(); }
    public static boolean isEmpty() { return airports.isEmpty(); }
    public static void clear() { airports.clear(); byCode.clear(); }
}


/*
public static Airport nearestUnblocked(Airport origin) {
Airport best = null;
double bestDist = Double.MAX_VALUE;
for (Airport a : airports) {
    if (a == origin || a.isBlocked()) continue;
    double d = Airport.distance(origin, a);
    if (d < bestDist) {
        bestDist = d;
        best = a;
    }
}
return best;
}
*/
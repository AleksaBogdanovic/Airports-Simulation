package systemdata;

import java.util.ArrayList;

import exceptions.DuplicateAirportCodeException;

import project.Airport;
import project.Flight;

public class SystemData {


	public static void store(ArrayList<Object> arr) throws DuplicateAirportCodeException {
		for (Object obj : arr) {
			if (obj instanceof Airport) {
				AirportRegistry.add((Airport) obj);
			} else if (obj instanceof Flight) {
				FlightSchedule.add((Flight) obj);
			}
		}
	}

}

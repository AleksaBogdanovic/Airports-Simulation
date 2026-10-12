package file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import exceptions.FileReadException;
import exceptions.FileWriteException;
import exceptions.InvalidAirportCodeException;
import exceptions.InvalidCoordinateException;
import exceptions.InvalidHeaderException;
import exceptions.InvalidTimeFormatException;
import exceptions.MalformedLineException;
import exceptions.MissingHeaderException;
import exceptions.ParsingException;
import exceptions.UnknownAirportCodeException;

import project.Airport;
import project.Flight;

import systemdata.AirportRegistry;
import systemdata.FlightSchedule;

public class CSVParser implements Parser {
	
	private Map<String, Airport> mapAirport = new HashMap<>();  		//koristi se za brzu proveru da li je aerodorom postojeci ili ne 

	
	@Override
	public ArrayList<Object> load(String path) throws FileReadException, ParsingException {
		ArrayList<Object> result = new ArrayList<>();
		String mode = "";

		try (BufferedReader br = new BufferedReader(new FileReader(path))) {
			String line;
			while ((line = br.readLine()) != null) {

				line = line.trim();
				if (line.isEmpty()) {
					continue;
				}

				if (line.startsWith("# AIRPORTS")) {
					mode = "AIRPORTS";

					line = br.readLine();
					if (line == null) {
						throw new MissingHeaderException("AIRPORTS");
					}
					if (!line.trim().toUpperCase().equals("CODE,NAME,X,Y")) {
						throw new InvalidHeaderException("AIRPORTS", "CODE,NAME,X,Y", line);
					}
					continue;
				}

				if (line.startsWith("# FLIGHTS")) {
					mode = "FLIGHTS";

					line = br.readLine();
					if (line == null) {
						throw new MissingHeaderException("FLIGHTS");
					}
					if (!line.trim().toUpperCase().equals("FROM,TO,DEPARTURE,DURATION")) {
						throw new InvalidHeaderException("FLIGHTS", "FROM,TO,DEPARTURE,DURATION", line);
					}
					continue;
				}

				result.add(parseLine(line, mode));
			}
		} catch (IOException e) {
			throw new FileReadException(path, e.getMessage());
		}

		return result;
	}

	private Object parseLine(String line, String mode) throws ParsingException, InvalidAirportCodeException, InvalidCoordinateException {
		if (mode.equals("AIRPORTS")) {
			return parseAirportLine(line);
		} else if (mode.equals("FLIGHTS")) {
			return parseFlightLine(line);
		}
		// linija van sekcije - i ovo je greška u fajlu
		throw new MalformedLineException(line, "line must appear after '# AIRPORTS' or '# FLIGHTS'");
	}

	private Airport parseAirportLine(String line) throws MalformedLineException, InvalidAirportCodeException, InvalidCoordinateException {
		String[] data = line.split(",");
		if (data.length != 4) {
			throw new MalformedLineException(line, "CODE,NAME,X,Y");
		}

		String code = data[0].trim();
		String name = data[1].trim();
		int x, y;
		try {
			x = Integer.parseInt(data[2].trim());
			y = Integer.parseInt(data[3].trim());
		} catch (NumberFormatException e) {
			throw new MalformedLineException(line, "CODE,NAME,X,Y (X and Y must be whole numbers)");
		}

		
		Airport a = new Airport(code, name, x, y);
		mapAirport.put(code, a);

		return a;
	}

	private Flight parseFlightLine(String line) throws MalformedLineException, InvalidTimeFormatException, UnknownAirportCodeException{
		String[] data = line.split(",");
		if (data.length != 4) {
			throw new MalformedLineException(line, "FROM,TO,DEPARTURE,DURATION");
		}

		String fromCode = data[0].trim();
		String toCode = data[1].trim();
		String departure = data[2].trim();

		int duration;
		try {
			duration = Integer.parseInt(data[3].trim());
		} catch (NumberFormatException e) {
			throw new MalformedLineException(line, "FROM,TO,DEPARTURE,DURATION (DURATION must be a whole number)");
		}

		String[] time = departure.split(":");
		if (time.length != 2) {
			throw new InvalidTimeFormatException(departure); 
		}
		int depHour, depMinute;
		try {
			depHour = Integer.parseInt(time[0].trim());
			depMinute = Integer.parseInt(time[1].trim());
		} catch (NumberFormatException e) {
			throw new InvalidTimeFormatException(departure);
		}

		Airport from = mapAirport.get(fromCode);
		if(from == null) {
			throw new UnknownAirportCodeException(fromCode);
		}
		Airport to = mapAirport.get(toCode);
		if(to == null) {
			throw new UnknownAirportCodeException(toCode);
		}

		return new Flight(from, to, depHour, depMinute, duration);
	}

	@Override
	public void save(String path) throws FileWriteException {
		try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
			pw.println("# AIRPORTS");
			pw.println("CODE,NAME,X,Y");
			for (Airport a : AirportRegistry.all()) {
				pw.println(a.serialize());
			}

			pw.println("# FLIGHTS");
			pw.println("FROM,TO,DEPARTURE,DURATION");
			for (Flight f : FlightSchedule.all()) {
				pw.println(f.serialize());
			}
		} catch (IOException e) {
			throw new FileWriteException(path, e.getMessage());
		}
	}
}
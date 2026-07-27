package file;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import exceptions.FileReadException;
import exceptions.FileWriteException;
import exceptions.InvalidAirportCodeException;
import exceptions.InvalidCoordinateException;
import exceptions.InvalidTimeFormatException;
import exceptions.MalformedLineException;
import exceptions.ParsingException;
import exceptions.UnknownAirportCodeException;
import project.Airport;
import project.Flight;
import systemdata.SystemData;

public class JSONParser implements Parser {

    @Override
    public ArrayList<Object> load(String path) throws FileReadException, ParsingException{
        ArrayList<Object> result = new ArrayList<>();
        String mode = "";

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) continue;

                if (line.contains("\"airports\"") && line.contains("[")) {
                    mode = "AIRPORTS";
                    continue;
                }
                if (line.contains("\"flights\"") && line.contains("[")) {
                    mode = "FLIGHTS";
                    continue;
                }

                if (line.equals("{") || line.equals("]") || line.equals("}") || line.equals(",") ||
                    line.startsWith("},") || line.startsWith("]}")) {
                    continue;
                }

                if (line.startsWith("{")) {
                    if (line.endsWith(",")) {
                        line = line.substring(0, line.length() - 1);
                    }
                    if (!line.endsWith("}")) {
                        throw new MalformedLineException(line, "JSON object must end with '}'");
                    }

                    if (mode.equals("AIRPORTS")) {
                        Airport a = parseAirportLine(line);
                        result.add(a);
                        //SystemData.mapAirport(a); narusava enkapsulaciju
                    } else if (mode.equals("FLIGHTS")) {
                        Flight f = parseFlightLine(line);
                        result.add(f);
                    } else {
                        throw new MalformedLineException(line, "object must appear inside \"airports\" or \"flights\" array");
                    }
                }
            }
        } catch (IOException e) {
            throw new FileReadException(path, e.getMessage());
        }

        return result;
    }

  //------------------------------HELPER------------------------------------------------------------------------------------

    private String extractString(String json, String key) {
        String search = "\"" + key + "\":\"";
        int start = json.indexOf(search);
        if (start == -1) return null;
        start += search.length();
        int end = json.indexOf("\"", start);
        if (end == -1) return null;
        return json.substring(start, end);
    }

    private int extractInt(String json, String key) {
        String search = "\"" + key + "\":";
        int start = json.indexOf(search);
        if (start == -1) return 0;
        start += search.length();
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        if (end < json.length() && json.charAt(end) == '-') {
            end++;
        }
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (end == start) return 0;
        return Integer.parseInt(json.substring(start, end));
    }

    // ----- parse za objekte -----

    private Airport parseAirportLine(String line) throws MalformedLineException, InvalidAirportCodeException, InvalidCoordinateException {
        String code = extractString(line, "code");
        String name = extractString(line, "name");
        int x = extractInt(line, "x");
        int y = extractInt(line, "y");

        if (code == null || name == null) {
            throw new MalformedLineException(line, "airport object must contain \"code\", \"name\", \"x\" and \"y\"");
        }
        return new Airport(code, name, x, y);
    }

    private Flight parseFlightLine(String line) throws MalformedLineException, InvalidTimeFormatException, UnknownAirportCodeException {
        String fromCode = extractString(line, "from");
        String toCode = extractString(line, "to");
        String departure = extractString(line, "departure");
        int duration = extractInt(line, "duration");

        if (fromCode == null || toCode == null || departure == null) {
            throw new MalformedLineException(line, "flight object must contain \"from\", \"to\", \"departure\" and \"duration\"");
        }

        String[] parts = departure.split(":");
        if (parts.length != 2) {
            throw new InvalidTimeFormatException(departure);
        }
        int depHour, depMinute;
        try {
            depHour = Integer.parseInt(parts[0].trim());
            depMinute = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException e) {
            throw new InvalidTimeFormatException(departure);
        }

        Airport from = SystemData.getAirport(fromCode);
        Airport to = SystemData.getAirport(toCode);
        if (from == null) {
            throw new UnknownAirportCodeException(fromCode);
        }
        if (to == null) {
            throw new UnknownAirportCodeException(toCode);
        }

        return new Flight(from, to, depHour, depMinute, duration);
    }


	@Override
	public void save(String path) throws FileWriteException {
	    try (PrintWriter pw = new PrintWriter(new FileWriter(path, false))) {
	        pw.println("{");

	        // AIRPORTS 
	        pw.println("\"airports\":[");
	        ArrayList<Airport> airports = SystemData.getAirports();
	        for (int i = 0; i < airports.size(); i++) {
	            Airport a = airports.get(i);
	            String comma = (i < airports.size() - 1) ? "," : "";
	            pw.println(serializeAirport(a) + comma);
	        }
	        pw.println("],");

	        // FLIGHTS 
	        pw.println("\"flights\":[");
	        ArrayList<Flight> flights = SystemData.flights;
	        for (int i = 0; i < flights.size(); i++) {
	            Flight f = flights.get(i);
	            String comma = (i < flights.size() - 1) ? "," : "";
	            pw.println(serializeFlight(f) + comma);
	        }
	        pw.println("]");

	        pw.println("}");
	    } catch (IOException e) {
	        throw new FileWriteException(path, e.getMessage());
	    }
	}

//------------------------------HELPER------------------------------------------------------------------------------------
	private String serializeAirport(Airport a) {
	    return "{\"code\":\"" + a.getCode() + "\","
	            + "\"name\":\"" + a.getName() + "\","
	            + "\"x\":" + a.getX() + ","
	            + "\"y\":" + a.getY() + "}";
	}

	private String serializeFlight(Flight f) {
	    return "{\"from\":\"" + f.getFrom().getCode() + "\","
	            + "\"to\":\"" + f.getTo().getCode() + "\","
	            + "\"departure\":\"" + String.format("%02d:%02d", f.getDepHour(), f.getDepMinute()) + "\","
	            + "\"duration\":" + f.getDuration() + "}";
	}
}
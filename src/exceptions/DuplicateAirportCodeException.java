package exceptions;

public class DuplicateAirportCodeException extends ParsingException{
	public DuplicateAirportCodeException(String airportCode) {
		super("Airport with code name \"" + airportCode + "\" already exists.");
	}
}

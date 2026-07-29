package exceptions;

public class InvalidAirportCodeException extends ParsingException{

	public InvalidAirportCodeException(String code, String airportName) {
		super("Airport code: \"" + code + "\" for airport " + airportName + " is invalid. Should only contain three uperrcase letters.");
	}
}

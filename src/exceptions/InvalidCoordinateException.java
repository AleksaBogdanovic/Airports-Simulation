package exceptions;

public class InvalidCoordinateException extends ParsingException{
	public InvalidCoordinateException(String axis, int value, double xMin, double xMax, String airportName) {
        super("Invalid " + axis + " coordinate: " + value
                + "for airport \"" + airportName + "\". Value must be between " + xMin + " and " + xMax + "." );
    }
}

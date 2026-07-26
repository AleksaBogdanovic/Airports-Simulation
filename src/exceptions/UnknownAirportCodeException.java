package exceptions;

public class UnknownAirportCodeException extends ParsingException {
    public UnknownAirportCodeException(String code) {
        super("Flight references unknown airport code \"" + code
                + "\". Make sure all airports are defined before flights in the file.");
    }
}
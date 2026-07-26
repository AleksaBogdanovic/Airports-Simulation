package exceptions;

public class InvalidHeaderException extends ParsingException {
    public InvalidHeaderException(String section, String expected, String actual) {
        super("Invalid header for section '" + section + "'. Expected \"" + expected
                + "\", but found \"" + actual + "\".");
    }
}

package exceptions;

public class InvalidTimeFormatException extends ParsingException {
    public InvalidTimeFormatException(String value) {
        super("Invalid departure time \"" + value + "\". Expected format is HH:MM.");
    }
}

package exceptions;

public class MissingHeaderException extends ParsingException {
    public MissingHeaderException(String section) {
        super("Missing header line after '# " + section + "' marker.");
    }
}

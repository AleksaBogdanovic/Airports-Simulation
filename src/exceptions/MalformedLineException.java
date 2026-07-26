package exceptions;

public class MalformedLineException extends ParsingException {
    public MalformedLineException(String line, String expectedFormat) {
        super("Malformed line: \"" + line + "\". Expected format: " + expectedFormat + ".");
    }
}
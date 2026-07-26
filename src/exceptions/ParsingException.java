package exceptions;

public abstract class ParsingException extends Exception {
    protected ParsingException(String message) {
        super(message);
    }
}
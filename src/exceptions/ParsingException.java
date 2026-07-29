package exceptions;

public abstract class ParsingException extends MyException {
    protected ParsingException(String message) {
        super(message);
    }
}
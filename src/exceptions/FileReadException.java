package exceptions;

public class FileReadException extends MyException {
    public FileReadException(String path, String reason) {
        super("Could not read file \"" + path + "\": " + reason);
    }
}
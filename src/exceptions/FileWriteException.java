package exceptions;

public class FileWriteException extends MyException {
    public FileWriteException(String path, String reason) {
        super("Could not write file \"" + path + "\": " + reason);
    }
}

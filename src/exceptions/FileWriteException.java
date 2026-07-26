package exceptions;

public class FileWriteException extends Exception {
    public FileWriteException(String path, String reason) {
        super("Could not write file \"" + path + "\": " + reason);
    }
}

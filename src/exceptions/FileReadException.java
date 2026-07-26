package exceptions;

public class FileReadException extends Exception {
    public FileReadException(String path, String reason) {
        super("Could not read file \"" + path + "\": " + reason);
    }
}
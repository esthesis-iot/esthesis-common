package esthesis.common.exception;

/**
 * An exception representing a generic processing error.
 */
public class QProcessingException extends QException {

  public QProcessingException() {
    super();
  }

  public QProcessingException(String message) {
    super(message);
  }

  public QProcessingException(String message, Throwable cause) {
    super(message, cause);
  }

  public QProcessingException(String message, Object... args) {
    super(message, args);
  }
}

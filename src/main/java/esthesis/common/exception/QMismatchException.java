package esthesis.common.exception;

/**
 * A generic exception representing a "mismatch" condition.
 */
public class QMismatchException extends QException {

  public QMismatchException() {
    super();
  }

  public QMismatchException(String message) {
    super(message);
  }

  public QMismatchException(String message, Throwable cause) {
    super(message, cause);
  }

  public QMismatchException(String message, Object... args) {
    super(message, args);
  }
}

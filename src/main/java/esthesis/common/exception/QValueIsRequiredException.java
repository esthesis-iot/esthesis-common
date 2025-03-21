package esthesis.common.exception;

/**
 * A generic exception representing a "data has null value, but is required" condition.
 */
public class QValueIsRequiredException extends QException {

  public QValueIsRequiredException() {
    super();
  }

  public QValueIsRequiredException(String message) {
    super(message);
  }

  public QValueIsRequiredException(String message, Throwable cause) {
    super(message, cause);
  }

  public QValueIsRequiredException(String message, Object... args) {
    super(message, args);
  }
}

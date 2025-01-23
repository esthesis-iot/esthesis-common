package esthesis.common.exception;

/**
 * A generic exception representing a "can not save the object" condition.
 */
public class QCouldNotSaveException extends QException {

  public QCouldNotSaveException() {
    super();
  }

  public QCouldNotSaveException(String message) {
    super(message);
  }

  public QCouldNotSaveException(String message, Throwable cause) {
    super(message, cause);
  }
}

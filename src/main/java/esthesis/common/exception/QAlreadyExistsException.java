package esthesis.common.exception;

/**
 * A generic exception representing an "entity already exists in the system" condition.
 */
public class QAlreadyExistsException extends QException {

  public QAlreadyExistsException() {
    super();
  }

  public QAlreadyExistsException(String message) {
    super(message);
  }

  public QAlreadyExistsException(String message, Throwable cause) {
    super(message, cause);
  }

  public QAlreadyExistsException(String message, Object... args) {
    super(message, args);
  }
}

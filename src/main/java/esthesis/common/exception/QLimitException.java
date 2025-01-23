package esthesis.common.exception;

/**
 * A generic exception indicating that a limit has been reached.
 */
public class QLimitException extends QException {

  public QLimitException() {
    super();
  }

  public QLimitException(String msg) {
    super(msg);
  }

  public QLimitException(String msg, Object... args) {
    super(msg, args);
  }
}

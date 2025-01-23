package esthesis.common.exception;

/**
 * A generic exception superclass to facilitate marking of any type of security-related exception.
 */
public class QSecurityException extends QException {

  public QSecurityException() {
    super();
  }

  public QSecurityException(String msg) {
    super(msg);
  }

  public QSecurityException(String msg, Throwable cause) {
    super(msg, cause);
  }

  public QSecurityException(String msg, Throwable cause, Object... args) {
    super(msg, cause, args);
  }

  public QSecurityException(String msg, Object... args) {
    super(msg, args);
  }
}

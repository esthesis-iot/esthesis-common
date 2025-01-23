package esthesis.common.exception;

/**
 * A generic exception superclass to facilitate marking of authorisation-related exception.
 */
@SuppressWarnings("squid:MaximumInheritanceDepth")
public class QAuthorisationException extends QSecurityException {

  public QAuthorisationException() {
    super();
  }

  public QAuthorisationException(String msg) {
    super(msg);
  }

  public QAuthorisationException(String msg, Object... args) {
    super(msg, args);
  }
}

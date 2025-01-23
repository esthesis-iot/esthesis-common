package esthesis.common.exception;

import java.io.Serializable;

/**
 * Parent exception type.
 */
public abstract class QException extends RuntimeException implements Serializable {

  protected QException() {
  }

  protected QException(String message) {
    super(message);
  }

  protected QException(String message, Object... args) {
    super(org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage());
  }

  protected QException(String message, Throwable cause) {
    super(message, cause);
  }

  protected QException(String message, Throwable cause, Object... args) {
    super(org.slf4j.helpers.MessageFormatter.arrayFormat(message, args).getMessage(), cause);
  }

  protected QException(Throwable cause) {
    super(cause);
  }

  protected QException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }
}

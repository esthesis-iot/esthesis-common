package esthesis.common.util;

/**
 * Global constants used in various components across modules.
 */
public class EsthesisCommonConstants {

  private EsthesisCommonConstants() {
  }

  public static class Device {

    public enum Type {
      CORE, EDGE, OTHER
    }

    public enum Capability {
      PING,           // The device can send ping beacons.
      REBOOT,         // The device can be rebooted.
      SHUTDOWN,       // The device can be shut down.
      EXECUTE,        // The device has a shell to execute commands.
      PROVISIONING,   // The device can receive and process provisioning commands.
      TELEMETRY,      // The device can send telemetry data.
      METADATA        // The device can send metadata.
    }
  }
}

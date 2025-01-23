package esthesis.common.avro;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

/**
 * Represents a single entry (line) in an ELP file. Each line may contain multiple measurements.
 */
@Data
@Builder
public class ELPEntry {

  private String category;
  private Instant date;
  private List<ELPMeasurement> measurements;

  /**
   * Represents a single measurement in an ELP entry.
   */
  @Data
  @AllArgsConstructor
  public static class ELPMeasurement {

    private String name;
    private String value;
  }

  /**
   * Builder class for ELP entries.
   */
  public static class ELPEntryBuilder {

    public ELPEntryBuilder measurement(String name, String value) {
      if (this.measurements == null) {
        this.measurements = new java.util.ArrayList<>();
      }
      this.measurements.add(new ELPMeasurement(name, value));
      return this;
    }
  }

  /**
   * Creates a custom string representation of the ELP entry.
   *
   * @return a string representation of the ELP entry
   */
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(category);
    sb.append(" ");
    for (ELPMeasurement measurement : measurements) {
      sb.append(measurement.name);
      sb.append("=");
      sb.append(measurement.value);
      sb.append(" ");
    }
    if (date != null) {
      sb.append(date);
    } else {
      sb.deleteCharAt(sb.length() - 1);
    }

    return sb.toString();
  }
}

package esthesis.common.avro;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ELPEntry {

  private String category;
  private Instant date;
  private List<ELPMeasurement> measurements;

  @Data
  @AllArgsConstructor
  public static class ELPMeasurement {

    private String name;
    private String value;
  }

  public static class ELPEntryBuilder {

    public ELPEntryBuilder measurement(String name, String value) {
      if (this.measurements == null) {
        this.measurements = new java.util.ArrayList<>();
      }
      this.measurements.add(new ELPMeasurement(name, value));
      return this;
    }
  }

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

package esthesis.common.avro;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.quarkus.test.junit.QuarkusTest;
import java.time.Instant;
import org.junit.jupiter.api.Test;

@QuarkusTest
class ELPEntryTest {

  @Test
  void testToString() {
    Instant date = Instant.parse("2022-10-10T23:59:59Z");
    ELPEntry entry = ELPEntry.builder()
        .category("category")
        .measurement("name", "value")
        .date(date)
        .build();
    assertEquals("category name=value 2022-10-10T23:59:59Z", entry.toString());

    entry = ELPEntry.builder()
        .category("category")
        .measurement("name1", "value1")
        .measurement("name2", "value2")
        .date(date)
        .build();
    assertEquals("category name1=value1 name2=value2 2022-10-10T23:59:59Z", entry.toString());

    entry = ELPEntry.builder()
        .category("category")
        .measurement("name", "value")
        .build();
    assertEquals("category name=value", entry.toString());

    entry = ELPEntry.builder()
        .category("category")
        .measurement("name1", "value1")
        .measurement("name2", "value2")
        .build();
    assertEquals("category name1=value1 name2=value2", entry.toString());
  }
}

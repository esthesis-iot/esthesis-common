package esthesis.common.avro;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

@QuarkusTest
class AvroUtilsTest {

  @Inject
  AvroUtils avroUtils;

  @Test
  void parsePayload() {
    String[] dataList = {
        "cpu load=1",
        "cpu load=1 2022-01-01T01:02:03Z",
        "cpu load=1,temperature=20",
        "cpu load=1,temperature=20 2022-01-01T01:02:03Z",
        "net ip1='primary 192.168.1.1'",
        "net ip1='primary 192.168.1.1' 2022-01-01T01:02:03Z",
        "net ip1='primary 192.168.1.1',ip2='secondary 10.250.1.1'",
        "net ip1='primary 192.168.1.1',ip2='secondary 10.250.1.1' 2022-01-01T01:02:03Z"
    };

    for (String data : dataList) {
      System.out.println("Testing: " + data);
      PayloadData payloadData = avroUtils.parsePayload(data);
      assertNotNull(payloadData);
    }
  }

  @Test
  void parseCommandReplyLP() {
  }

  @Test
  void commandRequestToLineProtocol() {
  }
}

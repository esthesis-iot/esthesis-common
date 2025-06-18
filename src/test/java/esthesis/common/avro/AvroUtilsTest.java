package esthesis.common.avro;

import esthesis.common.exception.QMismatchException;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Slf4j
@QuarkusTest
class AvroUtilsTest {

    @Inject
    AvroUtils avroUtils;

    @ParameterizedTest
    @ValueSource(strings = {
            "cpu load=1",
            "cpu load='1m'",
            "cpu load=1 2022-01-01T01:02:03Z",
            "cpu load=1,temperature=20",
            "cpu load=1,temperature=20 2022-01-01T01:02:03Z",
            "net ip1='primary 192.168.1.1'",
            "net ip1='primary 192.168.1.1' 2022-01-01T01:02:03Z",
            "net ip1='primary 192.168.1.1',ip2='secondary 10.250.1.1'",
            "net ip1='primary 192.168.1.1',ip2='secondary 10.250.1.1' 2022-01-01T01:02:03Z"
    })
    void parseValidPayloads(String input) {
        log.info("Testing valid payload: {}", input);
        PayloadData payloadData = avroUtils.parsePayload(input);
        log.debug("Parsed payload: {}", payloadData);
        assertNotNull(payloadData.getCategory());
        assertNotNull(payloadData.getTimestamp());
        assertFalse(payloadData.getValues().isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "load=1",
            "cpu",
            "cpu ",
            "cpu 2022-01-01T01:02:03Z",
            "cpu load=",
            "cpu load=,temperature=20",
            "cpu load=1,temperature=",
            "cpu load=1,temperature",
            "cpu load=1 temperature=20",
            "cpu load=1,,temperature=20",
            "cpu load=1 2022-01-01",
            "cpu load=1 01:02:03Z",
            "cpu load=1 20220101T010203Z",
            "cpu load=1 2022-01-01T01:02:03",
            "cpu load='1",
            "!@#$%^&*()",
            "cpu !@#$%^&*()"
    })
    void parseInvalidPayloads(String input) {
        log.info("Testing invalid payload: {}", input);
        assertThrows(QMismatchException.class, () -> avroUtils.parsePayload(input),
                "Expected QMismatchException for input: " + input);
    }

}

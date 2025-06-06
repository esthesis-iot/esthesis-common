package esthesis.common.crypto.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.security.KeyPair;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of a request to create a certificate.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode
@RegisterForReflection
@Accessors(chain = true)
public class CreateCertificateRequestDTO {

  private KeyPair keyPair;
  private String cn;
  private boolean includeCertificateChain;
}

package esthesis.common.crypto.dto;

import jakarta.validation.constraints.NotNull;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.util.Locale;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of a request to sign a certificate.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CertificateSignRequestDTO {

  @NotNull
  private PrivateKey privateKey;
  @NotNull
  private PublicKey publicKey;
  @NotNull
  private String issuerCN;
  @NotNull
  private PrivateKey issuerPrivateKey;
  @NotNull
  private Instant validForm;
  @NotNull
  private Instant validTo;
  @NotNull
  private Locale locale;
  @NotNull
  private String subjectCN;
  // A comma-separated list of SANs that will be added to the certificate as a Subject Alternative
  // Names of type DNS.
  private String san;
  @NotNull
  private String signatureAlgorithm;

  private boolean ca;
}

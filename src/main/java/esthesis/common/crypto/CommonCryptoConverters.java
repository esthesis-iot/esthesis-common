package esthesis.common.crypto;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

@Slf4j
public class CommonCryptoConverters {
  
  public static String removeHeaderFooter(final String key) {
    String regex = "---.*---\\n*";
    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    final Matcher matcher = pattern.matcher(key);

    return matcher.replaceAll("");
  }

  /**
   * Parses a certificate in PEM format encoded as X.509.
   *
   * @param cert the certificate in PEM format
   * @return the generated certificate
   * @throws CertificateException thrown when something unexpected happens while generating the
   *                              certificate
   */
  public static X509Certificate pemToCertificate(final String cert)
  throws CertificateException {
    log.trace("Parsing '{}' PEM certificate.", cert);
    CertificateFactory fact = CertificateFactory.getInstance("X.509");

    return (X509Certificate) fact.generateCertificate(
        new ByteArrayInputStream(cert.getBytes(StandardCharsets.UTF_8)));
  }

  /**
   * Converts a PEM/PKCS8 private key to a {@link PrivateKey}.
   *
   * @param privateKey the private key to convert
   * @param algorithm  the security algorithm with which this key was generated
   * @return the generated PEM format
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws InvalidKeySpecException  thrown when the provided key is invalid
   */
  public static PrivateKey pemToPrivateKey(String privateKey, final String algorithm)
  throws NoSuchAlgorithmException, InvalidKeySpecException {
    log.trace("Converting PEM private key '{}' to PrivateKey.", privateKey);

    // Cleanup the PEM from unwanted text.
    privateKey = removeHeaderFooter(privateKey).trim();

    // Read the cleaned up PEM and generate the public key.
    byte[] encoded = Base64.decodeBase64(privateKey);
    final PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(encoded);
    final KeyFactory factory = KeyFactory.getInstance(algorithm);

    return factory.generatePrivate(keySpec);
  }
}

package esthesis.common.crypto;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x509.Certificate;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcaPKCS8Generator;
import org.bouncycastle.util.io.pem.PemObject;
import org.bouncycastle.util.io.pem.PemWriter;

/**
 * Utility class for converting between different cryptographic formats.
 */
@Slf4j
public class CryptoConvertersUtil {

  private CryptoConvertersUtil() {
  }

  // PEM header for certificates.
  private static final String CERTIFICATE = "CERTIFICATE";

  /**
   * Removes the header and footer of a PEM encoded key.
   *
   * @param key the key to remove the header and footer from
   * @return the key without the header and footer
   */
  public static String removeHeaderFooter(final String key) {
    String regex = "---.*---\\n*";
    final Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
    final Matcher matcher = pattern.matcher(key);

    return matcher.replaceAll("");
  }

  /**
   * Converts a certificate to a PEM format encoded as X.509.
   *
   * @return the generated PEM
   * @throws IOException thrown when something unexpected happens
   */
  public static String certificateToPEM(final Certificate certificate)
  throws IOException {
    log.trace("Converting '{}' certificate to PEM.", certificate);
    try (StringWriter pemStrWriter = new StringWriter()) {
      try (PemWriter writer = new PemWriter(pemStrWriter)) {
        writer.writeObject(
            new PemObject(CERTIFICATE, certificate.getEncoded()));
        writer.flush();
        return pemStrWriter.toString();
      }
    }
  }

  /**
   * Converts a {@link PrivateKey} to a PEM format.
   *
   * @param key the private key to convert
   * @return the generated PEM format
   * @throws IOException
   */
  public static String privateKeyToPEM(final PrivateKey key) throws IOException {
    try (StringWriter pemStrWriter = new StringWriter()) {
      try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
        pemWriter.writeObject(new JcaPKCS8Generator(key, null));
        pemWriter.flush();
        return pemStrWriter.toString();
      }
    }
  }

  /**
   * Converts a {@link PublicKey} to a PEM format.
   *
   * @param key the public key to convert
   * @return the generated PEM format
   * @throws IOException thrown when something unexpected happens
   */
  public static String publicKeyToPEM(final PublicKey key) throws IOException {
    try (StringWriter pemStrWriter = new StringWriter()) {
      try (JcaPEMWriter pemWriter = new JcaPEMWriter(pemStrWriter)) {
        pemWriter.writeObject(key);
        pemWriter.flush();
        return pemStrWriter.toString();
      }
    }
  }

  /**
   * Converts a text-based public key (in PEM format) to {@link PublicKey}.
   *
   * @param publicKey the public key in PEM format to convert
   * @param algorithm the security algorithm with which this key was generated
   * @return the generated PEM format
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws InvalidKeySpecException  thrown when the provided key is invalid
   */
  public static PublicKey pemToPublicKey(String publicKey, final String algorithm)
  throws NoSuchAlgorithmException, InvalidKeySpecException {
    PublicKey key;

    // Cleanup the PEM from unwanted text.
    publicKey = removeHeaderFooter(publicKey).trim();

    // Read the cleaned up PEM and generate the public key.
    byte[] encoded = Base64.decodeBase64(publicKey);
    final X509EncodedKeySpec keySpec = new X509EncodedKeySpec(encoded);
    final KeyFactory factory = KeyFactory.getInstance(algorithm);
    key = factory.generatePublic(keySpec);

    return key;
  }

  /**
   * Converts a {@link KeyStore} to a byte array.
   *
   * @param keystore         The keystore to convert.
   * @param keystorePassword The password of the keystore.
   */
  public static byte[] keystoreSerialize(final KeyStore keystore,
      final String keystorePassword)
  throws IOException, CertificateException, NoSuchAlgorithmException,
         KeyStoreException {
    try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(baos)) {
      if (StringUtils.isNotBlank(keystorePassword)) {
        keystore.store(bos, keystorePassword.toCharArray());
      } else {
        keystore.store(bos, null);
      }

      return baos.toByteArray();
    }
  }

  /**
   * Converts a byte array representing a {@link KeyStore} to a KeyStore.
   *
   * @param keystore         The keystore representation as a byte array.
   * @param keystoreType     The type of the keystore, e.g. PKCS12
   * @param keystorePassword The password of the keystore.
   * @param keystoreProvider A provider for the specific keystore type.
   */
  public static KeyStore keystoreDeserialize(final byte[] keystore,
      final String keystoreType, final String keystorePassword,
      final String keystoreProvider)
  throws KeyStoreException, NoSuchProviderException, IOException,
         CertificateException,
         NoSuchAlgorithmException {
    final KeyStore ks;

    if (StringUtils.isBlank(keystoreType) || StringUtils
        .isBlank(keystoreProvider)) {
      ks = KeyStore.getInstance(KeyStore.getDefaultType());
    } else {
      ks = KeyStore.getInstance(keystoreType, keystoreProvider);
    }
    try (BufferedInputStream bis = new BufferedInputStream(
        new ByteArrayInputStream(keystore))) {
      if (StringUtils.isNotBlank(keystorePassword)) {
        ks.load(bis, keystorePassword.toCharArray());
      } else {
        ks.load(bis, null);
      }
    }

    return ks;
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

package esthesis.common.crypto;

import esthesis.common.crypto.dto.CAHolderDTO;
import esthesis.common.crypto.dto.CertificateSignRequestDTO;
import esthesis.common.crypto.dto.CreateCARequestDTO;
import esthesis.common.crypto.dto.CreateKeyPairRequestDTO;
import esthesis.common.crypto.dto.SSLSocketFactoryCertificateDTO;
import esthesis.common.crypto.dto.SSLSocketFactoryDTO;
import esthesis.common.crypto.dto.SignatureVerificationRequestDTO;
import esthesis.common.exception.QDoesNotExistException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;

/**
 * Utility class for cryptographic operations.
 */
@Slf4j
public class CryptoUtil {

  private CryptoUtil() {
  }

  private static final Pattern ipv4Pattern = Pattern.compile(
      "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");
  private static final String CN = "CN";
  public static final String CERT_TYPE = "X509";

  /**
   * Checks whether the provided string is a valid IPV4 address.
   *
   * @param ipAddress the IP address to check.
   * @return true if the IP address is valid, false if it is not.
   */
  private static boolean isValidIPV4Address(final String ipAddress) {
    Matcher matcher = ipv4Pattern.matcher(ipAddress);
    return matcher.matches();
  }

  /**
   * Finds the requested secure random algorithm or returns the default one.
   *
   * @param secureRandomAlgorithm the secure random algorithm to find.
   * @return the secure random algorithm.
   */
  private static SecureRandom getSecureRandomAlgorithm(final String secureRandomAlgorithm)
  throws NoSuchAlgorithmException {
    SecureRandom selectedAlgorithm;
    if (StringUtils.isBlank(secureRandomAlgorithm)) {
      selectedAlgorithm = SecureRandom.getInstanceStrong();
    } else {
      selectedAlgorithm = SecureRandom.getInstance(secureRandomAlgorithm);
    }

    return selectedAlgorithm;
  }

  /**
   * Removes "CN=" part from the CN string.
   *
   * @param cn CN string
   * @return cleaned up CN string
   */
  public static String cleanUpCn(String cn) {
    if (cn.startsWith(CN + "=")) {
      return cn.substring(CN.length() + 1);
    } else {
      return cn;
    }
  }

  /**
   * Create a new Certificate Authority. This method also supports creating a sub-CA by providing
   * the issuer's information.
   *
   * @param createCARequestDTO the details of the CA to be created
   * @return the generated certificate
   * @throws NoSuchAlgorithmException  thrown when no algorithm is found for encryption
   * @throws InvalidKeySpecException   thrown when the provided key is invalid
   * @throws OperatorCreationException thrown when something unexpected happens during the
   *                                   encryption
   * @throws IOException               thrown when something unexpected happens
   */
  public static CAHolderDTO createCA(final CreateCARequestDTO createCARequestDTO)
  throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException,
         NoSuchProviderException {
    // Create a keypair for this CA.
    final KeyPair keyPair = createKeyPair(createCARequestDTO.getCreateKeyPairRequestDTO());

    // Prepare signing.
    CertificateSignRequestDTO certificateSignRequestDTO = new CertificateSignRequestDTO();
    certificateSignRequestDTO.setValidForm(createCARequestDTO.getValidFrom());
    certificateSignRequestDTO.setValidTo(createCARequestDTO.getValidTo());
    certificateSignRequestDTO.setLocale(createCARequestDTO.getLocale());
    certificateSignRequestDTO.setPublicKey(keyPair.getPublic());
    certificateSignRequestDTO.setPrivateKey(keyPair.getPrivate());
    certificateSignRequestDTO.setSignatureAlgorithm(createCARequestDTO.getSignatureAlgorithm());
    certificateSignRequestDTO.setSubjectCN(createCARequestDTO.getSubjectCN());
    certificateSignRequestDTO.setCa(true);

    // Choose which private key to use. If no parent key is found then this is a self-signed
    // certificate and the private key created for the keypair will be used.
    if (StringUtils.isNotEmpty(createCARequestDTO.getIssuerCN())
        && createCARequestDTO.getIssuerPrivateKey() != null) {
      certificateSignRequestDTO.setIssuerPrivateKey(createCARequestDTO.getIssuerPrivateKey());
      certificateSignRequestDTO.setIssuerCN(createCARequestDTO.getIssuerCN());
    } else {
      certificateSignRequestDTO.setIssuerPrivateKey(keyPair.getPrivate());
      certificateSignRequestDTO.setIssuerCN(createCARequestDTO.getSubjectCN());
    }

    final X509CertificateHolder certHolder = generateCertificate(certificateSignRequestDTO);

    // Prepare reply.
    final CAHolderDTO cppPemKey = new CAHolderDTO();
    cppPemKey.setPublicKey(keyPair.getPublic());
    cppPemKey.setPrivateKey(keyPair.getPrivate());
    cppPemKey.setCertificate(certHolder.toASN1Structure());

    return cppPemKey;
  }

  /**
   * Signs a key with another key providing a certificate.
   *
   * @param certificateSignRequestDTO the details of the signing to take place
   * @return the generated signature
   * @throws OperatorCreationException thrown when something unexpected happens during the
   *                                   encryption
   * @throws CertIOException           thrown when something unexpected happens while generating the
   *                                   certificate
   */
  @SuppressWarnings({"squid:S2274", "squid:S2142"})
  public static X509CertificateHolder generateCertificate(
      final CertificateSignRequestDTO certificateSignRequestDTO)
  throws OperatorCreationException, CertIOException {
    // Create a generator for the certificate including all certificate details.
    final X509v3CertificateBuilder certGenerator;

    certGenerator = new X509v3CertificateBuilder(new X500Name(
        CN + "=" + StringUtils.defaultIfBlank(certificateSignRequestDTO.getIssuerCN(),
            certificateSignRequestDTO.getSubjectCN())),
        certificateSignRequestDTO.isCa() ? BigInteger.ONE
            : BigInteger.valueOf(Instant.now().toEpochMilli()),
        new Date(certificateSignRequestDTO.getValidForm().toEpochMilli()),
        new Date(certificateSignRequestDTO.getValidTo().toEpochMilli()),
        certificateSignRequestDTO.getLocale(),
        new X500Name(CN + "=" + certificateSignRequestDTO.getSubjectCN()),
        SubjectPublicKeyInfo.getInstance(certificateSignRequestDTO.getPublicKey().getEncoded()));

    // Add SANs.
    if (StringUtils.isNotEmpty(certificateSignRequestDTO.getSan())) {
      GeneralNames subjectAltNames = new GeneralNames(
          Arrays.stream(certificateSignRequestDTO.getSan().split(",")).map(String::trim).map(s -> {
            if (isValidIPV4Address(s)) {
              return new GeneralName(GeneralName.iPAddress, s);
            } else {
              return new GeneralName(GeneralName.dNSName, s);
            }
          }).toArray(GeneralName[]::new));
      certGenerator.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);
    }

    // Check if this is a CA certificate and in that case add the necessary key extensions.
    if (certificateSignRequestDTO.isCa()) {
      certGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
      certGenerator.addExtension(Extension.keyUsage, true,
          new KeyUsage(KeyUsage.cRLSign | KeyUsage.keyCertSign));
    } else {
      certGenerator.addExtension(Extension.basicConstraints, true, new BasicConstraints(false));
    }

    // Generate the certificate.
    final X509CertificateHolder certHolder;
    certHolder = certGenerator.build(
        new JcaContentSignerBuilder(certificateSignRequestDTO.getSignatureAlgorithm()).build(
            certificateSignRequestDTO.getIssuerPrivateKey()));

    return certHolder;
  }

  /**
   * Generates a new keypair consisting of a public key and a private key.
   *
   * @param createKeyPairRequestDTO The details of the keypair to create
   * @return the generated keypair
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   */
  public static KeyPair createKeyPair(final CreateKeyPairRequestDTO createKeyPairRequestDTO)
  throws NoSuchAlgorithmException, NoSuchProviderException {
    final KeyPairGenerator keyPairGenerator;

    // Set the provider.
    if (StringUtils.isNotBlank(createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm())
        && StringUtils.isNotBlank(createKeyPairRequestDTO.getKeyPairGeneratorProvider())) {
      keyPairGenerator = KeyPairGenerator.getInstance(
          createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm(),
          createKeyPairRequestDTO.getKeyPairGeneratorProvider());
    } else {
      keyPairGenerator = KeyPairGenerator.getInstance(
          createKeyPairRequestDTO.getKeyPairGeneratorAlgorithm());
    }

    // Set the secret provider and generator.
    keyPairGenerator.initialize(createKeyPairRequestDTO.getKeySize(),
        getSecureRandomAlgorithm(createKeyPairRequestDTO.getSecureRandomAlgorithm()));

    return keyPairGenerator.generateKeyPair();
  }

  /**
   * Converts a byte array holding a private key in DER format to a private key.
   *
   * @param key          the private key in DER format
   * @param keyAlgorithm the security algorithm with which this key was generated
   * @param keyProvider  the provider for the specific key type
   * @return the generated private key
   * @throws NoSuchProviderException  thrown when the provided provider is invalid
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws InvalidKeySpecException  thrown when the provided key is invalid
   */
  public static PrivateKey privateKeyFromByteArray(final byte[] key, final String keyAlgorithm,
      final String keyProvider)
  throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException {
    log.debug("Converting private key '{}' PrivateKey.", key);
    KeyFactory keyFactory;
    if (StringUtils.isNotBlank(keyProvider)) {
      keyFactory = KeyFactory.getInstance(keyAlgorithm, keyProvider);
    } else {
      keyFactory = KeyFactory.getInstance(keyAlgorithm);
    }
    EncodedKeySpec encodedKeySpec = new PKCS8EncodedKeySpec(key);
    return keyFactory.generatePrivate(encodedKeySpec);
  }

  /**
   * Creates an empty keystore. This keystore can later on be used to add keys and certificates into
   * it.
   *
   * @param keystoreType     the type of the keystore.
   * @param keystoreProvider the provider for the specific type of keystore.
   * @param keystorePassword the password of the keystore.
   * @return the keystore as a byte array.
   * @throws KeyStoreException        thrown when something unexpected happens while generating the
   * @throws NoSuchProviderException  thrown when the provided provider is invalid
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws IOException              thrown when something unexpected happens
   * @throws CertificateException     thrown when something unexpected happens while generating the
   */
  public static byte[] createKeystore(final String keystoreType, final String keystoreProvider,
      final String keystorePassword)
  throws KeyStoreException, NoSuchProviderException, NoSuchAlgorithmException, IOException,
         CertificateException {
    // Create a new keystore.
    KeyStore ks;
    if (StringUtils.isBlank(keystoreType) || StringUtils.isBlank(keystoreProvider)) {
      ks = KeyStore.getInstance(KeyStore.getDefaultType());
    } else {
      ks = KeyStore.getInstance(keystoreType, keystoreProvider);
    }

    // Initialise the new keystore with user-provided password.
    if (StringUtils.isNotBlank(keystorePassword)) {
      ks.load(null, keystorePassword.toCharArray());
    } else {
      ks.load(null, null);
    }

    return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
  }

  /**
   * Saves a certificate to the keystore. If the certificate identified by the alias already exists
   * it gets overwritten.
   *
   * @param keystore         The keystore to save the certificate into.
   * @param keystoreType     The type of the keystore.
   * @param keystoreProvider The provider for the specific type of keystore.
   * @param keystorePassword The password of the keystore.
   * @param certificateAlias The alias under which the certificate will be saved.
   * @param certificate      The certificate to save in DER format.
   * @return the keystore as a byte array.
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws CertificateException     thrown when something unexpected happens while generating the
   * @throws NoSuchProviderException  thrown when the provided provider is invalid
   * @throws KeyStoreException        thrown when something unexpected happens
   * @throws IOException
   */
  public static byte[] saveCertificateToKeystore(final byte[] keystore, final String keystoreType,
      final String keystoreProvider, final String keystorePassword, final String certificateAlias,
      final byte[] certificate)
  throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
         IOException {
    // Load the keystore.
    KeyStore ks = CryptoConvertersUtil.keystoreDeserialize(keystore, keystoreType,
        keystorePassword,
        keystoreProvider);

    // Add the certificate.
    ks.setCertificateEntry(certificateAlias,
        new JcaX509CertificateConverter().getCertificate(new X509CertificateHolder(certificate)));

    return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
  }

  /**
   * Saves a private key to the keystore. If the key identified by the alias of the key already
   * exists, it gets overwritten.
   *
   * @param keystore         The keystore to save the key into.
   * @param keystoreType     The type of the keystore.
   * @param keystoreProvider The provider for the specific type of keystore.
   * @param keystorePassword The password of the keystore.
   * @param keyAlias         The alias under which the key will be saved.
   * @param key              The private key to save in DER format.
   * @param keyPassword      The password of the key.
   * @param certificateChain The certificate chain in PEM format.
   * @return the keystore as a byte array.
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws CertificateException     thrown when something unexpected happens while generating the
   * @throws NoSuchProviderException  thrown when the provided provider is invalid
   * @throws KeyStoreException        thrown when something unexpected happens
   * @throws IOException              thrown when something unexpected happens
   */
  @SuppressWarnings("squid:S00107")
  public static byte[] savePrivateKeyToKeystore(final byte[] keystore, final String keystoreType,
      final String keystoreProvider, final String keystorePassword, final String keyAlias,
      final PrivateKey key, final String keyPassword, final String certificateChain)
  throws NoSuchAlgorithmException, CertificateException, NoSuchProviderException, KeyStoreException,
         IOException {
    // Load the keystore.
    KeyStore ks = CryptoConvertersUtil.keystoreDeserialize(keystore, keystoreType,
        keystorePassword,
        keystoreProvider);

    Collection<? extends Certificate> certificates = CertificateFactory.getInstance("X.509")
        .generateCertificates(
            new ByteArrayInputStream(certificateChain.getBytes(StandardCharsets.UTF_8)));

    // Add the key.
    ks.setKeyEntry(keyAlias, key,
        keyPassword != null ? keyPassword.toCharArray() : "".toCharArray(),
        certificates.toArray(new Certificate[0]));

    return CryptoConvertersUtil.keystoreSerialize(ks, keystorePassword);
  }

  /**
   * Verifies a signature.
   *
   * @param request the request to verify the signature
   * @return true if the signature is valid, false if it is not
   * @throws NoSuchAlgorithmException thrown when no algorithm is found for encryption
   * @throws InvalidKeySpecException  thrown when the provided key is invalid
   * @throws InvalidKeyException      thrown when the provided key is invalid
   * @throws SignatureException       thrown when something unexpected happens while generating the
   */
  public static boolean verifySignature(final SignatureVerificationRequestDTO request)
  throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
         SignatureException {
    log.debug("Received signature verification request '{}'.", request);
    if (StringUtils.isBlank(request.getSignature())) {
      throw new QDoesNotExistException("The provided signature to validate is empty.");
    }
    final Signature signature = Signature.getInstance(request.getSignatureAlgorithm());
    signature.initVerify(CryptoConvertersUtil.pemToPublicKey(request.getPublicKey(),
        request.getKeyAlgorithm()));
    signature.update(request.getPayload());

    boolean verification = signature.verify(Base64.getDecoder().decode(request.getSignature()));
    log.debug("Signature verification result is '{}'.", verification);

    return verification;
  }

  /**
   * Returns a list of all keystore types supported in the underlying JVM.
   *
   * @return the list of supported keystore types
   */
  public static List<String> getSupportedKeystoreTypes() {
    List<String> types = new ArrayList<>();
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      for (Provider.Service service : provider.getServices()) {
        if ("KeyStore".equals(service.getType())) {
          types.add(service.getAlgorithm() + "/" + provider.getName());
        }
      }
    }
    return types;
  }

  /**
   * Returns a list of all key algorithms supported in the underlying JVM.
   *
   * @return the list of supported key algorithms
   */
  public static List<String> getSupportedKeyAlgorithms() {
    List<String> algorithms = new ArrayList<>();
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      for (Provider.Service service : provider.getServices()) {
        if ("KeyFactory".equals(service.getType())) {
          algorithms.add(service.getAlgorithm() + "/" + provider.getName());
        }
      }
    }
    return algorithms;
  }

  /**
   * Returns a list of all signature algorithms supported in the underlying JVM.
   *
   * @return the list of supported signature algorithms
   */
  public static List<String> getSupportedSignatureAlgorithms() {
    List<String> algorithms = new ArrayList<>();
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      for (Provider.Service service : provider.getServices()) {
        if ("Signature".equals(service.getType())) {
          algorithms.add(service.getAlgorithm() + "/" + provider.getName());
        }
      }
    }
    return algorithms;
  }

  /**
   * Returns a list of all message digest algorithms supported in the underlying JVM.
   *
   * @return the list of supported message digest algorithms
   */
  public static List<String> getSupportedMessageDigestAlgorithms() {
    List<String> algorithms = new ArrayList<>();
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      for (Provider.Service service : provider.getServices()) {
        if ("MessageDigest".equals(service.getType())) {
          algorithms.add(service.getAlgorithm() + "/" + provider.getName());
        }
      }
    }
    return algorithms;
  }

  /**
   * Returns a list of all key agreement algorithms supported in the underlying JVM.
   *
   * @return the list of supported key agreement algorithms
   */
  public static List<String> getSupportedKeyAgreementAlgorithms() {
    List<String> algorithms = new ArrayList<>();
    Provider[] providers = Security.getProviders();
    for (Provider provider : providers) {
      for (Provider.Service service : provider.getServices()) {
        if ("KeyAgreement".equals(service.getType())) {
          algorithms.add(service.getAlgorithm() + "/" + provider.getName());
        }
      }
    }
    return algorithms;
  }

  /**
   * Creates an SSL socket factory to be used in clients requiring certificate-based
   * authentication.
   *
   * @param sslSocketFactoryDTO the details of the SSL socket factory to create
   * @return the generated SSL socket factory
   * @throws CertificateException      thrown when the certificate cannot be generated
   * @throws IOException               thrown when something unexpected happens
   * @throws KeyStoreException         thrown when the required keystore is not available
   * @throws NoSuchAlgorithmException  thrown when no algorithm is found for encryption
   * @throws UnrecoverableKeyException thrown when the provided key is invalid
   * @throws KeyManagementException    thrown when the provided key is invalid
   * @throws InvalidKeySpecException   thrown when the provided key is invalid
   */
  public static SSLSocketFactory createSSLSocketFactory(SSLSocketFactoryDTO sslSocketFactoryDTO)
  throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
         UnrecoverableKeyException, KeyManagementException, InvalidKeySpecException {

    // Certificates to trust.
    KeyStore caKs = KeyStore.getInstance(KeyStore.getDefaultType());
    caKs.load(null, null);
    for (SSLSocketFactoryCertificateDTO certificate : sslSocketFactoryDTO
        .getTrustedCertificates()) {
      caKs.setCertificateEntry(certificate.getName(),
          CryptoConvertersUtil.pemToCertificate(certificate.getPemCertificate()));
    }
    TrustManagerFactory tmf = TrustManagerFactory.getInstance(CERT_TYPE);
    tmf.init(caKs);

    // Client key and certificate.
    String randomPassword = UUID.randomUUID().toString();
    KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
    ks.load(null, null);
    ks.setCertificateEntry(sslSocketFactoryDTO.getClientCertificate().getName(),
        CryptoConvertersUtil
            .pemToCertificate(
                sslSocketFactoryDTO.getClientCertificate().getPemCertificate()));
    ks.setKeyEntry(sslSocketFactoryDTO.getClientPrivateKey().getName(),
        CryptoConvertersUtil
            .pemToPrivateKey(
                sslSocketFactoryDTO.getClientPrivateKey().getPemPrivateKey(),
                sslSocketFactoryDTO.getClientPrivateKey().getAlgorithm()),
        randomPassword.toCharArray(),
        new java.security.cert.Certificate[]{CryptoConvertersUtil
            .pemToCertificate(
            sslSocketFactoryDTO.getClientCertificate().getPemCertificate())});
    KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory
        .getDefaultAlgorithm());
    kmf.init(ks, randomPassword.toCharArray());

    // Create SSL socket factory
    SSLContext context = SSLContext
        .getInstance(sslSocketFactoryDTO.getTlsVersion());
    context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

    return context.getSocketFactory();
  }
}

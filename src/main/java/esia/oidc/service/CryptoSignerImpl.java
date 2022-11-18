package esia.oidc.service;

import com.objsys.asn1j.runtime.*;
import esia.oidc.EsiaProperties;
import org.springframework.stereotype.Service;
import ru.CryptoPro.JCP.ASN.CryptographicMessageSyntax.*;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.CertificateSerialNumber;
import ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Name;
import ru.CryptoPro.JCP.JCP;
import ru.CryptoPro.JCP.params.AlgIdSpec;
import ru.CryptoPro.JCP.params.OID;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

/**
 * based on http://www.cryptopro.ru/forum2/default.aspx?g=posts&t=16526
 */
@Service
class CryptoSignerImpl implements CryptoSigner {

    private final PrivateKey privateKey;
    private final Certificate certificate;

    public CryptoSignerImpl(EsiaProperties esiaProperties) {
        try {
            KeyStore keyStore = KeyStore.getInstance(JCP.HD_STORE_NAME);
            keyStore.load(null, null); // loads from system-wide CryptoPro container
            if (keyStore.size() == 0) throw new CryptoSignerException("KeyStore is empty");
            String keystoreAlias = esiaProperties.getKeystoreAlias();
            char[] keystorePassword = esiaProperties.getKeystorePassword().toCharArray();

            privateKey = (PrivateKey) keyStore.getKey(keystoreAlias, keystorePassword);
            certificate = keyStore.getCertificate(esiaProperties.getKeystoreAlias());

            if (privateKey == null)
                throw new CryptoSignerException("KeyStore private key by alias '" + keystoreAlias + "' doesn't exist");
            if (certificate == null)
                throw new CryptoSignerException("KeyStore public certificate by alias '" + keystoreAlias + "' doesn't exist");

        } catch (Exception e) {
            throw new CryptoSignerException("Unable to create " + CryptoSignerImpl.class.getSimpleName(), e);
        }
    }

    /**
     * PKCS#7 detached signature
     */
    @Override
    public byte[] sign(String textToSign) {
        boolean detached = true;
        try {
            return cmsSign(textToSign.getBytes(StandardCharsets.UTF_8.name()), privateKey, certificate, detached);
        } catch (Exception e) {
            throw new CryptoSignerException("Unable to sign '" + textToSign.substring(0, 50) + '\'', e);
        }
    }

    private byte[] cmsSign(byte[] data, PrivateKey key, Certificate cert, boolean detached) throws Exception {
        Signature signature = Signature.getInstance(JCP.GOST_SIGN_2012_256_NAME);

        signature.initSign(key);
        signature.update(data);
        byte[] sign = signature.sign();
        return createCMS(data, sign, cert, detached);
    }

    /**
     * CMS - Cryptographic Message Syntax
     * https://tools.ietf.org/html/rfc5652
     */
    private byte[] createCMS(byte[] buffer, byte[] sign, Certificate cert, boolean detached) throws Exception {
        ContentInfo all = new ContentInfo();
        all.contentType = new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.2")).value);
        SignedData cms = new SignedData();
        all.content = cms;
        cms.version = new CMSVersion(1L);
        cms.digestAlgorithms = new DigestAlgorithmIdentifiers(1);
        DigestAlgorithmIdentifier a = new DigestAlgorithmIdentifier((new OID("1.2.643.2.2.9")).value);
        a.parameters = new Asn1Null();
        cms.digestAlgorithms.elements[0] = a;
        if (detached) {
            cms.encapContentInfo = new EncapsulatedContentInfo(new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.1")).value), (Asn1OctetString) null);
        } else {
            cms.encapContentInfo = new EncapsulatedContentInfo(new Asn1ObjectIdentifier((new OID("1.2.840.113549.1.7.1")).value), new Asn1OctetString(buffer));
        }

        cms.certificates = new CertificateSet(1);
        ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate cryptoProCertificate = new ru.CryptoPro.JCP.ASN.PKIX1Explicit88.Certificate();
        Asn1BerDecodeBuffer decodeBuffer = new Asn1BerDecodeBuffer(cert.getEncoded());
        cryptoProCertificate.decode(decodeBuffer);
        cms.certificates.elements = new CertificateChoices[1];
        cms.certificates.elements[0] = new CertificateChoices();
        cms.certificates.elements[0].set_certificate(cryptoProCertificate);
        cms.signerInfos = new SignerInfos(1);
        cms.signerInfos.elements[0] = new SignerInfo();
        cms.signerInfos.elements[0].version = new CMSVersion(1L);
        cms.signerInfos.elements[0].sid = new SignerIdentifier();
        byte[] encodedName = ((X509Certificate) cert).getIssuerX500Principal().getEncoded();
        Asn1BerDecodeBuffer nameBuf = new Asn1BerDecodeBuffer(encodedName);
        Name name = new Name();
        name.decode(nameBuf);
        CertificateSerialNumber num = new CertificateSerialNumber(((X509Certificate) cert).getSerialNumber());
        cms.signerInfos.elements[0].sid.set_issuerAndSerialNumber(new IssuerAndSerialNumber(name, num));

        cms.signerInfos.elements[0].digestAlgorithm = new DigestAlgorithmIdentifier(AlgIdSpec.OID_DIGEST_2012_256.value);
        cms.signerInfos.elements[0].digestAlgorithm.parameters = new Asn1Null();

        cms.signerInfos.elements[0].signatureAlgorithm = new SignatureAlgorithmIdentifier(AlgIdSpec.OID_PARAMS_SIG_2012_256.value);

        cms.signerInfos.elements[0].signatureAlgorithm.parameters = new Asn1Null();
        cms.signerInfos.elements[0].signature = new SignatureValue(sign);
        Asn1BerEncodeBuffer asnBuf = new Asn1BerEncodeBuffer();
        all.encode(asnBuf, true);
        return asnBuf.getMsgCopy();
    }
}

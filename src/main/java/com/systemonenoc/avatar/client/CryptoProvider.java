package com.systemonenoc.avatar.client;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.Signature;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openssl.PEMReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;

public class CryptoProvider {
	
	private final static Logger logger = LoggerFactory.getLogger(CryptoProvider.class);
	
	static SSLContext sslContext;
	static ClientHttpRequestFactory requestFactory;
	static Signature signature;
	static Cipher asymmetricEncryptor;
	
	static {
		Security.addProvider(new BouncyCastleProvider());
		try {
			sslContext = SSLContext.getInstance("TLS");
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return new X509Certificate[0];
				}
				
				@Override
				public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
				
				@Override
				public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
				}
			} };
			
			sslContext.init(null, trustAllCerts, null);
		} catch (NoSuchAlgorithmException e) {
			logger.error("{}", e);
		} catch (KeyManagementException e) {
			logger.error("{}", e);
		}
		
		SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(sslContext,
				new AllowAllHostnameVerifier());
		CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(connectionFactory).build();
		requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
	}
	
	public static void load(String pemFile, String privateKey)
			throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException {
		// ATAX CERTIFICATE - counters
		asymmetricEncryptor = Cipher.getInstance("RSA/NONE/PKCS1Padding", "BC");
		try (InputStream stream = new FileInputStream(pemFile)) {
			X509Certificate x059 = (X509Certificate) CertificateFactory.getInstance("X.509")
					.generateCertificate(stream);
			asymmetricEncryptor.init(Cipher.ENCRYPT_MODE, x059.getPublicKey());
		} catch (Exception e) {
			logger.error("{}", e);
		}
		
		// PRIVATE KEY - signature
		try (PEMReader reader = new PEMReader(new FileReader(privateKey))) {
			KeyPair pair = (KeyPair) reader.readObject();
			PrivateKey privKey = pair.getPrivate();
			signature = Signature.getInstance("SHA1withRSA");
			signature.initSign(privKey);
		} catch (FileNotFoundException e) {
			logger.error("{}", e);
		} catch (IOException e) {
			logger.error("{}", e);
		}
		
	}
	
	public static ClientHttpRequestFactory getHttpFactory(String certificate, String password)
			throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException, CertificateException,
			IOException, UnrecoverableKeyException {
		logger.debug("getHttpFactory");
		
		KeyStore keyStore = null;
		FileInputStream fis = null;
		try {
			keyStore = KeyStore.getInstance("PKCS12");
			fis = new FileInputStream(new ClassPathResource(certificate).getFile());
			keyStore.load(fis, password.toCharArray());
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException ex) {
			}
		}
		KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
		kmf.init(keyStore, password.toCharArray());
		KeyManager[] keyManagers = kmf.getKeyManagers();
		TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[0];
			}
			
			@Override
			public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
			
			@Override
			public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
			}
		} };
		
		CryptoProvider.getSslContext().init(keyManagers, trustAllCerts, null);
		
		SSLContext.setDefault(CryptoProvider.getSslContext());
		SSLConnectionSocketFactory connectionFactory = new SSLConnectionSocketFactory(CryptoProvider.getSslContext(),
				new AllowAllHostnameVerifier());
		CloseableHttpClient httpClient = HttpClientBuilder.create().setSSLSocketFactory(connectionFactory).build();
		ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
		return requestFactory;
	}
	
	public static SSLContext getSslContext() {
		return sslContext;
	}
	
	public static ClientHttpRequestFactory getRequestFactory() {
		return requestFactory;
	}
	
	public static Signature getSignature() {
		return signature;
	}
	
	public static Cipher getAsymmetricEncryptor() {
		return asymmetricEncryptor;
	}
	
	public static void setSslContext(SSLContext sslContext) {
		CryptoProvider.sslContext = sslContext;
	}
	
	public static void setRequestFactory(ClientHttpRequestFactory requestFactory) {
		CryptoProvider.requestFactory = requestFactory;
	}
	
	public static void setSignature(Signature signature) {
		CryptoProvider.signature = signature;
	}
	
	public static void setAsymmetricEncryptor(Cipher asymmetricEncryptor) {
		CryptoProvider.asymmetricEncryptor = asymmetricEncryptor;
	}
	
}

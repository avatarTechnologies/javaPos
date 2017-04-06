package com.systemonenoc.avatar.client;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class RestClient {
	
	private final static Logger logger = LoggerFactory.getLogger(RestClient.class);
	
	public static void sendVsdc(Invoice invoice, String host, String p12, String pass)
			throws JsonGenerationException, JsonMappingException, IOException, KeyManagementException,
			UnrecoverableKeyException, NoSuchAlgorithmException, KeyStoreException, CertificateException {
		logger.debug("sendVsdc");
		
		ObjectMapper mapper = new ObjectMapper();
		Writer strWriter = new StringWriter();
		mapper.writeValue(strWriter, invoice);
		
		RestTemplate rt = new RestTemplate();
		rt.setRequestFactory(CryptoProvider.getHttpFactory(p12, pass));
		rt.setErrorHandler(new DefaultResponseErrorHandler() {
			@Override
			protected boolean hasError(HttpStatus statusCode) {
				return false;
			}
		});
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<String>(strWriter.toString(), headers);
		ResponseEntity<String> result = rt.exchange(host, HttpMethod.POST, entity, String.class);
		
		logger.info("{}", result.getBody());
	}
	
	public static void sendG5(Invoice invoice, String host)
			throws IllegalBlockSizeException, BadPaddingException, IOException, SignatureException {
		logger.debug("sendG5");
		
		invoice.setInvoiceCounter(0);
		invoice.setPartialCounter(0);
		invoice.setInternalDataHash(new String("EMPTY").getBytes());
		invoice.setEncryptedCounters(CryptoProvider.getAsymmetricEncryptor().doFinal(FiscalData.internalData(0, 0, 0, 0,
				0, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO)));
		CryptoProvider.getSignature().update(invoice.signatureData(), 0, invoice.signatureData().length);
		invoice.setSignature(CryptoProvider.getSignature().sign());
		
		RestTemplate rt = new RestTemplate();
		rt.setRequestFactory(CryptoProvider.getRequestFactory());
		HttpHeaders headers = new HttpHeaders();
		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
		
		HttpEntity<byte[]> entity = new HttpEntity<byte[]>(invoice.serialize(), headers);
		ResponseEntity<String> result = rt.exchange(host, HttpMethod.POST, entity, String.class);
		logger.info("{}", result.getBody());
	}
}

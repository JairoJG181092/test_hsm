package com.coltomex.arc.hsm.service.impl;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

//import javax.annotation.PostConstruct;

//import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import com.coltomex.arc.common.exception.HSMException;
import com.coltomex.arc.hsm.service.HSMService;



@Service
public class HSMServiceImpl implements HSMService {

	@Value("${hsm.realsec.pin}")
	private String pin;
	
	@Value("${hsm.realsec.type}")
	private String keyStoreType;
	
	@Value("${hsm.realsec.provider.name}")
	private String providerName;

	@Autowired(required=false)
	@Qualifier("providerRealsec")
	private Provider provider;

	private KeyStore keyStore;
		
	/*
	@PostConstruct
	public void posConstruct() throws HSMException, NoSuchProviderException {
		
		if ( provider != null ) {
			log.info("KeyStore info:: [{}]-[{}]-[{}] ...", provider.getName(), provider.getVersion(), provider.getInfo());
			keyStore = this.refresh();
		} else {
			log.warn("No provider de HSM disponible");
		}
	}
	*/
	
	@Override
	public KeyStore refresh(String alias) throws HSMException, NoSuchProviderException {

		try {
			keyStore = KeyStore.getInstance(keyStoreType, providerName);
			keyStore.load(null, pin.toCharArray());
			
			//Valida alias
			if ( !keyStore.containsAlias(alias) ) {
				System.out.println("No se encuentra el alias [" + alias + "] en el servidor HSM");
				keyStore = null;
			}
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException
				| IOException e) {

			String msg = "Fallo la conexión al servidor HSM Realsec";
			System.out.println(msg);

			if (e.getMessage().contains("CKR_PIN_INCORRECT")) {
				System.out.println("El PIN es incorrecto. Verificar con administrador de servidor HSM.");
				msg += " - El PIN es incorrecto. Verificar con administrador de servidor HSM.";
			} else if (e.getMessage().contains("CKR_FUNCTION_FAILED")) {
				System.out.println(
						"No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.");
				msg += " - No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.";
			}
			//throw new HSMException(msg, e);

		}

		return keyStore;
	}
	
	
	//Agregue estos 2 metodos, EAT
	
	
	@Override
	public KeyStore refresh_conexion() throws HSMException, NoSuchProviderException {

		try {
			keyStore = KeyStore.getInstance(keyStoreType, providerName);
		} catch (Exception e) {

			String msg = "Fallo la conexión al servidor HSM Realsec";
			System.out.println(msg);

			if (e.getMessage().contains("CKR_PIN_INCORRECT")) {
				System.out.println("El PIN es incorrecto. Verificar con administrador de servidor HSM.");
				msg += " - El PIN es incorrecto. Verificar con administrador de servidor HSM.";
			} else if (e.getMessage().contains("CKR_FUNCTION_FAILED")) {
				System.out.println(
						"No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.");
				msg += " - No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.";
			}
			throw new HSMException(msg, e);

		}

		return keyStore;
	}
	
	
	@Override
	public KeyStore refresh_load(KeyStore keystore) throws HSMException, NoSuchProviderException {

		try {
			keyStore.load(null, pin.toCharArray());
		
			
			if ( keyStore != null ) {
								
			} else {
				System.out.println("No se pudo cargar el keystore..");
			}
			

		} catch (NoSuchAlgorithmException | CertificateException
				| IOException e) {

			String msg = "Fallo la conexión al servidor HSM Realsec";
			System.out.println(msg);

			if (e.getMessage().contains("CKR_PIN_INCORRECT")) {
				System.out.println("El PIN es incorrecto. Verificar con administrador de servidor HSM.");
				msg += " - El PIN es incorrecto. Verificar con administrador de servidor HSM.";
			} else if (e.getMessage().contains("CKR_FUNCTION_FAILED")) {
				System.out.println(
						"No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.");
				msg += " - No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.";
			}
			throw new HSMException(msg, e);

		}

		return keyStore;
	}
	
	//----------------------------------------------------------------------------------------

	@Override
	public PrivateKey getPrivateKey(String alias) throws HSMException {	
		try {

			if ( keyStore != null ) {
				
			} else {
				System.out.println("No se pudo cargar el keystore...");
			}
			
			PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, null);
			if (privateKey != null) {
				return privateKey;
			}

			return null;
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			
			return null;
		}
	}

	@Override
	public PublicKey getPublicKey(String alias) throws HSMException {
				
		try {

			if ( keyStore != null ) {
				
			} else {
				System.out.println("No se pudo cargar el keystore....");
			}
			
			X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);
			if (cert != null) {
				return (PublicKey)cert.getPublicKey();
			}
			
			return null;

		} catch (KeyStoreException e) {
			return null;
		} catch (Exception e) {
			//e.printStackTrace();
		}
		return null;
	}

	@Override
	public Provider getProvider() {
		return provider;
	}

	@Override
	public KeyStore getKeyStore() {
		return keyStore;
	}

}

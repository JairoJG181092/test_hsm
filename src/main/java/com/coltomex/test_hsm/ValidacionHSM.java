package com.coltomex.test_hsm;

import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.coltomex.arc.hsm.service.HSMService;
import com.coltomex.arc.jwe.dto.KeyPairDto;
import com.nimbusds.jose.EncryptionMethod;
//import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEDecrypter;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.RSADecrypter;
import com.nimbusds.jose.crypto.RSAEncrypter;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;


@Service
public class ValidacionHSM {
	
	@Autowired
	private HSMService hsmService;
	
	@Autowired
	@Qualifier("providerRealsec")
	private Provider provider;
			
	@Value("${jwe.alg}")
	private String jweAlg;

	@Value("${jwe.enc}")
	private String enc;

	@Value("${jwe.cty}")
	private String jweCty;
	
	@Value("${jws.alg}")
	private String jwsAlg;

	@Value("${jws.cty}")
	private String jwsCty;
	
	@Value("${jws.iss}")
	private String iss;

	@Value("${jws.sub}")
	private String sub;
	
	private String STR_PRUEBA = "ESTO_ES_UNA_CADENA_DE_PRUEBA_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_ABCDEFGHIJKLMNOPQRSTUVW";
	
	
	/**
	* Metodo para realizar pruebas de validacion al HSM
	* 
	* Alias de prueba: "arcs-coltomex-uat-t1-v4" -> Ok
	* 				   "ColtomexPSS-Firm"        -> Falla get private key
	* @return
	*/
	public String validacion(String alias) {
		
		System.out.println("============================================================================");
		System.out.println("");
		System.out.println("        V A L I D A C I O N    H S M - A R C S   S Y S T E M");
		System.out.println("");
		System.out.println("============================================================================");
		System.out.println("");
		
		//---- Prueba de conexion ----
		//Valida conexion (provider != null)
		if (provider == null) {
			System.out.println("[ X  ] - Fallo en conexion al HSM");
			return "FIN DE TEST. NO SE PUDO CONECTAR AL HSM";
		}
		else {
			System.out.println("[ OK ] - Conexion al HSM exitosa");
		}
		
		//---- Valida obtencion get keystore ----
		KeyStore keystore = getKeyStore(alias);
		
		if (keystore == null) {
			System.out.println("[ X  ] - Fallo al obtener Keystore del HSM");
			return "FIN DE TEST. NO SE PUDO OBTENER KEYSTORE DEL HSM";
		} else {
			System.out.println("[ OK ] - Obtencion de Keystore exitosa");
		}
		
		//---- Valida obtencion de llaves publica y privada ----		
		KeyPairDto keyPairDto = new KeyPairDto();
						
		try {
			keyPairDto.setProvider(provider);
			
			//-- Obtiene Public Key --
			PublicKey publickey = hsmService.getPublicKey(alias);
			
			if (publickey == null) {
				System.out.println("[ X  ] - Fallo al obtener la llave publica del HSM");
			} else {
				System.out.println("[ OK ] - Obtencion de llave publica exitosa");
				keyPairDto.setPublicKey(publickey);
			}
			
			
			//-- Obtiene Private Key --
			PrivateKey privatekey = hsmService.getPrivateKey(alias);
			
			if (privatekey == null) {
				System.out.println("[ X  ] - Fallo al obtener la llave privada del HSM");
			} else {
				System.out.println("[ OK ] - Obtencion de llave privada exitosa");
				keyPairDto.setPrivateKey(privatekey);
			}
			
		} catch (Exception e) {
			System.out.println("[ X  ] - Fallo al obtener la llave privada del HSM");
		}
		
		 		
		//---- Obtiene JWS (Firma) ----
		SignedJWT objetoJWTFirmado = null;
		try {						
			if(keyPairDto.getPrivateKey() == null) {
				System.out.println("[ -  ] - No es posible realizar el firmado (Se necesita la llave privada)");
			}
			else {
				objetoJWTFirmado = firmaJWS(keyPairDto);
				//Descomentar para pruebas
				//System.out.println(objetoJWTFirmado.getPayload());
				//System.out.println(objetoJWTFirmado.getSignature());
				if (objetoJWTFirmado == null) {
					System.out.println("[ X  ] - Fallo al firmar");
				}
				else {
					System.out.println("[ OK ] - Firmado realizado exitosamente");
				}
			}
			
		} catch (Exception e) {
			System.out.println("[ X  ] - Fallo al firmar");
		}
		
		//---- Obtiene JWE (Cifra) ----
		String jwe = null;
		try {
			if(keyPairDto.getPublicKey() == null) {
				System.out.println("[ -  ] - No es posible realizar el cifrado (Se necesita la llave publica)");
			}	
			else {
				jwe = creaJWE(keyPairDto, objetoJWTFirmado);
				//Descomentar para pruebas
				//System.out.println(jwe);
				if (jwe == null) {
					System.out.println("[ X  ] - Fallo al cifrar");
				}
				else {
					System.out.println("[ OK ] - Cifrado realizado exitosamente");
				}
			}		
		} catch (Exception e) {
			System.out.println("[ X  ] - Fallo al cifrar");
		}
		
		//---- Descifra JWE ---
		JWEObject jweDescifrado = null;
		try {
			if(keyPairDto.getPrivateKey() == null) {
				System.out.println("[ -  ] - No es posible realizar el descifrado (Se necesita la llave privada)");
			}	
			else {
				jweDescifrado = descifraJWE(keyPairDto, jwe);
				
				//Descomentar para pruebas
				//System.out.println(jweDescifrado.getPayload());
				//System.out.println(jweDescifrado.getState());
				
				if (jweDescifrado == null) {
					System.out.println("[ X  ] - Fallo al descifrar");
				}
				else {
					System.out.println("[ OK ] - Descifrado realizado exitosamente");
				}
			}
			
		} catch (Exception e) {
			System.out.println("[ X  ] - Fallo al descifrar");
		}
		
		try {
			
			if(keyPairDto.getPrivateKey() == null || keyPairDto.getPublicKey() == null) {
				System.out.println("[ -  ] - No es posible validar la firma");
			}
			else {
				boolean validacionFirma = validaFirma(keyPairDto, jweDescifrado);
				//Descomentar para pruebas
				//System.out.println(validacionFirma);
				if (validacionFirma == false) {
					System.out.println("[ X  ] - Fallo al validar firma");
				}
				else {
					System.out.println("[ OK ] - Validacion de firma exitosa");
				}
			}
			
		} catch (Exception e) {
			System.out.println("[ X  ] - Fallo al validar firma");
		}
		
		//--- Test sobre PDF ---
		//encripta_pdf();
		//desencripta_pdf();
		System.out.println("");
		System.out.println("========================================================================================");
		System.out.println("");
		return "[-_-]    F I N     D E     V A L I D A C I O N     H S M - A R C S    S Y S T E M    [-_-]";
	}
	
		
	/**
	 * Obtiene keystore del HSM 
	 * @return Keystore keystore
	 */
	private KeyStore getKeyStore(String alias) {
		
		KeyStore keystore = null;
		
		try {
			keystore = hsmService.refresh(alias);
						
		} catch (Exception e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return keystore;
	}
	
	/**
	 * 
	 * Obtiene Firma JWS (JSON Web Signature)
	 *
	 * args: Objeto keyPairDto
	 * Tomado del metodo SignedJWT create, de la clase JWSServiceImpl de la clase original proyecto ARC
	 * 
	 */
	private SignedJWT firmaJWS(KeyPairDto keyPairDto) throws Exception {
				
		SignedJWT signedJWT = null;
		
		Map<String, Object> claim = new HashMap<String, Object>();
		claim.put("clientNumber1", "123456789");
		claim.put("datos", STR_PRUEBA);
				
		//sub = 'arc'
		//iss = 'arc'
		try {
		JWTClaimsSet.Builder jwtBuilder = new JWTClaimsSet.Builder().subject(sub).issueTime(new Date()).issuer(iss);
				
		jwtBuilder.claim("data", claim);
		JWTClaimsSet jwtClaimsSet = jwtBuilder.build();
		
		// CREATE JWT
		//jws.alg=RS256
		//jws.cty=JWT
		//jws.iss=arc
		//jws.sub=arc
		signedJWT = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.parse(jwsAlg)).contentType(jwsCty)
				.keyID(UUID.randomUUID().toString()).build(), jwtClaimsSet);
				
		RSASSASigner signer = new RSASSASigner(keyPairDto.getPrivateKey());
		
		if (keyPairDto.getProvider() != null) {
			signer.getJCAContext().setProvider(keyPairDto.getProvider());
		}
		
		// SIGN JWT
		signedJWT.sign(signer);						
		} catch (Exception e) {
			signedJWT = null;
		}
		
		return signedJWT;
	}
	
	
	/**
	* 
	* Obtiene JWE
	* 
	* Obtenido de JWEServiceImpl, Metodo create_g
	* 
	*/	
	private String creaJWE(KeyPairDto keyPairDto, SignedJWT objetoJWTFirmado) throws Exception {
		
		Payload payload = null;
		if (objetoJWTFirmado == null) {
			payload = new Payload(STR_PRUEBA); 
		}
		else {
			payload = new Payload(objetoJWTFirmado);
		}
		String jwe = "";
		
		//jwe.alg=RSA1_5
		//Content encryption class |JWE enc identifier |Key bitlength
		//------------------------------------------------------------
		//AES/GCM                  |A256GCM            |256      
		//jwe.enc=A256GCM
		//jwe.cty=JWT
				
		try {
			// CREATE HEADER
			// required to indicate nested JWT
			JWEHeader jweHeader = new JWEHeader.Builder(JWEAlgorithm.parse(jweAlg), EncryptionMethod.parse(enc))
					.contentType(jweCty).build();
						
			JWEEncrypter encrypter = new RSAEncrypter((RSAPublicKey) keyPairDto.getPublicKey());		
			if (keyPairDto.getProvider() != null) {
				encrypter.getJCAContext().setKeyEncryptionProvider(keyPairDto.getProvider());
			}
			
			// CREATE JWE		
			JWEObject jweObject;
					
			//El payload que recibe es el JSON firmado (SignedJWT signedJWT) - EAT
			jweObject = new JWEObject(jweHeader, payload);
			jweObject.encrypt(encrypter);
			jwe = jweObject.serialize();
		} catch (Exception e) {
			jwe = null;
		}
				
		return jwe;
	}
	
	/**
	* 
	* Descifra JWE
	* Tomado de JWSServiceImpl
	* metodo getJWS
	*/
	private JWEObject descifraJWE(KeyPairDto keyPairDto, String jwe)throws Exception {				
		// Parse JWE
		JWEObject jweObject;
		JWEDecrypter decrypter = null;
		
		try {
			jweObject = JWEObject.parse(jwe);
			// Init decrypter
			decrypter = new RSADecrypter(keyPairDto.getPrivateKey());
			if (keyPairDto.getProvider() != null) {
				decrypter.getJCAContext().setKeyEncryptionProvider(keyPairDto.getProvider());
			}
			
			// DECRYPT WITH PRIVATE KEY
			jweObject.decrypt(decrypter);
		} catch (Exception e) {
			return null;
		}
	
		
		return jweObject;
	}
		
		
	/**
	* 
	* Valida Firma
	* 
	*/
	private boolean validaFirma(KeyPairDto keyPairDto, JWEObject jweObject) throws Exception {				
		RSAKey jwk = null;
		boolean isValid = false;
		try {
			jwk = new RSAKey.Builder( (RSAPublicKey) keyPairDto.getPublicKey() )
			        .privateKey(keyPairDto.getPrivateKey())
			        .keyUse(KeyUse.SIGNATURE)
			        .keyID(UUID.randomUUID().toString())
			        .build();
				
			RSAKey senderPublicJWK = jwk.toPublicJWK();
			RSASSAVerifier verifier;
			verifier = new RSASSAVerifier(senderPublicJWK);
			if (keyPairDto.getProvider() != null) {
				verifier.getJCAContext().setProvider(keyPairDto.getProvider());
			}
			
			// CHECK SIGNATURE
			SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
			
			//Descomentar para pruebas
			/*
			System.out.println("-- payload --");
			System.out.println(signedJWT.getPayload());
			System.out.println("-- signature --");
			System.out.println(signedJWT.getSignature());
			*/
			isValid = signedJWT.verify(verifier);
		} catch (Exception e) {
				//System.out.println(e);
				return isValid;
		}
			
		return isValid;
	}
	
	
	
	
	
}




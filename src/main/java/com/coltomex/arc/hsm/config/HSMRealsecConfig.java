package com.coltomex.arc.hsm.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.cert.CertificateException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Configuration
public class HSMRealsecConfig {
		
	@Value("${hsm.realsec.pin}")
	private String pin;
	
	@Value("${hsm.realsec.config.path}")
	private String configFile;
	
	@Value("${hsm.realsec.config.pathserver}")
	private String configFileServer;
	
	@Value("${hsm.realsec.type}")
	private String keyStoreType;
	
	@Value("${hsm.realsec.provider.name}")
	private String providerName;

    @Bean("providerRealsec")
    Provider loadProvider() throws Exception {
    	Provider provider = null;
    	    	
    	//--- Valida ping a servidor ---
		// boolean respuesta_ping = ping_servidor();
		
		// if (respuesta_ping == true) {
		// 	System.out.println("[ OK ] - Prueba de ping exitosa");
		// 	System.out.println("");
		// }	
		// else {
		// 	Syst                                em.out.println("[ X  ] - Fallo en prueba de ping");
		// 	return provider;
		// }
    	  		
		
		try {
	    	//provider = new sun.security.pkcs11.SunPKCS11(configFile);
			provider = new es.provider.realsec.RealsecPKCS11Provider();
	    } catch(Exception e) {
	    	e.printStackTrace();
	    }
		
		System.out.println("==========================================================");
		System.out.println("HSM REALSEC PROVIDER");
		System.out.println("----------------------------------------------------------");
		System.out.println("Name    : " + provider.getName());
		System.out.println("Info    : " + provider.getInfo());
		System.out.println("Versión : " + provider.getVersion());
		System.out.println("Config  : " + configFileServer);
		System.out.println("==========================================================");
		
		Security.addProvider(provider);
		
		if ( !validateProvider() ) {
			provider = null;
		}
		
		return provider;
	}
	
    /**
     * Valida provider
     * @return boolean true si provider es valido
     * @throws Exception
     */
	private boolean validateProvider() throws Exception {
		boolean status = true;
		KeyStore ks = null;

		try {
			ks = KeyStore.getInstance(keyStoreType, providerName);
			//System.out.println("PIN: " + pin);
			ks.load(null, pin.toCharArray());

			System.out.println("Key Store cargado");
			
		} catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException e) {

			System.out.println("Fallo la conexión al servidor HSM Realsec");
			
			if ( e.getMessage().contains("CKR_PIN_INCORRECT") ) {
				System.out.println("El PIN es incorrecto. Verificar con administrador de servidor HSM.");
			} else if ( e.getMessage().contains("CKR_FUNCTION_FAILED") ) {
				System.out.println("No se pudo alcanzar la IP del servidor HSM Realsec. Verificar con administrador de servidor HSM.");
			}
			
			status = false;
			
		} 
			
		return status;
	}
	
	
	/****************************************************************************
	 * 
	 * Realiza ping a un servidor
	 * @return boolean, true si el ping fue exitoso, de lo contrario false
	 * 
	 *****************************************************************************/
	private boolean ping_servidor() {

		boolean respuesta = false;
        try {
        	
        	ProcessBuilder processBuilder = null;
        	
        	//Detecta sistema operativo
        	String os = System.getProperty("os.name").toLowerCase();
            String osVersion = System.getProperty("os.version");
            String ipHSM = "";
            
            if (os.contains("win")) {
                System.out.println("Sistema operativo: Windows " + osVersion);
                
                //Lee ip de archivo de configuracion
                ipHSM = getIPConfigFile(configFile);
                processBuilder = new ProcessBuilder("ping", "-n", "4", ipHSM); // Para Windows
            }
            else {
            	ipHSM = getIPConfigFile(configFileServer);
            	processBuilder = new ProcessBuilder("ping", "-c", "4", ipHSM); // Para Linux/Mac
            }
            
            System.out.println("======================================================================");
            System.out.println("Iniciando ping a IP del HSM: " + ipHSM + " ...");
            System.out.println("----------------------------------------------------------------------");
            
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            
            int exitCode = process.waitFor();
            System.out.println("Proceso ping finalizado con codigo de salida: " + exitCode);           
            
            if (exitCode == 0)
            	respuesta = true;
            
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
       
        return respuesta;       
	}
	
	/**
	 * Obtiene la ip del archivo de configuracion del HSM
	 * @param rutaArchivoConfig ruta del archivo config del HSM
	 * @return String ipConfig con la ip del HSM seteada en el archivo config del HSM
	 */
	private String getIPConfigFile(String rutaArchivoConfig) {
		String ipConfig = "";
        try {
            List<String> lineas = Files.readAllLines(Paths.get(rutaArchivoConfig));
            
            System.out.println("-----------------------------------------------------------");
            System.out.println(" ARCHIVO CONFIGURACION HSM ");
            System.out.println("-----------------------------------------------------------");
            for (String linea : lineas) {
            	System.out.println(linea);
            	if (linea.startsWith("IPADDRESS")) {
            		String[] segmentosIP = linea.split("=");
            		ipConfig = segmentosIP[1];
            	}
                
            }
            System.out.println("-----------------------------------------------------------");
            System.out.println("");
            
        } catch (IOException e) {
            //e.printStackTrace();
        }
		return ipConfig;
	}

}

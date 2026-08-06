package com.coltomex.arc.hsm.service;

import java.security.KeyStore;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;

import com.coltomex.arc.common.exception.HSMException;


public interface HSMService {
	
	public KeyStore refresh(String alias) throws HSMException, NoSuchProviderException ;
	
	//Agregue estos 2, EAT
	public KeyStore refresh_conexion() throws HSMException, NoSuchProviderException ;
	public KeyStore refresh_load(KeyStore keystore) throws HSMException, NoSuchProviderException ;
	
	
	public PrivateKey getPrivateKey(String alias) throws HSMException;
	
	public PublicKey getPublicKey(String alias) throws HSMException;
	
	public Provider getProvider();
	
	public KeyStore getKeyStore();

}

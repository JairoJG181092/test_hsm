package com.coltomex.test_hsm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;

import org.apache.commons.codec.digest.DigestUtils;
//import org.springframework.stereotype.Service;

//@Service
public class CryptoAES256GCM {


	private final String ALGORITHM_KEY_NAME = "AES";
	private final String ALGORITHM_KEY_MODE = "AES/GCM/NoPadding";
	private final int ALGORITHM_KEY_SIZE = 256;
	private final int ALGORITHM_IV_SIZE = 16;
	private final int MODE_GCM_TLEN = 128;
	private final String ALGORITHM_PRNG = "SHA1PRNG";
	
	public Provider provider;

	public CryptoAES256GCM() {
		this.provider = null;
	}

	public CryptoAES256GCM(Provider provider) {
		this.provider = provider;
	}
	
	public Provider getProvider() {
		return this.provider;
	}

	public void setProvider(Provider provider) {
		this.provider = provider;
	}

	public SecretKey generateKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGenerator = null;
		keyGenerator = KeyGenerator.getInstance(this.ALGORITHM_KEY_NAME);
		keyGenerator.init(this.ALGORITHM_KEY_SIZE);
		SecretKey key = keyGenerator.generateKey();
		return key;
	}

	public IvParameterSpec generateIv() throws NoSuchAlgorithmException {
		byte[] iv = new byte[this.ALGORITHM_IV_SIZE];
		SecureRandom secureRandom = null;
		System.out.println("Provider: " + this.provider + " - " + this.provider.getInfo());
		try {
			if (this.provider != null) {
				System.out.println("-- 1 --");
				secureRandom = SecureRandom.getInstance(this.ALGORITHM_PRNG, this.provider);
				System.out.println("-- Provider: " + this.provider + " --");
			} else {
				System.out.println("-- 2 --");
				secureRandom = new SecureRandom();
				System.out.println("SecureRandom [x-X]");
			}
		} catch (NoSuchAlgorithmException e) {
			secureRandom = new SecureRandom();
			
			System.out.println(e.getMessage());
		}
		secureRandom.nextBytes(iv);
		
		//System.out.println(this.provider.getInfo());
		return new IvParameterSpec(iv);
	}

	/**
	 * @param String filename - nombre de archivo a encriptar
	 * @param SecretKey key - secret key
	 * @param IvParameterSpec iv
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchPaddingException
	 * @throws InvalidKeyException
	 * @throws InvalidAlgorithmParameterException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 * @throws IOException
	 * @throws NoSuchProviderException
	 */
	public File encryptFile(String filename, SecretKey key, IvParameterSpec iv)
			throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, NoSuchProviderException {
		Cipher cipher = null;
		cipher = Cipher.getInstance(this.ALGORITHM_KEY_MODE);
		cipher.init(Cipher.ENCRYPT_MODE,  key, new GCMParameterSpec(this.MODE_GCM_TLEN, iv.getIV()));

		File fileIn = new File(filename);
		byte[] bytesFile = new byte[(int) fileIn.length()];
		FileInputStream fileInputStream = new FileInputStream(fileIn);
		fileInputStream.read(bytesFile, 0, (int) fileIn.length());
		fileInputStream.close();

		byte[] encryptedBytes = cipher.doFinal(bytesFile);

		File fileOut = new File(filename + "." + "CRYPT");

		FileOutputStream fileOutput = new FileOutputStream(fileOut);
		fileOutput.write(encryptedBytes);
		fileOutput.flush();
		fileOutput.close();

		return fileOut;
	}

	/**
	 * Desencripta archivo
	 * @param filename
	 * @param key
	 * @param iv
	 * @return
	 * @throws NoSuchPaddingException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeyException
	 * @throws BadPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws IOException
	 */
	public File decryptFile(String filename, SecretKey key, IvParameterSpec iv)
			throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidAlgorithmParameterException,
			InvalidKeyException, BadPaddingException, IllegalBlockSizeException, IOException {
		Cipher cipher = null;
		/*if ( this.provider != null ) {
			cipher = Cipher.getInstance(this.ALGORITHM_KEY_MODE, this.provider);
		} else {
			cipher = Cipher.getInstance(this.ALGORITHM_KEY_MODE);
		}*/
		cipher = Cipher.getInstance(this.ALGORITHM_KEY_MODE);
		cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(this.MODE_GCM_TLEN, iv.getIV()));

		File fileIn = new File(filename);
		byte[] bytesFile = new byte[(int) fileIn.length()];
		FileInputStream fileInputStream = new FileInputStream(fileIn);
		fileInputStream.read(bytesFile, 0, (int) fileIn.length());
		fileInputStream.close();

		byte[] decryptedBytes = cipher.doFinal(bytesFile);

		File fileOut = new File(filename + ".DECRYPT.PDF");
		FileOutputStream fileOutput = new FileOutputStream(fileOut);
		fileOutput.write(decryptedBytes);
		fileOutput.flush();
		fileOutput.close();

		return fileOut;
	}
	
	
	
	public static String Sha1(String input) throws NoSuchAlgorithmException {
		return DigestUtils.sha1Hex(input);
	}

}

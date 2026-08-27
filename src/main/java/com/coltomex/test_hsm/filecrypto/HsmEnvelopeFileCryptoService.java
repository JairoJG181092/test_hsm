package com.coltomex.test_hsm.filecrypto;

import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;

@Service
public class HsmEnvelopeFileCryptoService {

    private static final byte[] MAGIC = new byte[]{'H', 'S', 'M', 'F'};
    private static final byte VERSION = 1;

    private final HsmKeyAccess keys;
    private final FileCryptoProperties properties;
    private final SecureRandom secureRandom = new SecureRandom();

    public HsmEnvelopeFileCryptoService(HsmKeyAccess keys, FileCryptoProperties properties) {
        this.keys = keys;
        this.properties = properties;
    }

    public FileCryptoResult encrypt(Path source, Path target, String alias) throws Exception {
        validateSource(source);
        PublicKey publicKey = keys.publicKey(alias);

        KeyGenerator generator = KeyGenerator.getInstance("AES");
        generator.init(properties.aesKeySize());
        SecretKey dataKey = generator.generateKey();

        byte[] iv = new byte[properties.gcmIvBytes()];
        secureRandom.nextBytes(iv);

        Cipher keyCipher = Cipher.getInstance(properties.rsaTransformation());
        keyCipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] wrappedKey = keyCipher.doFinal(dataKey.getEncoded());

        Cipher dataCipher = Cipher.getInstance("AES/GCM/NoPadding");
        dataCipher.init(Cipher.ENCRYPT_MODE, dataKey,
                new GCMParameterSpec(properties.gcmTagBits(), iv));

        Files.createDirectories(target.toAbsolutePath().getParent());
        try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
                Files.newOutputStream(target, StandardOpenOption.CREATE_NEW)));
             var rawIn = new BufferedInputStream(Files.newInputStream(source));
             CipherOutputStream encryptedOut = new CipherOutputStream(out, dataCipher)) {

            out.write(MAGIC);
            out.writeByte(VERSION);
            out.writeShort(iv.length);
            out.writeInt(wrappedKey.length);
            out.write(iv);
            out.write(wrappedKey);
            copy(rawIn, encryptedOut);
        }

        return FileCryptoResult.success(source, target, "ENCRYPT", Files.size(source));
    }

    public FileCryptoResult decrypt(Path source, Path target, String alias) throws Exception {
        validateSource(source);
        PrivateKey privateKey = keys.privateKey(alias);

        try (DataInputStream in = new DataInputStream(new BufferedInputStream(Files.newInputStream(source)))) {
            byte[] magic = in.readNBytes(MAGIC.length);
            if (!java.util.Arrays.equals(MAGIC, magic)) {
                throw new IOException("Formato de archivo HSMF no válido");
            }
            byte version = in.readByte();
            if (version != VERSION) {
                throw new IOException("Versión de formato no soportada: " + version);
            }
            int ivLength = Short.toUnsignedInt(in.readShort());
            int wrappedKeyLength = in.readInt();
            if (ivLength < 12 || ivLength > 32 || wrappedKeyLength < 1 || wrappedKeyLength > 65536) {
                throw new IOException("Encabezado de archivo inválido");
            }
            byte[] iv = in.readNBytes(ivLength);
            byte[] wrappedKey = in.readNBytes(wrappedKeyLength);
            if (iv.length != ivLength || wrappedKey.length != wrappedKeyLength) {
                throw new EOFException("Archivo incompleto");
            }

            Cipher keyCipher = Cipher.getInstance(properties.rsaTransformation());
            keyCipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] rawKey = keyCipher.doFinal(wrappedKey);
            SecretKey dataKey = new javax.crypto.spec.SecretKeySpec(rawKey, "AES");

            Cipher dataCipher = Cipher.getInstance("AES/GCM/NoPadding");
            dataCipher.init(Cipher.DECRYPT_MODE, dataKey,
                    new GCMParameterSpec(properties.gcmTagBits(), iv));

            Files.createDirectories(target.toAbsolutePath().getParent());
            try (CipherInputStream decryptedIn = new CipherInputStream(in, dataCipher);
                 var out = new BufferedOutputStream(Files.newOutputStream(target, StandardOpenOption.CREATE_NEW))) {
                copy(decryptedIn, out);
            }
        }

        return FileCryptoResult.success(source, target, "DECRYPT", Files.size(source));
    }

    private void validateSource(Path source) throws IOException {
        if (!Files.isRegularFile(source)) {
            throw new IOException("La ruta no corresponde a un archivo regular: " + source);
        }
        if (properties.maxFileSize() > 0 && Files.size(source) > properties.maxFileSize()) {
            throw new IOException("El archivo excede el tamaño máximo permitido");
        }
    }

    private void copy(java.io.InputStream input, java.io.OutputStream output) throws IOException {
        byte[] buffer = new byte[properties.bufferBytes()];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
    }
}

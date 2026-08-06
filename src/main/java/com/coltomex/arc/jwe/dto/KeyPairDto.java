package com.coltomex.arc.jwe.dto;

import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;

import lombok.Data;

/**
 * Clase para definir objeto con lllaves y provider
 */

public class KeyPairDto {

    private PrivateKey privateKey;
    private PublicKey publicKey;
    private Provider provider;

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(PrivateKey privateKey) {
        this.privateKey = privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public Provider getProvider() {
        return provider;
    }

    public void setProvider(Provider provider) {
        this.provider = provider;
    }
}
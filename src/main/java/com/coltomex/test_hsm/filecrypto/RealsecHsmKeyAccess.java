package com.coltomex.test_hsm.filecrypto;

import com.coltomex.arc.hsm.service.HSMService;
import org.springframework.stereotype.Component;

import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class RealsecHsmKeyAccess implements HsmKeyAccess {

    private final HSMService hsmService;

    public RealsecHsmKeyAccess(HSMService hsmService) {
        this.hsmService = hsmService;
    }

    @Override
    public PublicKey publicKey(String alias) throws Exception {
        hsmService.refresh(alias);
        var key = hsmService.getPublicKey(alias);
        if (key == null) {
            throw new IllegalStateException("No se encontró la llave pública para el alias: " + alias);
        }
        return key;
    }

    @Override
    public PrivateKey privateKey(String alias) throws Exception {
        hsmService.refresh(alias);
        var key = hsmService.getPrivateKey(alias);
        if (key == null) {
            throw new IllegalStateException("No se encontró la llave privada para el alias: " + alias);
        }
        return key;
    }
}

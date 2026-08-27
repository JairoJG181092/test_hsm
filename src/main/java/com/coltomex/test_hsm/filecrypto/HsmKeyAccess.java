package com.coltomex.test_hsm.filecrypto;

import java.security.PrivateKey;
import java.security.PublicKey;

public interface HsmKeyAccess {
    PublicKey publicKey(String alias) throws Exception;
    PrivateKey privateKey(String alias) throws Exception;
}

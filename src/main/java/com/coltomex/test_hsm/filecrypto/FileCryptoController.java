package com.coltomex.test_hsm.filecrypto;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
public class FileCryptoController {

    private final HsmEnvelopeFileCryptoService cryptoService;
    private final BulkFileCryptoService bulkService;
    private final FileCryptoProperties properties;

    public FileCryptoController(HsmEnvelopeFileCryptoService cryptoService,
                                BulkFileCryptoService bulkService,
                                FileCryptoProperties properties) {
        this.cryptoService = cryptoService;
        this.bulkService = bulkService;
        this.properties = properties;
    }

    @PostMapping("/encrypt")
    public FileCryptoResult encrypt(@RequestParam String source,
                                    @RequestParam String target,
                                    @RequestParam String alias) throws Exception {
        return cryptoService.encrypt(Path.of(source), Path.of(target), alias);
    }

    @PostMapping("/decrypt")
    public FileCryptoResult decrypt(@RequestParam String source,
                                    @RequestParam String target,
                                    @RequestParam String alias) throws Exception {
        return cryptoService.decrypt(Path.of(source), Path.of(target), alias);
    }

    @PostMapping("/encrypt-directory")
    public List<FileCryptoResult> encryptDirectory(@RequestParam String inputDir,
                                                   @RequestParam String outputDir,
                                                   @RequestParam String alias) throws Exception {
        return bulkService.encryptDirectory(Path.of(inputDir), Path.of(outputDir), alias);
    }

    @PostMapping("/decrypt-directory")
    public List<FileCryptoResult> decryptDirectory(@RequestParam String inputDir,
                                                   @RequestParam String outputDir,
                                                   @RequestParam String alias) throws Exception {
        return bulkService.decryptDirectory(Path.of(inputDir), Path.of(outputDir), alias);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<FileCryptoResult> handle(Exception exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(FileCryptoResult.failure(null, "REQUEST", exception));
    }
}

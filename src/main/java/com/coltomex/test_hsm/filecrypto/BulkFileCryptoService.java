package com.coltomex.test_hsm.filecrypto;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

@Service
public class BulkFileCryptoService {

    private final HsmEnvelopeFileCryptoService cryptoService;
    private final FileCryptoProperties properties;

    public BulkFileCryptoService(HsmEnvelopeFileCryptoService cryptoService, FileCryptoProperties properties) {
        this.cryptoService = cryptoService;
        this.properties = properties;
    }

    public List<FileCryptoResult> encryptDirectory(Path inputDir, Path outputDir, String alias) throws Exception {
        return process(inputDir, outputDir, alias, true);
    }

    public List<FileCryptoResult> decryptDirectory(Path inputDir, Path outputDir, String alias) throws Exception {
        return process(inputDir, outputDir, alias, false);
    }

    private List<FileCryptoResult> process(Path inputDir, Path outputDir, String alias, boolean encrypt) throws Exception {
        if (!Files.isDirectory(inputDir)) {
            throw new IllegalArgumentException("La entrada debe ser un directorio: " + inputDir);
        }
        Files.createDirectories(outputDir);

        List<Path> files;
        try (var stream = Files.walk(inputDir)) {
            files = stream.filter(Files::isRegularFile)
                    .filter(path -> encrypt || path.getFileName().toString().endsWith(properties.outputExtension()))
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }

        try (ExecutorService executor = Executors.newFixedThreadPool(Math.max(1, properties.parallelism()))) {
            List<Future<FileCryptoResult>> futures = files.stream().map(source -> executor.submit(() -> {
                Path relative = inputDir.relativize(source);
                String name = relative.getFileName().toString();
                String targetName = encrypt
                        ? name + properties.outputExtension()
                        : name.substring(0, name.length() - properties.outputExtension().length());
                Path target = outputDir.resolve(relative).resolveSibling(targetName);
                try {
                    return encrypt ? cryptoService.encrypt(source, target, alias)
                                   : cryptoService.decrypt(source, target, alias);
                } catch (Exception e) {
                    return FileCryptoResult.failure(source, encrypt ? "ENCRYPT" : "DECRYPT", e);
                }
            })).toList();

            java.util.ArrayList<FileCryptoResult> results = new java.util.ArrayList<>(futures.size());
            for (Future<FileCryptoResult> future : futures) {
                results.add(future.get());
            }
            return List.copyOf(results);
        }
    }
}

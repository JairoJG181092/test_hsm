# Modernización: cifrado de archivos con HSM

## Objetivo

Esta versión agrega cifrado y descifrado de archivos individuales y procesamiento masivo por directorios, reutilizando la integración existente con el HSM Realsec mediante `HSMService`.

## Cambios principales

- Java 21.
- Spring Boot 4.x.
- AES-256-GCM para cifrado del contenido.
- Cifrado híbrido/envelope: se genera una llave AES aleatoria por archivo y se protege con la llave pública RSA del alias del HSM.
- Descifrado de la llave AES mediante la llave privada asociada al alias.
- Procesamiento en streaming: los archivos no se cargan completos en memoria.
- Procesamiento paralelo configurable para directorios.
- Formato `HSMF` versionado para almacenar IV, llave AES protegida y contenido cifrado.
- RSA-OAEP con SHA-256 como valor por defecto en lugar de RSA1_5.
- Secretos externalizados mediante variables de entorno.

## Importante sobre el HSM

La llave privada no debe salir del HSM. El proveedor PKCS#11 debe realizar la operación privada cuando la implementación del proveedor expone correctamente la `PrivateKey` del token. Debe validarse en el entorno real que `RSA/ECB/OAEPWithSHA-256AndMGF1Padding` sea soportado por la combinación de firmware/proveedor Realsec instalada.

## Configuración mínima

```bash
export HSM_REALSEC_PIN='***'
export HSM_REALSEC_CONFIG_PATH='/ruta/config.txt'
export HSM_REALSEC_CONFIG_SERVER_PATH='/ruta/config.txt'
```

No guardar PINes en `application.properties`. Si un PIN fue publicado previamente en el repositorio, debe rotarse.

## API

### Archivo individual

`POST /api/v1/files/encrypt?source=/entrada/a.pdf&target=/salida/a.pdf.hsm&alias=MI_ALIAS`

`POST /api/v1/files/decrypt?source=/entrada/a.pdf.hsm&target=/salida/a.pdf&alias=MI_ALIAS`

### Directorio masivo

`POST /api/v1/files/encrypt-directory?inputDir=/entrada&outputDir=/cifrado&alias=MI_ALIAS`

`POST /api/v1/files/decrypt-directory?inputDir=/cifrado&outputDir=/descifrado&alias=MI_ALIAS`

## Parámetros de rendimiento

- `FILE_CRYPTO_PARALLELISM`: número de archivos procesados simultáneamente; iniciar con 2-4 y ajustar según los límites del HSM.
- `FILE_CRYPTO_BUFFER_BYTES`: tamaño del buffer de streaming; por defecto 1 MiB.
- `FILE_CRYPTO_MAX_FILE_SIZE`: 0 significa sin límite de aplicación.

## Pendientes recomendados

1. Agregar pruebas unitarias con un proveedor HSM simulado.
2. Agregar pruebas de integración contra un HSM de pruebas.
3. Confirmar compatibilidad de OAEP-SHA256 con el proveedor Realsec; si no existe soporte, usar una transformación aprobada por el fabricante.
4. Añadir autenticación/autorización antes de exponer la API fuera de una red controlada.
5. Agregar auditoría sin registrar PINes, llaves ni contenido sensible.
6. Considerar una cola de trabajos para lotes muy grandes y reintentos controlados.

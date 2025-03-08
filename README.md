# Gestor FTP

Este proyecto es una aplicación Java que permite sincronizar archivos entre una carpeta local y un servidor FTP. Además, incluye funcionalidades para cifrar y descifrar archivos utilizando el algoritmo AES. El proyecto consta de tres clases principales: `GestorFTP`, `DescargadorFTP` y `AESSimpleManager`.

## Clases principales

### 1. `GestorFTP`

La clase `GestorFTP` es el núcleo del proyecto. Se encarga de sincronizar los archivos entre una carpeta local y una carpeta remota en un servidor FTP. Las principales funcionalidades son:

- **Sincronización automática**: Cada 2 segundos, verifica los cambios en la carpeta local y los replica en el servidor FTP.
- **Cifrado de archivos**: Los archivos se cifran antes de ser subidos al servidor FTP utilizando el algoritmo AES.
- **Gestión de hilos**: Utiliza hilos para mejorar la eficiencia en la subida y borrado de archivos.

#### Métodos principales:
- `sincronizar()`: Realiza la sincronización entre la carpeta local y la remota.
- `subirArchivo(String archivo)`: Sube un archivo cifrado al servidor FTP.
- `borrarArchivo(String archivo)`: Borra un archivo del servidor FTP.
- `cifrarArchivo(File file)`: Cifra el contenido de un archivo utilizando AES.

### 2. `DescargadorFTP`

La clase `DescargadorFTP` permite descargar y descifrar archivos desde el servidor FTP. Las principales funcionalidades son:

- **Descarga de archivos**: Descarga todos los archivos de la carpeta remota en el servidor FTP.
- **Descifrado de archivos**: Descifra los archivos descargados utilizando el algoritmo AES.
- **Almacenamiento local**: Guarda los archivos descifrados en una carpeta local (`C:\Users\carma\Desktop\DescargasFTP`).

#### Métodos principales:
- `main(String[] args)`: Inicia la descarga y descifrado de archivos.
- `descifrarArchivo(File archivoCifrado)`: Descifra el contenido de un archivo utilizando AES.

### 3. `AESSimpleManager`

La clase `AESSimpleManager` proporciona las funciones de cifrado y descifrado utilizando el algoritmo AES. Es utilizada por las clases `GestorFTP` y `DescargadorFTP` para manejar la seguridad de los archivos.

#### Métodos principales:
- `obtenerClave(String password, int longitud)`: Genera una clave AES a partir de una contraseña.
- `cifrar(String textoEnClaro, Key key)`: Cifra un texto utilizando la clave AES.
- `descifrar(String textoCifrado, Key key)`: Descifra un texto utilizando la clave AES.

## Configuración

### Credenciales FTP
- **Servidor FTP**: `127.0.0.1`
- **Usuario**: `Alvaro`
- **Contraseña**: `1234`

### Rutas
- **Carpeta local**: `C:\Users\carma\Desktop\CARPETA_LOCAL`
- **Carpeta remota**: `CARPETA_REMOTA`
- **Carpeta de descargas**: `C:\Users\carma\Desktop\DescargasFTP`

### Cifrado AES
- **Contraseña de cifrado**: `NoMeLlamoSpidermanMeLlamoSpidermanMeLlamoSpiderman`
- **Longitud de bloque**: `32 bytes`

### Defensa
(https://youtu.be/2YMUqzUwRqI)

## Ejecución

### GestorFTP
Para ejecutar la sincronización automática, ejecuta la clase `GestorFTP`. La sincronización se realizará cada 2 segundos.

```bash
java -cp . com.mycompany.gestorftp.GestorFTP

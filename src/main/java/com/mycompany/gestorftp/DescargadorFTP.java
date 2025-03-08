package com.mycompany.gestorftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * Clase que permite descargar y descifrar archivos desde un servidor FTP.
 * Los archivos descargados se almacenan en una carpeta local y se descifran utilizando el algoritmo AES.
 */
public class DescargadorFTP {

    // Configuración del servidor FTP
    private static final String SERVIDOR_FTP = "127.0.0.1";
    private static final String USUARIO = "Alvaro";
    private static final String CONTRASEÑA = "1234";

    // Rutas de las carpetas remota y local
    private static final String CARPETA_REMOTA = "CARPETA_REMOTA";
    private static final String CARPETA_DESCARGAS = "C:\\Users\\carma\\Desktop\\DescargasFTP";

    // Configuración del cifrado AES
    private static final int LONGITUD_BLOQUE = 32; // Expresado en bytes
    private static final String PASSWORD = "NoMeLlamoSpidermanMeLlamoSpidermanMeLlamoSpiderman";

    /**
     * Método principal que inicia la aplicación.
     * Descarga todos los archivos de la carpeta remota, los descifra y los guarda en una carpeta local.
     *
     * @param args Argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        FTPClient clienteFTP = new FTPClient();

        try {
            // Conectar al servidor FTP
            clienteFTP.connect(SERVIDOR_FTP);
            clienteFTP.login(USUARIO, CONTRASEÑA);

            // Crear la carpeta de descargas si no existe
            File carpetaDescargas = new File(CARPETA_DESCARGAS);
            if (!carpetaDescargas.exists()) {
                carpetaDescargas.mkdirs();
            }

            // Cambiar al directorio remoto
            clienteFTP.changeWorkingDirectory(CARPETA_REMOTA);

            // Obtener la lista de archivos en la carpeta remota
            FTPFile[] archivosRemotos = clienteFTP.listFiles();
            if (archivosRemotos == null || archivosRemotos.length == 0) {
                System.out.println("No hay archivos en la carpeta remota.");
                return;
            }

            // Descargar y descifrar cada archivo
            for (FTPFile archivoRemoto : archivosRemotos) {
                if (archivoRemoto.isFile()) {
                    String nombreArchivo = archivoRemoto.getName();
                    File archivoCifrado = new File(carpetaDescargas, nombreArchivo);

                    // Descargar el archivo cifrado
                    try (OutputStream outputStream = new FileOutputStream(archivoCifrado)) {
                        boolean descargado = clienteFTP.retrieveFile(nombreArchivo, outputStream);
                        if (descargado) {
                            System.out.println("Archivo " + nombreArchivo + " descargado correctamente.");
                        } else {
                            System.out.println("No se pudo descargar el archivo " + nombreArchivo);
                            continue; // Saltar al siguiente archivo
                        }
                    }

                    // Descifrar el archivo
                    String contenidoDescifrado = descifrarArchivo(archivoCifrado);
                    File archivoDescifrado = new File(carpetaDescargas, "descifrado_" + nombreArchivo);
                    try (FileWriter writer = new FileWriter(archivoDescifrado)) {
                        writer.write(contenidoDescifrado);
                        System.out.println("Archivo descifrado guardado en: " + archivoDescifrado.getAbsolutePath());
                    }

                    // Eliminar el archivo cifrado después de descifrarlo (opcional)
                    archivoCifrado.delete();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clienteFTP.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Descifra el contenido de un archivo utilizando el algoritmo AES.
     *
     * @param archivoCifrado Archivo cifrado a descifrar.
     * @return Contenido descifrado en formato de texto.
     * @throws IOException Si ocurre un error al leer el archivo o descifrar el contenido.
     */
    private static String descifrarArchivo(File archivoCifrado) throws IOException {
        StringBuilder contenidoCifrado = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(archivoCifrado))) {
            String line;
            while ((line = br.readLine()) != null) {
                contenidoCifrado.append(line);
            }
        }

        try {
            Key key = AESSimpleManager.obtenerClave(PASSWORD, LONGITUD_BLOQUE);
            return AESSimpleManager.descifrar(contenidoCifrado.toString(), key);
        } catch (Exception e) {
            throw new IOException("Error al descifrar el archivo", e);
        }
    }
}
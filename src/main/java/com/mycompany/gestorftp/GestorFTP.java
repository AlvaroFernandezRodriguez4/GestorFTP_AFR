package com.mycompany.gestorftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Clase principal que gestiona la sincronización de archivos entre una carpeta local y un servidor FTP.
 * Los archivos se cifran antes de ser subidos al servidor FTP utilizando el algoritmo AES.
 * La sincronización se realiza en intervalos regulares y utiliza hilos para mejorar la eficiencia.
 */
public class GestorFTP {

    // Configuración del servidor FTP
    private static final String SERVIDOR_FTP = "127.0.0.1";
    private static final String USUARIO = "Alvaro";
    private static final String CONTRASEÑA = "1234";

    // Rutas de las carpetas local y remota
    private static final String CARPETA_LOCAL = "C:\\Users\\carma\\Desktop\\CARPETA_LOCAL";
    private static final String CARPETA_REMOTA = "CARPETA_REMOTA";

    // Intervalo de sincronización en segundos
    private static final long SINCRONIZAR = 2; // 2 segundos

    // Configuración del cifrado AES
    private static final int LONGITUD_BLOQUE = 32; // Expresado en bytes
    private static final String PASSWORD = "NoMeLlamoSpidermanMeLlamoSpidermanMeLlamoSpiderman";

    // ExecutorService para gestionar hilos
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    /**
     * Método principal que inicia la aplicación.
     * Programa la ejecución periódica de la sincronización cada 2 segundos.
     *
     * @param args Argumentos de la línea de comandos (no se utilizan).
     */
    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> {
            try {
                sincronizar();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 0, SINCRONIZAR, TimeUnit.SECONDS);
    }

    /**
     * Realiza la sincronización entre la carpeta local y la carpeta remota en el servidor FTP.
     * Compara los archivos locales y remotos, y realiza las acciones necesarias (subir, borrar o actualizar).
     *
     * @throws IOException Si ocurre un error de entrada/salida durante la conexión FTP o la manipulación de archivos.
     */
    private static void sincronizar() throws IOException {
        FTPClient clienteFTP = new FTPClient();
        clienteFTP.connect(SERVIDOR_FTP);
        clienteFTP.login(USUARIO, CONTRASEÑA);

        // Obtener listas de archivos locales y remotos
        List<String> archivosLocales = obtenerArchivosLocales(CARPETA_LOCAL);
        List<String> archivosRemotos = obtenerArchivosRemotos(clienteFTP, CARPETA_REMOTA);

        // Borrar archivos remotos que ya no existen localmente
        for (String archivoRemoto : archivosRemotos) {
            if (!archivosLocales.contains(archivoRemoto)) {
                executor.submit(() -> borrarArchivo(archivoRemoto));
            }
        }

        // Subir o actualizar archivos (cifrados)
        for (String archivoLocal : archivosLocales) {
            File fileLocal = new File(CARPETA_LOCAL + File.separator + archivoLocal);
            if (!archivosRemotos.contains(archivoLocal)
                || estaArchivoActualizado(clienteFTP, CARPETA_REMOTA, archivoLocal, fileLocal.lastModified())) {
                executor.submit(() -> subirArchivo(archivoLocal));
            }
        }

        clienteFTP.disconnect();
    }

    /**
     * Obtiene la lista de archivos en la carpeta remota del servidor FTP.
     *
     * @param clienteFTP Cliente FTP conectado.
     * @param carpeta    Carpeta remota a listar.
     * @return Lista de nombres de archivos en la carpeta remota.
     * @throws IOException Si ocurre un error al listar los archivos.
     */
    private static List<String> obtenerArchivosRemotos(FTPClient clienteFTP, String carpeta) throws IOException {
        List<String> archivos = new ArrayList<>();
        clienteFTP.changeWorkingDirectory(carpeta);
        for (FTPFile archivo : clienteFTP.listFiles()) {
            if (archivo.isFile()) {
                archivos.add(archivo.getName());
            }
        }
        return archivos;
    }

    /**
     * Obtiene la lista de archivos en la carpeta local.
     *
     * @param carpeta Ruta de la carpeta local.
     * @return Lista de nombres de archivos en la carpeta local.
     */
    private static List<String> obtenerArchivosLocales(String carpeta) {
        List<String> archivos = new ArrayList<>();
        File directorio = new File(carpeta);
        File[] listaArchivos = directorio.listFiles();
        if (listaArchivos != null) {
            for (File archivo : listaArchivos) {
                if (archivo.isFile()) {
                    archivos.add(archivo.getName());
                }
            }
        }
        return archivos;
    }

    /**
     * Borra un archivo en el servidor FTP.
     *
     * @param archivo Nombre del archivo a borrar.
     */
    private static void borrarArchivo(String archivo) {
        FTPClient clienteFTP = new FTPClient();
        try {
            clienteFTP.connect(SERVIDOR_FTP);
            clienteFTP.login(USUARIO, CONTRASEÑA);
            clienteFTP.deleteFile(archivo);
            System.out.println("Archivo " + archivo + " eliminado");
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
     * Sube un archivo al servidor FTP después de cifrarlo.
     *
     * @param archivo Nombre del archivo a subir.
     */
    private static void subirArchivo(String archivo) {
        FTPClient clienteFTP = new FTPClient();
        try {
            clienteFTP.connect(SERVIDOR_FTP);
            clienteFTP.login(USUARIO, CONTRASEÑA);

            File localFile = new File(CARPETA_LOCAL + File.separator + archivo);
            String encryptedContent = cifrarArchivo(localFile);

            try (InputStream inputStream = new ByteArrayInputStream(encryptedContent.getBytes(StandardCharsets.UTF_8))) {
                clienteFTP.storeFile(archivo, inputStream);
                System.out.println("Archivo " + archivo + " subido (cifrado)");
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
     * Cifra el contenido de un archivo utilizando el algoritmo AES.
     *
     * @param file Archivo a cifrar.
     * @return Contenido cifrado en formato Base64.
     * @throws IOException Si ocurre un error al leer el archivo o cifrar el contenido.
     */
    private static String cifrarArchivo(File file) throws IOException {
        StringBuilder fileContent = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                fileContent.append(line).append("\n");
            }
        }

        try {
            Key key = AESSimpleManager.obtenerClave(PASSWORD, LONGITUD_BLOQUE);
            return AESSimpleManager.cifrar(fileContent.toString(), key);
        } catch (Exception e) {
            throw new IOException("Error al cifrar el archivo", e);
        }
    }

    /**
     * Verifica si un archivo local está actualizado comparando su fecha de modificación con la del archivo remoto.
     *
     * @param clienteFTP             Cliente FTP conectado.
     * @param carpetaRemota          Carpeta remota donde se encuentra el archivo.
     * @param nombreArchivo          Nombre del archivo a verificar.
     * @param ultimaModificacionLocal Fecha de última modificación del archivo local.
     * @return true si el archivo local está actualizado, false en caso contrario.
     * @throws IOException Si ocurre un error al acceder al servidor FTP.
     */
    private static boolean estaArchivoActualizado(FTPClient clienteFTP, String carpetaRemota, String nombreArchivo, long ultimaModificacionLocal) throws IOException {
        clienteFTP.changeWorkingDirectory(carpetaRemota);
        FTPFile[] archivosRemotos = clienteFTP.listFiles();

        for (FTPFile archivoRemoto : archivosRemotos) {
            if (archivoRemoto.getName().equals(nombreArchivo)) {
                long ultimaModificacionRemota = archivoRemoto.getTimestamp().getTimeInMillis();
                return ultimaModificacionLocal > ultimaModificacionRemota;
            }
        }
        return true;
    }
}
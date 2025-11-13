package co.edu.uniquindio.service.utils;

import co.edu.uniquindio.exception.ElementoNoValidoException;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Servicio de utilidad para la gestión de archivos multimedia (imágenes y audio) utilizando la plataforma Cloudinary.
 *
 * <p>Esta clase se encarga de la configuración, la validación de tamaño y la lógica de subida (upload)
 * de archivos {@code MultipartFile} a un servidor externo (Cloudinary), retornando las URLs de acceso seguro.
 *
 * <p>El servicio utiliza inyección de valores desde `application.properties` para las credenciales de Cloudinary.
 */
@Service
public class CloudinaryService {

    // Inyecta el nombre de la nube desde application.properties
    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    // Inyecta la clave API desde application.properties
    @Value("${cloudinary.api-key}")
    private String apiKey;

    // Inyecta el secreto API desde application.properties
    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    // Objeto de Cloudinary para subir archivos
    private Cloudinary cloudinary;

    // Tamaño máximo permitido para imágenes (5 MB)
    private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024; // 5 MB

    // Tamaño máximo permitido para archivos MP3 (15 MB)
    private static final long MAX_AUDIO_SIZE = 15 * 1024 * 1024; // 15 MB


    /**
     * Obtiene y/o inicializa la instancia Singleton del cliente de Cloudinary.
     *
     * <p>El cliente se inicializa con las credenciales inyectadas si aún no existe.
     *
     * @return La instancia configurada de {@link Cloudinary}.
     */
    private Cloudinary getCloudinary() {
        if (cloudinary == null) {   // Si cloudinary es null, lo inicializamos
            cloudinary = new Cloudinary(ObjectUtils.asMap(
                    "cloud_name", cloudName,    // Asigna el nombre de la nube
                    "api_key", apiKey,                    // Asigna la clave API
                    "api_secret", apiSecret               // Asigna el secreto API
            ));
        }
        // Retorna la instancia de Cloudinary
        return cloudinary;
    }

    /**
     * Convierte un archivo recibido en un {@code MultipartFile} a un archivo temporal de tipo {@code File}.
     *
     * <p>Esta conversión es necesaria porque la librería de Cloudinary trabaja directamente con objetos {@code File}
     * del sistema de archivos.
     *
     * @param file El archivo entrante en formato {@code MultipartFile}.
     * @return El archivo temporal creado en el sistema de archivos local.
     * @throws IOException Si ocurre un error durante la creación o escritura del archivo.
     */
    private File convertir(MultipartFile file) throws IOException {
        // Crea un archivo temporal
        File archivo = File.createTempFile(file.getOriginalFilename(), null);
        // Flujo para escribir bytes en el archivo
        try (FileOutputStream fos = new FileOutputStream(archivo)) {
            // Escribe el contenido del MultipartFile en el archivo temporal
            fos.write(file.getBytes());
        }
        // Retorna el archivo temporal creado
        return archivo;
    }


    /**
     * Sube un archivo de imagen a Cloudinary.
     *
     * <p>El método realiza una validación de tamaño previa y elimina el archivo temporal después de la subida.
     *
     * @param file El archivo de imagen (portada) en formato {@code MultipartFile}.
     * @return La URL de acceso seguro (`secure_url`) a la imagen subida.
     * @throws ElementoNoValidoException Si el tamaño del archivo excede {@code MAX_IMAGE_SIZE}.
     * @throws RuntimeException Si ocurre un {@code IOException} durante la subida o conversión.
     */
    public String uploadImage(MultipartFile file) throws ElementoNoValidoException {
        if (file.getSize() > MAX_IMAGE_SIZE) { // Verifica si la imagen supera el tamaño máximo
            throw new ElementoNoValidoException("La imagen excede el tamaño máximo permitido"); // Lanza excepción si es muy grande
        }
        try {
            File archivo = convertir(file); // Convierte MultipartFile a File temporal
            Map<String, Object> result = getCloudinary().uploader() // Subida a Cloudinary
                    .upload(archivo, ObjectUtils.asMap()); // Sin carpeta específica, configuración por defecto
            archivo.delete(); // Elimina el archivo temporal
            return (String) result.get("secure_url"); // Retorna la URL segura de la imagen subida
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo imagen a Cloudinary", e); // Maneja errores de IO
        }
    }


    /**
     * Sube un archivo de audio (MP3) a Cloudinary.
     *
     * <p>El método realiza una validación de tamaño y configura el recurso como tipo "video"
     * (el tipo que Cloudinary usa para archivos de audio).
     *
     * @param file El archivo de audio (MP3) en formato {@code MultipartFile}.
     * @return La URL de acceso seguro (`secure_url`) al archivo de audio subido.
     * @throws ElementoNoValidoException Si el tamaño del archivo excede {@code MAX_AUDIO_SIZE}.
     * @throws RuntimeException Si ocurre un {@code IOException} durante la subida o conversión.
     */
    public String uploadMp3(MultipartFile file) throws ElementoNoValidoException {
        if (file.getSize() > MAX_AUDIO_SIZE) { // Verifica si el archivo MP3 supera el tamaño máximo
            throw new ElementoNoValidoException("El archivo de audio excede el tamaño máximo permitido"); // Lanza excepción si es muy grande
        }
        try {
            File archivo = convertir(file); // Convierte MultipartFile a File temporal
            Map<String, Object> result = getCloudinary().uploader() // Subida a Cloudinary
                    .upload(archivo, ObjectUtils.asMap(
                            "resource_type", "video" // Cloudinary trata mp3 como video/audio
                    ));
            archivo.delete(); // Elimina el archivo temporal
            return (String) result.get("secure_url"); // Retorna la URL segura del MP3 subido
        } catch (IOException e) {
            throw new RuntimeException("Error subiendo archivo MP3 a Cloudinary", e); // Maneja errores de IO
        }
    }
}

package co.edu.sena.autenticacion.seguridad;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Utilidades para proteger las contraseñas.
 *
 * Una contraseña nunca se guarda tal cual en la base de datos. Se guarda un "hash":
 * un resultado que no se puede revertir. Al iniciar sesión se calcula el hash de la
 * contraseña recibida y se compara con el guardado.
 *
 * Se usa PBKDF2 con una "sal" aleatoria distinta para cada usuario, de modo que dos
 * usuarios con la misma contraseña tengan hashes diferentes.
 */
public final class ClaveUtil {

    private static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    private static final int ITERACIONES = 65536;   // más iteraciones = más difícil de adivinar
    private static final int LONGITUD_BITS = 256;   // tamaño del hash resultante
    private static final int LONGITUD_SALT = 16;    // bytes de la sal

    private static final SecureRandom ALEATORIO = new SecureRandom();

    /** Clase de utilidades: no se instancia. */
    private ClaveUtil() {
    }

    /** Genera una sal aleatoria y la devuelve en texto (Base64) para guardarla en la BD. */
    public static String generarSalt() {
        byte[] sal = new byte[LONGITUD_SALT];
        ALEATORIO.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    /** Calcula el hash de la contraseña usando la sal indicada. */
    public static String hashear(String contrasena, String salt) {
        try {
            PBEKeySpec especificacion = new PBEKeySpec(
                    contrasena.toCharArray(),
                    Base64.getDecoder().decode(salt),
                    ITERACIONES,
                    LONGITUD_BITS);
            SecretKeyFactory fabrica = SecretKeyFactory.getInstance(ALGORITMO);
            byte[] hash = fabrica.generateSecret(especificacion).getEncoded();
            return Base64.getEncoder().encodeToString(hash);
        } catch (GeneralSecurityException e) {
            // Solo ocurre si el servidor no soporta el algoritmo: es un error de configuración
            throw new IllegalStateException("No se pudo calcular el hash de la contraseña", e);
        }
    }

    /**
     * Comprueba si la contraseña recibida corresponde al hash guardado.
     * Usa una comparación de tiempo constante para no filtrar información por el tiempo de respuesta.
     */
    public static boolean coincide(String contrasena, String salt, String hashGuardado) {
        String hashCalculado = hashear(contrasena, salt);
        return MessageDigest.isEqual(
                hashCalculado.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                hashGuardado.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}

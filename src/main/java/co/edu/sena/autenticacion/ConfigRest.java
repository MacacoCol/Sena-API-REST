package co.edu.sena.autenticacion;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

/**
 * Configuración de la API REST.
 *
 * La anotación @ApplicationPath define la ruta base de todos los servicios:
 * todos los recursos quedan bajo "/api". Por ejemplo:
 *   POST http://localhost:8080/servicio-autenticacion/api/auth/registro
 *   POST http://localhost:8080/servicio-autenticacion/api/auth/login
 *
 * No hace falta registrar las clases a mano: el servidor las detecta solo.
 */
@ApplicationPath("api")
public class ConfigRest extends Application {
    // Clase vacía a propósito: basta con extender Application y poner la anotación.
}

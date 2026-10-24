package co.edu.sena.autenticacion.recurso;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.sena.autenticacion.config.ConexionBD;
import com.sun.net.httpserver.HttpServer;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;
import org.glassfish.jersey.jdkhttp.JdkHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Pruebas automáticas del servicio.
 *
 * Levanta el servicio de verdad en http://localhost:9998 y le hace peticiones HTTP,
 * con una base de datos H2 en memoria (así no hace falta instalar MySQL para probar).
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AuthResourceTest {

    private static final String BASE = "http://localhost:9998/api/auth";
    private static HttpServer servidor;
    private static final HttpClient cliente = HttpClient.newHttpClient();

    @BeforeAll
    static void iniciar() throws Exception {
        // Base de datos en memoria con la misma tabla del script sql/crear_base.sql
        System.setProperty("db.url", "jdbc:h2:mem:pruebas;DB_CLOSE_DELAY=-1");
        System.setProperty("db.user", "sa");
        System.setProperty("db.pass", "");
        try (Connection c = ConexionBD.obtener(); Statement st = c.createStatement()) {
            st.execute("CREATE TABLE usuarios ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "usuario VARCHAR(50) NOT NULL UNIQUE, "
                    + "clave_hash VARCHAR(255) NOT NULL, "
                    + "salt VARCHAR(64) NOT NULL, "
                    + "fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
        }
        // Arranca el servicio con el recurso de autenticación
        servidor = JdkHttpServerFactory.createHttpServer(
                URI.create("http://localhost:9998/api/"), new ResourceConfig(AuthResource.class));
    }

    @AfterAll
    static void detener() {
        servidor.stop(0);
    }

    /** Envía un POST con JSON y devuelve la respuesta. */
    private static HttpResponse<String> post(String ruta, String json) throws Exception {
        HttpRequest peticion = HttpRequest.newBuilder(URI.create(BASE + ruta))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
                .build();
        return cliente.send(peticion, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
    }

    private static String json(String usuario, String contrasena) {
        return "{\"usuario\":\"" + usuario + "\",\"contrasena\":\"" + contrasena + "\"}";
    }

    @Test
    @Order(1)
    void registroCorrectoDevuelve201() throws Exception {
        HttpResponse<String> r = post("/registro", json("santiago", "clave123"));
        assertEquals(201, r.statusCode());
        assertTrue(r.body().contains("Usuario registrado correctamente"));
    }

    @Test
    @Order(2)
    void registroDuplicadoDevuelve409() throws Exception {
        HttpResponse<String> r = post("/registro", json("santiago", "otraClave1"));
        assertEquals(409, r.statusCode());
    }

    @Test
    @Order(3)
    void registroConMayusculasEsElMismoUsuario() throws Exception {
        // "SANTIAGO" se normaliza a "santiago", que ya existe
        HttpResponse<String> r = post("/registro", json("SANTIAGO", "otraClave1"));
        assertEquals(409, r.statusCode());
    }

    @Test
    @Order(4)
    void registroConContrasenaCortaDevuelve400() throws Exception {
        HttpResponse<String> r = post("/registro", json("ana", "123"));
        assertEquals(400, r.statusCode());
    }

    @Test
    @Order(5)
    void registroConCamposVaciosDevuelve400() throws Exception {
        HttpResponse<String> r = post("/registro", json("", ""));
        assertEquals(400, r.statusCode());
    }

    @Test
    @Order(6)
    void loginCorrectoDevuelveAutenticacionSatisfactoria() throws Exception {
        HttpResponse<String> r = post("/login", json("santiago", "clave123"));
        assertEquals(200, r.statusCode());
        assertTrue(r.body().contains("Autenticación satisfactoria"), r.body());
    }

    @Test
    @Order(7)
    void loginConContrasenaIncorrectaDevuelveError() throws Exception {
        HttpResponse<String> r = post("/login", json("santiago", "claveMala"));
        assertEquals(401, r.statusCode());
        assertTrue(r.body().contains("Error en la autenticación"), r.body());
    }

    @Test
    @Order(8)
    void loginConUsuarioInexistenteDevuelveError() throws Exception {
        HttpResponse<String> r = post("/login", json("fantasma", "clave123"));
        assertEquals(401, r.statusCode());
        assertTrue(r.body().contains("Error en la autenticación"), r.body());
    }

    @Test
    @Order(9)
    void loginSinDatosDevuelveError() throws Exception {
        HttpResponse<String> r = post("/login", "{}");
        assertEquals(401, r.statusCode());
    }

    @Test
    @Order(10)
    void inyeccionSqlNoPermiteEntrar() throws Exception {
        HttpResponse<String> r = post("/login", json("' OR '1'='1", "' OR '1'='1"));
        assertEquals(401, r.statusCode());
    }
}

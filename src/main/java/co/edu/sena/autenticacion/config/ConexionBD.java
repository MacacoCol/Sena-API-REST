package co.edu.sena.autenticacion.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Entrega conexiones JDBC a la base de datos.
 *
 * Los datos de conexión se leen, en este orden, de:
 *   1. Una propiedad del sistema (-Ddb.url=..., -Ddb.user=..., -Ddb.pass=...)
 *   2. Una variable de entorno (DB_URL, DB_USER, DB_PASS)
 *   3. Un valor por defecto pensado para MySQL instalado en el mismo equipo.
 *
 * Así no hay que escribir la contraseña real dentro del código.
 */
public class ConexionBD {

    // Valores por defecto: MySQL local, base "autenticacion_db"
    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/autenticacion_db?useSSL=false&allowPublicKeyRetrieval=true";
    private static final String USUARIO_DEFECTO = "root";
    private static final String CLAVE_DEFECTO = "";

    /** Evita crear objetos de esta clase: solo se usan sus métodos estáticos. */
    private ConexionBD() {
    }

    /**
     * Abre una conexión nueva. Quien la pide es responsable de cerrarla
     * (en este proyecto se usa try-with-resources, que la cierra sola).
     */
    public static Connection obtener() throws SQLException {
        String url = leer("db.url", "DB_URL", URL_DEFECTO);
        String usuario = leer("db.user", "DB_USER", USUARIO_DEFECTO);
        String clave = leer("db.pass", "DB_PASS", CLAVE_DEFECTO);
        return DriverManager.getConnection(url, usuario, clave);
    }

    /** Busca un valor en propiedad del sistema, luego en variable de entorno, y si no, usa el defecto. */
    private static String leer(String propiedad, String variableEntorno, String defecto) {
        String valor = System.getProperty(propiedad);
        if (valor == null) {
            valor = System.getenv(variableEntorno);
        }
        return valor != null ? valor : defecto;
    }
}

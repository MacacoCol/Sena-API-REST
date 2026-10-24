package co.edu.sena.autenticacion.dao;

import co.edu.sena.autenticacion.config.ConexionBD;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * DAO (Data Access Object): clase que concentra todas las consultas SQL de la tabla "usuarios".
 * Así el resto del programa no necesita saber nada de SQL.
 *
 * Todas las consultas usan PreparedStatement con parámetros (?) para evitar la
 * inyección SQL, y try-with-resources para cerrar siempre la conexión.
 */
public class UsuarioDAO {

    /** Indica si ya existe un usuario con ese nombre. */
    public boolean existe(String usuario) throws SQLException {
        String sql = "SELECT 1 FROM usuarios WHERE usuario = ?";
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                return resultado.next(); // true si encontró una fila
            }
        }
    }

    /** Inserta un usuario nuevo con el hash de su contraseña y su sal. */
    public void crear(String usuario, String claveHash, String salt) throws SQLException {
        String sql = "INSERT INTO usuarios (usuario, clave_hash, salt) VALUES (?, ?, ?)";
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario);
            consulta.setString(2, claveHash);
            consulta.setString(3, salt);
            consulta.executeUpdate();
        }
    }

    /**
     * Busca el hash y la sal de un usuario.
     *
     * @return arreglo {hash, salt}, o null si el usuario no existe
     */
    public String[] buscarCredenciales(String usuario) throws SQLException {
        String sql = "SELECT clave_hash, salt FROM usuarios WHERE usuario = ?";
        try (Connection conexion = ConexionBD.obtener();
             PreparedStatement consulta = conexion.prepareStatement(sql)) {
            consulta.setString(1, usuario);
            try (ResultSet resultado = consulta.executeQuery()) {
                if (resultado.next()) {
                    return new String[]{resultado.getString("clave_hash"), resultado.getString("salt")};
                }
                return null; // el usuario no existe
            }
        }
    }
}

package co.edu.sena.autenticacion.modelo;

/**
 * Datos que el cliente envía en la petición (cuerpo JSON):
 * {
 *   "usuario": "santiago",
 *   "contrasena": "miClave123"
 * }
 *
 * El servicio convierte el JSON a este objeto automáticamente.
 */
public class Credenciales {

    private String usuario;
    private String contrasena;

    /** Constructor vacío: lo necesita la librería JSON para crear el objeto. */
    public Credenciales() {
    }

    public Credenciales(String usuario, String contrasena) {
        this.usuario = usuario;
        this.contrasena = contrasena;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getContrasena() {
        return contrasena;
    }

    public void setContrasena(String contrasena) {
        this.contrasena = contrasena;
    }
}

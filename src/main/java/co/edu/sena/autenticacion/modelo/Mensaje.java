package co.edu.sena.autenticacion.modelo;

/**
 * Respuesta del servicio. Se envía al cliente en formato JSON:
 * { "mensaje": "Autenticación satisfactoria" }
 */
public class Mensaje {

    private String mensaje;

    /** Constructor vacío requerido por la librería JSON. */
    public Mensaje() {
    }

    public Mensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}

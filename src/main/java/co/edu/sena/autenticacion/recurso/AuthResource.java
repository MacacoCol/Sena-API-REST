package co.edu.sena.autenticacion.recurso;

import co.edu.sena.autenticacion.dao.UsuarioDAO;
import co.edu.sena.autenticacion.modelo.Credenciales;
import co.edu.sena.autenticacion.modelo.Mensaje;
import co.edu.sena.autenticacion.seguridad.ClaveUtil;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servicio web de registro e inicio de sesión.
 *
 * Rutas (la base es /api, definida en ConfigRest):
 *   POST /api/auth/registro -> crea un usuario nuevo
 *   POST /api/auth/login    -> valida usuario y contraseña
 *
 * Ambas reciben y devuelven JSON.
 */
@Path("auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private static final Logger LOG = Logger.getLogger(AuthResource.class.getName());

    // Mensajes que ve el cliente
    static final String MSG_LOGIN_OK = "Autenticación satisfactoria";
    static final String MSG_LOGIN_ERROR = "Error en la autenticación";

    // Reglas de validación del registro
    private static final int USUARIO_MAX = 50;
    private static final int CLAVE_MIN = 6;

    private final UsuarioDAO dao = new UsuarioDAO();

    /**
     * POST /api/auth/registro
     *
     * Respuestas:
     *   201 - usuario creado
     *   400 - datos inválidos (vacíos, usuario muy largo o contraseña muy corta)
     *   409 - el usuario ya existe
     *   500 - error interno (por ejemplo, la base de datos no responde)
     */
    @POST
    @Path("registro")
    public Response registrar(Credenciales datos) {
        // 1. Validar que llegaron los datos
        if (datos == null || estaVacio(datos.getUsuario()) || estaVacio(datos.getContrasena())) {
            return responder(Response.Status.BAD_REQUEST, "Usuario y contraseña son obligatorios");
        }
        String usuario = normalizar(datos.getUsuario());
        if (usuario.length() > USUARIO_MAX) {
            return responder(Response.Status.BAD_REQUEST,
                    "El usuario no puede superar " + USUARIO_MAX + " caracteres");
        }
        if (datos.getContrasena().length() < CLAVE_MIN) {
            return responder(Response.Status.BAD_REQUEST,
                    "La contraseña debe tener al menos " + CLAVE_MIN + " caracteres");
        }

        try {
            // 2. Verificar que el usuario no exista
            if (dao.existe(usuario)) {
                return responder(Response.Status.CONFLICT, "El usuario ya existe");
            }

            // 3. Guardar el usuario con la contraseña protegida (hash + sal)
            String salt = ClaveUtil.generarSalt();
            String hash = ClaveUtil.hashear(datos.getContrasena(), salt);
            dao.crear(usuario, hash, salt);
            return responder(Response.Status.CREATED, "Usuario registrado correctamente");

        } catch (SQLIntegrityConstraintViolationException e) {
            // Dos registros simultáneos con el mismo usuario: la BD rechaza el segundo
            return responder(Response.Status.CONFLICT, "El usuario ya existe");
        } catch (SQLException e) {
            return errorInterno(e);
        }
    }

    /**
     * POST /api/auth/login
     *
     * Respuestas:
     *   200 - "Autenticación satisfactoria"
     *   401 - "Error en la autenticación" (usuario inexistente o contraseña incorrecta;
     *         se devuelve el mismo mensaje para no revelar si el usuario existe)
     *   500 - error interno
     */
    @POST
    @Path("login")
    public Response iniciarSesion(Credenciales datos) {
        // Si faltan datos, la autenticación no puede ser correcta
        if (datos == null || estaVacio(datos.getUsuario()) || estaVacio(datos.getContrasena())) {
            return responder(Response.Status.UNAUTHORIZED, MSG_LOGIN_ERROR);
        }
        String usuario = normalizar(datos.getUsuario());

        try {
            // 1. Buscar el hash y la sal guardados para ese usuario
            String[] guardado = dao.buscarCredenciales(usuario);

            if (guardado == null) {
                // El usuario no existe. Se calcula igual un hash para que la respuesta
                // tarde parecido y no se pueda descubrir qué usuarios existen.
                ClaveUtil.hashear(datos.getContrasena(), ClaveUtil.generarSalt());
                return responder(Response.Status.UNAUTHORIZED, MSG_LOGIN_ERROR);
            }

            // 2. Comparar la contraseña recibida con la guardada
            if (ClaveUtil.coincide(datos.getContrasena(), guardado[1], guardado[0])) {
                return responder(Response.Status.OK, MSG_LOGIN_OK);
            }
            return responder(Response.Status.UNAUTHORIZED, MSG_LOGIN_ERROR);

        } catch (SQLException e) {
            return errorInterno(e);
        }
    }

    // ------------------------------------------------------------------
    // Métodos de apoyo
    // ------------------------------------------------------------------

    /** El usuario se guarda sin espacios sobrantes y en minúsculas ("Ana" y "ana" son el mismo). */
    private String normalizar(String usuario) {
        return usuario.trim().toLowerCase(Locale.ROOT);
    }

    private boolean estaVacio(String texto) {
        return texto == null || texto.trim().isEmpty();
    }

    /** Construye una respuesta con el código HTTP y el mensaje en JSON. */
    private Response responder(Response.Status estado, String mensaje) {
        return Response.status(estado).entity(new Mensaje(mensaje)).build();
    }

    /** Registra el error real en el log del servidor, pero al cliente solo le da un mensaje genérico. */
    private Response errorInterno(SQLException e) {
        LOG.log(Level.SEVERE, "Error de base de datos", e);
        return responder(Response.Status.INTERNAL_SERVER_ERROR, "Error interno del servidor");
    }
}

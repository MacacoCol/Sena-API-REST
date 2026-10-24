# Servicio web de registro e inicio de sesión

Evidencia GA7-220501096-AA5-EV01 – Diseño y desarrollo de servicios web (SENA, ADSO).
Autor: Santiago García Castañeda.

Servicio web REST en Java que registra usuarios y valida su inicio de sesión:

- Si el usuario y la contraseña son correctos, responde **"Autenticación satisfactoria"**.
- Si no, responde **"Error en la autenticación"**.

## Tecnologías

| Herramienta | Uso |
| --- | --- |
| Java 17 o superior | Lenguaje |
| Maven | Gestión de librerías y construcción |
| Jakarta REST (Jersey) | Creación de la API |
| Apache Tomcat 10.1 | Servidor donde se despliega |
| MySQL | Base de datos |
| Git | Control de versiones |

## Estructura del proyecto

```
servicio-autenticacion/
├── pom.xml                      Librerías del proyecto
├── sql/crear_base.sql           Script de la base de datos
├── src/main/java/.../
│   ├── ConfigRest.java          Ruta base de la API (/api)
│   ├── config/ConexionBD.java   Conexión JDBC
│   ├── modelo/                  Credenciales (entrada) y Mensaje (salida)
│   ├── seguridad/ClaveUtil.java Hash de contraseñas (PBKDF2 + sal)
│   ├── dao/UsuarioDAO.java      Consultas SQL
│   └── recurso/AuthResource.java Servicios de registro y login
├── src/main/webapp/index.html   Página sencilla para probar el servicio
└── src/test/java/...            Pruebas automáticas
```

## Cómo ejecutarlo

1. **Crear la base de datos.** En MySQL ejecutar el script `sql/crear_base.sql`.
2. **Configurar la conexión.** Por defecto usa `root` sin contraseña en `localhost:3306`.
   Para cambiarlo, defina las variables de entorno `DB_URL`, `DB_USER` y `DB_PASS`
   (o las propiedades `-Ddb.url`, `-Ddb.user`, `-Ddb.pass`).
3. **Abrir en NetBeans:** *File > Open Project* y elegir la carpeta `servicio-autenticacion`.
4. **Ejecutar:** clic derecho sobre el proyecto > *Run*, con Apache Tomcat 10.1 como servidor.
   También se puede construir con `mvn clean package` y copiar `target/servicio-autenticacion.war`
   a la carpeta `webapps` de Tomcat.
5. **Probar:** abrir `http://localhost:8080/servicio-autenticacion/` (página de prueba)
   o usar Postman / curl.

## Servicios

Ruta base: `http://localhost:8080/servicio-autenticacion/api`

| Método | Ruta | Función |
| --- | --- | --- |
| POST | /auth/registro | Crea un usuario nuevo |
| POST | /auth/login | Valida usuario y contraseña |

Cuerpo de las dos peticiones (JSON):

```json
{ "usuario": "santiago", "contrasena": "clave123" }
```

Respuestas:

| Caso | Código | Mensaje |
| --- | --- | --- |
| Registro correcto | 201 | Usuario registrado correctamente |
| Usuario ya existe | 409 | El usuario ya existe |
| Datos inválidos (vacíos, contraseña de menos de 6 caracteres) | 400 | Mensaje explicando el problema |
| Login correcto | 200 | Autenticación satisfactoria |
| Usuario o contraseña incorrectos | 401 | Error en la autenticación |

Ejemplos con curl:

```bash
curl -X POST http://localhost:8080/servicio-autenticacion/api/auth/registro \
     -H "Content-Type: application/json" \
     -d '{"usuario":"santiago","contrasena":"clave123"}'

curl -X POST http://localhost:8080/servicio-autenticacion/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"usuario":"santiago","contrasena":"clave123"}'
```

## Decisiones de seguridad

- La contraseña no se guarda en texto plano: se guarda un hash PBKDF2 con una sal aleatoria por usuario.
- Las consultas usan `PreparedStatement`, que evita la inyección SQL.
- Ante un usuario inexistente o una contraseña incorrecta se responde lo mismo, para no revelar qué usuarios existen.
- Los errores internos se registran en el servidor y al cliente solo le llega un mensaje genérico.

## Pruebas automáticas

`mvn test` levanta el servicio con una base de datos H2 en memoria y envía peticiones HTTP reales
(registro correcto, duplicado, datos inválidos, login correcto e incorrecto, inyección SQL).

## Control de versiones

El proyecto se desarrolló con Git. Para ver el historial: `git log --oneline`.

Para publicarlo en GitHub (el enlace va en `enlace_repositorio.txt`):

```bash
git remote add origin https://github.com/TU_USUARIO/servicio-autenticacion.git
git push -u origin main
```

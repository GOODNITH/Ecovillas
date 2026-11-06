/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Usuario
 * Programado por : Harol Stiven Holguín Ospina
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-USU-01  Como Administrador quiero crear un usuario para que el habitante ingrese con los permisos de su rol
 * HU-USU-02  Como Administrador quiero consultar un usuario para verificar sus datos y estado
 * RF-001  Crear usuarios con estado inicial Activo, correo único y válido, contraseña mínimo 8 y nombres solo letras
 * RF-002  Consultar un usuario sin mostrar su contraseña
 * RN-04  El correo es único y de formato válido; la contraseña tiene mínimo 8 caracteres
 * RN-01  Cada usuario tiene un único rol
 * -----------------------------------------------------------
 * Criterios: CA-USU-01-01 (bueno) CA-USU-01-02 (falla) CA-USU-01-03 (falla)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

// Entidad Usuario: los atributos se llaman como en el diccionario de datos.
public class Usuario {
    int id_usuario; // Entero, único
    String nombres; // Texto 80, obligatorio, solo letras y espacios
    String apellidos; // Texto 80, obligatorio, solo letras y espacios
    String correo; // Texto 100, obligatorio, formato válido, único
    String telefono; // Texto 15, opcional ("" si no se registra)
    String contrasena; // Texto 255, almacenada como hash (SHA-256)
    String estado; // "Activo" | "Inactivo" (por defecto Activo)
    int id_rol; // Entero, FK a Rol
    int id_apartamento; // Entero, FK a Apartamento (de él se obtiene la torre)

    Usuario(int id_usuario, String nombres, String apellidos, String correo, String telefono,
            String contrasena, String estado, int id_rol, int id_apartamento) {
        this.id_usuario = id_usuario;
        this.nombres = nombres;
        this.apellidos = apellidos;
        this.correo = correo;
        this.telefono = telefono;
        this.contrasena = contrasena;
        this.estado = estado;
        this.id_rol = id_rol;
        this.id_apartamento = id_apartamento;
    }

    // RF-002 · CA-USU-02-01 — muestra todos los datos del usuario excepto la
    // contraseña.
    void mostrar() {
        System.out.println("  id_usuario     : " + id_usuario);
        System.out.println("  nombres        : " + nombres);
        System.out.println("  apellidos      : " + apellidos);
        System.out.println("  correo         : " + correo);
        System.out.println("  telefono       : " + (telefono.isEmpty() ? "(sin teléfono)" : telefono));
        System.out.println("  estado         : " + estado);
        System.out.println("  id_rol         : " + id_rol);
        System.out.println("  id_apartamento : " + id_apartamento);
    }
}

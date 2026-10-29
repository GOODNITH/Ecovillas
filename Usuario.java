/* ===========================================================
 * Proyecto       : EcoVillas
 * Entidad        : Usuario
 * Programado por : harol  holguin - yeider solar
 * Fecha          : 29/10/2026
 * -----------------------------------------------------------
 * Representa a los residentes registrados en EcoVillas.
 * HU relacionadas: HU-03 y HU-04.
 * RF relacionados: RF-06, RF-07, RF-08 y RF-09.
 * Estructura      : clase de objetos Usuario.
 * ===========================================================
 */

public class Usuario {

    private int id_usuario;
    private String nombre;
    private String apellido;
    private String telefono;
    private String correo;

    // Constructor
    public Usuario(int id_usuario, String nombre, String apellido,
            String telefono, String correo) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.telefono = telefono;
        this.correo = correo;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void mostrarInformacion() {
        System.out.println("ID: " + id_usuario);
        System.out.println("Nombre: " + nombre);
        System.out.println("Apellido: " + apellido);
        System.out.println("Teléfono: " + telefono);
        System.out.println("Correo: " + correo);
    }
}

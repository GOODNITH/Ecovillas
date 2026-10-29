
/*
 * Proyecto: EcoVillas
 * Entidad: Inscripciones_jornada
 * Programador: harol holguin - yeider solar
 * Fecha: 29/10/2026
 *
 * Historia de usuario: HU-03
 * Requisitos funcionales: RF-06 y RF-07
 * Estructura: Clase para registrar la inscripción de un usuario
 * a una jornada de limpieza.
 */

public class Inscripciones_jornada {

    private int id_inscripcion;
    private int id_usuario;
    private int id_jornada;

    // Constructor de la clase.
    public Inscripciones_jornada(int id_inscripcion, int id_usuario,
            int id_jornada) {
        this.id_inscripcion = id_inscripcion;
        this.id_usuario = id_usuario;
        this.id_jornada = id_jornada;
    }

    // Consultar el identificador de la inscripción.
    public int getId_inscripcion() {
        return id_inscripcion;
    }

    // Consultar el identificador del usuario.
    public int getId_usuario() {
        return id_usuario;
    }

    // Actualizar el usuario inscrito.
    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    // Consultar el identificador de la jornada.
    public int getId_jornada() {
        return id_jornada;
    }

    // Actualizar la jornada de la inscripción.
    public void setId_jornada(int id_jornada) {
        this.id_jornada = id_jornada;
    }

    // Mostrar los datos de la inscripción.
    public void mostrarInformacion() {
        System.out.println("ID de inscripción: " + id_inscripcion);
        System.out.println("ID del usuario: " + id_usuario);
        System.out.println("ID de la jornada: " + id_jornada);
    }
}

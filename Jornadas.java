
/*
 * Proyecto: EcoVillas
 * Entidad: Jornadas
 * Programador: Harol Stiven Holguín Ospina
 * Fecha: 29/10/2026
 *
 * Historia de usuario: HU-01 y HU-02
 * Requisitos funcionales: RF-01, RF-02, RF-03, RF-04 y RF-05
 * Estructura: Clase para crear, consultar y almacenar información
 * de las jornadas de limpieza del conjunto residencial.
 */

public class Jornadas {

    private int id_jornada;
    private String fecha;
    private String hora;
    private String zona;
    private String materiales;
    private String responsable;

    public Jornadas(int id_jornada, String fecha, String hora,
            String zona, String materiales, String responsable) {

        this.id_jornada = id_jornada;
        this.fecha = fecha;
        this.hora = hora;
        this.zona = zona;
        this.materiales = materiales;
        this.responsable = responsable;
    }

    // Consultar el identificador de la jornada.
    public int getId_jornada() {
        return id_jornada;
    }

    // Consultar la fecha de la jornada.
    public String getFecha() {
        return fecha;
    }

    // Actualizar la fecha de la jornada.
    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    // Consultar la hora de la jornada.
    public String getHora() {
        return hora;
    }

    // Actualizar la hora de la jornada.
    public void setHora(String hora) {
        this.hora = hora;
    }

    // Consultar la zona donde se realizará la jornada.
    public String getZona() {
        return zona;
    }

    // Actualizar la zona.
    public void setZona(String zona) {
        this.zona = zona;
    }

    // Consultar los materiales necesarios.
    public String getMateriales() {
        return materiales;
    }

    // Actualizar los materiales.
    public void setMateriales(String materiales) {
        this.materiales = materiales;
    }

    // Consultar el responsable de la jornada.
    public String getResponsable() {
        return responsable;
    }

    // Actualizar el responsable.
    public void setResponsable(String responsable) {
        this.responsable = responsable;
    }

    // Mostrar toda la información de la jornada.
    public void mostrarInformacion() {
        System.out.println("ID de la jornada: " + id_jornada);
        System.out.println("Fecha: " + fecha);
        System.out.println("Hora: " + hora);
        System.out.println("Zona: " + zona);
        System.out.println("Materiales: " + materiales);
        System.out.println("Responsable: " + responsable);
    }
}


/*
 * Proyecto: EcoVillas
 * Entidad: Zona_Comun
 * Programador:  harol holguin - yeider solar
 * Fecha: 29/10/2026
 *
 * Historias de usuario: HU-05 y HU-06
 * Requisitos funcionales: RF-10, RF-11, RF-12, RF-13 y RF-14
 * Estructura: Clase para almacenar y consultar el estado de una zona común.
 */

public class Zona_Comun {

    private int id_estado_zona;
    private String zona;
    private String estado;
    private String nivel_ocupacion;
    private String foto;
    private String fecha_revision;
    private int id_administrador;
    private String fecha_ultima_recoleccion;

    // Constructor de la clase.
    public Zona_Comun(int id_estado_zona, String zona, String estado,
            String nivel_ocupacion, String foto,
            String fecha_revision, int id_administrador,
            String fecha_ultima_recoleccion) {

        this.id_estado_zona = id_estado_zona;
        this.zona = zona;
        this.estado = estado;
        this.nivel_ocupacion = nivel_ocupacion;
        this.foto = foto;
        this.fecha_revision = fecha_revision;
        this.id_administrador = id_administrador;
        this.fecha_ultima_recoleccion = fecha_ultima_recoleccion;
    }

    // Consultar el identificador del estado de la zona.
    public int getId_estado_zona() {
        return id_estado_zona;
    }

    // Consultar el nombre de la zona.
    public String getZona() {
        return zona;
    }

    // Actualizar el nombre de la zona.
    public void setZona(String zona) {
        this.zona = zona;
    }

    // Consultar el estado de la zona.
    public String getEstado() {
        return estado;
    }

    // Actualizar el estado con un valor permitido.
    public void setEstado(String estado) {
        if (estado.equals("Limpia")
                || estado.equals("En proceso")
                || estado.equals("Dañada")) {
            this.estado = estado;
        } else {
            System.out.println("Estado no válido.");
        }
    }

    // Consultar el nivel de ocupación.
    public String getNivel_ocupacion() {
        return nivel_ocupacion;
    }

    // Actualizar el nivel de ocupación.
    public void setNivel_ocupacion(String nivel_ocupacion) {
        if (nivel_ocupacion.equals("Bajo")
                || nivel_ocupacion.equals("Medio")
                || nivel_ocupacion.equals("Alto")) {
            this.nivel_ocupacion = nivel_ocupacion;
        } else {
            System.out.println("Nivel de ocupación no válido.");
        }
    }

    // Consultar la fotografía.
    public String getFoto() {
        return foto;
    }

    // Actualizar la fotografía.
    public void setFoto(String foto) {
        this.foto = foto;
    }

    // Consultar la fecha de revisión.
    public String getFecha_revision() {
        return fecha_revision;
    }

    // Actualizar la fecha de revisión.
    public void setFecha_revision(String fecha_revision) {
        this.fecha_revision = fecha_revision;
    }

    // Consultar el administrador responsable.
    public int getId_administrador() {
        return id_administrador;
    }

    // Actualizar el administrador responsable.
    public void setId_administrador(int id_administrador) {
        this.id_administrador = id_administrador;
    }

    // Consultar la fecha de la última recolección.
    public String getFecha_ultima_recoleccion() {
        return fecha_ultima_recoleccion;
    }

    // Actualizar la fecha de la última recolección.
    public void setFecha_ultima_recoleccion(
            String fecha_ultima_recoleccion) {
        this.fecha_ultima_recoleccion = fecha_ultima_recoleccion;
    }

    // Mostrar los datos de la zona común.
    public void mostrarInformacion() {
        System.out.println("ID del estado: " + id_estado_zona);
        System.out.println("Zona: " + zona);
        System.out.println("Estado: " + estado);
        System.out.println("Nivel de ocupación: " + nivel_ocupacion);
        System.out.println("Foto: "
                + (foto == null || foto.isEmpty() ? "No registrada" : foto));
        System.out.println("Fecha de revisión: " + fecha_revision);
        System.out.println("ID del administrador: " + id_administrador);
        System.out.println("Última recolección: "
                + (fecha_ultima_recoleccion == null
                        || fecha_ultima_recoleccion.isEmpty()
                                ? "No registrada"
                                : fecha_ultima_recoleccion));
    }
}

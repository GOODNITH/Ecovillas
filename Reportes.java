
/*
 * Proyecto: EcoVillas
 * Entidad: Reportes
 * Programador: harol holguin - yeider solar
 * Fecha: 29/10/2026
 *
 * Historia de usuario: HU-04
 * Requisitos funcionales: RF-08 y RF-09
 * Estructura: Clase para registrar y consultar reportes de incidencias.
 */

public class Reportes {

    private int id_reporte;
    private int id_usuario;
    private String ubicacion;
    private String descripcion;
    private String foto;
    private String fecha_hora;
    private String estado_reporte;

    // Constructor de la clase.
    public Reportes(int id_reporte, int id_usuario, String ubicacion,
            String descripcion, String foto, String fecha_hora,
            String estado_reporte) {

        this.id_reporte = id_reporte;
        this.id_usuario = id_usuario;
        this.ubicacion = ubicacion;
        this.descripcion = descripcion;
        this.foto = foto;
        this.fecha_hora = fecha_hora;
        this.estado_reporte = estado_reporte;
    }

    // Consultar el identificador del reporte.
    public int getId_reporte() {
        return id_reporte;
    }

    // Consultar el usuario que creó el reporte.
    public int getId_usuario() {
        return id_usuario;
    }

    // Actualizar el usuario del reporte.
    public void setId_usuario(int id_usuario) {
        this.id_usuario = id_usuario;
    }

    // Consultar la ubicación.
    public String getUbicacion() {
        return ubicacion;
    }

    // Actualizar la ubicación.
    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    // Consultar la descripción.
    public String getDescripcion() {
        return descripcion;
    }

    // Actualizar la descripción.
    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    // Consultar la fotografía.
    public String getFoto() {
        return foto;
    }

    // Actualizar la fotografía.
    public void setFoto(String foto) {
        this.foto = foto;
    }

    // Consultar la fecha y hora del reporte.
    public String getFecha_hora() {
        return fecha_hora;
    }

    // Actualizar la fecha y hora.
    public void setFecha_hora(String fecha_hora) {
        this.fecha_hora = fecha_hora;
    }

    // Consultar el estado del reporte.
    public String getEstado_reporte() {
        return estado_reporte;
    }

    // Actualizar el estado con un valor permitido.
    public void setEstado_reporte(String estado_reporte) {
        if (estado_reporte.equals("Pendiente")
                || estado_reporte.equals("Solucionado")) {
            this.estado_reporte = estado_reporte;
        } else {
            System.out.println("Estado del reporte no válido.");
        }
    }

    // Mostrar los datos del reporte.
    public void mostrarInformacion() {
        System.out.println("ID del reporte: " + id_reporte);
        System.out.println("ID del usuario: " + id_usuario);
        System.out.println("Ubicación: " + ubicacion);
        System.out.println("Descripción: " + descripcion);
        System.out.println("Foto: "
                + (foto == null || foto.isEmpty() ? "No adjunta" : foto));
        System.out.println("Fecha y hora: " + fecha_hora);
        System.out.println("Estado: " + estado_reporte);
    }
}

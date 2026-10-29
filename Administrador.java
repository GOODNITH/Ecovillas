
/* ===========================================================
 * Proyecto       : EcoVillas
 * Entidad        : Administradores
 * Programado por : harol holguin - yeider solar
 * Fecha          : 29/10/2026
 * -----------------------------------------------------------
 * Representa a los administradores de las torres.
 * Relación       : Administrador pertenece a un Usuario.
 * Restricción    : El número de torre debe estar entre 1 y 16.
 * Estructura     : clase de objetos Administrador.
 * ===========================================================
 */

public class Administrador {

    private int id_administrador;
    private int id_usuario;
    private int numero_torre;

    public Administrador(int id_administrador, int id_usuario,
            int numero_torre) {
        this.id_administrador = id_administrador;
        this.id_usuario = id_usuario;
        this.numero_torre = numero_torre;
    }

    public int getId_administrador() {
        return id_administrador;
    }

    public int getId_usuario() {
        return id_usuario;
    }

    public int getNumero_torre() {
        return numero_torre;
    }

    public void setNumero_torre(int numero_torre) {
        if (numero_torre >= 1 && numero_torre <= 16) {
            this.numero_torre = numero_torre;
        } else {
            System.out.println(
                    "Error: la torre debe estar entre 1 y 16.");
        }
    }

    public void mostrarInformacion() {
        System.out.println("ID administrador: " + id_administrador);
        System.out.println("ID usuario asociado: " + id_usuario);
        System.out.println("Número de torre: " + numero_torre);
    }
}

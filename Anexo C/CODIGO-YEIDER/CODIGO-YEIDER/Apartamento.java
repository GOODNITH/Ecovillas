/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Apartamento
 * Programado por : Yeider Andrés Solar Montes
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-APT-01  Como Administrador quiero crear un apartamento registrando su número y su torre para asignar a cada habitante el apartamento donde vive
 * HU-APT-02  Como Administrador quiero consultar un apartamento para verificar su número y su torre
 * RF-018  Crear apartamentos validando que no exista otro con el mismo número en la misma torre
 * RF-019  Consultar un apartamento mostrando su número y su torre
 * RN-05  Cada apartamento pertenece a una única torre
 * RN-06  El número puede repetirse entre torres distintas, pero no dentro de la misma
 * -----------------------------------------------------------
 * Criterios: CA-APT-01-01 (bueno) CA-APT-01-03 (falla) CA-APT-02-01 (bueno)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

// Entidad Apartamento: los atributos se llaman como en el diccionario de datos.
public class Apartamento {
    int id_apartamento; // Entero, único
    String numero_apartamento; // Texto 10, obligatorio (Ej: 101, 202A)
    int id_torre; // Entero, FK a Torre (16 torres)

    Apartamento(int id_apartamento, String numero_apartamento, int id_torre) {
        this.id_apartamento = id_apartamento;
        this.numero_apartamento = numero_apartamento;
        this.id_torre = id_torre;
    }

    // RF-019 · CA-APT-02-01 — muestra el número del apartamento y la torre a la que pertenece.
    void mostrar() {
        System.out.println("  id_apartamento     : " + id_apartamento);
        System.out.println("  numero_apartamento : " + numero_apartamento);
        System.out.println("  id_torre           : " + id_torre);
    }
}

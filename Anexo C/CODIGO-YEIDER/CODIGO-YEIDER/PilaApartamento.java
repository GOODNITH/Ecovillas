/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Apartamento
 * Programado por : Yeider Andrés Solar Montes
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-APT-04  Como Administrador quiero eliminar un apartamento que no tiene usuarios ni aportes asociados para mantener el catálogo libre de apartamentos que no existen
 * RF-021  Eliminar apartamentos previa confirmación; la pila guarda el eliminado para poder deshacer
 * RN-08  Un registro eliminado se conserva en la pila hasta que se deshaga o se agote
 * -----------------------------------------------------------
 * Criterios: CA-APT-04-01 (bueno) CA-APT-04-02 (falla) CA-APT-04-03 (falla)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

// Pila propia de Apartamento sobre un arreglo; "cima" marca la posición del último elemento (-1 = vacía).
public class PilaApartamento {
    private Apartamento[] datos;
    private int cima;

    PilaApartamento(int capacidad) {
        datos = new Apartamento[capacidad];
        cima = -1;
    }

    // RF-021 · CA-APT-04-01 — apila un apartamento eliminado; rechaza (false) si la pila está llena.
    boolean apilar(Apartamento a) {
        if (cima == datos.length - 1) return false;
        cima++;
        datos[cima] = a;
        return true;
    }

    // RF-021 · CA-APT-04-01 — saca y devuelve el último apartamento apilado; null si la pila está vacía.
    Apartamento desapilar() {
        if (estaVacia()) return null;
        Apartamento a = datos[cima];
        datos[cima] = null;
        cima--;
        return a;
    }

    // RF-021 — devuelve el último apartamento apilado sin sacarlo; null si la pila está vacía.
    Apartamento cima() {
        if (estaVacia()) return null;
        return datos[cima];
    }

    // RF-021 — indica si no hay nada que deshacer.
    boolean estaVacia() {
        return cima == -1;
    }

    // RF-021 — cantidad de apartamentos apilados.
    int tamano() {
        return cima + 1;
    }
}

/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Usuario
 * Programado por : Harol Stiven Holguín Ospina
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-USU-04  Como Administrador quiero eliminar un usuario para impedir el ingreso de quien ya no vive en el conjunto
 * RF-004  Eliminar usuarios previa confirmación; la pila guarda el eliminado para poder deshacer
 * RN-08  Un registro eliminado se conserva en la pila hasta que se deshaga o se agote
 * -----------------------------------------------------------
 * Criterios: CA-USU-04-01 (bueno) CA-USU-04-02 (falla) CA-USU-04-04 (falla)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

// Pila propia de Usuario sobre un arreglo; "cima" marca la posición del último elemento (-1 = vacía).
public class PilaUsuario {
    private Usuario[] datos;
    private int cima;

    PilaUsuario(int capacidad) {
        datos = new Usuario[capacidad];
        cima = -1;
    }

    // RF-004 · CA-USU-04-01 — apila un usuario eliminado; rechaza (false) si la pila está llena.
    boolean apilar(Usuario u) {
        if (cima == datos.length - 1) return false;
        cima++;
        datos[cima] = u;
        return true;
    }

    // RF-004 · CA-USU-04-01 — saca y devuelve el último usuario apilado; null si la pila está vacía.
    Usuario desapilar() {
        if (estaVacia()) return null;
        Usuario u = datos[cima];
        datos[cima] = null;
        cima--;
        return u;
    }

    // RF-004 — devuelve el último usuario apilado sin sacarlo; null si la pila está vacía.
    Usuario cima() {
        if (estaVacia()) return null;
        return datos[cima];
    }

    // RF-004 — indica si no hay nada que deshacer.
    boolean estaVacia() {
        return cima == -1;
    }

    // RF-004 — cantidad de usuarios apilados.
    int tamano() {
        return cima + 1;
    }
}
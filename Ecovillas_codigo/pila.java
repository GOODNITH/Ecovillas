/* ===========================================================
 * Proyecto: EcoVillas | Estructura: Pila
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * Requisitos relacionados: RF-15, RNF-01
 * Criterios relacionados: CA-01.1, CA-03.1, CA-04.1, CA-05.1
 * Métodos: apilar, desapilar, cima, estaVacia, tamaño.
 * =========================================================== */
public class pila<T> {
    private Object[] elementos;
    private int tope;

    public pila() { this(200); }

    public pila(int capacidad) {
        if (capacidad <= 0) throw new IllegalArgumentException("Capacidad inválida");
        elementos = new Object[capacidad];
        tope = -1;
    }

    public boolean apilar(T elemento) {
        if (elemento == null || tope == elementos.length - 1) return false;
        elementos[++tope] = elemento;
        return true;
    }

    @SuppressWarnings("unchecked")
    public T desapilar() {
        if (estaVacia()) return null;
        T valor = (T) elementos[tope];
        elementos[tope--] = null;
        return valor;
    }

    @SuppressWarnings("unchecked")
    public T cima() {
        if (estaVacia()) return null;
        return (T) elementos[tope];
    }

    public boolean estaVacia() { return tope == -1; }
    public int tamaño() { return tope + 1; }
}

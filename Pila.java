
/*
 * Proyecto: EcoVillas
 * Clase: Pila
 * Programador: Harol Stiven Holguín Ospina
 * Fecha: 29/10/2026
 *
 * Requisito: Deshacer la última eliminación.
 * Estructura: Arreglo implementado manualmente.
 * Funcionamiento: LIFO (último en entrar, primero en salir).
 */

public class Pila {

    private Object[] elementos;
    private int cima;
    private int capacidad;

    // Crear la pila con una capacidad determinada.
    public Pila(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Object[capacidad];
        this.cima = -1;
    }

    // Verificar si la pila está vacía.
    public boolean estaVacia() {
        return cima == -1;
    }

    // Verificar si la pila está llena.
    public boolean estaLlena() {
        return cima == capacidad - 1;
    }

    // Insertar un elemento en la cima de la pila.
    public boolean apilar(Object elemento) {
        if (estaLlena()) {
            System.out.println("La pila está llena.");
            return false;
        }

        cima++;
        elementos[cima] = elemento;
        return true;
    }

    // Retirar y devolver el último elemento insertado.
    public Object desapilar() {
        if (estaVacia()) {
            System.out.println("La pila está vacía.");
            return null;
        }

        Object elemento = elementos[cima];
        elementos[cima] = null;
        cima--;

        return elemento;
    }

    // Consultar el elemento que está en la cima sin retirarlo.
    public Object verCima() {
        if (estaVacia()) {
            return null;
        }

        return elementos[cima];
    }

    // Consultar la cantidad de elementos almacenados.
    public int cantidad() {
        return cima + 1;
    }
}

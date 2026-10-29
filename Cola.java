
/*
 * Proyecto: EcoVillas
 * Clase: Cola
 * Programador: Harol Stiven Holguín Ospina
 * Fecha: 29/10/2026
 *
 * Estructura: Arreglo circular implementado manualmente.
 * Funcionamiento: FIFO (primero en entrar, primero en salir).
 * Uso posible: Gestionar solicitudes pendientes de atención.
 */

public class Cola {

    private Object[] elementos;
    private int frente;
    private int fin;
    private int cantidad;
    private int capacidad;

    // Crear la cola con una capacidad determinada.
    public Cola(int capacidad) {
        this.capacidad = capacidad;
        this.elementos = new Object[capacidad];
        this.frente = 0;
        this.fin = 0;
        this.cantidad = 0;
    }

    // Verificar si la cola está vacía.
    public boolean estaVacia() {
        return cantidad == 0;
    }

    // Verificar si la cola está llena.
    public boolean estaLlena() {
        return cantidad == capacidad;
    }

    // Insertar un elemento al final de la cola.
    public boolean encolar(Object elemento) {
        if (estaLlena()) {
            System.out.println("La cola está llena.");
            return false;
        }

        elementos[fin] = elemento;
        fin = (fin + 1) % capacidad;
        cantidad++;

        return true;
    }

    // Retirar y devolver el primer elemento de la cola.
    public Object desencolar() {
        if (estaVacia()) {
            System.out.println("La cola está vacía.");
            return null;
        }

        Object elemento = elementos[frente];
        elementos[frente] = null;
        frente = (frente + 1) % capacidad;
        cantidad--;

        return elemento;
    }

    // Consultar el primer elemento sin retirarlo.
    public Object verFrente() {
        if (estaVacia()) {
            return null;
        }

        return elementos[frente];
    }

    // Consultar la cantidad de elementos.
    public int cantidad() {
        return cantidad;
    }
}

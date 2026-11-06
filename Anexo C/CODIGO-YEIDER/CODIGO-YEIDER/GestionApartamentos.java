/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Apartamento
 * Programado por : Yeider Andrés Solar Montes
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-APT-01  Como Administrador quiero crear un apartamento para asignar a cada habitante el apartamento donde vive
 * HU-APT-02  Como Administrador quiero consultar un apartamento para verificar su número y su torre
 * HU-APT-03  Como Administrador quiero actualizar el número o la torre de un apartamento para corregir información registrada con error
 * HU-APT-04  Como Administrador quiero eliminar un apartamento sin usuarios ni aportes asociados para mantener el catálogo libre de apartamentos que no existen
 * HU-APT-05  Como Administrador quiero listar los apartamentos para tener una vista general por torre
 * RF-018  Crear apartamentos (número obligatorio de máximo 10 caracteres, torre válida, sin repetir número en la misma torre)
 * RF-019  Consultar un apartamento mostrando su número y su torre
 * RF-020  Actualizar número y torre validando que no coincida con otro apartamento de la misma torre
 * RF-021  Eliminar apartamentos sin usuarios asociados; el eliminado se guarda en una pila para poder deshacer
 * RF-022  Listar apartamentos con filtro por torre
 * RN-05  Cada apartamento pertenece a una única torre
 * RN-06  El número puede repetirse entre torres distintas, pero no dentro de la misma
 * RN-08  Un registro con información relacionada no puede eliminarse; lo eliminado queda en la pila hasta deshacer
 * -----------------------------------------------------------
 * Criterios: CA-APT-01-01 (bueno) CA-APT-01-03 (falla) CA-APT-04-01 (bueno) CA-APT-04-03 (falla) CA-APT-05-03 (falla)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

import java.io.*;
import java.util.Scanner;

public class GestionApartamentos {

    static final int MAX = 1000; // capacidad del arreglo
    static final int TORRES = 16; // el conjunto tiene 16 torres
    static final String ARCHIVO = "apartamentos.txt"; // archivo de datos
    static final String[] RUTAS_USUARIOS = { "../harol-codigo/usuarios.txt", "harol-codigo/usuarios.txt",
            "usuarios.txt" }; // archivo de Usuario (carpeta de Harol), para validar relaciones

    static Apartamento[] apartamentos = new Apartamento[MAX]; // arreglo con contador (siempre ordenado por
                                                              // id_apartamento)
    static int cantidad = 0; // contador de apartamentos registrados
    static PilaApartamento pilaEliminados = new PilaApartamento(MAX); // pila propia para deshacer

    // ---------- utilidades internas ----------

    // Busca la posición de un apartamento por id_apartamento; -1 si no existe.
    static int buscarPosicion(int id_apartamento) {
        for (int i = 0; i < cantidad; i++)
            if (apartamentos[i].id_apartamento == id_apartamento)
                return i;
        return -1;
    }

    // RN-06 — posición del apartamento con ese número en esa torre (ignora
    // mayúsculas) o -1; "excepto" se omite al comparar.
    static int buscarPorNumeroYTorre(String numero, int id_torre, int excepto) {
        for (int i = 0; i < cantidad; i++)
            if (apartamentos[i].id_apartamento != excepto && apartamentos[i].id_torre == id_torre
                    && apartamentos[i].numero_apartamento.equalsIgnoreCase(numero))
                return i;
        return -1;
    }

    // Inserta manteniendo el orden por id_apartamento, desplazando a la derecha
    // (así deshacer devuelve el registro a su sitio).
    static void insertarOrdenado(Apartamento a) {
        int pos = cantidad;
        while (pos > 0 && apartamentos[pos - 1].id_apartamento > a.id_apartamento) {
            apartamentos[pos] = apartamentos[pos - 1];
            pos--;
        }
        apartamentos[pos] = a;
        cantidad++;
    }

    // Siguiente identificador incremental cuando no se digita uno.
    static int siguienteId() {
        return cantidad == 0 ? 1 : apartamentos[cantidad - 1].id_apartamento + 1;
    }

    // Número de apartamento: solo letras y números, sin espacios (Ej: 101, 202A).
    static boolean numeroValido(String n) {
        return n.matches("^[A-Za-z0-9]+$");
    }

    // Busca un archivo de datos del OTRO programa: prueba varias rutas y devuelve
    // la
    // primera que exista (funciona se ejecute desde la carpeta del programa o desde
    // la carpeta que contiene a las dos). Si ninguna existe, devuelve la primera.
    static File buscarArchivo(String... rutas) {
        for (String r : rutas)
            if (new File(r).exists())
                return new File(r);
        return new File(rutas[0]);
    }

    // RN-08 — indica si algún usuario (usuarios.txt, campo 9) usa este
    // id_apartamento. Los aportes aún no existen en el prototipo.
    static boolean tieneUsuarios(int id_apartamento) {
        File f = buscarArchivo(RUTAS_USUARIOS);
        if (!f.exists())
            return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                if (c.length == 9 && c[8].trim().equals(String.valueOf(id_apartamento)))
                    return true;
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer " + f.getPath() + ": " + e.getMessage());
        }
        return false;
    }

    // Valida número y torre (obligatorios, formato, longitud y unicidad por torre);
    // imprime el motivo y devuelve false si algo falla.
    static boolean validarNumeroYTorre(String numero, int id_torre, int id_propio) {
        if (numero.isEmpty()) {
            System.out.println("Falta el campo obligatorio: numero_apartamento");
            return false;
        }
        if (id_torre <= 0) {
            System.out.println("Falta el campo obligatorio: id_torre (número de la torre)");
            return false;
        }
        if (numero.length() > 10) {
            System.out.println("El número no puede superar los 10 caracteres");
            return false;
        }
        if (!numeroValido(numero)) {
            System.out.println("El número solo admite letras y números, sin espacios ni símbolos");
            return false;
        }
        if (id_torre > TORRES) {
            System.out.println("La torre no existe: el conjunto tiene torres de la 1 a la " + TORRES);
            return false;
        }
        if (buscarPorNumeroYTorre(numero, id_torre, id_propio) != -1) {
            System.out.println("El apartamento ya está registrado en esa torre");
            return false;
        }
        return true;
    }

    // ---------- las siete operaciones ----------

    // RF-018 · CA-APT-01-01 — crea el apartamento. Rechaza id repetido, campos
    // vacíos (CA-APT-01-02), número repetido en la misma torre (CA-APT-01-03) y
    // número de más de 10 caracteres (CA-APT-01-05). El mismo número en otra torre
    // es válido (CA-APT-01-04).
    static boolean crearApartamento(int id_apartamento, String numero_apartamento, int id_torre) {
        numero_apartamento = numero_apartamento.trim().toUpperCase();
        if (cantidad == MAX) {
            System.out.println("No hay espacio: el arreglo está lleno");
            return false;
        }
        if (id_apartamento <= 0) {
            System.out.println("El id_apartamento debe ser un entero mayor a cero");
            return false;
        }
        if (buscarPosicion(id_apartamento) != -1) {
            System.out.println("El id_apartamento " + id_apartamento + " ya existe");
            return false;
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, -1))
            return false;
        insertarOrdenado(new Apartamento(id_apartamento, numero_apartamento, id_torre));
        System.out.println("Apartamento creado correctamente");
        return true;
    }

    // RF-019 · CA-APT-02-01 — devuelve el apartamento con ese id; CA-APT-02-02 —
    // null si no existe.
    static Apartamento consultarApartamento(int id_apartamento) {
        int pos = buscarPosicion(id_apartamento);
        if (pos == -1) {
            System.out.println("No se encontró el apartamento");
            return null;
        }
        return apartamentos[pos];
    }

    // RF-020 · CA-APT-03-01 — actualiza número y torre. Rechaza si no existe,
    // número ya existente en la misma torre (CA-APT-03-02) o campos obligatorios
    // vacíos (CA-APT-03-03). Los usuarios del apartamento se muestran con la nueva
    // torre porque su torre se obtiene del apartamento (CA-APT-03-04).
    static boolean actualizarApartamento(int id_apartamento, String numero_apartamento, int id_torre) {
        numero_apartamento = numero_apartamento.trim().toUpperCase();
        int pos = buscarPosicion(id_apartamento);
        if (pos == -1) {
            System.out.println("No se encontró el apartamento");
            return false;
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, id_apartamento))
            return false;
        apartamentos[pos].numero_apartamento = numero_apartamento;
        apartamentos[pos].id_torre = id_torre;
        System.out.println("Apartamento actualizado correctamente");
        return true;
    }

    // RF-021 · CA-APT-04-01 — elimina el apartamento desplazando los siguientes
    // una posición a la izquierda (sin hueco) y lo apila para poder deshacer.
    // Rechaza si no existe, si tiene usuarios asociados (CA-APT-04-03) o si la pila
    // está llena.
    static Apartamento eliminarApartamento(int id_apartamento) {
        int pos = buscarPosicion(id_apartamento);
        if (pos == -1) {
            System.out.println("No se encontró el apartamento");
            return null;
        }
        if (tieneUsuarios(id_apartamento)) {
            System.out.println("No se puede eliminar un apartamento con usuarios o aportes asociados");
            return null;
        }
        Apartamento eliminado = apartamentos[pos];
        if (!pilaEliminados.apilar(eliminado)) {
            System.out.println("No se puede eliminar: la pila de deshacer está llena");
            return null;
        }
        for (int i = pos; i < cantidad - 1; i++)
            apartamentos[i] = apartamentos[i + 1]; // desplaza, no deja null en el medio
        apartamentos[cantidad - 1] = null;
        cantidad--;
        System.out.println("Apartamento eliminado correctamente");
        return eliminado;
    }

    // RF-022 · CA-APT-05-01 — lista número y torre; el filtro por torre
    // (CA-APT-05-02) es 0 para mostrar todos. CA-APT-05-03 — si no hay
    // apartamentos muestra "Aún no hay apartamentos registrados" en lugar de una
    // lista vacía.
    static void listarApartamentos(int filtroTorre) {
        if (cantidad == 0) {
            System.out.println("Aún no hay apartamentos registrados");
            return;
        }
        System.out.printf("%-6s %-12s %-6s%n", "ID", "Apartamento", "Torre");
        int mostrados = 0;
        for (int i = 0; i < cantidad; i++) {
            Apartamento a = apartamentos[i];
            if (filtroTorre > 0 && a.id_torre != filtroTorre)
                continue;
            System.out.printf("%-6d %-12s %-6d%n", a.id_apartamento, a.numero_apartamento, a.id_torre);
            mostrados++;
        }
        if (mostrados == 0)
            System.out.println("No hay apartamentos en la torre " + filtroTorre);
    }

    // RF-021 · CA-APT-04-01 — deshace la última eliminación: devuelve a su sitio el
    // MISMO apartamento que salió de la pila. Rechaza si no hay nada que deshacer
    // o si su id o su número en esa torre ya fueron tomados por otro registro.
    static boolean deshacer() {
        Apartamento a = pilaEliminados.cima();
        if (a == null) {
            System.out.println("No hay nada que deshacer");
            return false;
        }
        if (cantidad == MAX) {
            System.out.println("No hay espacio: el arreglo está lleno");
            return false;
        }
        if (buscarPosicion(a.id_apartamento) != -1
                || buscarPorNumeroYTorre(a.numero_apartamento, a.id_torre, -1) != -1) {
            System.out.println("No se puede deshacer: el id o el número en esa torre ya los usa otro apartamento");
            return false;
        }
        pilaEliminados.desapilar();
        insertarOrdenado(a);
        System.out.println("Se restauró el apartamento " + a.id_apartamento + " (" + a.numero_apartamento + ", torre "
                + a.id_torre + ")");
        return true;
    }

    // RF-018 a RF-022 — guarda todos los apartamentos en el archivo de datos para
    // que sobrevivan al cierre del programa.
    static void guardarArchivo() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (int i = 0; i < cantidad; i++) {
                Apartamento a = apartamentos[i];
                pw.println(a.id_apartamento + ";" + a.numero_apartamento + ";" + a.id_torre);
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    // RF-018 a RF-022 — carga los apartamentos del archivo al iniciar; si no
    // existe, arranca vacío.
    static void cargarArchivo() {
        File f = new File(ARCHIVO);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null && cantidad < MAX) {
                String[] c = linea.split(";", -1);
                if (c.length != 3)
                    continue;
                insertarOrdenado(new Apartamento(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[2])));
            }
        } catch (Exception e) {
            System.out.println("No se pudo leer el archivo: " + e.getMessage());
        }
    }

    // ---------- menú ----------

    static Scanner sc = new Scanner(System.in);

    // Lee un entero; devuelve 0 si lo digitado no es número (los validadores lo
    // rechazan).
    static int leerEntero(String pregunta) {
        System.out.print(pregunta);
        try {
            return Integer.parseInt(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    static String leerTexto(String pregunta) {
        System.out.print(pregunta);
        return sc.nextLine();
    }

    public static void main(String[] args) {
        cargarArchivo();
        int op;
        do {
            System.out.println("\n=== Villas del Socorro · Apartamentos (" + cantidad + " registrados) ===");
            System.out.println("1. Crear apartamento");
            System.out.println("2. Consultar apartamento");
            System.out.println("3. Actualizar apartamento");
            System.out.println("4. Eliminar apartamento");
            System.out.println("5. Listar apartamentos");
            System.out.println("6. Deshacer última eliminación");
            System.out.println("0. Guardar y salir");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: {
                    String txt = leerTexto("id_apartamento (Enter = automático): ").trim();
                    int id = txt.isEmpty() ? siguienteId() : -1;
                    if (id == -1) {
                        try {
                            id = Integer.parseInt(txt);
                        } catch (NumberFormatException e) {
                            id = 0;
                        }
                    }
                    String num = leerTexto("numero_apartamento: ");
                    int torre = leerEntero("id_torre (1 a " + TORRES + "): ");
                    if (crearApartamento(id, num, torre))
                        guardarArchivo();
                    break;
                }
                case 2: {
                    Apartamento a = consultarApartamento(leerEntero("id_apartamento: "));
                    if (a != null)
                        a.mostrar();
                    break;
                }
                case 3: {
                    int id = leerEntero("id_apartamento a actualizar: ");
                    if (consultarApartamento(id) == null)
                        break;
                    String num = leerTexto("nuevo numero_apartamento: ");
                    int torre = leerEntero("nuevo id_torre (1 a " + TORRES + "): ");
                    if (actualizarApartamento(id, num, torre))
                        guardarArchivo();
                    break;
                }
                case 4: {
                    int id = leerEntero("id_apartamento a eliminar: ");
                    if (consultarApartamento(id) == null)
                        break;
                    String r = leerTexto("¿Confirma eliminar? (s/n): ").trim();
                    if (r.equalsIgnoreCase("s")) {
                        if (eliminarApartamento(id) != null)
                            guardarArchivo();
                    } else
                        System.out.println("Operación cancelada, no se hizo ningún cambio");
                    break;
                }
                case 5: {
                    String f = leerTexto("Filtrar por torre (número, Enter = todas): ").trim();
                    int torre = 0;
                    if (!f.isEmpty()) {
                        try {
                            torre = Integer.parseInt(f);
                        } catch (NumberFormatException e) {
                            torre = 0;
                        }
                        if (torre <= 0) {
                            System.out.println("La torre debe ser un número mayor a cero");
                            break;
                        }
                    }
                    listarApartamentos(torre);
                    break;
                }
                case 6:
                    if (deshacer())
                        guardarArchivo();
                    break;
                case 0:
                    guardarArchivo();
                    System.out.println("Datos guardados en " + ARCHIVO + ". Hasta luego.");
                    break;
                default:
                    System.out.println("Opción no válida");
            }
        } while (op != 0);
    }
}
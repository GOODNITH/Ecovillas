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

import java.io.*; // Importa clases para trabajar con archivos y lectura/escritura de datos.
import java.util.Scanner; // Importa Scanner para leer lo que el usuario escribe por consola.

public class GestionApartamentos { // Declara la clase principal que administra los apartamentos.

    static final int MAX = 1000; // capacidad del arreglo Explicación: Asigna un valor o actualiza una variable.
    static final int TORRES = 16; // el conjunto tiene 16 torres Explicación: Asigna un valor o actualiza una variable.
    static final String ARCHIVO = "apartamentos.txt"; // archivo de datos Explicación: Asigna un valor o actualiza una variable.
    static final String[] RUTAS_USUARIOS = { "../harol-codigo/usuarios.txt", "harol-codigo/usuarios.txt", // Asigna un valor o actualiza una variable.
            "usuarios.txt" }; // archivo de Usuario (carpeta de Harol), para validar relaciones // Última ruta alternativa que se intenta encontrar.

    static Apartamento[] apartamentos = new Apartamento[MAX]; // arreglo con contador (siempre ordenado por Explicación: Asigna un valor o actualiza una variable.
                                                              // id_apartamento)
    static int cantidad = 0; // contador de apartamentos registrados Explicación: Asigna un valor o actualiza una variable.
    static PilaApartamento pilaEliminados = new PilaApartamento(MAX); // pila propia para deshacer Explicación: Asigna un valor o actualiza una variable.

    // ---------- utilidades internas ----------

    // Busca la posición de un apartamento por id_apartamento; -1 si no existe.
    static int buscarPosicion(int id_apartamento) { // Instrucción del programa según la operación actual.
        for (int i = 0; i < cantidad; i++) // Recorre únicamente las posiciones ocupadas del arreglo, desde 0 hasta cantidad - 1.
            if (apartamentos[i].id_apartamento == id_apartamento) // Compara el ID del elemento actual con el ID que se está buscando.
                return i; // Devuelve inmediatamente la posición cuando encuentra coincidencia.
        return -1; // Informa que terminó la búsqueda sin encontrar el apartamento.
    }

    // RN-06 — posición del apartamento con ese número en esa torre (ignora
    // mayúsculas) o -1; "excepto" se omite al comparar.
    static int buscarPorNumeroYTorre(String numero, int id_torre, int excepto) { // Instrucción del programa según la operación actual.
        for (int i = 0; i < cantidad; i++) // Recorre únicamente las posiciones ocupadas del arreglo, desde 0 hasta cantidad - 1.
            if (apartamentos[i].id_apartamento != excepto && apartamentos[i].id_torre == id_torre // Exige que no sea el registro excluido y que pertenezca a la torre indicada.
                    && apartamentos[i].numero_apartamento.equalsIgnoreCase(numero)) // Compara el número sin distinguir mayúsculas y minúsculas.
                return i; // Devuelve inmediatamente la posición cuando encuentra coincidencia.
        return -1; // Informa que terminó la búsqueda sin encontrar el apartamento.
    }

    // Inserta manteniendo el orden por id_apartamento, desplazando a la derecha
    // (así deshacer devuelve el registro a su sitio).
    static void insertarOrdenado(Apartamento a) { // Instrucción del programa según la operación actual.
        int pos = cantidad; // Empieza en la primera posición libre del arreglo.
        while (pos > 0 && apartamentos[pos - 1].id_apartamento > a.id_apartamento) { // Mientras haya elementos a la izquierda con ID mayor, los desplaza a la derecha.
            apartamentos[pos] = apartamentos[pos - 1]; // Copia el elemento anterior a la posición siguiente para abrir espacio.
            pos--; // Retrocede una posición para seguir buscando dónde insertar.
        }
        apartamentos[pos] = a; // Coloca el nuevo apartamento en la posición correcta.
        cantidad++; // Aumenta el contador porque ahora hay un registro más.
    }

    // Siguiente identificador incremental cuando no se digita uno.
    static int siguienteId() { // Instrucción del programa según la operación actual.
        return cantidad == 0 ? 1 : apartamentos[cantidad - 1].id_apartamento + 1; // Si no hay registros inicia en 1; si hay, suma uno al último ID.
    }

    // Número de apartamento: solo letras y números, sin espacios (Ej: 101, 202A).
    static boolean numeroValido(String n) { // Instrucción del programa según la operación actual.
        return n.matches("^[A-Za-z0-9]+$"); // La expresión regular permite únicamente letras y números, sin espacios ni símbolos.
    }

    // Busca un archivo de datos del OTRO programa: prueba varias rutas y devuelve
    // la
    // primera que exista (funciona se ejecute desde la carpeta del programa o desde
    // la carpeta que contiene a las dos). Si ninguna existe, devuelve la primera.
    static File buscarArchivo(String... rutas) { // Instrucción del programa según la operación actual.
        for (String r : rutas) // Recorre cada ruta candidata recibida en el parámetro rutas.
            if (new File(r).exists()) // Crea una referencia al archivo y comprueba si existe en esa ubicación.
                return new File(r); // Devuelve la primera ruta encontrada; no sigue buscando.
        return new File(rutas[0]); // Si ninguna existe, devuelve la primera ruta para que el programa pueda informar o crearla.
    }

    // RN-08 — indica si algún usuario (usuarios.txt, campo 9) usa este
    // id_apartamento. Los aportes aún no existen en el prototipo.
    static boolean tieneUsuarios(int id_apartamento) { // Instrucción del programa según la operación actual.
        File f = buscarArchivo(RUTAS_USUARIOS); // Busca el archivo de usuarios usando las rutas alternativas definidas.
        if (!f.exists()) // Comprueba si todavía no existe el archivo.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // Abre el archivo para lectura eficiente línea por línea.
            String linea; // Variable temporal que almacena cada línea leída.
            while ((linea = br.readLine()) != null) { // Lee hasta llegar al final del archivo, cuando readLine devuelve null.
                String[] c = linea.split(";", -1); // Separa los campos de la línea usando ; y conserva campos vacíos.
                if (c.length == 9 && c[8].trim().equals(String.valueOf(id_apartamento))) // Comprueba el formato de nueve campos y compara el campo 9 con el ID buscado.
                    return true; // Confirma que existe al menos un usuario asociado al apartamento.
            } // Cierra la selección switch.
        } catch (IOException e) { // Captura errores que ocurran al escribir el archivo.
            System.out.println("No se pudo leer " + f.getPath() + ": " + e.getMessage()); // Muestra la ruta y el motivo del error de lectura.
        }
        return false; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
    }

    // Valida número y torre (obligatorios, formato, longitud y unicidad por torre);
    // imprime el motivo y devuelve false si algo falla.
    static boolean validarNumeroYTorre(String numero, int id_torre, int id_propio) { // Instrucción del programa según la operación actual.
        if (numero.isEmpty()) { // Comprueba que el número obligatorio no esté vacío.
            System.out.println("Falta el campo obligatorio: numero_apartamento"); // Informa cuál dato obligatorio falta.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (id_torre <= 0) { // Rechaza identificadores de torre iguales o menores que cero.
            System.out.println("Falta el campo obligatorio: id_torre (número de la torre)"); // Explica que se debe indicar una torre válida.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (numero.length() > 10) { // Verifica que el número no supere el máximo de diez caracteres.
            System.out.println("El número no puede superar los 10 caracteres"); // Informa el límite permitido.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (!numeroValido(numero)) { // Niega el resultado si el número contiene espacios o símbolos.
            System.out.println("El número solo admite letras y números, sin espacios ni símbolos"); // Informa el formato aceptado.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (id_torre > TORRES) { // Comprueba que la torre no supere el total de 16 torres.
            System.out.println("La torre no existe: el conjunto tiene torres de la 1 a la " + TORRES); // Informa el rango de torres válidas.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (buscarPorNumeroYTorre(numero, id_torre, id_propio) != -1) { // Rechaza duplicados del mismo número dentro de la misma torre.
            System.out.println("El apartamento ya está registrado en esa torre"); // Informa que el número ya está ocupado en esa torre.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        return true; // Todas las validaciones pasaron correctamente.
    }

    // ---------- las siete operaciones ----------

    // RF-018 · CA-APT-01-01 — crea el apartamento. Rechaza id repetido, campos
    // vacíos (CA-APT-01-02), número repetido en la misma torre (CA-APT-01-03) y
    // número de más de 10 caracteres (CA-APT-01-05). El mismo número en otra torre
    // es válido (CA-APT-01-04).
    static boolean crearApartamento(int id_apartamento, String numero_apartamento, int id_torre) { // Instrucción del programa según la operación actual.
        numero_apartamento = numero_apartamento.trim().toUpperCase(); // Quita espacios externos y normaliza el número a mayúsculas.
        if (cantidad == MAX) { // Comprueba que exista espacio para recuperar el apartamento.
            System.out.println("No hay espacio: el arreglo está lleno"); // Informa que no hay posiciones disponibles.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (id_apartamento <= 0) { // El identificador debe ser un entero positivo.
            System.out.println("El id_apartamento debe ser un entero mayor a cero"); // Explica la regla del identificador.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (buscarPosicion(id_apartamento) != -1) { // Comprueba que el ID no esté registrado previamente.
            System.out.println("El id_apartamento " + id_apartamento + " ya existe"); // Informa el ID duplicado.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, -1)) // Ejecuta las validaciones; -1 indica que no se excluye ningún registro.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        insertarOrdenado(new Apartamento(id_apartamento, numero_apartamento, id_torre)); // Crea el objeto y lo inserta manteniendo el orden del arreglo.
        System.out.println("Apartamento creado correctamente"); // Confirma la creación.
        return true; // Todas las validaciones pasaron correctamente.
    }

    // RF-019 · CA-APT-02-01 — devuelve el apartamento con ese id; CA-APT-02-02 —
    // null si no existe.
    static Apartamento consultarApartamento(int id_apartamento) { // Instrucción del programa según la operación actual.
        int pos = buscarPosicion(id_apartamento); // Busca la posición del registro que se modificará.
        if (pos == -1) { // No permite actualizar un apartamento inexistente.
            System.out.println("No se encontró el apartamento"); // Informa que el ID no está registrado.
            return null; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
        }
        return apartamentos[pos]; // Devuelve el objeto ubicado en la posición encontrada.
    }

    // RF-020 · CA-APT-03-01 — actualiza número y torre. Rechaza si no existe,
    // número ya existente en la misma torre (CA-APT-03-02) o campos obligatorios
    // vacíos (CA-APT-03-03). Los usuarios del apartamento se muestran con la nueva
    // torre porque su torre se obtiene del apartamento (CA-APT-03-04).
    static boolean actualizarApartamento(int id_apartamento, String numero_apartamento, int id_torre) { // Instrucción del programa según la operación actual.
        numero_apartamento = numero_apartamento.trim().toUpperCase(); // Quita espacios externos y normaliza el número a mayúsculas.
        int pos = buscarPosicion(id_apartamento); // Busca la posición del registro que se modificará.
        if (pos == -1) { // No permite actualizar un apartamento inexistente.
            System.out.println("No se encontró el apartamento"); // Informa que el ID no está registrado.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, id_apartamento)) // Valida los nuevos datos e ignora el propio ID al comprobar duplicados.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        apartamentos[pos].numero_apartamento = numero_apartamento; // Reemplaza el número del apartamento en el objeto encontrado.
        apartamentos[pos].id_torre = id_torre; // Actualiza la torre del mismo apartamento.
        System.out.println("Apartamento actualizado correctamente"); // Confirma que los cambios se aplicaron.
        return true; // Todas las validaciones pasaron correctamente.
    }

    // RF-021 · CA-APT-04-01 — elimina el apartamento desplazando los siguientes
    // una posición a la izquierda (sin hueco) y lo apila para poder deshacer.
    // Rechaza si no existe, si tiene usuarios asociados (CA-APT-04-03) o si la pila
    // está llena.
    static Apartamento eliminarApartamento(int id_apartamento) { // Instrucción del programa según la operación actual.
        int pos = buscarPosicion(id_apartamento); // Busca la posición del registro que se modificará.
        if (pos == -1) { // No permite actualizar un apartamento inexistente.
            System.out.println("No se encontró el apartamento"); // Informa que el ID no está registrado.
            return null; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
        }
        if (tieneUsuarios(id_apartamento)) { // Impide borrar un apartamento que todavía tenga usuarios relacionados.
            System.out.println("No se puede eliminar un apartamento con usuarios o aportes asociados"); // Explica por qué se bloqueó la eliminación.
            return null; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
        }
        Apartamento eliminado = apartamentos[pos]; // Guarda una referencia al objeto antes de quitarlo del arreglo.
        if (!pilaEliminados.apilar(eliminado)) { // Intenta guardar el objeto en la pila para permitir deshacer; false indica pila llena.
            System.out.println("No se puede eliminar: la pila de deshacer está llena"); // Informa que no se puede conservar el registro eliminado.
            return null; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
        }
        for (int i = pos; i < cantidad - 1; i++) // Recorre desde la posición eliminada hasta el penúltimo elemento.
            apartamentos[i] = apartamentos[i + 1]; // desplaza, no deja null en el medio // Mueve cada elemento siguiente una posición a la izquierda para cerrar el espacio.
        apartamentos[cantidad - 1] = null; // Limpia la última posición que quedó duplicada después del desplazamiento.
        cantidad--; // Reduce el contador porque se eliminó un registro.
        System.out.println("Apartamento eliminado correctamente"); // Confirma la eliminación.
        return eliminado; // Devuelve el resultado al método que hizo la llamada y termina su ejecución.
    }

    // RF-022 · CA-APT-05-01 — lista número y torre; el filtro por torre
    // (CA-APT-05-02) es 0 para mostrar todos. CA-APT-05-03 — si no hay
    // apartamentos muestra "Aún no hay apartamentos registrados" en lugar de una
    // lista vacía.
    static void listarApartamentos(int filtroTorre) { // Instrucción del programa según la operación actual.
        if (cantidad == 0) { // Comprueba si no hay apartamentos registrados.
            System.out.println("Aún no hay apartamentos registrados"); // Informa que el arreglo está vacío.
            return; // Instrucción del programa según la operación actual.
        }
        System.out.printf("%-6s %-12s %-6s%n", "ID", "Apartamento", "Torre"); // Imprime los encabezados en columnas alineadas.
        int mostrados = 0; // Cuenta cuántos apartamentos cumplen el filtro.
        for (int i = 0; i < cantidad; i++) { // Ciclo que repite el bloque recorriendo los elementos indicados.
            Apartamento a = apartamentos[i]; // Obtiene el apartamento de la posición actual.
            if (filtroTorre > 0 && a.id_torre != filtroTorre) // Si hay filtro y la torre no coincide, omite este registro.
                continue; // Salta a la siguiente iteración del for sin imprimir este apartamento.
            System.out.printf("%-6d %-12s %-6d%n", a.id_apartamento, a.numero_apartamento, a.id_torre); // Muestra ID, número y torre en columnas.
            mostrados++; // Suma uno al total de registros mostrados.
        }
        if (mostrados == 0) // Comprueba si ningún registro cumplió el filtro.
            System.out.println("No hay apartamentos en la torre " + filtroTorre); // Informa que la torre seleccionada no tiene apartamentos registrados.
    }

    // RF-021 · CA-APT-04-01 — deshace la última eliminación: devuelve a su sitio el
    // MISMO apartamento que salió de la pila. Rechaza si no hay nada que deshacer
    // o si su id o su número en esa torre ya fueron tomados por otro registro.
    static boolean deshacer() { // Instrucción del programa según la operación actual.
        Apartamento a = pilaEliminados.cima(); // Consulta el elemento de la cima sin retirarlo de la pila.
        if (a == null) { // Si cima devuelve null, no hay elementos para restaurar.
            System.out.println("No hay nada que deshacer"); // Informa que la pila está vacía.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (cantidad == MAX) { // Comprueba que exista espacio para recuperar el apartamento.
            System.out.println("No hay espacio: el arreglo está lleno"); // Informa que no hay posiciones disponibles.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        if (buscarPosicion(a.id_apartamento) != -1 // Comprueba que el ID del eliminado no haya sido usado de nuevo.
                || buscarPorNumeroYTorre(a.numero_apartamento, a.id_torre, -1) != -1) { // También evita duplicar número y torre con otro apartamento.
            System.out.println("No se puede deshacer: el id o el número en esa torre ya los usa otro apartamento"); // Informa el conflicto que impide restaurar.
            return false; // Sin archivo no se encuentra una relación registrada, por eso devuelve false.
        }
        pilaEliminados.desapilar(); // Retira de la pila el objeto que se va a restaurar.
        insertarOrdenado(a); // Devuelve el mismo objeto al arreglo en su posición ordenada.
        System.out.println("Se restauró el apartamento " + a.id_apartamento + " (" + a.numero_apartamento + ", torre " // Muestra los datos del apartamento recuperado.
                + a.id_torre + ")"); // Completa el mensaje con el número de torre.
        return true; // Todas las validaciones pasaron correctamente.
    }

    // RF-018 a RF-022 — guarda todos los apartamentos en el archivo de datos para
    // que sobrevivan al cierre del programa.
    static void guardarArchivo() { // Instrucción del programa según la operación actual.
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) { // Abre el archivo para escritura; FileWriter escribe y PrintWriter facilita imprimir líneas.
            for (int i = 0; i < cantidad; i++) { // Ciclo que repite el bloque recorriendo los elementos indicados.
                Apartamento a = apartamentos[i]; // Obtiene cada apartamento que se va a guardar.
                pw.println(a.id_apartamento + ";" + a.numero_apartamento + ";" + a.id_torre); // Guarda los tres campos separados por punto y coma.
            } // Cierra la selección switch.
        } catch (IOException e) { // Captura errores que ocurran al escribir el archivo.
            System.out.println("No se pudo guardar el archivo: " + e.getMessage()); // Muestra el motivo del error de escritura.
        }
    }

    // RF-018 a RF-022 — carga los apartamentos del archivo al iniciar; si no
    // existe, arranca vacío.
    static void cargarArchivo() { // Instrucción del programa según la operación actual.
        File f = new File(ARCHIVO); // Crea una referencia al archivo de datos.
        if (!f.exists()) // Comprueba si todavía no existe el archivo.
            return; // Instrucción del programa según la operación actual.
        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // Abre el archivo para lectura eficiente línea por línea.
            String linea; // Variable temporal que almacena cada línea leída.
            while ((linea = br.readLine()) != null && cantidad < MAX) { // Continúa mientras haya líneas y el arreglo tenga espacio.
                String[] c = linea.split(";", -1); // Separa los campos de la línea usando ; y conserva campos vacíos.
                if (c.length != 3) // Verifica que la línea tenga exactamente los tres campos esperados.
                    continue; // Ignora la línea mal formada y pasa a la siguiente.
                insertarOrdenado(new Apartamento(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[2]))); // Convierte los números a int, crea el objeto y lo inserta ordenadamente.
            } // Cierra la selección switch.
        } catch (Exception e) { // Captura errores de lectura o conversión de datos.
            System.out.println("No se pudo leer el archivo: " + e.getMessage()); // Informa el motivo por el que no se pudo cargar.
        }
    }

    // ---------- menú ----------

    static Scanner sc = new Scanner(System.in); // Asigna un valor o actualiza una variable.

    // Lee un entero; devuelve 0 si lo digitado no es número (los validadores lo
    // rechazan).
    // Lee exclusivamente la opción principal del menú; devuelve -1 cuando se ingresan letras.
    static int leerOpcionMenu(String pregunta) { // Separa la validación del menú sin modificar leerEntero().
        System.out.print(pregunta); // Muestra el mensaje que solicita la opción.
        try { // Intenta convertir el texto ingresado a un número entero.
            return Integer.parseInt(sc.nextLine().trim()); // Devuelve la opción numérica escrita.
        } catch (NumberFormatException e) { // Se ejecuta si el usuario escribe letras u otro formato no entero.
            return -1; // Valor que no coincide con ningún case y llega al default.
        } // Termina el bloque de captura de error.
    } // Termina el método exclusivo para leer el menú.

    static int leerEntero(String pregunta) { // Se conserva este lector para las entradas numéricas de las demás operaciones.
        System.out.print(pregunta); // Muestra la solicitud sin saltar a otra línea.
        try { // Inicia un bloque protegido para manejar posibles errores.
            return Integer.parseInt(sc.nextLine().trim()); // Lee una línea, quita espacios externos y convierte el contenido a int.
        } catch (NumberFormatException e) { // Captura cuando el texto no representa un número entero.
            return 0; // Devuelve cero como valor de error, según el comportamiento original.
        }
    }

    static String leerTexto(String pregunta) { // Instrucción del programa según la operación actual.
        System.out.print(pregunta); // Muestra la solicitud sin saltar a otra línea.
        return sc.nextLine(); // Lee y devuelve toda la línea ingresada por el usuario.
    }

    public static void main(String[] args) { // Instrucción del programa según la operación actual.
        cargarArchivo(); // Recupera los apartamentos guardados en ejecuciones anteriores.
        int op; // Guarda la opción elegida en el menú.
        do { // Ejecuta el menú al menos una vez y lo repite hasta elegir 0.
            System.out.println("\n=== Villas del Socorro · Apartamentos (" + cantidad + " registrados) ==="); // Muestra el título y la cantidad actual de apartamentos.
            System.out.println("1. Crear apartamento"); // Opción para registrar un apartamento.
            System.out.println("2. Consultar apartamento"); // Opción para buscar un apartamento.
            System.out.println("3. Actualizar apartamento"); // Opción para modificar número o torre.
            System.out.println("4. Eliminar apartamento"); // Opción para eliminar un apartamento permitido.
            System.out.println("5. Listar apartamentos"); // Opción para mostrar todos o filtrar por torre.
            System.out.println("6. Deshacer última eliminación"); // Opción para recuperar el último apartamento eliminado.
            System.out.println("0. Guardar y salir"); // Opción que guarda los datos y termina el ciclo.
            op = leerOpcionMenu("Opción: "); // Asigna un valor o actualiza una variable.
            switch (op) { // Selecciona qué operación ejecutar según el valor de op.
                case 1: { // Ejecuta la creación de un apartamento.
                    String txt = leerTexto("id_apartamento (Enter = automático): ").trim(); // Declara una variable de texto para almacenar datos.
                    int id = txt.isEmpty() ? siguienteId() : -1; // Declara una variable entera para almacenar un número.
                    if (id == -1) { // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        try { // Inicia un bloque protegido para manejar posibles errores.
                            id = Integer.parseInt(txt); // Asigna un valor o actualiza una variable.
                        } catch (NumberFormatException e) { // Captura la excepción indicada para manejar el error.
                            id = 0; // Asigna un valor o actualiza una variable.
                        }
                    }
                    String num = leerTexto("numero_apartamento: "); // Declara una variable de texto para almacenar datos.
                    int torre = leerEntero("id_torre (1 a " + TORRES + "): "); // Declara una variable entera para almacenar un número.
                    if (crearApartamento(id, num, torre)) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        guardarArchivo(); // Instrucción del programa según la operación actual.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                }
                case 2: { // Ejecuta la consulta de un apartamento.
                    Apartamento a = consultarApartamento(leerEntero("id_apartamento: ")); // Asigna un valor o actualiza una variable.
                    if (a != null) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        a.mostrar(); // Instrucción del programa según la operación actual.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                }
                case 3: { // Ejecuta la actualización de un apartamento.
                    int id = leerEntero("id_apartamento a actualizar: "); // Declara una variable entera para almacenar un número.
                    if (consultarApartamento(id) == null) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        break; // Sale inmediatamente del case o ciclo actual.
                    String num = leerTexto("nuevo numero_apartamento: "); // Declara una variable de texto para almacenar datos.
                    int torre = leerEntero("nuevo id_torre (1 a " + TORRES + "): "); // Declara una variable entera para almacenar un número.
                    if (actualizarApartamento(id, num, torre)) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        guardarArchivo(); // Instrucción del programa según la operación actual.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                }
                case 4: { // Ejecuta la eliminación con confirmación.
                    int id = leerEntero("id_apartamento a eliminar: "); // Declara una variable entera para almacenar un número.
                    if (consultarApartamento(id) == null) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        break; // Sale inmediatamente del case o ciclo actual.
                    String r = leerTexto("¿Confirma eliminar? (s/n): ").trim(); // Declara una variable de texto para almacenar datos.
                    if (r.equalsIgnoreCase("s")) { // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        if (eliminarApartamento(id) != null) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                            guardarArchivo(); // Instrucción del programa según la operación actual.
                    } else // Instrucción del programa según la operación actual.
                        System.out.println("Operación cancelada, no se hizo ningún cambio"); // Muestra un mensaje en la consola y termina la línea.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                }
                case 5: { // Ejecuta el listado general o filtrado.
                    String f = leerTexto("Filtrar por torre (número, Enter = todas): ").trim(); // Declara una variable de texto para almacenar datos.
                    int torre = 0; // Declara una variable entera para almacenar un número.
                    if (!f.isEmpty()) { // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        try { // Inicia un bloque protegido para manejar posibles errores.
                            torre = Integer.parseInt(f); // Asigna un valor o actualiza una variable.
                        } catch (NumberFormatException e) { // Captura la excepción indicada para manejar el error.
                            torre = 0; // Asigna un valor o actualiza una variable.
                        }
                        if (torre <= 0) { // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                            System.out.println("La torre debe ser un número mayor a cero"); // Muestra un mensaje en la consola y termina la línea.
                            break; // Sale inmediatamente del case o ciclo actual.
                        }
                    }
                    listarApartamentos(torre); // Instrucción del programa según la operación actual.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                }
                case 6: // Ejecuta la restauración de la última eliminación.
                    if (deshacer()) // Evalúa la condición y ejecuta el bloque únicamente si resulta verdadera.
                        guardarArchivo(); // Instrucción del programa según la operación actual.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                case 0: // Guarda los datos y prepara la salida.
                    guardarArchivo(); // Instrucción del programa según la operación actual.
                    System.out.println("Datos guardados en " + ARCHIVO + ". Hasta luego."); // Muestra un mensaje en la consola y termina la línea.
                    break; // Termina el case actual y evita ejecutar los siguientes casos.
                default: // Atiende letras convertidas a -1 y números fuera de las opciones.
                    System.out.println("Opción no válida"); // Muestra un mensaje en la consola y termina la línea.
            } // Cierra la selección switch.
        } while (op != 0); // Repite el menú mientras la opción sea diferente de cero.
    }
}

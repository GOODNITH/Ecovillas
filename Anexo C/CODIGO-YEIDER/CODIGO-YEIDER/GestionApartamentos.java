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
 
import java.io.*;  // -> Trae las herramientas para leer/escribir archivos (File, BufferedReader, PrintWriter)
import java.util.Scanner;  // -> Trae Scanner: la herramienta para leer lo que se escribe por teclado
 
public class GestionApartamentos {  // -> class = el "molde/contenedor" donde vive todo el programa de apartamentos
 
    // -> Constante: máximo de apartamentos (1000). final = no cambia; static = es de la clase; int = número entero
    static final int MAX = 1000; // capacidad del arreglo
    // -> Constante con el número de torres; sirve para validar que la torre exista
    static final int TORRES = 16; // el conjunto tiene 16 torres
    // -> Constante: nombre del archivo donde se guardan los datos. String = texto
    static final String ARCHIVO = "apartamentos.txt"; // archivo de datos
    static final String[] RUTAS_USUARIOS = { "../harol-codigo/usuarios.txt", "harol-codigo/usuarios.txt",  // -> String[] = lista de textos; aquí, sitios donde buscar el usuarios.txt del compañero
            "usuarios.txt" }; // archivo de Usuario (carpeta de Harol), para validar relaciones
 
    // -> Arreglo = "cajonera" de 1000 espacios para apartamentos; new lo crea; cada espacio empieza vacío (null)
    static Apartamento[] apartamentos = new Apartamento[MAX]; // arreglo con contador (siempre ordenado por
                                                              // id_apartamento)
    // -> Contador: cuántos apartamentos hay realmente guardados
    static int cantidad = 0; // contador de apartamentos registrados
    // -> Crea la pila (la "papelera" para deshacer eliminaciones) con capacidad MAX
    static PilaApartamento pilaEliminados = new PilaApartamento(MAX); // pila propia para deshacer
 
    // ---------- utilidades internas ----------
 
    // Busca la posición de un apartamento por id_apartamento; -1 si no existe.
    static int buscarPosicion(int id_apartamento) {  // -> Busca en qué posición del arreglo está un apartamento por su id; devuelve -1 si no existe
        for (int i = 0; i < cantidad; i++)  // -> for = repetir; recorre desde la posición 0 hasta donde hay apartamentos (i++ suma 1 a i en cada vuelta)
            if (apartamentos[i].id_apartamento == id_apartamento)  // -> if = "si"; ¿el id de este apartamento es el que busco? (== compara)
                return i;  // -> return = devolver un resultado y salir del método: devuelve la posición donde lo encontró
        return -1;  // -> No lo encontró: -1 significa "no existe"
    }
    
    //esta es la que cumple la regla RN-06: el número se puede repetir entre torres distintas, pero NO dentro de la misma torre
//    numero: el número que quieres revisar, ej: "101"  el número que está en la puerta del apartamento
// id_torre: en qué torre lo quieres revisar, ej: 1
// excepto: un id que se debe saltar. 
    static int buscarPorNumeroYTorre(String numero, int id_torre, int excepto) {  // -> Busca si ya existe ese número en esa torre; "excepto" es un id que se salta (para no compararse consigo mismo)
        for (int i = 0; i < cantidad; i++)  // -> Recorre los apartamentos guardados
            if (apartamentos[i].id_apartamento != excepto && apartamentos[i].id_torre == id_torre  // -> Revisa 3 cosas unidas con && ("y"): que no sea "excepto", misma torre y mismo número (equalsIgnoreCase ignora mayúsculas)
                    && apartamentos[i].numero_apartamento.equalsIgnoreCase(numero))
                return i;  // -> Cumple las tres: ya existe, devuelve su posición
        return -1;  // -> No hay ninguno igual: -1
    }
    // Inserta manteniendo el orden por id_apartamento, desplazando a la derecha
    // (así deshacer devuelve el registro a su sitio).
    // insetar un nuevo apartameno  'a' es el nuevo 
    static void insertarOrdenado(Apartamento a) {  // -> Mete un apartamento sin dañar el orden por id. void = no devuelve nada
        int pos = cantidad;  // -> Empieza en la primera posición libre (justo después del último)
        while (pos > 0 && apartamentos[pos - 1].id_apartamento > a.id_apartamento) {  // -> while = repetir mientras se cumpla: mientras el de la izquierda tenga id más grande, hay que correrlo
            apartamentos[pos] = apartamentos[pos - 1];  // -> Corre ese apartamento una posición a la derecha
            pos--;  // -> Resta 1 a pos (se mueve una posición a la izquierda)
        }
        apartamentos[pos] = a;  // -> Ya con el hueco en su lugar, guarda el apartamento nuevo
        cantidad++;  // -> Suma 1 al contador: ahora hay uno más
    }
//  if (cantidad == 0) {    return 1; } else {  return otra cosa;}
    // Siguiente identificador incremental cuando no se digita uno. 512+1=513
    static int siguienteId() {  // -> Calcula el id que sigue cuando no escriben uno (automático)
        return cantidad == 0 ? 1 : apartamentos[cantidad - 1].id_apartamento + 1;  // -> Si no hay ninguno devuelve 1; si hay, el id del último + 1. El "? :" es un if resumido (condición ? si_sí : si_no)
    }
    // Número de apartamento: solo letras y números, sin espacios (Ej: 101, 202A).
    static boolean numeroValido(String n) {  // -> Revisa que el número tenga solo letras y números. boolean = true (sirve) o false (no sirve)
        return n.matches("^[A-Za-z0-9]+$");  // -> matches compara con una expresión regular (un patrón): ^ inicio, [A-Za-z0-9] letras o números, + uno o más, $ fin
    }//true or false
 
    // Busca un archivo de datos del OTRO programa: prueba varias rutas y devuelve
    // la
    // primera que exista (funciona se ejecute desde la carpeta del programa o desde
    // es la que hace que la carpeta  Mía  y la d eharol de encuentren ejemplo verifica que un apartamento tenga usuarios 
    // buscarArchivo(rut1"../harol-codigo/usuarios.txt", rut2"harol-codigo/usuarios.txt", "usuarios.txt") 
    static File buscarArchivo(String... rutas) {  // -> Devuelve el primer archivo que exista entre varias rutas ("..." permite pasar varias). File = representa un archivo
        for (String r : rutas)  // -> for-each: "para cada ruta r de la lista"
            if (new File(r).exists())  // -> new File(r) crea el objeto archivo; exists() pregunta si de verdad existe
                return new File(r);  // -> Existe: lo devuelve
        return new File(rutas[0]);  // -> Ninguna existe: devuelve la primera igual, para que no se caiga con null
    }// "no puedes eliminar un apartamento si tiene usuarios asociados".
 
    // RN-08 — indica si algún usuario (usuarios.txt, campo 9) usa este
    // id_apartamento. Los aportes aún no existen en el prototipo.
    static boolean tieneUsuarios(int id_apartamento) {  // -> Dice si algún usuario vive en ese apartamento (lee usuarios.txt) para no eliminar apartamentos ocupados
        File f = buscarArchivo(RUTAS_USUARIOS);  // -> Busca dónde está usuarios.txt
        if (!f.exists())  // -> El ! significa "no": si el archivo no existe...
            return false;  // -> ...responde false (como si no hubiera usuarios). Ojo: no avisa
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {  // -> try = intentar; abre el archivo para leerlo (BufferedReader = lector de líneas) y lo cierra solo al terminar
            String linea;  // -> Variable donde se guarda cada línea leída
            while ((linea = br.readLine()) != null) {  // -> readLine() lee una línea; repite hasta que no haya más (null = fin del archivo)
                String[] c = linea.split(";", -1);  // -> split parte la línea en pedazos usando ";" (el -1 cuenta también los campos vacíos)
                if (c.length == 9 && c[8].trim().equals(String.valueOf(id_apartamento)))  // -> ¿Tiene los 9 campos y el 9.º (posición 8) es este id? trim() quita espacios; equals() compara textos
                    return true;  // -> Sí hay un usuario ahí: true
            }
        } catch (IOException e) {  // -> catch = si falla la lectura (IOException = error de archivos) atrapa el error en vez de que el programa se caiga
            System.out.println("No se pudo leer " + f.getPath() + ": " + e.getMessage());  // -> println muestra texto en pantalla con salto de línea; aquí, el aviso de error
        }
        return false;  // -> Nadie usa ese apartamento: false
    }
 
    // Valida número y torre (obligatorios, formato, longitud y unicidad por torre);
    //  true = "todo bien, puedes seguir" o false = "algo falló, no puedes seguir"
    // numero: el número de la puerta
// id_torre: la torre
// id_propio: tu propio id para que no te compares contigo mismo cuando actualizas. Es el mismo excepto de la función anterior, solo que aquí le cambiaron el nombre.
    static boolean validarNumeroYTorre(String numero, int id_torre, int id_propio) {  // -> Junta las validaciones del número y la torre; si algo falla explica y devuelve false. id_propio = id que se excluye al actualizar
        if (numero.isEmpty()) {  // -> isEmpty() = ¿está vacío?
            System.out.println("Falta el campo obligatorio: numero_apartamento");  // -> Avisa que falta el número
            return false;  // -> Sale con false: no pasó la validación
        }
        if (id_torre <= 0) {  // -> ¿Torre 0 o negativa? (leerEntero da 0 cuando no escriben un número)
            System.out.println("Falta el campo obligatorio: id_torre (número de la torre)");  // -> Avisa que falta la torre
            return false;  // -> Sale con false: no pasó la validación
        }
        if (numero.length() > 10) {  // -> length() = cantidad de caracteres; el máximo permitido es 10
            System.out.println("El número no puede superar los 10 caracteres");  // -> Avisa que se pasó del largo
            return false;  // -> Sale con false: no pasó la validación
        }
        if (!numeroValido(numero)) {  // -> Si el número NO cumple "solo letras y números"...
            System.out.println("El número solo admite letras y números, sin espacios ni símbolos");  // -> ...avisa que no se permiten espacios ni símbolos
            return false;  // -> Sale con false: no pasó la validación
        }
        if (id_torre > TORRES) {  // -> ¿Torre mayor a 16? Esa torre no existe
            System.out.println("La torre no existe: el conjunto tiene torres de la 1 a la " + TORRES);  // -> Avisa en qué rango están las torres
            return false;  // -> Sale con false: no pasó la validación
        }
        if (buscarPorNumeroYTorre(numero, id_torre, id_propio) != -1) {  // -> ¿Ya existe ese número en esa torre? (!= -1 significa que lo encontró)
            System.out.println("El apartamento ya está registrado en esa torre");  // -> Avisa que ya está registrado en esa torre
            return false;  // -> Sale con false: no pasó la validación
        }
        return true;  // -> Pasó todas las validaciones: true
    }
 
    // ---------- las siete operaciones ----------
 
    // RF-018 · CA-APT-01-01 — crea el apartamento. Rechaza id repetido, campos
    // vacíos (CA-APT-01-02), número repetido en la misma torre (CA-APT-01-03) y
    // número de más de 10 caracteres (CA-APT-01-05). El mismo número en otra torre
    // es válido (CA-APT-01-04).
    static boolean crearApartamento(int id_apartamento, String numero_apartamento, int id_torre) {  // -> Crea un apartamento nuevo; devuelve true si lo creó y false si algo falló
        numero_apartamento = numero_apartamento.trim().toUpperCase();  // -> trim() quita espacios de los lados; toUpperCase() pasa a mayúsculas ("202a" queda "202A")
        if (cantidad == MAX) {  // -> ¿Ya se llenó el arreglo?
            System.out.println("No hay espacio: el arreglo está lleno");  // -> Avisa que ya no cabe otro
            return false;  // -> Sale sin crear nada
        }
        if (id_apartamento <= 0) {  // -> ¿Id 0 o negativo? No sirve
            System.out.println("El id_apartamento debe ser un entero mayor a cero");  // -> Avisa que el id debe ser mayor a cero
            return false;  // -> Sale sin crear nada
        }
        if (buscarPosicion(id_apartamento) != -1) {  // -> ¿Ya existe ese id? (si no da -1, sí existe)
            System.out.println("El id_apartamento " + id_apartamento + " ya existe");  // -> Avisa cuál id está repetido
            return false;  // -> Sale sin crear nada
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, -1))  // -> Corre las validaciones (-1 = no excluyo a nadie, porque es nuevo). Si NO las pasa...
            return false;  // -> ...sale sin crear (el error ya lo mostró la validación)
        insertarOrdenado(new Apartamento(id_apartamento, numero_apartamento, id_torre));  // -> new Apartamento(...) crea el objeto con los datos y lo mete ordenado al arreglo
        System.out.println("Apartamento creado correctamente");  // -> Mensaje de éxito
        return true;  // -> Devuelve true: sí se creó
    }
 
    // RF-019 · CA-APT-02-01 — devuelve el apartamento con ese id; CA-APT-02-02 —
    // null si no existe.
    static Apartamento consultarApartamento(int id_apartamento) {  // -> Busca un apartamento por id y lo devuelve; si no está, devuelve null (nada)
        int pos = buscarPosicion(id_apartamento);  // -> Posición donde está (o -1 si no existe)
        if (pos == -1) {  // -> ¿No lo encontró?
            System.out.println("No se encontró el apartamento");  // -> Avisa que no existe
            return null;  // -> null = "no hay nada"
        }
        return apartamentos[pos];  // -> Sí existe: devuelve ese apartamento
    }
 
    // RF-020 · CA-APT-03-01 — actualiza número y torre. Rechaza si no existe,
    // número ya existente en la misma torre (CA-APT-03-02) o campos obligatorios
    // vacíos (CA-APT-03-03). Los usuarios del apartamento se muestran con la nueva
    // torre porque su torre se obtiene del apartamento (CA-APT-03-04).
    static boolean actualizarApartamento(int id_apartamento, String numero_apartamento, int id_torre) {  // -> Cambia número y torre de un apartamento que ya existe; true si lo logró
        numero_apartamento = numero_apartamento.trim().toUpperCase();  // -> Limpia el número nuevo: sin espacios en los lados y en mayúsculas
        int pos = buscarPosicion(id_apartamento);  // -> Busca su posición
        if (pos == -1) {  // -> ¿No existe?
            System.out.println("No se encontró el apartamento");  // -> Avisa que no existe
            return false;  // -> Sale sin cambiar nada
        }
        if (!validarNumeroYTorre(numero_apartamento, id_torre, id_apartamento))  // -> Valida lo nuevo; pasa su propio id para no compararse consigo mismo (dejar el mismo número no da error)
            return false;  // -> No pasó: no cambia nada
        apartamentos[pos].numero_apartamento = numero_apartamento;  // -> Cambia el número guardado (el punto "." entra a un dato del objeto)
        apartamentos[pos].id_torre = id_torre;  // -> Cambia la torre
        System.out.println("Apartamento actualizado correctamente");  // -> Mensaje de éxito
        return true;  // -> Devuelve true: sí se actualizó
    }
 
    // RF-021 · CA-APT-04-01 — elimina el apartamento desplazando los siguientes
    // una posición a la izquierda (sin hueco) y lo apila para poder deshacer.
    // Rechaza si no existe, si tiene usuarios asociados (CA-APT-04-03) o si la pila
    // está llena.
    static Apartamento eliminarApartamento(int id_apartamento) {  // -> Elimina un apartamento y devuelve el eliminado (null si no pudo)
        int pos = buscarPosicion(id_apartamento);  // -> Busca su posición
        if (pos == -1) {  // -> ¿No existe?
            System.out.println("No se encontró el apartamento");  // -> Avisa que no existe
            return null;  // -> Sale sin eliminar nada
        }
        if (tieneUsuarios(id_apartamento)) {  // -> ¿Hay usuarios viviendo ahí? Entonces no se elimina
            System.out.println("No se puede eliminar un apartamento con usuarios o aportes asociados");  // -> Avisa por qué no se puede eliminar
            return null;  // -> Sale sin eliminar nada
        }
        Apartamento eliminado = apartamentos[pos];  // -> Guarda el apartamento en una variable antes de perderlo
        if (!pilaEliminados.apilar(eliminado)) {  // -> apilar = meterlo a la pila (la papelera); da false si la pila está llena
            System.out.println("No se puede eliminar: la pila de deshacer está llena");  // -> Avisa que la pila está llena
            return null;  // -> Sale sin eliminar nada
        }
        for (int i = pos; i < cantidad - 1; i++)  // -> Recorre desde la posición borrada hasta el penúltimo apartamento
            // -> Cada apartamento se corre una posición a la izquierda para tapar el hueco
            apartamentos[i] = apartamentos[i + 1]; // desplaza, no deja null en el medio
        apartamentos[cantidad - 1] = null;  // -> La última posición queda repetida: se limpia con null
        cantidad--;  // -> Resta 1 al contador: hay uno menos
        System.out.println("Apartamento eliminado correctamente");  // -> Mensaje de éxito
        return eliminado;  // -> Devuelve el eliminado (el menú usa eso para saber que sí funcionó)
    }
 
    // RF-022 · CA-APT-05-01 — lista número y torre; el filtro por torre
    // (CA-APT-05-02) es 0 para mostrar todos. CA-APT-05-03 — si no hay
    // apartamentos muestra "Aún no hay apartamentos registrados" en lugar de una
    // lista vacía. Si es 0 = muestra TODAS las torres. Si es otro número, ej: 1 = muestra SOLO la torre 1.
    static void listarApartamentos(int filtroTorre) {  // -> Muestra la tabla de apartamentos; filtroTorre 0 = todas las torres, otro número = solo esa torre
        if (cantidad == 0) {  // -> ¿No hay ninguno registrado?
            System.out.println("Aún no hay apartamentos registrados");  // -> Avisa en vez de mostrar una lista vacía
            return;  // -> Sale del método (void no devuelve nada)
        }
        System.out.printf("%-6s %-12s %-6s%n", "ID", "Apartamento", "Torre");  // -> printf imprime con formato: %-6s = texto alineado a la izquierda en 6 espacios; %n = salto de línea
        int mostrados = 0;  // -> Cuenta cuántos se imprimen
        for (int i = 0; i < cantidad; i++) {  // -> Recorre todos los apartamentos
            Apartamento a = apartamentos[i];  // -> Guarda el actual en "a" para escribir menos
            if (filtroTorre > 0 && a.id_torre != filtroTorre)  // -> Si hay filtro y este no es de esa torre...
                continue;  // -> ...lo salta (continue = pasa a la siguiente vuelta del for)
            System.out.printf("%-6d %-12s %-6d%n", a.id_apartamento, a.numero_apartamento, a.id_torre);  // -> Imprime la fila: %d = número entero, %s = texto
            mostrados++;  // -> Suma 1 a los mostrados
        }
        if (mostrados == 0)  // -> Si no se mostró ninguno (el filtro no encontró nada)...
            System.out.println("No hay apartamentos en la torre " + filtroTorre);  // -> ...avisa que esa torre no tiene apartamentos
    }
 
    // RF-021 · CA-APT-04-01 — deshace la última eliminación: devuelve a su sitio el
    // MISMO apartamento que salió de la pila. Rechaza si no hay nada que deshacer
    // o si su id o su número en esa torre ya fueron tomados por otro registro.
    static boolean deshacer() {  // -> Restaura el último apartamento eliminado (como un Ctrl+Z); true si lo logró
        Apartamento a = pilaEliminados.cima();  // -> cima() espía el último eliminado SIN sacarlo de la pila
        if (a == null) {  // -> null = la pila está vacía
            System.out.println("No hay nada que deshacer");  // -> Avisa que no hay nada para restaurar
            return false;  // -> Sale sin hacer nada
        }
        if (cantidad == MAX) {  // -> ¿Arreglo lleno? No cabría
            System.out.println("No hay espacio: el arreglo está lleno");  // -> Avisa que no hay espacio
            return false;  // -> Sale sin hacer nada
        }
        if (buscarPosicion(a.id_apartamento) != -1  // -> Revisa con || ("o"): que otro no use ya ese id, o que otro no use ya ese número en esa torre
                || buscarPorNumeroYTorre(a.numero_apartamento, a.id_torre, -1) != -1) {
            System.out.println("No se puede deshacer: el id o el número en esa torre ya los usa otro apartamento");  // -> Avisa que otro registro ya ocupó ese id o ese número
            return false;  // -> Sale sin restaurar
        }
        pilaEliminados.desapilar();  // -> desapilar = ahora sí lo saca de la pila (ya se va a restaurar)
        insertarOrdenado(a);  // -> Lo vuelve a meter al arreglo en su lugar
        System.out.println("Se restauró el apartamento " + a.id_apartamento + " (" + a.numero_apartamento + ", torre "  // -> Avisa cuál apartamento se restauró
                + a.id_torre + ")");
        return true;  // -> Devuelve true: sí se restauró
    }
 
    // RF-018 a RF-022 — guarda todos los apartamentos en el archivo de datos para
    // que sobrevivan al cierre del programa.
    static void guardarArchivo() {  // -> Guarda todos los apartamentos en apartamentos.txt para no perderlos al cerrar
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {  // -> PrintWriter = escritor de archivos: abre el archivo (borra lo viejo y escribe todo de nuevo) y lo cierra solo
        //   FileWriter(ARCHIVO) = abre el archivo apartamentos.txt para escribir. No agrega al final, empieza de cero.
// PrintWriter = el escritor que te deja escribir líneas fácil con println.
// try (...) = esto es un try-with-resources. Significa: "ábrelo, úsalo, y cuando termines, ciérralo tú solo, aunque haya error". Por eso no ves un pw.close() en ninguna parte, Java lo cierra solo.
            for (int i = 0; i < cantidad; i++) {  // -> Recorre los apartamentos
                Apartamento a = apartamentos[i];  // -> Guarda el actual en "a"
                pw.println(a.id_apartamento + ";" + a.numero_apartamento + ";" + a.id_torre);  // -> Escribe una línea por apartamento: id;número;torre
            }
        } catch (IOException e) {  // -> Si falla al escribir, atrapa el error en vez de caerse
            System.out.println("No se pudo guardar el archivo: " + e.getMessage());  // -> Muestra el error
        }
    }
 
    // RF-018 a RF-022 — carga los apartamentos del archivo al iniciar; si no
    // existe, arranca vacío.
    static void cargarArchivo() {  // -> Lee apartamentos.txt al arrancar y llena el arreglo con lo que había guardado
        File f = new File(ARCHIVO);  // -> Objeto que apunta al archivo de datos
        if (!f.exists())  // -> Si no existe (por ejemplo, la primera vez)...
            return;  // -> ...sale y el programa arranca vacío
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {  // -> Abre el archivo para leerlo
            String linea;  // -> Variable donde se guarda cada línea
            while ((linea = br.readLine()) != null && cantidad < MAX) {  // -> Lee línea por línea mientras haya y quepan en el arreglo (&& = "y")
                String[] c = linea.split(";", -1);  // -> Parte la línea en pedazos por ";"
                if (c.length != 3)  // -> ¿No tiene exactamente 3 campos (id, número, torre)?
                    continue;  // -> Salta esa línea dañada y sigue con la siguiente
                insertarOrdenado(new Apartamento(Integer.parseInt(c[0]), c[1], Integer.parseInt(c[2])));  // -> parseInt convierte texto a número; arma el Apartamento y lo mete ordenado
            }
        } catch (Exception e) {  // -> Exception = cualquier error (archivo dañado, texto donde va un número...)
            System.out.println("No se pudo leer el archivo: " + e.getMessage());  // -> Muestra el error
        }
    }
 
    // ---------- menú ----------
 
    static Scanner sc = new Scanner(System.in);  // -> Scanner que lee el teclado (System.in = teclado); uno solo para todo el programa
 
    // Lee un entero; devuelve 0 si lo digitado no es número (los validadores lo
    // rechazan).
    static int leerEntero(String pregunta) {  // -> Hace una pregunta y lee un número entero; si no es número devuelve 0
        System.out.print(pregunta);  // -> print muestra texto SIN salto de línea (así se escribe justo después)
        try {  // -> Intenta convertir lo escrito a número
            return Integer.parseInt(sc.nextLine().trim());  // -> nextLine() lee lo escrito, trim() quita espacios y parseInt() lo convierte a entero
        } catch (NumberFormatException e) {  // -> Si no era número (letras, símbolos) cae aquí en vez de romper el programa
            return 0;  // -> 0 = "dato inválido" (los validadores lo rechazan)
        }
    }
 
    static int leerOpcionMenu(String pregunta) {  // -> Igual a leerEntero pero para el menú: si no es número devuelve -1 en vez de 0
        System.out.print(pregunta);  // -> Muestra la pregunta (ej: "Opción: ")
        try {  // -> Intenta convertir lo escrito a número
            return Integer.parseInt(sc.nextLine().trim());  // -> Lee lo escrito, le quita espacios y lo convierte a entero
        } catch (NumberFormatException e) {  // -> Cae aquí si escribió letras o símbolos como a, || o >
            return -1;  // -> -1 no es ninguna opción: cae en default ("Opción no válida") y no se confunde con el 0 (salir)
        }
    }
 
    static String leerTexto(String pregunta) {  // -> Hace una pregunta y devuelve lo escrito tal cual, como texto
        System.out.print(pregunta);  // -> Muestra la pregunta
        return sc.nextLine();  // -> Lee la línea completa y la devuelve
    }
 
    public static void main(String[] args) {  // -> Aquí arranca el programa (Java siempre empieza por main); args = datos al ejecutar, no los usamos
        cargarArchivo();  // -> Primero carga lo que estaba guardado en el archivo
        int op;  // -> Variable donde se guarda la opción elegida en el menú
        do {  // -> do-while: repite el menú hasta que elijan salir (se ejecuta mínimo una vez)
            System.out.println("\n=== Villas del Socorro · Apartamentos (" + cantidad + " registrados) ===");  // -> Título del menú con la cantidad de apartamentos (\n = una línea en blanco antes)
            System.out.println("1. Crear apartamento");  // -> Opción 1 del menú
            System.out.println("2. Consultar apartamento");  // -> Opción 2 del menú
            System.out.println("3. Actualizar apartamento");  // -> Opción 3 del menú
            System.out.println("4. Eliminar apartamento");  // -> Opción 4 del menú
            System.out.println("5. Listar apartamentos");  // -> Opción 5 del menú
            System.out.println("6. Deshacer última eliminación");  // -> Opción 6 del menú
            System.out.println("0. Guardar y salir");  // -> Opción 0 del menú
            op = leerOpcionMenu("Opción: ");  // -> Pide la opción; usa leerOpcionMenu para que letras o símbolos no se confundan con el 0 (salir)
            switch (op) {  // -> switch = según el número elegido salta al case que corresponde
                case 1: {  // -> Caso 1: crear apartamento
                    String txt = leerTexto("id_apartamento (Enter = automático): ").trim();  // -> Pide el id como texto (si da Enter queda vacío)
                    int id = txt.isEmpty() ? siguienteId() : -1;  // -> Si lo dejó vacío usa el id automático; si escribió algo deja -1 para procesarlo abajo
                    if (id == -1) {  // -> Si escribió un id...
                        try {  // -> ...intenta convertirlo a número
                            id = Integer.parseInt(txt);  // -> Convierte el texto a número entero
                        } catch (NumberFormatException e) {  // -> Si no era un número...
                            id = 0;  // -> ...pone 0 (crearApartamento lo rechaza por inválido)
                        }
                    }
                    String num = leerTexto("numero_apartamento: ");  // -> Pide el número del apartamento
                    int torre = leerEntero("id_torre (1 a " + TORRES + "): ");  // -> Pide la torre (de 1 a 16)
                    if (crearApartamento(id, num, torre))  // -> Llama a crear; si devolvió true (lo creó)...
                        guardarArchivo();  // -> ...guarda en el archivo para no perder el cambio
                    break;  // -> break = termina este caso (sin él seguiría con el siguiente)
                }
                case 2: {  // -> Caso 2: consultar un apartamento
                    Apartamento a = consultarApartamento(leerEntero("id_apartamento: "));  // -> Pide el id y busca el apartamento; queda en "a" (o null si no existe)
                    if (a != null)  // -> Si sí lo encontró... (!= significa "diferente de")
                        a.mostrar();  // -> ...imprime sus datos con el método mostrar() de la clase Apartamento
                    break;  // -> Termina el caso 2
                }
                case 3: {  // -> Caso 3: actualizar un apartamento
                    int id = leerEntero("id_apartamento a actualizar: ");  // -> Pide el id del apartamento a modificar
                    if (consultarApartamento(id) == null)  // -> Verifica que exista antes de pedir los datos nuevos...
                        break;  // -> ...si no existe, sale del caso
                    String num = leerTexto("nuevo numero_apartamento: ");  // -> Pide el número nuevo
                    int torre = leerEntero("nuevo id_torre (1 a " + TORRES + "): ");  // -> Pide la torre nueva
                    if (actualizarApartamento(id, num, torre))  // -> Llama a actualizar; si devolvió true...
                        guardarArchivo();  // -> ...guarda en el archivo
                    break;  // -> Termina el caso 3
                }
                case 4: {  // -> Caso 4: eliminar un apartamento
                    int id = leerEntero("id_apartamento a eliminar: ");  // -> Pide el id del apartamento a eliminar
                    if (consultarApartamento(id) == null)  // -> Verifica que exista...
                        break;  // -> ...si no existe, sale del caso
                    String r = leerTexto("¿Confirma eliminar? (s/n): ").trim();  // -> Pide confirmación (s/n) antes de borrar
                    if (r.equalsIgnoreCase("s")) {  // -> Si respondió "s" (equalsIgnoreCase ignora mayúsculas/minúsculas)...
                        if (eliminarApartamento(id) != null)  // -> ...intenta eliminar; si devuelve algo distinto de null, sí lo eliminó...
                            guardarArchivo();  // -> ...entonces guarda en el archivo
                    } else  // -> else = "si no": si respondió cualquier otra cosa...
                        System.out.println("Operación cancelada, no se hizo ningún cambio");  // -> ...avisa que se canceló y no se cambió nada
                    break;  // -> Termina el caso 4
                }
                case 5: {  // -> Caso 5: listar apartamentos
                    String f = leerTexto("Filtrar por torre (número, Enter = todas): ").trim();  // -> Pide la torre para filtrar (si da Enter queda vacío)
                    int torre = 0;  // -> Empieza en 0 = "sin filtro" (todas las torres)
                    if (!f.isEmpty()) {  // -> Si escribió una torre...
                        try {  // -> ...intenta convertirla a número
                            torre = Integer.parseInt(f);  // -> Convierte el texto a número entero
                        } catch (NumberFormatException e) {  // -> Si no era un número...
                            torre = 0;  // -> ...la deja en 0 (se rechaza justo abajo)
                        }
                        if (torre <= 0) {  // -> Si la torre es 0 o negativa (no válida)...
                            System.out.println("La torre debe ser un número mayor a cero");  // -> ...avisa que debe ser un número mayor a cero
                            break;  // -> ...y sale del caso sin listar
                        }
                    }
                    listarApartamentos(torre);  // -> Muestra la lista (con filtro o sin él, según torre)
                    break;  // -> Termina el caso 5
                }
                case 6:  // -> Caso 6: deshacer la última eliminación
                    if (deshacer())  // -> Llama a deshacer; si devolvió true (restauró algo)...
                        guardarArchivo();  // -> ...guarda en el archivo
                    break;  // -> Termina el caso 6
                case 0:  // -> Caso 0: guardar y salir
                    guardarArchivo();  // -> Guarda todo en el archivo antes de cerrar
                    System.out.println("Datos guardados en " + ARCHIVO + ". Hasta luego.");  // -> Avisa dónde quedaron guardados los datos
                    break;  // -> Termina el caso 0; como op vale 0, el ciclo se acaba
                default:  // -> default = si la opción no es ninguna de las anteriores (letras, 10, -1...) cae aquí
                    System.out.println("Opción no válida");  // -> Avisa que la opción no es válida; luego el ciclo vuelve a mostrar el menú
            }
        } while (op != 0);  // -> Fin del ciclo: se repite mientras op sea diferente de 0; con 0 el programa termina
    }
}
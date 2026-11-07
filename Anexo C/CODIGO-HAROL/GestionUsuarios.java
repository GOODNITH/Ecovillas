/* ===========================================================
 * Proyecto       : Sistema de Coordinación Comunitaria para la Gestión de Residuos en Villas del Socorro
 * Entidad        : Usuario
 * Programado por : Harol Stiven Holguín Ospina
 * Fecha          : 05/11/2026
 * -----------------------------------------------------------
 * HU-USU-01  Como Administrador quiero crear un usuario para que el habitante ingrese con los permisos de su rol
 * HU-USU-02  Como Administrador quiero consultar un usuario para verificar sus datos, rol y apartamento
 * HU-USU-03  Como Administrador quiero actualizar los datos de un usuario para mantener la información correcta
 * HU-USU-04  Como Administrador quiero eliminar un usuario para impedir el ingreso de quien ya no vive en el conjunto
 * HU-USU-05  Como Administrador quiero listar los usuarios para tener una vista general por estado
 * RF-001  Crear usuarios (estado Activo, correo único y válido, contraseña >= 8, nombres solo letras)
 * RF-002  Consultar un usuario sin mostrar su contraseña
 * RF-003  Actualizar teléfono, correo, rol, apartamento y estado; el correo no puede ser de otro usuario
 * RF-004  Eliminar usuarios; el eliminado se guarda en una pila para poder deshacer
 * RF-005  Listar usuarios con filtro por estado
 * RN-04  Correo único y válido; contraseña de mínimo 8 caracteres
 * RN-08  Lo eliminado no se pierde: queda en la pila hasta deshacer
 * -----------------------------------------------------------
 * Criterios: CA-USU-01-01 (bueno) CA-USU-01-03 (falla) CA-USU-04-01 (bueno) CA-USU-05-04 (falla)
 * Integridad: id_apartamento debe existir en apartamentos.txt (RNF-06; diccionario: FK a Apartamento)
 * Estructuras: Arreglo con contador . Pila propia
 * =========================================================== */

import java.io.*;
import java.security.MessageDigest;
import java.util.Scanner;

public class GestionUsuarios {

    static final int MAX = 1000; // capacidad del arreglo
    static final String ARCHIVO = "usuarios.txt"; // archivo de datos
    static final String[] RUTAS_APARTAMENTOS = { "../CODIGO-YEIDER/apartamentos.txt", "CODIGO-YEIDER/apartamentos.txt",
            "apartamentos.txt" }; // archivo de Apartamento (carpeta de Yeider), para validar la FK

    static Usuario[] usuarios = new Usuario[MAX]; // arreglo con contador (siempre ordenado por id_usuario)
    static int cantidad = 0; // contador de usuarios registrados
    static PilaUsuario pilaEliminados = new PilaUsuario(MAX); // pila propia para deshacer

    // ---------- utilidades internas ----------

    // Busca la posición de un usuario por id_usuario; -1 si no existe.
    static int buscarPosicion(int id_usuario) {
        for (int i = 0; i < cantidad; i++)
            if (usuarios[i].id_usuario == id_usuario)
                return i;
        return -1;
    }

    // Devuelve la posición del usuario con ese correo (ignora mayúsculas) o -1;
    // "excepto" se omite al comparar.
    static int buscarPorCorreo(String correo, int excepto) {
        for (int i = 0; i < cantidad; i++)
            if (usuarios[i].id_usuario != excepto && usuarios[i].correo.equalsIgnoreCase(correo))
                return i;
        return -1;
    }

    // Inserta manteniendo el orden por id_usuario, desplazando a la derecha (así
    // deshacer devuelve el registro a su sitio).
    static void insertarOrdenado(Usuario u) {
        int pos = cantidad;
        while (pos > 0 && usuarios[pos - 1].id_usuario > u.id_usuario) {
            usuarios[pos] = usuarios[pos - 1];
            pos--;
        }
        usuarios[pos] = u;
        cantidad++;
    }

    // Siguiente identificador incremental cuando no se digita uno.
    static int siguienteId() {
        return cantidad == 0 ? 1 : usuarios[cantidad - 1].id_usuario + 1;
    }

    // Calcula el hash SHA-256 de la contraseña (la clave original nunca se
    // almacena).
    static String hash(String clave) {
        try {
            byte[] h = MessageDigest.getInstance("SHA-256").digest(clave.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : h)
                sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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

    // FK id_apartamento: indica si el apartamento existe en apartamentos.txt
    // (formato de cada línea: id_apartamento;numero_apartamento;id_torre).
    // Si el archivo no existe, no hay apartamentos registrados y devuelve false.
    static boolean apartamentoExiste(int id_apartamento) {
        File f = buscarArchivo(RUTAS_APARTAMENTOS);
        if (!f.exists())
            return false;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                String[] c = linea.split(";", -1);
                if (c.length == 3 && c[0].trim().equals(String.valueOf(id_apartamento)))
                    return true;
            }
        } catch (IOException e) {
            System.out.println("No se pudo leer " + f.getPath() + ": " + e.getMessage());
        }
        return false;
    }

    // RN-04 — formato de correo válido.
    static boolean correoValido(String c) {
        return c.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    // Texto solo con letras y espacios, no vacío.
    static boolean soloLetras(String t) {
        return t.matches("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$");
    }

    // Teléfono opcional: solo números, "+", "-" y espacios.
    static boolean telefonoValido(String t) {
        return t.matches("^[0-9+\\- ]+$");
    }

    // Valida el correo (formato, longitud y unicidad) y el teléfono; imprime el
    // motivo y devuelve false si algo falla.
    static boolean validarCorreoYTelefono(String correo, String telefono, int id_propio) {
        if (correo.isEmpty()) {
            System.out.println("Falta el campo obligatorio: correo");
            return false;
        }
        if (correo.length() > 100 || !correoValido(correo)) {
            System.out.println("Formato de correo inválido");
            return false;
        }
        if (buscarPorCorreo(correo, id_propio) != -1) {
            System.out.println("El correo ya está registrado");
            return false;
        }
        if (!telefono.isEmpty() && (telefono.length() > 15 || !telefonoValido(telefono))) {
            System.out.println("Formato de teléfono inválido");
            return false;
        }
        return true;
    }

    // ---------- las siete operaciones ----------

    // RF-001 · CA-USU-01-01 — crea el usuario Activo con contraseña cifrada.
    // Rechaza id repetido, campos vacíos (CA-USU-01-02),
    // correo repetido (CA-USU-01-03) o inválido (CA-USU-01-04), contraseña corta
    // (CA-USU-01-05), nombres con símbolos (CA-USU-01-06)
    // y teléfono inválido (CA-USU-01-08). El teléfono vacío es válido
    // (CA-USU-01-07).
    static boolean crearUsuario(int id_usuario, String nombres, String apellidos, String correo, String telefono,
            String contrasena, int id_rol, int id_apartamento) {
        nombres = nombres.trim();
        apellidos = apellidos.trim();
        correo = correo.trim();
        telefono = telefono.trim();
        if (cantidad == MAX) {
            System.out.println("No hay espacio: el arreglo está lleno");
            return false;
        }
        if (id_usuario <= 0) {
            System.out.println("El id_usuario debe ser un entero mayor a cero");
            return false;
        }
        if (buscarPosicion(id_usuario) != -1) {
            System.out.println("El id_usuario " + id_usuario + " ya existe");
            return false;
        }
        if (nombres.isEmpty() || apellidos.isEmpty() || contrasena.isEmpty() || id_rol <= 0 || id_apartamento <= 0) {
            System.out.println("Faltan campos obligatorios: nombres, apellidos, correo, contraseña, rol y apartamento");
            return false;
        }
        if (!apartamentoExiste(id_apartamento)) {
            System.out.println("El apartamento no existe");
            return false;
        }
        if (nombres.length() > 80 || apellidos.length() > 80 || !soloLetras(nombres) || !soloLetras(apellidos)) {
            System.out.println("Los nombres y apellidos solo admiten letras y espacios");
            return false;
        }
        if (!validarCorreoYTelefono(correo, telefono, -1))
            return false;
        if (contrasena.length() < 8) {
            System.out.println("La contraseña debe tener mínimo 8 caracteres");
            return false;
        }
        insertarOrdenado(new Usuario(id_usuario, nombres, apellidos, correo, telefono, hash(contrasena), "Activo",
                id_rol, id_apartamento));
        System.out.println("Usuario creado correctamente");
        return true;
    }

    // RF-002 · CA-USU-02-01 — devuelve el usuario con ese id (sin mostrar
    // contraseña al imprimirlo); CA-USU-02-02 — null si no existe.
    static Usuario consultarUsuario(int id_usuario) {
        int pos = buscarPosicion(id_usuario);
        if (pos == -1) {
            System.out.println("No se encontró el usuario");
            return null;
        }
        return usuarios[pos];
    }

    // RF-003 · CA-USU-03-01 — actualiza teléfono, correo, rol, apartamento y
    // estado. Rechaza si no existe, correo de otro usuario
    // (CA-USU-03-02), correo inválido (CA-USU-03-03), campos obligatorios vacíos
    // (CA-USU-03-04) o estado distinto de Activo/Inactivo.
    // Cambiar el estado a Activo reactiva al usuario (CA-USU-03-07).
    static boolean actualizarUsuario(int id_usuario, String telefono, String correo, int id_rol, int id_apartamento,
            String estado) {
        telefono = telefono.trim();
        correo = correo.trim();
        estado = estado.trim();
        int pos = buscarPosicion(id_usuario);
        if (pos == -1) {
            System.out.println("No se encontró el usuario");
            return false;
        }
        if (correo.isEmpty() || id_rol <= 0 || id_apartamento <= 0 || estado.isEmpty()) {
            System.out.println("Faltan campos obligatorios: correo, rol, apartamento y estado");
            return false;
        }
        if (!apartamentoExiste(id_apartamento)) {
            System.out.println("El apartamento no existe");
            return false;
        }
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) {
            System.out.println("El estado debe ser Activo o Inactivo");
            return false;
        }
        if (!validarCorreoYTelefono(correo, telefono, id_usuario))
            return false;
        Usuario u = usuarios[pos];
        u.telefono = telefono;
        u.correo = correo;
        u.id_rol = id_rol;
        u.id_apartamento = id_apartamento;
        u.estado = estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo";
        System.out.println("Usuario actualizado correctamente");
        return true;
    }

    // RF-004 · CA-USU-04-01 — elimina el usuario desplazando los siguientes una
    // posición a la izquierda (sin hueco) y lo apila
    // para poder deshacer. Rechaza si no existe o si la pila está llena.
    static Usuario eliminarUsuario(int id_usuario) {
        int pos = buscarPosicion(id_usuario);
        if (pos == -1) {
            System.out.println("No se encontró el usuario");
            return null;
        }
        Usuario eliminado = usuarios[pos];
        if (!pilaEliminados.apilar(eliminado)) {
            System.out.println("No se puede eliminar: la pila de deshacer está llena");
            return null;
        }
        for (int i = pos; i < cantidad - 1; i++)
            usuarios[i] = usuarios[i + 1]; // desplaza, no deja null en el medio
        usuarios[cantidad - 1] = null;
        cantidad--;
        System.out.println("Usuario eliminado correctamente");
        return eliminado;
    }

    // RF-005 · CA-USU-05-01 — lista nombre, estado y demás datos; el filtro por
    // estado (CA-USU-05-03) es null para mostrar todos.
    // CA-USU-05-04 — si no hay usuarios muestra "Aún no hay usuarios registrados"
    // en lugar de una lista vacía.
    static void listarUsuarios(String filtroEstado) {
        if (cantidad == 0) {
            System.out.println("Aún no hay usuarios registrados");
            return;
        }
        System.out.printf("%-5s %-28s %-30s %-9s %-4s %-5s%n", "ID", "Nombre", "Correo", "Estado", "Rol", "Apto");
        int mostrados = 0;
        for (int i = 0; i < cantidad; i++) {
            Usuario u = usuarios[i];
            if (filtroEstado != null && !u.estado.equalsIgnoreCase(filtroEstado))
                continue;
            System.out.printf("%-5d %-28s %-30s %-9s %-4d %-5d%n", u.id_usuario, u.nombres + " " + u.apellidos,
                    u.correo, u.estado, u.id_rol, u.id_apartamento);
            mostrados++;
        }
        if (mostrados == 0)
            System.out.println("No hay usuarios con estado " + filtroEstado);
    }

    // RF-004 · CA-USU-04-01 — deshace la última eliminación: devuelve a su sitio el
    // MISMO usuario que salió de la pila.
    // Rechaza si no hay nada que deshacer o si su id o correo ya fueron tomados por
    // otro registro.
    static boolean deshacer() {
        Usuario u = pilaEliminados.cima();
        if (u == null) {
            System.out.println("No hay nada que deshacer");
            return false;
        }
        if (cantidad == MAX) {
            System.out.println("No hay espacio: el arreglo está lleno");
            return false;
        }
        if (buscarPosicion(u.id_usuario) != -1 || buscarPorCorreo(u.correo, -1) != -1) {
            System.out.println("No se puede deshacer: el id o el correo ya los usa otro usuario");
            return false;
        }
        if (!apartamentoExiste(u.id_apartamento)) {
            System.out
                    .println("No se puede deshacer: el apartamento " + u.id_apartamento + " del usuario ya no existe");
            return false;
        }
        pilaEliminados.desapilar();
        insertarOrdenado(u);
        System.out.println("Se restauró el usuario " + u.id_usuario + " (" + u.nombres + " " + u.apellidos + ")");
        return true;
    }

    // RF-001 a RF-005 — guarda todos los usuarios en el archivo de datos para que
    // sobrevivan al cierre del programa.
    static void guardarArchivo() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) {
            for (int i = 0; i < cantidad; i++) {
                Usuario u = usuarios[i];
                pw.println(u.id_usuario + ";" + u.nombres + ";" + u.apellidos + ";" + u.correo + ";" + u.telefono + ";"
                        + u.contrasena + ";" + u.estado + ";" + u.id_rol + ";" + u.id_apartamento);
            }
        } catch (IOException e) {
            System.out.println("No se pudo guardar el archivo: " + e.getMessage());
        }
    }

    // RF-001 a RF-005 — carga los usuarios del archivo al iniciar; si no existe,
    // arranca vacío.
    static void cargarArchivo() {
        File f = new File(ARCHIVO);
        if (!f.exists())
            return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String linea;
            while ((linea = br.readLine()) != null && cantidad < MAX) {
                String[] c = linea.split(";", -1);
                if (c.length != 9)
                    continue;
                insertarOrdenado(new Usuario(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5], c[6],
                        Integer.parseInt(c[7]), Integer.parseInt(c[8])));
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
            System.out.println("\n=== Villas del Socorro · Usuarios (" + cantidad + " registrados) ===");
            System.out.println("1. Crear usuario");
            System.out.println("2. Consultar usuario");
            System.out.println("3. Actualizar usuario");
            System.out.println("4. Eliminar usuario");
            System.out.println("5. Listar usuarios");
            System.out.println("6. Deshacer última eliminación");
            System.out.println("0. Guardar y salir");
            op = leerEntero("Opción: ");
            switch (op) {
                case 1: {
                    String txt = leerTexto("id_usuario (Enter = automático): ").trim();
                    int id = txt.isEmpty() ? siguienteId() : -1;
                    if (id == -1) {
                        try {
                            id = Integer.parseInt(txt);
                        } catch (NumberFormatException e) {
                            id = 0;
                        }
                    }
                    String nom = leerTexto("nombres: "), ape = leerTexto("apellidos: "), cor = leerTexto("correo: ");
                    String tel = leerTexto("telefono (opcional): "), con = leerTexto("contrasena: ");
                    int rol = leerEntero("id_rol: "), apto = leerEntero("id_apartamento: ");
                    if (crearUsuario(id, nom, ape, cor, tel, con, rol, apto))
                        guardarArchivo();
                    break;
                }
                case 2: {
                    Usuario u = consultarUsuario(leerEntero("id_usuario: "));
                    if (u != null)
                        u.mostrar();
                    break;
                }
                case 3: {
                    int id = leerEntero("id_usuario a actualizar: ");
                    if (consultarUsuario(id) == null)
                        break;
                    String tel = leerTexto("nuevo telefono (vacío = sin teléfono): "),
                            cor = leerTexto("nuevo correo: ");
                    int rol = leerEntero("nuevo id_rol: "), apto = leerEntero("nuevo id_apartamento: ");
                    String est = leerTexto("estado (Activo/Inactivo): ");
                    if (actualizarUsuario(id, tel, cor, rol, apto, est))
                        guardarArchivo();
                    break;
                }
                case 4: {
                    int id = leerEntero("id_usuario a eliminar: ");
                    if (consultarUsuario(id) == null)
                        break;
                    String r = leerTexto("¿Confirma eliminar? (s/n): ").trim();
                    if (r.equalsIgnoreCase("s")) {
                        if (eliminarUsuario(id) != null)
                            guardarArchivo();
                    } else
                        System.out.println("Operación cancelada, no se hizo ningún cambio");
                    break;
                }
                case 5: {
                    String f = leerTexto("Filtrar por estado (Activo/Inactivo, Enter = todos): ").trim();
                    listarUsuarios(f.isEmpty() ? null : f);
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

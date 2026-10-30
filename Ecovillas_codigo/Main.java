
/* ===========================================================
 * Proyecto: EcoVillas | Clase principal: Main
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-01 a HU-07 | RF: RF-01 a RF-15
 * RN: RN-01 a RN-06 | RNF: RNF-01 a RNF-06
 * CA: CA-01.1 a CA-07.3
 * Estructuras: objetos de las entidades y Scanner.
 * =========================================================== */
import java.util.Scanner;

public class Main {
    private static final Scanner sc = new Scanner(System.in);
    private static final Usuario usuarios = new Usuario();
    private static final Administrador administradores = new Administrador();
    private static final Jornadas jornadas = new Jornadas();
    private static final Reportes reportes = new Reportes();
    private static final Zonas_Comun zonas = new Zonas_Comun();
    private static final Inscripciones_jornada inscripciones = new Inscripciones_jornada();

    public static void main(String[] args) {
        int op;
        do {
            System.out.println("\n========== ECOVILLAS ==========");
            System.out.println("1. Registrar residente");
            System.out.println("2. Registrar administrador");
            System.out.println("3. Iniciar sesión");
            System.out.println("4. Salir");
            op = leerInt("Seleccione: ");
            switch (op) {
                case 1:
                    registrarUsuario("Residente");
                    break;
                case 2:
                    registrarUsuario("Administrador");
                    break;
                case 3:
                    iniciarSesion();
                    break;
                case 4:
                    guardarTodo();
                    System.out.println("Datos guardados. Hasta pronto.");
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (op != 4);
    }

    private static void registrarUsuario(String rol) {
        int id = leerInt("Identificación: ");
        String nombre = leer("Nombre: "), apellido = leer("Apellido: ");
        String telefono = leer("Teléfono: "), correo = leer("Correo: ");
        String clave = leer("Contraseña: ");
        if (usuarios.crearUsuario(id, nombre, apellido, telefono, correo, clave, rol)) {
            if (rol.equals("Administrador")) {
                String torre = leer("Torre que administra: ");
                int idAdmin = leerInt("Identificador de administrador: ");
                if (!administradores.crearAdministrador(idAdmin, id, torre, usuarios))
                    System.out.println("El usuario se creó, pero no se pudo crear su ficha de administrador.");
            }
            System.out.println("Usuario registrado.");
        } else
            System.out.println("No se pudo registrar el usuario.");
    }

    private static void iniciarSesion() {
        int id = leerInt("Identificación: ");
        String clave = leer("Contraseña: ");
        if (!usuarios.autenticar(id, clave)) {
            System.out.println("Credenciales incorrectas.");
            return;
        }
        if (usuarios.esAdministrador(id))
            menuAdministrador(id);
        else if (usuarios.esResidente(id))
            menuResidente(id);
    }

    private static void menuAdministrador(int id) {
        int op;
        do {
            System.out.println("\n===== ADMINISTRADOR =====");
            System.out.println("1. Crear jornada");
            System.out.println("2. Consultar jornadas");
            System.out.println("3. Registrar zona común");
            System.out.println("4. Actualizar estado de zona");
            System.out.println("5. Registrar última recolección");
            System.out.println("6. Consultar zonas");
            System.out.println("7. Consultar reportes");
            System.out.println("8. Cambiar reporte a Solucionado");
            System.out.println("9. Consultar inscripciones");
            System.out.println("10. Listar usuarios");
            System.out.println("11. Cerrar sesión");
            op = leerInt("Seleccione: ");
            switch (op) {
                case 1:
                    crearJornada(id);
                    break;
                case 2:
                    jornadas.listarJornadas();
                    break;
                case 3:
                    crearZona(id);
                    break;
                case 4:
                    zonas.actualizarEstado(leerInt("ID zona: "), leer("Estado (Limpia/En proceso/Dañada): "), id,
                            usuarios);
                    break;
                case 5:
                    zonas.registrarRecoleccion(leerInt("ID zona: "), leer("Fecha y hora (dd/MM/yyyy HH:mm): "), id,
                            usuarios);
                    break;
                case 6:
                    zonas.listarZonas();
                    break;
                case 7:
                    reportes.listarReportes();
                    break;
                case 8:
                    reportes.actualizarEstado(leerInt("ID reporte: "), "Solucionado", id, usuarios);
                    break;
                case 9:
                    inscripciones.listarInscripciones();
                    break;
                case 10:
                    usuarios.listarUsuarios();
                    break;
                case 11:
                    guardarTodo();
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (op != 11);
    }

    private static void menuResidente(int id) {
        int op;
        do {
            System.out.println("\n===== RESIDENTE =====");
            System.out.println("1. Consultar jornadas");
            System.out.println("2. Inscribirme en una jornada");
            System.out.println("3. Ver inscritos de una jornada");
            System.out.println("4. Reportar una incidencia");
            System.out.println("5. Consultar reportes");
            System.out.println("6. Consultar estado de zonas");
            System.out.println("7. Cerrar sesión");
            op = leerInt("Seleccione: ");
            switch (op) {
                case 1:
                    jornadas.listarJornadas();
                    break;
                case 2:
                    inscripciones.crearInscripcion(leerInt("ID inscripción: "), id,
                            leerInt("ID jornada: "), usuarios, jornadas);
                    break;
                case 3:
                    inscripciones.listarPorJornada(leerInt("ID jornada: "));
                    break;
                case 4:
                    reportes.crearReporte(leerInt("ID reporte: "), id,
                            leer("Zona afectada: "), leer("Descripción: "), usuarios);
                    break;
                case 5:
                    reportes.listarReportes();
                    break;
                case 6:
                    zonas.listarZonas();
                    break;
                case 7:
                    guardarTodo();
                    break;
                default:
                    System.out.println("Opción inválida.");
            }
        } while (op != 7);
    }

    private static void crearJornada(int id) {
        int codigo = leerInt("ID jornada: ");
        String fecha = leer("Fecha (AAAA-MM-DD): ");
        String hora = leer("Hora (HH:MM): ");
        String zona = leer("Zona común: ");
        String materiales = leer("Materiales necesarios: ");
        String responsable = leer("Residente o torre responsable: ");
        jornadas.crearJornada(codigo, fecha, hora, zona, materiales, responsable, id, usuarios);
    }

    private static void crearZona(int id) {
        zonas.crearZona(leerInt("ID zona: "), leer("Nombre de la zona: "),
                leer("Estado (Limpia/En proceso/Dañada): "), id, usuarios);
    }

    private static String leer(String mensaje) {
        System.out.print(mensaje);
        return sc.nextLine().trim();
    }

    private static int leerInt(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    private static void guardarTodo() {
        usuarios.guardarArchivo();
        administradores.guardarArchivo();
        jornadas.guardarArchivo();
        reportes.guardarArchivo();
        zonas.guardarArchivo();
        inscripciones.guardarArchivo();
    }
}

/*
 * Proyecto: EcoVillas
 * Archivo: Principal.java
 * Programador: Harol Stiven Holguín Ospina
 * Fecha: 29/10/2026
 *
 * Objetivo: Integrar las entidades mediante arreglos de objetos.
 * HU-01/HU-02: Crear y consultar jornadas.
 * HU-03: Inscripción a jornadas sin duplicados.
 * HU-04: Registrar y consultar reportes.
 * HU-05/HU-06: Actualizar y consultar estados de zonas.
 *
 * Nota: almacenamiento temporal en memoria. No usa ArrayList.
 */

import java.util.Scanner;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;

public class Principal {

    static Scanner teclado = new Scanner(System.in);

    static Usuario[] usuarios = new Usuario[100];
    static Administrador[] administradores = new Administrador[16];
    static Zona_Comun[] zonas = new Zona_Comun[100];
    static Jornadas[] jornadas = new Jornadas[100];
    static Reportes[] reportes = new Reportes[100];
    static Inscripciones_jornada[] inscripciones = new Inscripciones_jornada[200];

    static int totalUsuarios = 0;
    static int totalAdministradores = 0;
    static int totalZonas = 0;
    static int totalJornadas = 0;
    static int totalReportes = 0;
    static int totalInscripciones = 0;

    static Pila pilaEliminaciones = new Pila(100);
    static Cola colaReportes = new Cola(100);

    static int siguienteIdUsuario = 1;
    static int siguienteIdAdministrador = 1;
    static int siguienteIdZona = 1;
    static int siguienteIdJornada = 1;
    static int siguienteIdReporte = 1;
    static int siguienteIdInscripcion = 1;

    public static void main(String[] args) {
        cargarDatosIniciales();

        int opcion;
        do {
            System.out.println("\n========== ECOVILLAS ==========");
            System.out.println("1. Ingresar como usuario");
            System.out.println("2. Registrarse como usuario");
            System.out.println("3. Ingresar como administrador");
            System.out.println("0. Salir");
            opcion = leerEntero("Seleccione una opción: ");

            switch (opcion) {
                case 1:
                    iniciarSesionUsuario();
                    break;
                case 2:
                    registrarUsuario();
                    break;
                case 3:
                    iniciarSesionAdministrador();
                    break;
                case 0:
                    System.out.println("Gracias por utilizar EcoVillas.");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);

        teclado.close();
    }

    // Datos de ejemplo para que se puedan probar los módulos inmediatamente.
    // En una versión posterior se podrán crear desde un módulo de administración.
    static void cargarDatosIniciales() {
        usuarios[totalUsuarios++] = new Usuario(
                siguienteIdUsuario++, "Ana", "Gómez", "3001112233",
                "ana@ecovillas.com");
        usuarios[totalUsuarios++] = new Usuario(
                siguienteIdUsuario++, "Carlos", "Pérez", "3004445566",
                "carlos@ecovillas.com");

        administradores[totalAdministradores++] = new Administrador(
                siguienteIdAdministrador++, 1, 1);

        zonas[totalZonas++] = new Zona_Comun(
                siguienteIdZona++, "Zona verde principal", "Limpia",
                "Bajo", "", LocalDate.now().toString(), 1, "");

        zonas[totalZonas++] = new Zona_Comun(
                siguienteIdZona++, "Sendero de la torre 2", "En proceso",
                "Medio", "", LocalDate.now().toString(), 1, "");

        jornadas[totalJornadas++] = new Jornadas(
                siguienteIdJornada++, LocalDate.now().plusDays(2).toString(),
                "09:00", "Zona verde principal",
                "Guantes, bolsas y pinzas", "Ana Gómez");

        System.out.println("Se cargaron datos de ejemplo para probar el sistema.");
        System.out.println("Usuario de prueba: ID 2 (Carlos Pérez).");
        System.out.println("Administrador de prueba: ID de usuario 1 (Ana Gómez).");
    }

    // Permite ingresar usando el ID existente.
    static void iniciarSesionUsuario() {
        int id = leerEntero("Ingrese su ID de usuario: ");
        Usuario usuario = buscarUsuario(id);

        if (usuario == null) {
            System.out.println("No existe ese usuario. Puede registrarse desde el menú principal.");
            return;
        }

        System.out.println("Bienvenido/a, " + usuario.getNombre() + ".");
        menuUsuario(usuario);
    }

    static void menuUsuario(Usuario usuario) {
        int opcion;
        do {
            System.out.println("\n====== MENÚ DE USUARIO ======");
            System.out.println("Sesión: " + usuario.getNombre() + " " + usuario.getApellido());
            System.out.println("1. Registrar reporte ambiental");
            System.out.println("2. Consultar estado de zonas");
            System.out.println("3. Consultar jornadas");
            System.out.println("4. Inscribirme a una jornada");
            System.out.println("5. Consultar mis inscripciones");
            System.out.println("0. Cerrar sesión");

            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1:
                    crearReporte(usuario);
                    break;
                case 2:
                    consultarZonas();
                    break;
                case 3:
                    consultarJornadas();
                    break;
                case 4:
                    inscribirUsuario(usuario);
                    break;
                case 5:
                    consultarInscripcionesUsuario(usuario);
                    break;
                case 0:
                    System.out.println("Sesión cerrada.");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // Registro público de usuarios.
    static void registrarUsuario() {
        if (totalUsuarios >= usuarios.length) {
            System.out.println("No hay espacio para más usuarios.");
            return;
        }

        System.out.println("\n====== REGISTRO DE USUARIO ======");
        String nombre = leerTexto("Nombre: ");
        String apellido = leerTexto("Apellido: ");
        String telefono = leerTexto("Teléfono (puede dejarlo vacío): ");
        String correo = leerTexto("Correo: ");

        if (nombre.isEmpty() || apellido.isEmpty() || correo.isEmpty()) {
            System.out.println("Nombre, apellido y correo son obligatorios.");
            return;
        }
        if (buscarUsuarioPorCorreo(correo) != null) {
            System.out.println("Ya existe un usuario con ese correo.");
            return;
        }

        Usuario nuevo = new Usuario(siguienteIdUsuario++, nombre, apellido,
                telefono, correo);
        usuarios[totalUsuarios++] = nuevo;

        System.out.println("Usuario registrado correctamente. Su ID es: "
                + nuevo.getId_usuario());
        System.out.println("Guarde su ID para iniciar sesión.");
    }

    static void iniciarSesionAdministrador() {
        int idUsuario = leerEntero("Ingrese su ID de usuario administrador: ");
        Usuario usuario = buscarUsuario(idUsuario);

        if (usuario == null || buscarAdministradorPorUsuario(idUsuario) == null) {
            System.out.println("Acceso denegado. El ID no corresponde a un administrador registrado.");
            return;
        }

        System.out.println("Bienvenido/a, administrador " + usuario.getNombre() + ".");
        menuAdministrador(usuario);
    }

    static void menuAdministrador(Usuario usuario) {
        int opcion;
        do {
            System.out.println("\n====== MENÚ DE ADMINISTRADOR ======");
            System.out.println("Sesión: " + usuario.getNombre() + " " + usuario.getApellido());
            System.out.println("1. Crear jornada de limpieza");
            System.out.println("2. Cambiar estado de una zona");
            System.out.println("3. Consultar estados de zonas");
            System.out.println("4. Consultar jornadas");
            System.out.println("5. Consultar reportes");
            System.out.println("6. Atender siguiente reporte de la cola");
            System.out.println("0. Cerrar sesión");

            opcion = leerEntero("Seleccione una opción: ");
            switch (opcion) {
                case 1:
                    crearJornada();
                    break;
                case 2:
                    actualizarZona(buscarAdministradorPorUsuario(usuario.getId_usuario()));
                    break;
                case 3:
                    consultarZonas();
                    break;
                case 4:
                    consultarJornadas();
                    break;
                case 5:
                    consultarReportes();
                    break;
                case 6:
                    atenderSiguienteReporte();
                    break;
                case 0:
                    System.out.println("Sesión cerrada.");
                    break;
                default:
                    System.out.println("Opción no válida.");
            }
        } while (opcion != 0);
    }

    // Crea un reporte y lo agrega a los arreglos y a la cola de atención.
    static void crearReporte(Usuario usuario) {
        if (totalReportes >= reportes.length) {
            System.out.println("No hay espacio para más reportes.");
            return;
        }

        System.out.println("\n====== NUEVO REPORTE ======");
        String ubicacion = leerTexto("Ubicación: ");
        String descripcion = leerTexto("Descripción del problema ambiental: ");
        String foto = leerTexto("Foto o ruta de imagen (opcional): ");

        if (ubicacion.isEmpty() || descripcion.isEmpty()) {
            System.out.println("La ubicación y la descripción son obligatorias.");
            return;
        }

        String fechaHora = LocalDate.now() + " " +
                LocalTime.now().withSecond(0).withNano(0);

        Reportes reporte = new Reportes(siguienteIdReporte++,
                usuario.getId_usuario(), ubicacion, descripcion, foto,
                fechaHora, "Pendiente");

        reportes[totalReportes++] = reporte;
        if (!colaReportes.encolar(reporte)) {
            System.out.println("Advertencia: el reporte se guardó, pero la cola está llena.");
        }

        System.out.println("Reporte registrado. Número de reporte: "
                + reporte.getId_reporte());
    }

    static void consultarZonas() {
        System.out.println("\n====== ESTADO DE ZONAS ======");
        if (totalZonas == 0) {
            System.out.println("No hay zonas registradas.");
            return;
        }

        for (int i = 0; i < totalZonas; i++) {
            System.out.println("\n-----------------------------");
            zonas[i].mostrarInformacion();
        }
    }

    // Solo se llama desde el menú de administrador.
    static void actualizarZona(Administrador administrador) {
        if (totalZonas == 0) {
            System.out.println("No hay zonas registradas.");
            return;
        }

        consultarZonas();
        int idZona = leerEntero("Ingrese el ID del estado de zona que desea modificar: ");
        Zona_Comun zona = buscarZona(idZona);

        if (zona == null) {
            System.out.println("No se encontró esa zona.");
            return;
        }

        System.out.println("Estados permitidos: Limpia, En proceso, Dañada");
        String estado = leerTexto("Nuevo estado: ");
        if (!estado.equals("Limpia") && !estado.equals("En proceso")
                && !estado.equals("Dañada")) {
            System.out.println("Estado no válido.");
            return;
        }

        System.out.println("Niveles permitidos: Bajo, Medio, Alto");
        String nivel = leerTexto("Nuevo nivel de ocupación: ");
        if (!nivel.equals("Bajo") && !nivel.equals("Medio")
                && !nivel.equals("Alto")) {
            System.out.println("Nivel no válido.");
            return;
        }

        String foto = leerTexto("Foto o ruta (opcional; Enter para conservar): ");
        String fechaRecoleccion = leerTexto(
                "Fecha de última recolección (opcional; Enter para conservar): ");

        zona.setEstado(estado);
        zona.setNivel_ocupacion(nivel);
        zona.setFecha_revision(LocalDate.now().toString());
        zona.setId_administrador(administrador.getId_administrador());

        if (!foto.isEmpty()) {
            zona.setFoto(foto);
        }
        if (!fechaRecoleccion.isEmpty()) {
            zona.setFecha_ultima_recoleccion(fechaRecoleccion);
        }

        System.out.println("Estado de zona actualizado correctamente.");
    }

    // Crea una jornada y valida fecha, hora y campos requeridos.
    static void crearJornada() {
        if (totalJornadas >= jornadas.length) {
            System.out.println("No hay espacio para más jornadas.");
            return;
        }

        System.out.println("\n====== CREAR JORNADA ======");
        String fecha = leerTexto("Fecha (AAAA-MM-DD): ");
        LocalDate fechaJornada;

        try {
            fechaJornada = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de fecha inválido. Use AAAA-MM-DD.");
            return;
        }

        if (fechaJornada.isBefore(LocalDate.now())) {
            System.out.println("La fecha no puede ser anterior a hoy.");
            return;
        }

        String hora = leerTexto("Hora (HH:MM): ");
        try {
            LocalTime.parse(hora);
        } catch (DateTimeParseException e) {
            System.out.println("Formato de hora inválido. Use HH:MM.");
            return;
        }

        String zona = leerTexto("Zona donde se realizará: ");
        String materiales = leerTexto("Materiales necesarios: ");
        String responsable = leerTexto("Responsable: ");

        if (zona.isEmpty() || materiales.isEmpty() || responsable.isEmpty()) {
            System.out.println("Zona, materiales y responsable son obligatorios.");
            return;
        }

        Jornadas jornada = new Jornadas(siguienteIdJornada++, fecha, hora,
                zona, materiales, responsable);
        jornadas[totalJornadas++] = jornada;

        System.out.println("Jornada creada correctamente. ID: "
                + jornada.getId_jornada());
    }

    static void consultarJornadas() {
        System.out.println("\n====== JORNADAS DE LIMPIEZA ======");
        if (totalJornadas == 0) {
            System.out.println("No hay jornadas registradas.");
            return;
        }

        for (int i = 0; i < totalJornadas; i++) {
            System.out.println("\n-----------------------------");
            jornadas[i].mostrarInformacion();
            System.out.println("Cupos de almacenamiento: sin límite individual configurado.");
        }
    }

    // Permite varias jornadas distintas, pero impide repetir la misma.
    static void inscribirUsuario(Usuario usuario) {
        if (totalInscripciones >= inscripciones.length) {
            System.out.println("No hay espacio para más inscripciones.");
            return;
        }
        if (totalJornadas == 0) {
            System.out.println("No hay jornadas disponibles.");
            return;
        }

        consultarJornadas();
        int idJornada = leerEntero("Ingrese el ID de la jornada: ");

        if (buscarJornada(idJornada) == null) {
            System.out.println("La jornada indicada no existe.");
            return;
        }

        if (yaEstaInscrito(usuario.getId_usuario(), idJornada)) {
            System.out.println("Ya está inscrito en esta jornada. No puede inscribirse dos veces.");
            return;
        }

        Inscripciones_jornada inscripcion = new Inscripciones_jornada(
                siguienteIdInscripcion++, usuario.getId_usuario(), idJornada);
        inscripciones[totalInscripciones++] = inscripcion;

        System.out.println("Inscripción realizada correctamente.");
    }

    static boolean yaEstaInscrito(int idUsuario, int idJornada) {
        for (int i = 0; i < totalInscripciones; i++) {
            if (inscripciones[i].getId_usuario() == idUsuario
                    && inscripciones[i].getId_jornada() == idJornada) {
                return true;
            }
        }
        return false;
    }

    static void consultarInscripcionesUsuario(Usuario usuario) {
        System.out.println("\n====== MIS INSCRIPCIONES ======");
        boolean encontrada = false;

        for (int i = 0; i < totalInscripciones; i++) {
            if (inscripciones[i].getId_usuario() == usuario.getId_usuario()) {
                inscripciones[i].mostrarInformacion();
                encontrada = true;
                int idJornada = inscripciones[i].getId_jornada();
                Jornadas jornada = buscarJornada(idJornada);
                if (jornada != null) {
                    jornada.mostrarInformacion();
                }
                System.out.println("-----------------------------");
            }
        }

        if (!encontrada) {
            System.out.println("No tiene inscripciones registradas.");
        }
    }

    static void consultarReportes() {
        System.out.println("\n====== REPORTES AMBIENTALES ======");
        if (totalReportes == 0) {
            System.out.println("No hay reportes registrados.");
            return;
        }

        for (int i = 0; i < totalReportes; i++) {
            System.out.println("\n-----------------------------");
            reportes[i].mostrarInformacion();
        }
    }

    // Atiende el reporte más antiguo que siga pendiente.
    static void atenderSiguienteReporte() {
        while (!colaReportes.estaVacia()) {
            Object objeto = colaReportes.desencolar();

            if (objeto instanceof Reportes) {
                Reportes reporte = (Reportes) objeto;
                if ("Pendiente".equals(reporte.getEstado_reporte())) {
                    reporte.setEstado_reporte("Solucionado");
                    System.out.println("Reporte marcado como solucionado:");
                    reporte.mostrarInformacion();
                    return;
                }
            }
        }

        System.out.println("No hay reportes pendientes en la cola.");
    }

    static Usuario buscarUsuario(int id) {
        for (int i = 0; i < totalUsuarios; i++) {
            if (usuarios[i].getId_usuario() == id) {
                return usuarios[i];
            }
        }
        return null;
    }

    static Usuario buscarUsuarioPorCorreo(String correo) {
        for (int i = 0; i < totalUsuarios; i++) {
            if (usuarios[i].getCorreo().equalsIgnoreCase(correo)) {
                return usuarios[i];
            }
        }
        return null;
    }

    static Administrador buscarAdministradorPorUsuario(int idUsuario) {
        for (int i = 0; i < totalAdministradores; i++) {
            if (administradores[i].getId_usuario() == idUsuario) {
                return administradores[i];
            }
        }
        return null;
    }

    static Zona_Comun buscarZona(int id) {
        for (int i = 0; i < totalZonas; i++) {
            if (zonas[i].getId_estado_zona() == id) {
                return zonas[i];
            }
        }
        return null;
    }

    static Jornadas buscarJornada(int id) {
        for (int i = 0; i < totalJornadas; i++) {
            if (jornadas[i].getId_jornada() == id) {
                return jornadas[i];
            }
        }
        return null;
    }

    static int leerEntero(String mensaje) {
        while (true) {
            System.out.print(mensaje);
            String entrada = teclado.nextLine().trim();
            try {
                return Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Ingrese un número entero válido.");
            }
        }
    }

    static String leerTexto(String mensaje) {
        System.out.print(mensaje);
        return teclado.nextLine().trim();
    }
}

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

import java.io.*; // Importa las clases de entrada y salida de archivos, como File, FileReader, FileWriter y lectores/escritores.
import java.security.MessageDigest; // Importa MessageDigest, que permite calcular resúmenes criptográficos como SHA-256.
import java.util.Scanner; // Importa Scanner para leer lo que el usuario escribe por consola.

public class GestionUsuarios { // Declara la clase principal; el nombre debe coincidir con GestionUsuarios.java.

    static final int MAX = 1000; // capacidad del arreglo
    static final String ARCHIVO = "usuarios.txt"; // archivo de datos
    static final String[] RUTAS_APARTAMENTOS = { "../CODIGO-YEIDER/apartamentos.txt", "CODIGO-YEIDER/apartamentos.txt", // Declara las posibles ubicaciones del archivo de apartamentos; String[] es un arreglo de textos.
            "apartamentos.txt" }; // archivo de Apartamento (carpeta de Yeider), para validar la FK

    static Usuario[] usuarios = new Usuario[MAX]; // arreglo con contador (siempre ordenado por id_usuario)
    static int cantidad = 0; // contador de usuarios registrados
    static PilaUsuario pilaEliminados = new PilaUsuario(MAX); // pila propia para deshacer

    // ---------- utilidades internas ----------

    // Busca la posición de un usuario por id_usuario; -1 si no existe.
    static int buscarPosicion(int id_usuario) { // Busca y devuelve la posición del usuario cuyo identificador se recibe.
        for (int i = 0; i < cantidad; i++) // Recorre únicamente las posiciones ocupadas del arreglo, desde cero hasta cantidad - 1.
            if (usuarios[i].id_usuario == id_usuario) // Compara el id del usuario actual con el identificador solicitado.
                return i; // Devuelve la posición encontrada y termina el método.
        return -1; // Devuelve -1 para indicar que el usuario no aparece en el arreglo.
    } // Cierra el método de búsqueda.

    // Devuelve la posición del usuario con ese correo (ignora mayúsculas) o -1;
    // "excepto" se omite al comparar.
    static int buscarPorCorreo(String correo, int excepto) { // Busca un correo y excluye el id indicado en excepto; esto permite actualizar el propio correo.
        for (int i = 0; i < cantidad; i++) // Recorre los usuarios registrados, no la capacidad completa del arreglo.
            if (usuarios[i].id_usuario != excepto && usuarios[i].correo.equalsIgnoreCase(correo)) // Ignora al usuario excepto y compara correos sin distinguir mayúsculas y minúsculas.
                return i; // Devuelve la posición del usuario que tiene ese correo.
        return -1; // Devuelve -1 cuando ningún otro usuario utiliza el correo.
    } // Cierra el método.

    // Inserta manteniendo el orden por id_usuario, desplazando a la derecha (así
    // deshacer devuelve el registro a su sitio).
    static void insertarOrdenado(Usuario u) { // Inserta el objeto recibido conservando el orden ascendente por id_usuario.
        int pos = cantidad; // Comienza desde la primera posición libre, justo después del último usuario.
        while (pos > 0 && usuarios[pos - 1].id_usuario > u.id_usuario) { // Retrocede mientras haya elementos y el anterior tenga un id mayor al nuevo.
            usuarios[pos] = usuarios[pos - 1]; // Mueve el usuario anterior una posición a la derecha para abrir espacio.
            pos--; // Retrocede una posición para seguir buscando dónde insertar.
        } // Cierra el ciclo de desplazamiento.
        usuarios[pos] = u; // Ubica el nuevo usuario en el espacio correcto.
        cantidad++; // Aumenta la cantidad porque ahora hay un usuario más.
    } // Cierra el método.

    // Siguiente identificador incremental cuando no se digita uno.
    static int siguienteId() { // Calcula el siguiente identificador cuando el usuario deja el campo vacío.
        return cantidad == 0 ? 1 : usuarios[cantidad - 1].id_usuario + 1; // Si no hay usuarios empieza en 1; de lo contrario suma uno al id mayor, por estar ordenado.
    } // Cierra el método.

    // Calcula el hash SHA-256 de la contraseña (la clave original nunca se
    // almacena).
    static String hash(String clave) { // Recibe la contraseña original y devuelve su representación hash como String.
        try { // Inicia un bloque que captura posibles errores al crear el hash.
            byte[] h = MessageDigest.getInstance("SHA-256").digest(clave.getBytes("UTF-8")); // Calcula SHA-256 sobre los bytes UTF-8 de la contraseña y guarda los 32 bytes resultantes.
            StringBuilder sb = new StringBuilder(); // Crea un StringBuilder para construir eficientemente el texto hexadecimal.
            for (byte b : h) // Recorre cada byte del resultado criptográfico.
                sb.append(String.format("%02x", b)); // Convierte cada byte a dos caracteres hexadecimales y lo agrega al resultado.
            return sb.toString(); // Devuelve el hash completo como texto hexadecimal.
        } catch (Exception e) { // Captura cualquier excepción producida durante el cálculo.
            throw new RuntimeException(e); // Lanza una excepción de ejecución para no continuar silenciosamente sin hash.
        } // Cierra el catch.
    } // Cierra el método hash.

    // Busca un archivo de datos del OTRO programa: prueba varias rutas y devuelve
    // la
    // primera que exista (funciona se ejecute desde la carpeta del programa o desde
    // la carpeta que contiene a las dos). Si ninguna existe, devuelve la primera.
    static File buscarArchivo(String... rutas) { // Recibe una cantidad variable de rutas y devuelve el archivo que encuentre.
        for (String r : rutas) // Prueba cada ruta recibida, una por una.
            if (new File(r).exists()) // Comprueba si existe un archivo en la ruta actual.
                return new File(r); // Devuelve el primer archivo existente.
        return new File(rutas[0]); // Si ninguna ruta existe, devuelve un File con la primera ruta como ubicación predeterminada.
    } // Cierra el método.

    // FK id_apartamento: indica si el apartamento existe en apartamentos.txt
    // (formato de cada línea: id_apartamento;numero_apartamento;id_torre).
    // Si el archivo no existe, no hay apartamentos registrados y devuelve false.
    static boolean apartamentoExiste(int id_apartamento) { // Devuelve true si el apartamento indicado aparece en el archivo y false si no.
        File f = buscarArchivo(RUTAS_APARTAMENTOS); // Busca apartamentos probando las rutas configuradas.
        if (!f.exists()) // Comprueba si el archivo encontrado realmente existe.
            return false; // Sin archivo no hay forma de validar el apartamento, por eso devuelve false.
        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // Abre el archivo para lectura y crea un lector eficiente; try-with-resources lo cierra automáticamente.
            String linea; // Declara la variable que contendrá cada línea leída.
            while ((linea = br.readLine()) != null) { // Lee una línea por vuelta; termina cuando readLine devuelve null.
                String[] c = linea.split(";", -1); // Divide la línea usando punto y coma; -1 conserva también campos vacíos al final.
                if (c.length == 3 && c[0].trim().equals(String.valueOf(id_apartamento))) // Verifica que existan tres columnas y que la primera coincida con el id buscado.
                    return true; // Devuelve true al encontrar el apartamento.
            } // Cierra el ciclo de lectura.
        } catch (IOException e) { // Captura errores de lectura del archivo.
            System.out.println("No se pudo leer " + f.getPath() + ": " + e.getMessage()); // Muestra la ruta y el detalle del error para facilitar su diagnóstico.
        } // Cierra el bloque catch.
        return false; // Devuelve false si terminó la búsqueda sin encontrar el apartamento.
    } // Cierra el método.

    // RN-04 — formato de correo válido.
    static boolean correoValido(String c) { // Valida que el correo cumpla el patrón definido y devuelve un booleano.
        return c.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"); // matches compara todo el texto con el patrón de correo; true significa que cumple el formato.
    } // Cierra el método.

    // Texto solo con letras y espacios, no vacío.
    static boolean soloLetras(String t) { // Comprueba que el texto tenga únicamente letras permitidas y espacios.
        return t.matches("^[A-Za-zÁÉÍÓÚÜÑáéíóúüñ ]+$"); // La expresión regular admite letras latinas, tildes, ñ y espacios; no admite números ni símbolos.
    } // Cierra el método.

    // Teléfono opcional: solo números, "+", "-" y espacios.
    static boolean telefonoValido(String t) { // Valida los caracteres permitidos para un teléfono.
        return t.matches("^[0-9+\\- ]+$"); // Admite números, signo +, guion y espacios.
    } // Cierra el método.

    // Valida el correo (formato, longitud y unicidad) y el teléfono; imprime el
    // motivo y devuelve false si algo falla.
    static boolean validarCorreoYTelefono(String correo, String telefono, int id_propio) { // Valida correo y teléfono; id_propio permite excluir al usuario que se está actualizando.
        if (correo.isEmpty()) { // Comprueba si el correo obligatorio está vacío.
            System.out.println("Falta el campo obligatorio: correo"); // Informa cuál es el campo faltante.
            return false; // Devuelve false porque la validación falló.
        } // Cierra el if.
        if (correo.length() > 100 || !correoValido(correo)) { // Rechaza correos de más de 100 caracteres o con formato incorrecto.
            System.out.println("Formato de correo inválido"); // Informa que el correo no cumple el formato.
            return false; // Devuelve false ante un correo inválido.
        } // Cierra el if.
        if (buscarPorCorreo(correo, id_propio) != -1) { // Busca si otro usuario ya tiene ese correo; -1 significa que no hay coincidencia.
            System.out.println("El correo ya está registrado"); // Informa que el correo no es único.
            return false; // Devuelve false porque no se permiten correos duplicados.
        } // Cierra el if.
        if (!telefono.isEmpty() && (telefono.length() > 15 || !telefonoValido(telefono))) { // Si el teléfono no está vacío, valida longitud máxima y caracteres permitidos.
            System.out.println("Formato de teléfono inválido"); // Informa que el teléfono no cumple las reglas.
            return false; // Devuelve false ante un teléfono inválido.
        } // Cierra el if.
        return true; // Devuelve true cuando todas las validaciones pasan.
    } // Cierra el método de validación.

    // ---------- las siete operaciones ----------

    // RF-001 · CA-USU-01-01 — crea el usuario Activo con contraseña cifrada.
    // Rechaza id repetido, campos vacíos (CA-USU-01-02),
    // correo repetido (CA-USU-01-03) o inválido (CA-USU-01-04), contraseña corta
    // (CA-USU-01-05), nombres con símbolos (CA-USU-01-06)
    // y teléfono inválido (CA-USU-01-08). El teléfono vacío es válido
    // (CA-USU-01-07).
    static boolean crearUsuario(int id_usuario, String nombres, String apellidos, String correo, String telefono, // Declara la operación de creación; recibe datos personales, credenciales, rol y apartamento.
            String contrasena, int id_rol, int id_apartamento) { // Completa la lista de parámetros; el método devuelve true si crea el usuario y false si lo rechaza.
        nombres = nombres.trim(); // Elimina espacios al inicio y al final de los nombres.
        apellidos = apellidos.trim(); // Limpia los espacios externos de los apellidos.
        correo = correo.trim(); // Limpia los espacios externos del correo.
        telefono = telefono.trim(); // Limpia los espacios externos del teléfono.
        if (cantidad == MAX) { // Comprueba si el contador alcanzó la capacidad máxima.
            System.out.println("No hay espacio: el arreglo está lleno"); // Informa que no hay espacio disponible.
            return false; // Detiene la creación e indica fallo.
        } // Cierra la condición.
        if (id_usuario <= 0) { // El identificador debe ser positivo.
            System.out.println("El id_usuario debe ser un entero mayor a cero"); // Muestra el motivo del rechazo.
            return false; // Devuelve false porque el id no es válido.
        } // Cierra la condición.
        if (buscarPosicion(id_usuario) != -1) { // Verifica que no exista otro usuario con el mismo identificador.
            System.out.println("El id_usuario " + id_usuario + " ya existe"); // Muestra el identificador duplicado.
            return false; // Rechaza la creación.
        } // Cierra la condición.
        if (nombres.isEmpty() || apellidos.isEmpty() || contrasena.isEmpty() || id_rol <= 0 || id_apartamento <= 0) { // Verifica los campos obligatorios y que rol y apartamento sean positivos.
            System.out.println("Faltan campos obligatorios: nombres, apellidos, correo, contraseña, rol y apartamento"); // Indica que faltan datos obligatorios.
            return false; // Detiene la creación.
        } // Cierra la condición.
        if (!apartamentoExiste(id_apartamento)) { // Comprueba la existencia del apartamento en apartamentos.txt.
            System.out.println("El apartamento no existe"); // Informa que la referencia al apartamento no existe.
            return false; // Rechaza la creación para mantener la integridad referencial.
        } // Cierra la condición.
        if (nombres.length() > 80 || apellidos.length() > 80 || !soloLetras(nombres) || !soloLetras(apellidos)) { // Limita nombres y apellidos a 80 caracteres y exige solo letras y espacios.
            System.out.println("Los nombres y apellidos solo admiten letras y espacios"); // Explica el formato permitido.
            return false; // Devuelve false cuando los nombres no cumplen las reglas.
        } // Cierra la condición.
        if (!validarCorreoYTelefono(correo, telefono, -1)) // Valida formato, longitud y unicidad del correo, además del teléfono.
            return false; // Detiene la creación si falla alguna de esas validaciones.
        if (contrasena.length() < 8) { // Comprueba que la contraseña tenga al menos ocho caracteres.
            System.out.println("La contraseña debe tener mínimo 8 caracteres"); // Informa la longitud mínima requerida.
            return false; // Rechaza la contraseña corta.
        } // Cierra la condición.
        insertarOrdenado(new Usuario(id_usuario, nombres, apellidos, correo, telefono, hash(contrasena), "Activo", // Crea el objeto Usuario y aplica hash a la contraseña antes de almacenarla; el estado inicial es Activo.
                id_rol, id_apartamento)); // Completa los parámetros del constructor con el rol y el apartamento.
        System.out.println("Usuario creado correctamente"); // Confirma que la creación terminó correctamente.
        return true; // Devuelve true para indicar que el usuario fue creado.
    } // Cierra el método.

    // RF-002 · CA-USU-02-01 — devuelve el usuario con ese id (sin mostrar
    // contraseña al imprimirlo); CA-USU-02-02 — null si no existe.
    static Usuario consultarUsuario(int id_usuario) { // Busca y devuelve el usuario solicitado; null representa que no existe.
        int pos = buscarPosicion(id_usuario); // Obtiene la posición del id usando el método de búsqueda.
        if (pos == -1) { // Comprueba si la búsqueda devolvió -1.
            System.out.println("No se encontró el usuario"); // Informa que no se encontró el registro.
            return null; // Devuelve null para señalar que no hay objeto Usuario.
        } // Cierra la condición.
        return usuarios[pos]; // Devuelve el objeto Usuario ubicado en esa posición.
    } // Cierra el método.

    // RF-003 · CA-USU-03-01 — actualiza teléfono, correo, rol, apartamento y
    // estado. Rechaza si no existe, correo de otro usuario
    // (CA-USU-03-02), correo inválido (CA-USU-03-03), campos obligatorios vacíos
    // (CA-USU-03-04) o estado distinto de Activo/Inactivo.
    // Cambiar el estado a Activo reactiva al usuario (CA-USU-03-07).
    static boolean actualizarUsuario(int id_usuario, String telefono, String correo, int id_rol, int id_apartamento, // Declara la actualización; recibe id, teléfono, correo, rol, apartamento y estado.
            String estado) { // Completa los parámetros; devuelve true si actualiza y false si alguna regla falla.
        telefono = telefono.trim(); // Limpia espacios externos del teléfono.
        correo = correo.trim(); // Limpia espacios externos del correo.
        estado = estado.trim(); // Limpia espacios externos del estado.
        int pos = buscarPosicion(id_usuario); // Busca la posición del usuario que se actualizará.
        if (pos == -1) { // Comprueba si el usuario no existe.
            System.out.println("No se encontró el usuario"); // Informa que no se encontró.
            return false; // Devuelve false y no modifica ningún dato.
        } // Cierra la condición.
        if (correo.isEmpty() || id_rol <= 0 || id_apartamento <= 0 || estado.isEmpty()) { // Verifica que correo, rol, apartamento y estado sean obligatorios y válidos en lo básico.
            System.out.println("Faltan campos obligatorios: correo, rol, apartamento y estado"); // Informa que faltan campos.
            return false; // Rechaza la actualización.
        } // Cierra la condición.
        if (!apartamentoExiste(id_apartamento)) { // Verifica que el apartamento nuevo exista en el archivo externo.
            System.out.println("El apartamento no existe"); // Informa que el apartamento no está registrado.
            return false; // Rechaza el cambio.
        } // Cierra la condición.
        if (!estado.equalsIgnoreCase("Activo") && !estado.equalsIgnoreCase("Inactivo")) { // Acepta únicamente Activo o Inactivo, sin distinguir mayúsculas.
            System.out.println("El estado debe ser Activo o Inactivo"); // Muestra los dos estados permitidos.
            return false; // Devuelve false si el estado no corresponde.
        } // Cierra la condición.
        if (!validarCorreoYTelefono(correo, telefono, id_usuario)) // Valida el correo y teléfono, excluyendo el propio id para permitir conservar su correo.
            return false; // No continúa si los datos de contacto no pasan la validación.
        Usuario u = usuarios[pos]; // Obtiene la referencia al objeto que se actualizará.
        u.telefono = telefono; // Asigna el nuevo teléfono al objeto.
        u.correo = correo; // Asigna el nuevo correo al objeto.
        u.id_rol = id_rol; // Actualiza el identificador del rol.
        u.id_apartamento = id_apartamento; // Actualiza el identificador del apartamento.
        u.estado = estado.equalsIgnoreCase("Activo") ? "Activo" : "Inactivo"; // Normaliza el estado guardando exactamente Activo o Inactivo.
        System.out.println("Usuario actualizado correctamente"); // Confirma que la actualización fue exitosa.
        return true; // Devuelve true para indicar que los cambios se aplicaron.
    } // Cierra el método.

    // RF-004 · CA-USU-04-01 — elimina el usuario desplazando los siguientes una
    // posición a la izquierda (sin hueco) y lo apila
    // para poder deshacer. Rechaza si no existe o si la pila está llena.
    static Usuario eliminarUsuario(int id_usuario) { // Elimina el usuario indicado y devuelve el objeto eliminado; null indica que no se pudo.
        int pos = buscarPosicion(id_usuario); // Busca la posición del usuario que se quiere eliminar.
        if (pos == -1) { // Comprueba si no se encontró.
            System.out.println("No se encontró el usuario"); // Muestra el mensaje de usuario inexistente.
            return null; // Devuelve null porque no hay registro para eliminar.
        } // Cierra la condición.
        Usuario eliminado = usuarios[pos]; // Conserva la referencia al usuario antes de quitarlo del arreglo.
        if (!pilaEliminados.apilar(eliminado)) { // Intenta guardar primero el usuario en la pila; si falla no se elimina.
            System.out.println("No se puede eliminar: la pila de deshacer está llena"); // Informa que la pila alcanzó su capacidad.
            return null; // Devuelve null y conserva el usuario en el arreglo.
        } // Cierra la condición.
        for (int i = pos; i < cantidad - 1; i++) // Recorre desde la posición eliminada hasta el penúltimo usuario.
            usuarios[i] = usuarios[i + 1]; // desplaza, no deja null en el medio
        usuarios[cantidad - 1] = null; // Limpia la última posición para no dejar una referencia repetida.
        cantidad--; // Reduce el contador porque ahora hay un usuario menos.
        System.out.println("Usuario eliminado correctamente"); // Confirma la eliminación.
        return eliminado; // Devuelve el objeto eliminado, que también quedó guardado en la pila.
    } // Cierra el método.

    // RF-005 · CA-USU-05-01 — lista nombre, estado y demás datos; el filtro por
    // estado (CA-USU-05-03) es null para mostrar todos.
    // CA-USU-05-04 — si no hay usuarios muestra "Aún no hay usuarios registrados"
    // en lugar de una lista vacía.
    static void listarUsuarios(String filtroEstado) { // Lista los usuarios; null como filtro significa mostrar todos los estados.
        if (cantidad == 0) { // Comprueba si el arreglo está vacío según el contador.
            System.out.println("Aún no hay usuarios registrados"); // Informa que todavía no hay registros.
            return; // Sale del método porque no hay nada que listar.
        } // Cierra la condición.
        System.out.printf("%-5s %-28s %-30s %-9s %-4s %-5s%n", "ID", "Nombre", "Correo", "Estado", "Rol", "Apto"); // Imprime los encabezados; los formatos %- indican ancho y alineación de columnas.
        int mostrados = 0; // Cuenta cuántos usuarios pasan el filtro.
        for (int i = 0; i < cantidad; i++) { // Recorre los usuarios registrados.
            Usuario u = usuarios[i]; // Obtiene el usuario de la posición actual.
            if (filtroEstado != null && !u.estado.equalsIgnoreCase(filtroEstado)) // Si hay filtro y el estado no coincide, no se debe mostrar este usuario.
                continue; // Salta a la siguiente vuelta del for sin ejecutar la impresión.
            System.out.printf("%-5d %-28s %-30s %-9s %-4d %-5d%n", u.id_usuario, u.nombres + " " + u.apellidos, // Imprime los datos del usuario que pasó el filtro.
                    u.correo, u.estado, u.id_rol, u.id_apartamento); // Completa los argumentos de la fila: correo, estado, rol y apartamento.
            mostrados++; // Aumenta el total de usuarios mostrados.
        } // Cierra el ciclo.
        if (mostrados == 0) // Comprueba si ningún usuario coincidió con el filtro.
            System.out.println("No hay usuarios con estado " + filtroEstado); // Informa que no hay usuarios para el estado solicitado.
    } // Cierra el método.

    // RF-004 · CA-USU-04-01 — deshace la última eliminación: devuelve a su sitio el
    // MISMO usuario que salió de la pila.
    // Rechaza si no hay nada que deshacer o si su id o correo ya fueron tomados por
    // otro registro.
    static boolean deshacer() { // Deshace la última eliminación y devuelve true si logra restaurarla.
        Usuario u = pilaEliminados.cima(); // Consulta la cima de la pila sin retirar todavía el usuario.
        if (u == null) { // Comprueba si la pila está vacía; cima devuelve null en ese caso.
            System.out.println("No hay nada que deshacer"); // Informa que no hay una eliminación pendiente.
            return false; // Devuelve false porque no hay nada que restaurar.
        } // Cierra la condición.
        if (cantidad == MAX) { // Comprueba que haya espacio en el arreglo antes de restaurar.
            System.out.println("No hay espacio: el arreglo está lleno"); // Informa que el arreglo está lleno.
            return false; // Detiene la restauración.
        } // Cierra la condición.
        if (buscarPosicion(u.id_usuario) != -1 || buscarPorCorreo(u.correo, -1) != -1) { // Verifica que el id y el correo del usuario eliminado sigan libres.
            System.out.println("No se puede deshacer: el id o el correo ya los usa otro usuario"); // Informa que otro registro ocupa el id o correo.
            return false; // Devuelve false para evitar duplicados.
        } // Cierra la condición.
        if (!apartamentoExiste(u.id_apartamento)) { // Comprueba que el apartamento asociado todavía exista.
            System.out // Comienza la impresión del mensaje de error en la siguiente línea.
                    .println("No se puede deshacer: el apartamento " + u.id_apartamento + " del usuario ya no existe"); // Muestra el apartamento que ya no se encuentra registrado.
            return false; // No restaura el usuario si perdió la validez de su apartamento.
        } // Cierra la condición.
        pilaEliminados.desapilar(); // Retira el usuario de la cima de la pila una vez superadas las validaciones.
        insertarOrdenado(u); // Lo vuelve a insertar ordenadamente en el arreglo.
        System.out.println("Se restauró el usuario " + u.id_usuario + " (" + u.nombres + " " + u.apellidos + ")"); // Confirma qué usuario fue restaurado.
        return true; // Devuelve true porque la operación se completó.
    } // Cierra el método.

    // RF-001 a RF-005 — guarda todos los usuarios en el archivo de datos para que
    // sobrevivan al cierre del programa.
    static void guardarArchivo() { // Guarda el contenido actual del arreglo en usuarios.txt.
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO))) { // Abre el archivo para escritura; FileWriter lo crea o sobrescribe y PrintWriter facilita println.
            for (int i = 0; i < cantidad; i++) { // Recorre las posiciones ocupadas del arreglo.
                Usuario u = usuarios[i]; // Obtiene el usuario que se va a escribir.
                pw.println(u.id_usuario + ";" + u.nombres + ";" + u.apellidos + ";" + u.correo + ";" + u.telefono + ";" // Escribe los campos separados por punto y coma para poder recuperarlos después.
                        + u.contrasena + ";" + u.estado + ";" + u.id_rol + ";" + u.id_apartamento); // Completa la misma línea del registro con estado, rol y apartamento.
            } // Cierra el ciclo de escritura.
        } catch (IOException e) { // Captura errores que ocurran al escribir el archivo.
            System.out.println("No se pudo guardar el archivo: " + e.getMessage()); // Informa por consola el motivo del fallo de guardado.
        } // Cierra el catch.
    } // Cierra el método.

    // RF-001 a RF-005 — carga los usuarios del archivo al iniciar; si no existe,
    // arranca vacío.
    static void cargarArchivo() { // Carga al arreglo los usuarios guardados anteriormente.
        File f = new File(ARCHIVO); // Crea un objeto File que representa usuarios.txt.
        if (!f.exists()) // Comprueba si el archivo todavía no existe.
            return; // Sale sin error; el programa inicia con el arreglo vacío.
        try (BufferedReader br = new BufferedReader(new FileReader(f))) { // Abre el archivo para lectura con un BufferedReader.
            String linea; // Declara la variable que recibirá cada línea.
            while ((linea = br.readLine()) != null && cantidad < MAX) { // Lee líneas hasta el final o hasta llenar el arreglo.
                String[] c = linea.split(";", -1); // Separa los nueve campos del registro usando ; y conserva campos vacíos.
                if (c.length != 9) // Comprueba que la línea tenga exactamente nueve columnas.
                    continue; // Ignora una línea mal formada y continúa con la siguiente.
                insertarOrdenado(new Usuario(Integer.parseInt(c[0]), c[1], c[2], c[3], c[4], c[5], c[6], // Reconstruye el Usuario convirtiendo los campos numéricos y lo inserta ordenado.
                        Integer.parseInt(c[7]), Integer.parseInt(c[8]))); // Completa el constructor con id_rol e id_apartamento.
            } // Cierra el ciclo de lectura.
        } catch (Exception e) { // Captura errores de lectura o conversión de datos.
            System.out.println("No se pudo leer el archivo: " + e.getMessage()); // Muestra el motivo por el que no se pudo cargar el archivo.
        } // Cierra el catch.
    } // Cierra el método.

    // ---------- menú ----------

    static Scanner sc = new Scanner(System.in); // Crea un Scanner asociado a la entrada estándar para leer datos de consola.

    // Lee un entero; devuelve 0 si lo digitado no es número (los validadores lo
    // rechazan).
    static int leerEntero(String pregunta) { // Lee un entero y conserva el comportamiento original: devuelve 0 si la entrada no es numérica.
        System.out.print(pregunta); // Muestra la pregunta antes de leer.
        try { // Intenta convertir la línea ingresada a un entero.
            return Integer.parseInt(sc.nextLine().trim()); // Lee la línea, quita espacios externos y la convierte a int.
        } catch (NumberFormatException e) { // Captura el error cuando el texto no representa un entero.
            return 0; // Devuelve 0, tal como estaba definido originalmente.
        } // Cierra el catch.
    } // Cierra el método.

    // Lee la opción del menú de forma independiente para no confundir letras con la opción 0.
    static int leerOpcionMenu(String pregunta) { // Recibe el mensaje que se mostrará y devuelve un entero o -1 si no es válido.
        System.out.print(pregunta); // Muestra la pregunta antes de leer la opción.
        try { // Intenta convertir lo ingresado a un número entero.
            return Integer.parseInt(sc.nextLine().trim()); // Devuelve el número si la conversión es correcta.
        } catch (NumberFormatException e) { // Captura letras u otro texto que no pueda convertirse a entero.
            return -1; // Usa -1 para que el switch llegue a default y no confunda el error con salir.
        } // Termina el bloque de captura de error.
    } // Termina leerOpcionMenu.

    static String leerTexto(String pregunta) { // Declara la lectura de texto y devuelve una cadena.
        System.out.print(pregunta); // Muestra el texto que solicita el dato.
        return sc.nextLine(); // Lee y devuelve la línea completa escrita por el usuario.
    } // Cierra el método.

    public static void main(String[] args) { // Punto de entrada del programa; args recibe argumentos opcionales de ejecución.
        cargarArchivo(); // Carga los usuarios guardados antes de mostrar el menú.
        int op; // Declara la variable que almacena la opción elegida.
        do { // Inicia un do-while para mostrar el menú al menos una vez.
            System.out.println("\n=== Villas del Socorro · Usuarios (" + cantidad + " registrados) ==="); // Muestra el título del sistema y la cantidad actual de usuarios.
            System.out.println("1. Crear usuario"); // Presenta la opción para crear usuarios.
            System.out.println("2. Consultar usuario"); // Presenta la opción para consultar un usuario.
            System.out.println("3. Actualizar usuario"); // Presenta la opción para actualizar datos.
            System.out.println("4. Eliminar usuario"); // Presenta la opción para eliminar usuarios.
            System.out.println("5. Listar usuarios"); // Presenta la opción para listar usuarios.
            System.out.println("6. Deshacer última eliminación"); // Presenta la opción para deshacer la última eliminación.
            System.out.println("0. Guardar y salir"); // Presenta la opción cero para guardar y terminar.
            op = leerOpcionMenu("Opción: "); // Lee la opción principal con el método nuevo; letras producen -1 y llegan a default.
            switch (op) { // Evalúa la opción y ejecuta el case correspondiente.
                case 1: { // Inicia el bloque de la opción 1: crear usuario.
                    String txt = leerTexto("id_usuario (Enter = automático): ").trim(); // Solicita el id; Enter vacío indica que se genere automáticamente.
                    int id = txt.isEmpty() ? siguienteId() : -1; // Si el texto está vacío usa el siguiente id; si no, deja -1 para convertirlo.
                    if (id == -1) { // Solo convierte el texto cuando no se generó un id automático.
                        try { // Intenta convertir el identificador escrito.
                            id = Integer.parseInt(txt); // Convierte el texto a entero.
                        } catch (NumberFormatException e) { // Captura si el id escrito no es numérico.
                            id = 0; // Usa cero para que la validación de crearUsuario lo rechace.
                        } // Cierra el catch.
                    } // Cierra el if.
                    String nom = leerTexto("nombres: "), ape = leerTexto("apellidos: "), cor = leerTexto("correo: "); // Solicita nombres, apellidos y correo.
                    String tel = leerTexto("telefono (opcional): "), con = leerTexto("contrasena: "); // Solicita teléfono opcional y contraseña.
                    int rol = leerEntero("id_rol: "), apto = leerEntero("id_apartamento: "); // Solicita el rol y el apartamento; estas entradas mantienen leerEntero como se pidió.
                    if (crearUsuario(id, nom, ape, cor, tel, con, rol, apto)) // Llama al método de creación y comprueba si tuvo éxito.
                        guardarArchivo(); // Guarda los datos solo cuando la creación se realizó correctamente.
                    break; // Termina el case 1 y evita que se ejecuten otros cases.
                } // Cierra el bloque del case 1.
                case 2: { // Inicia el bloque de la opción 2: consultar usuario.
                    Usuario u = consultarUsuario(leerEntero("id_usuario: ")); // Lee el id y consulta el usuario correspondiente.
                    if (u != null) // Comprueba que la consulta no haya devuelto null.
                        u.mostrar(); // Muestra los datos del usuario mediante su método mostrar.
                    break; // Termina el case 2.
                } // Cierra el bloque del case 2.
                case 3: { // Inicia el bloque de la opción 3: actualizar usuario.
                    int id = leerEntero("id_usuario a actualizar: "); // Solicita el id del usuario que se modificará.
                    if (consultarUsuario(id) == null) // Comprueba si el usuario existe antes de pedir los nuevos datos.
                        break; // Sale del case si no existe el usuario.
                    String tel = leerTexto("nuevo telefono (vacío = sin teléfono): "), // Solicita el nuevo teléfono y correo.
                            cor = leerTexto("nuevo correo: "); // Continúa la declaración del correo solicitado.
                    int rol = leerEntero("nuevo id_rol: "), apto = leerEntero("nuevo id_apartamento: "); // Solicita los nuevos identificadores de rol y apartamento.
                    String est = leerTexto("estado (Activo/Inactivo): "); // Solicita el nuevo estado.
                    if (actualizarUsuario(id, tel, cor, rol, apto, est)) // Ejecuta la actualización y comprueba su resultado.
                        guardarArchivo(); // Guarda el archivo solo si la actualización fue exitosa.
                    break; // Termina el case 3.
                } // Cierra el bloque del case 3.
                case 4: { // Inicia el bloque de la opción 4: eliminar usuario.
                    int id = leerEntero("id_usuario a eliminar: "); // Solicita el identificador del usuario que se eliminará.
                    if (consultarUsuario(id) == null) // Verifica que el usuario exista antes de pedir confirmación.
                        break; // Sale del case si el usuario no existe.
                    String r = leerTexto("¿Confirma eliminar? (s/n): ").trim(); // Solicita confirmación y elimina espacios externos de la respuesta.
                    if (r.equalsIgnoreCase("s")) { // Continúa solo si la respuesta es s, sin distinguir mayúsculas.
                        if (eliminarUsuario(id) != null) // Intenta eliminar y comprueba que el método no devolvió null.
                            guardarArchivo(); // Guarda los cambios si la eliminación se completó.
                    } else // Si no se confirmó, ejecuta el bloque else.
                        System.out.println("Operación cancelada, no se hizo ningún cambio"); // Informa que se canceló sin modificar los datos.
                    break; // Termina el case 4.
                } // Cierra el bloque del case 4.
                case 5: { // Inicia el bloque de la opción 5: listar usuarios.
                    String f = leerTexto("Filtrar por estado (Activo/Inactivo, Enter = todos): ").trim(); // Solicita un filtro de estado; Enter vacío significa sin filtro.
                    listarUsuarios(f.isEmpty() ? null : f); // Pasa null para listar todos o el estado digitado para filtrar.
                    break; // Termina el case 5.
                } // Cierra el bloque del case 5.
                case 6: // Atiende la opción 6, que deshace la última eliminación.
                    if (deshacer()) // Intenta restaurar el último usuario eliminado.
                        guardarArchivo(); // Guarda el archivo únicamente si se restauró correctamente.
                    break; // Termina el case 6.
                case 0: // Atiende la opción 0 para salir.
                    guardarArchivo(); // Guarda los datos antes de cerrar el programa.
                    System.out.println("Datos guardados en " + ARCHIVO + ". Hasta luego."); // Confirma el archivo donde quedaron guardados los datos.
                    break; // Termina el case 0.
                default: // default recibe letras, números fuera del menú y cualquier opción no contemplada.
                    System.out.println("Opción no válida"); // Muestra el mensaje para una opción inválida.
            } // Cierra el switch.
        } while (op != 0); // Repite el menú mientras la opción sea diferente de cero.
    } // Cierra el método main.
} // Cierra la clase GestionUsuarios.

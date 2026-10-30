/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Usuario
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-04, HU-07 | RF: RF-14, RF-15
 * RN: RN-02 | RNF: RNF-01, RNF-02, RNF-03, RNF-04
 * CA: CA-04.1, CA-04.2, CA-07.1, CA-07.2, CA-07.3
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;

public class Usuario {
    private int idUsuario;
    private String nombre, apellido, telefono, correo, contrasena, rol;
    private Usuario[] datos = new Usuario[200];
    private int n = 0;
    private pila<Usuario> eliminados = new pila<>();

    private final String ARCHIVO = "datos/usuarios.txt";

    public Usuario() { cargarArchivo(); }

    private Usuario(int id, String nom, String ape, String tel,
                    String cor, String pass, String rol) {
        this.idUsuario=id; this.nombre=nom; this.apellido=ape;
        this.telefono=tel; this.correo=cor; this.contrasena=pass; this.rol=rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getNombre() { return nombre; }
    public String getRol() { return rol; }

    private int indice(int id) {
        for (int i=0;i<n;i++) if (datos[i].idUsuario==id) return i;
        return -1;
    }

    public boolean existeUsuario(int id) { return indice(id)>=0; }
    public boolean esAdministrador(int id) {
        int i=indice(id); return i>=0 && datos[i].rol.equalsIgnoreCase("Administrador");
    }
    public boolean esResidente(int id) {
        int i=indice(id); return i>=0 && datos[i].rol.equalsIgnoreCase("Residente");
    }

    public boolean crearUsuario(int id, String nombre, String apellido,
            String telefono, String correo, String contrasena, String rol) {
        if (n==200 || id<=0 || indice(id)>=0 || vacio(nombre) || vacio(apellido)
                || vacio(telefono) || vacio(correo) || vacio(contrasena)
                || (!"Residente".equalsIgnoreCase(rol) && !"Administrador".equalsIgnoreCase(rol))
                || contieneSeparador(nombre,apellido,telefono,correo,contrasena)) {
            System.out.println("No se pudo crear: revise los datos o el identificador.");
            return false;
        }
        datos[n++]=new Usuario(id,nombre.trim(),apellido.trim(),telefono.trim(),
                correo.trim(),contrasena,capitalizar(rol));
        guardarArchivo(); return true;
    }

    public boolean autenticar(int id, String clave) {
        int i=indice(id);
        return i>=0 && datos[i].contrasena.equals(clave);
    }

    public void consultarUsuario(int id) {
        int i=indice(id);
        if(i<0) { System.out.println("Usuario no encontrado."); return; }
        mostrar(datos[i]);
    }

    public void buscarPorTexto(String texto) {
        boolean ok=false;
        if(vacio(texto)) return;
        for(int i=0;i<n;i++) {
            Usuario u=datos[i];
            if((u.nombre+" "+u.apellido+" "+u.correo).toLowerCase().contains(texto.toLowerCase())) {
                mostrar(u); ok=true;
            }
        }
        if(!ok) System.out.println("No hay coincidencias.");
    }

    public boolean actualizarUsuario(int id, String nombre, String apellido,
            String telefono, String correo, String contrasena) {
        int i=indice(id);
        if(i<0 || vacio(nombre) || vacio(apellido) || vacio(telefono)
                || vacio(correo) || vacio(contrasena)
                || contieneSeparador(nombre,apellido,telefono,correo,contrasena)) return false;
        Usuario u=datos[i];
        u.nombre=nombre.trim(); u.apellido=apellido.trim(); u.telefono=telefono.trim();
        u.correo=correo.trim(); u.contrasena=contrasena;
        guardarArchivo(); return true;
    }

    public boolean eliminarUsuario(int id) {
        int i=indice(id); if(i<0) return false;
        if(!eliminados.apilar(datos[i])) return false;
        for(int k=i;k<n-1;k++) datos[k]=datos[k+1];
        datos[--n]=null; guardarArchivo(); return true;
    }

    public boolean deshacerEliminacion() {
        Usuario u=eliminados.desapilar();
        if(u==null || n==200 || indice(u.idUsuario)>=0) return false;
        datos[n++]=u; guardarArchivo(); return true;
    }

    public void listarUsuarios() {
        if(n==0) { System.out.println("No hay usuarios."); return; }
        int suma=0;
        for(int i=0;i<n;i++) { mostrar(datos[i]); suma+=datos[i].nombre.length(); }
        System.out.println("Total: "+n+" | Promedio de caracteres en nombres: "+(suma/(double)n));
    }

    private void mostrar(Usuario u) {
        System.out.println("ID: "+u.idUsuario+" | "+u.nombre+" "+u.apellido
                +" | Tel: "+u.telefono+" | Correo: "+u.correo+" | Rol: "+u.rol);
    }

    public void guardarArchivo() {
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))) {
            for(int i=0;i<n;i++) {
                Usuario u=datos[i];
                p.println(u.idUsuario+";"+u.nombre+";"+u.apellido+";"+u.telefono+";"
                        +u.correo+";"+u.contrasena+";"+u.rol);
            }
        } catch(IOException e) { System.out.println("Error guardando usuarios: "+e.getMessage()); }
    }

    private void cargarArchivo() {
        File f=new File(ARCHIVO); if(!f.exists()) return;
        try(BufferedReader br=new BufferedReader(new FileReader(f))) {
            String l;
            while((l=br.readLine())!=null && n<200) {
                String[] d=l.split(";",-1);
                if(d.length==7) {
                    int id=Integer.parseInt(d[0]);
                    if(id>0 && indice(id)<0 && !vacio(d[1]) && !vacio(d[2])
                            && ("Residente".equalsIgnoreCase(d[6]) || "Administrador".equalsIgnoreCase(d[6]))) {
                        datos[n++]=new Usuario(id,d[1],d[2],d[3],d[4],d[5],capitalizar(d[6]));
                    }
                }
            }
        } catch(Exception e) { System.out.println("Error leyendo usuarios: "+e.getMessage()); }
    }

    private static boolean vacio(String s) { return s==null || s.trim().isEmpty(); }
    private static boolean contieneSeparador(String... s) {
        for(String x:s) if(x==null || x.contains(";")) return true;
        return false;
    }
    private static String capitalizar(String s) {
        return s.substring(0,1).toUpperCase()+s.substring(1).toLowerCase();
    }
}

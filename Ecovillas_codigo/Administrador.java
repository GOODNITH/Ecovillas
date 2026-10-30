/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Administrador
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-01, HU-05, HU-07 | RF: RF-01, RF-09, RF-10, RF-14
 * RN: RN-01 | RNF: RNF-01, RNF-02, RNF-04
 * CA: CA-01.1, CA-05.1, CA-05.2, CA-07.1
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;

public class Administrador {
    private int idAdministrador, idUsuario;
    private String torre;
    private Administrador[] datos=new Administrador[200];
    private int n=0;
    private pila<Administrador> eliminados=new pila<>();
    private final String ARCHIVO="datos/administradores.txt";

    public Administrador() { cargarArchivo(); }
    private Administrador(int id,int usuario,String torre) {
        idAdministrador=id; idUsuario=usuario; this.torre=torre;
    }
    private int indice(int id) {
        for(int i=0;i<n;i++) if(datos[i].idAdministrador==id) return i;
        return -1;
    }
    private boolean usuarioRegistrado(int id) {
        for(int i=0;i<n;i++) if(datos[i].idUsuario==id) return true;
        return false;
    }
    public boolean crearAdministrador(int id,int idUsuario,String torre,Usuario usuarios) {
        if(n==200 || id<=0 || indice(id)>=0 || usuarios==null
                || !usuarios.esAdministrador(idUsuario) || usuarioRegistrado(idUsuario)
                || torre==null || torre.trim().isEmpty() || torre.contains(";")) return false;
        datos[n++]=new Administrador(id,idUsuario,torre.trim());
        guardarArchivo(); return true;
    }
    public void consultarAdministrador(int id) {
        int i=indice(id); if(i<0) {System.out.println("Administrador no encontrado.");return;}
        mostrar(datos[i]);
    }
    public void buscarPorTexto(String t) {
        boolean ok=false;
        if(t==null) return;
        for(int i=0;i<n;i++) if(datos[i].torre.toLowerCase().contains(t.toLowerCase())) {mostrar(datos[i]);ok=true;}
        if(!ok) System.out.println("Sin coincidencias.");
    }
    public boolean actualizarAdministrador(int id,String torre) {
        int i=indice(id);
        if(i<0 || torre==null || torre.trim().isEmpty() || torre.contains(";")) return false;
        datos[i].torre=torre.trim(); guardarArchivo(); return true;
    }
    public boolean eliminarAdministrador(int id) {
        int i=indice(id); if(i<0 || !eliminados.apilar(datos[i])) return false;
        for(int k=i;k<n-1;k++) datos[k]=datos[k+1];
        datos[--n]=null; guardarArchivo(); return true;
    }
    public boolean deshacerEliminacion() {
        Administrador a=eliminados.desapilar();
        if(a==null || n==200 || indice(a.idAdministrador)>=0 || usuarioRegistrado(a.idUsuario)) return false;
        datos[n++]=a; guardarArchivo(); return true;
    }
    public void listarAdministradores() {
        if(n==0) {System.out.println("No hay administradores registrados.");return;}
        for(int i=0;i<n;i++) mostrar(datos[i]);
        System.out.println("Total de administradores: "+n);
    }
    private void mostrar(Administrador a) {
        System.out.println("ID administrador: "+a.idAdministrador+" | ID usuario: "+a.idUsuario+" | Torre: "+a.torre);
    }
    public void guardarArchivo() {
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))) {
            for(int i=0;i<n;i++) p.println(datos[i].idAdministrador+";"+datos[i].idUsuario+";"+datos[i].torre);
        } catch(IOException e) {System.out.println("Error guardando administradores.");}
    }
    private void cargarArchivo() {
        File f=new File(ARCHIVO); if(!f.exists()) return;
        try(BufferedReader b=new BufferedReader(new FileReader(f))) {
            String l; while((l=b.readLine())!=null && n<200) {
                String[] d=l.split(";",-1);
                if(d.length==3) {
                    int id=Integer.parseInt(d[0]), u=Integer.parseInt(d[1]);
                    if(id>0 && u>0 && indice(id)<0 && !usuarioRegistrado(u))
                        datos[n++]=new Administrador(id,u,d[2]);
                }
            }
        } catch(Exception e) {System.out.println("Error leyendo administradores.");}
    }
}

/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Zonas_Comun
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-05, HU-06 | RF: RF-09, RF-10, RF-11, RF-12, RF-13
 * RN: RN-01, RN-06 | RNF: RNF-01, RNF-02, RNF-04
 * CA: CA-05.1, CA-05.2, CA-05.3, CA-06.1, CA-06.2, CA-06.3
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Zonas_Comun {
    private int idZona;
    private String nombre,estado,ultimaRecoleccion;
    private Zonas_Comun[] datos=new Zonas_Comun[200];
    private int n=0;
    private pila<Zonas_Comun> eliminados=new pila<>();
    private final String ARCHIVO="datos/estado_zonas.txt";
    private final DateTimeFormatter formato=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Zonas_Comun(){cargarArchivo();}
    private Zonas_Comun(int id,String nom,String est,String fecha){idZona=id;nombre=nom;estado=est;ultimaRecoleccion=fecha;}
    private int indice(int id){for(int i=0;i<n;i++)if(datos[i].idZona==id)return i;return -1;}
    private boolean estadoValido(String e){return "Limpia".equalsIgnoreCase(e)||"En proceso".equalsIgnoreCase(e)||"Dañada".equalsIgnoreCase(e);}
    private boolean valido(String s){return s!=null&&!s.trim().isEmpty()&&!s.contains(";");}

    public boolean crearZona(int id,String nombre,String estado,int admin,Usuario usuarios){
        if(n==200||id<=0||indice(id)>=0||usuarios==null||!usuarios.esAdministrador(admin)||!valido(nombre)||!estadoValido(estado))return false;
        datos[n++]=new Zonas_Comun(id,nombre,normalizar(estado),"No registrada");guardarArchivo();return true;
    }
    public void consultarZona(int id){int i=indice(id);if(i<0){System.out.println("Zona inexistente.");return;}mostrar(datos[i]);}
    public void buscarPorTexto(String t){
        boolean ok=false;if(t==null)return;
        for(int i=0;i<n;i++)if(datos[i].nombre.toLowerCase().contains(t.toLowerCase())){mostrar(datos[i]);ok=true;}
        if(!ok)System.out.println("Zona no encontrada.");
    }
    public boolean actualizarEstado(int id,String nuevo,int admin,Usuario usuarios){
        int i=indice(id);
        if(usuarios==null||!usuarios.esAdministrador(admin)){System.out.println("No tienes permisos para actualizar el estado de la zona.");return false;}
        if(i<0||!estadoValido(nuevo)){System.out.println("Zona o estado inválido.");return false;}
        datos[i].estado=normalizar(nuevo);guardarArchivo();return true;
    }
    public boolean registrarRecoleccion(int id,String fecha,int admin,Usuario usuarios){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esAdministrador(admin))return false;
        try{LocalDateTime.parse(fecha,formato);}catch(Exception e){System.out.println("Formato esperado: dd/MM/yyyy HH:mm");return false;}
        datos[i].ultimaRecoleccion=fecha;guardarArchivo();return true;
    }
    public boolean actualizarZona(int id,String nombre,int admin,Usuario usuarios){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esAdministrador(admin)||!valido(nombre))return false;
        datos[i].nombre=nombre;guardarArchivo();return true;
    }
    public boolean eliminarZona(int id){
        int i=indice(id);if(i<0||!eliminados.apilar(datos[i]))return false;
        for(int k=i;k<n-1;k++)datos[k]=datos[k+1];datos[--n]=null;guardarArchivo();return true;
    }
    public boolean deshacerEliminacion(){
        Zonas_Comun z=eliminados.desapilar();if(z==null||n==200||indice(z.idZona)>=0)return false;
        datos[n++]=z;guardarArchivo();return true;
    }
    public void listarZonas(){
        if(n==0){System.out.println("Aún no hay zonas registradas.");return;}
        int limpias=0;for(int i=0;i<n;i++){mostrar(datos[i]);if(datos[i].estado.equals("Limpia"))limpias++;}
        System.out.println("Total: "+n+" | Zonas limpias: "+limpias);
    }
    private void mostrar(Zonas_Comun z){
        System.out.println("ID: "+z.idZona+" | Zona: "+z.nombre+" | Estado: "+z.estado+" | Última recolección: "+z.ultimaRecoleccion);
    }
    private String normalizar(String e){
        if(e.equalsIgnoreCase("Limpia"))return "Limpia";
        if(e.equalsIgnoreCase("Dañada"))return "Dañada";
        return "En proceso";
    }
    public void guardarArchivo(){
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))){
            for(int i=0;i<n;i++){Zonas_Comun z=datos[i];p.println(z.idZona+";"+z.nombre+";"+z.estado+";"+z.ultimaRecoleccion);}
        }catch(IOException e){System.out.println("Error guardando zonas.");}
    }
    private void cargarArchivo(){
        File f=new File(ARCHIVO);if(!f.exists())return;
        try(BufferedReader b=new BufferedReader(new FileReader(f))){
            String l;while((l=b.readLine())!=null&&n<200){
                String[] d=l.split(";",-1);if(d.length==4){
                    int id=Integer.parseInt(d[0]);
                    if(id>0&&indice(id)<0&&valido(d[1])&&estadoValido(d[2]))
                        datos[n++]=new Zonas_Comun(id,d[1],normalizar(d[2]),d[3]);
                }
            }
        }catch(Exception e){System.out.println("Error leyendo zonas.");}
    }
}

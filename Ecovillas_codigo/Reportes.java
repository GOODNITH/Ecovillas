/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Reportes
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-04 | RF: RF-07, RF-08
 * RN: RN-02 | RNF: RNF-01, RNF-02, RNF-04
 * CA: CA-04.1, CA-04.2, CA-04.3, CA-04.4, CA-04.5
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Reportes {
    private int idReporte,idUsuario;
    private String zona,descripcion,fechaHora,estado;
    private Reportes[] datos=new Reportes[200];
    private int n=0;
    private pila<Reportes> eliminados=new pila<>();
    private final String ARCHIVO="datos/reportes.txt";
    private final DateTimeFormatter formato=DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public Reportes(){cargarArchivo();}
    private Reportes(int id,int u,String z,String d,String f,String e){idReporte=id;idUsuario=u;zona=z;descripcion=d;fechaHora=f;estado=e;}
    private int indice(int id){for(int i=0;i<n;i++)if(datos[i].idReporte==id)return i;return -1;}
    private boolean valido(String s){return s!=null&&!s.trim().isEmpty()&&!s.contains(";");}

    public boolean crearReporte(int id,int usuario,String zona,String descripcion,Usuario usuarios){
        if(n==200||id<=0||indice(id)>=0||usuarios==null||!usuarios.existeUsuario(usuario)||!valido(zona)||!valido(descripcion))return false;
        String fecha=LocalDateTime.now().format(formato);
        datos[n++]=new Reportes(id,usuario,zona,descripcion,fecha,"Pendiente");guardarArchivo();return true;
    }
    public void consultarReporte(int id){int i=indice(id);if(i<0){System.out.println("Reporte no encontrado.");return;}mostrar(datos[i]);}
    public void buscarPorTexto(String t){
        boolean ok=false;if(t==null)return;
        for(int i=0;i<n;i++){Reportes r=datos[i];if((r.zona+" "+r.descripcion+" "+r.estado).toLowerCase().contains(t.toLowerCase())){mostrar(r);ok=true;}}
        if(!ok)System.out.println("Sin coincidencias.");
    }
    public boolean actualizarEstado(int id,String nuevo,int admin,Usuario usuarios){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esAdministrador(admin)
            ||(!"Pendiente".equalsIgnoreCase(nuevo)&&!"Solucionado".equalsIgnoreCase(nuevo)))return false;
        datos[i].estado=nuevo.equalsIgnoreCase("Pendiente")?"Pendiente":"Solucionado";guardarArchivo();return true;
    }
    public boolean actualizarReporte(int id,String z,String d,int admin,Usuario usuarios){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esAdministrador(admin)||!valido(z)||!valido(d))return false;
        datos[i].zona=z;datos[i].descripcion=d;guardarArchivo();return true;
    }
    public boolean eliminarReporte(int id){
        int i=indice(id);if(i<0||!eliminados.apilar(datos[i]))return false;
        for(int k=i;k<n-1;k++)datos[k]=datos[k+1];datos[--n]=null;guardarArchivo();return true;
    }
    public boolean deshacerEliminacion(){
        Reportes r=eliminados.desapilar();if(r==null||n==200||indice(r.idReporte)>=0)return false;
        datos[n++]=r;guardarArchivo();return true;
    }
    public void listarReportes(){
        if(n==0){System.out.println("Aún no hay reportes registrados.");return;}
        int pendientes=0;for(int i=0;i<n;i++){mostrar(datos[i]);if(datos[i].estado.equals("Pendiente"))pendientes++;}
        System.out.println("Total: "+n+" | Pendientes: "+pendientes);
    }
    private void mostrar(Reportes r){
        System.out.println("ID: "+r.idReporte+" | Usuario: "+r.idUsuario+" | Zona: "+r.zona
            +" | Descripción: "+r.descripcion+" | Fecha: "+r.fechaHora+" | Estado: "+r.estado);
    }
    public void guardarArchivo(){
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))){
            for(int i=0;i<n;i++){Reportes r=datos[i];p.println(r.idReporte+";"+r.idUsuario+";"+r.zona+";"+r.descripcion+";"+r.fechaHora+";"+r.estado);}
        }catch(IOException e){System.out.println("Error guardando reportes.");}
    }
    private void cargarArchivo(){
        File f=new File(ARCHIVO);if(!f.exists())return;
        try(BufferedReader b=new BufferedReader(new FileReader(f))){
            String l;while((l=b.readLine())!=null&&n<200){
                String[] d=l.split(";",-1);if(d.length==6){
                    int id=Integer.parseInt(d[0]),u=Integer.parseInt(d[1]);
                    if(id>0&&indice(id)<0&&valido(d[2])&&valido(d[3])
                        &&("Pendiente".equals(d[5])||"Solucionado".equals(d[5])))
                        datos[n++]=new Reportes(id,u,d[2],d[3],d[4],d[5]);
                }
            }
        }catch(Exception e){System.out.println("Error leyendo reportes.");}
    }
}

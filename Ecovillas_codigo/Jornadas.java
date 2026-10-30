/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Jornadas
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-01, HU-02, HU-03 | RF: RF-01, RF-02, RF-03, RF-04
 * RN: RN-03, RN-05 | RNF: RNF-01, RNF-02, RNF-03
 * CA: CA-01.1, CA-01.2, CA-01.3, CA-02.1, CA-02.2, CA-02.3
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;
import java.time.*;

public class Jornadas {
    private int idJornada,idAdministrador;
    private String fecha,hora,zona,materiales,responsable;
    private Jornadas[] datos=new Jornadas[200];
    private int n=0;
    private pila<Jornadas> eliminados=new pila<>();
    private final String ARCHIVO="datos/jornadas.txt";

    public Jornadas(){cargarArchivo();}
    private Jornadas(int id,String f,String h,String z,String m,String r,int admin){
        idJornada=id;fecha=f;hora=h;zona=z;materiales=m;responsable=r;idAdministrador=admin;
    }
    private int indice(int id){for(int i=0;i<n;i++)if(datos[i].idJornada==id)return i;return -1;}
    public boolean existeJornada(int id){return indice(id)>=0;}
    public String getFechaJornada(int id){int i=indice(id);return i<0?null:datos[i].fecha;}

    private boolean validar(String f,String h,String z,String m,String r){
        try { if(LocalDate.parse(f).isBefore(LocalDate.now())) return false; LocalTime.parse(h); }
        catch(Exception e){return false;}
        return valido(z)&&valido(m)&&valido(r);
    }
    private boolean valido(String s){return s!=null&&!s.trim().isEmpty()&&!s.contains(";");}

    public boolean crearJornada(int id,String f,String h,String z,String m,String r,int admin,Usuario usuarios){
        if(n==200||id<=0||indice(id)>=0||usuarios==null||!usuarios.esAdministrador(admin)||!validar(f,h,z,m,r)){
            System.out.println("No se pudo crear la jornada: revise permisos y datos.");return false;
        }
        datos[n++]=new Jornadas(id,f,h,z,m,r,admin);guardarArchivo();return true;
    }
    public void consultarJornada(int id){int i=indice(id);if(i<0){System.out.println("Jornada no encontrada.");return;}mostrar(datos[i]);}
    public void buscarPorTexto(String t){
        boolean ok=false;if(t==null)return;
        for(int i=0;i<n;i++){Jornadas j=datos[i];if((j.zona+" "+j.materiales+" "+j.responsable).toLowerCase().contains(t.toLowerCase())){mostrar(j);ok=true;}}
        if(!ok)System.out.println("Sin coincidencias.");
    }
    public boolean actualizarJornada(int id,String f,String h,String z,String m,String r,int admin,Usuario usuarios){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esAdministrador(admin)||!validar(f,h,z,m,r))return false;
        Jornadas j=datos[i];j.fecha=f;j.hora=h;j.zona=z;j.materiales=m;j.responsable=r;guardarArchivo();return true;
    }
    public boolean eliminarJornada(int id){
        int i=indice(id);if(i<0||!eliminados.apilar(datos[i]))return false;
        for(int k=i;k<n-1;k++)datos[k]=datos[k+1];datos[--n]=null;guardarArchivo();return true;
    }
    public boolean deshacerEliminacion(){
        Jornadas j=eliminados.desapilar();if(j==null||n==200||indice(j.idJornada)>=0)return false;
        datos[n++]=j;guardarArchivo();return true;
    }
    public void listarJornadas(){
        if(n==0){System.out.println("Aún no hay jornadas programadas.");return;}
        int proximas=0;
        for(int i=0;i<n;i++){mostrar(datos[i]);if(!LocalDate.parse(datos[i].fecha).isBefore(LocalDate.now()))proximas++;}
        System.out.println("Total: "+n+" | Jornadas vigentes o futuras: "+proximas);
    }
    private void mostrar(Jornadas j){
        System.out.println("ID: "+j.idJornada+" | Fecha: "+j.fecha+" | Hora: "+j.hora
            +" | Zona: "+j.zona+" | Materiales: "+j.materiales+" | Responsable: "+j.responsable);
    }
    public void guardarArchivo(){
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))){
            for(int i=0;i<n;i++){Jornadas j=datos[i];p.println(j.idJornada+";"+j.fecha+";"+j.hora+";"+j.zona+";"+j.materiales+";"+j.responsable+";"+j.idAdministrador);}
        }catch(IOException e){System.out.println("Error guardando jornadas.");}
    }
    private void cargarArchivo(){
        File f=new File(ARCHIVO);if(!f.exists())return;
        try(BufferedReader b=new BufferedReader(new FileReader(f))){
            String l;while((l=b.readLine())!=null&&n<200){
                String[] d=l.split(";",-1);if(d.length==7){
                    int id=Integer.parseInt(d[0]),a=Integer.parseInt(d[6]);
                    if(id>0&&indice(id)<0&&validarCarga(d[1],d[2],d[3],d[4],d[5]))datos[n++]=new Jornadas(id,d[1],d[2],d[3],d[4],d[5],a);
                }
            }
        }catch(Exception e){System.out.println("Error leyendo jornadas.");}
    }
    private boolean validarCarga(String f,String h,String z,String m,String r){
        try{LocalDate.parse(f);LocalTime.parse(h);}catch(Exception e){return false;}
        return valido(z)&&valido(m)&&valido(r);
    }
}

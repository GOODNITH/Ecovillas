/* ===========================================================
 * Proyecto: EcoVillas | Entidad: Inscripciones_jornada
 * Programadores: Harol y Yeider | Fecha: 30/10/2026
 * HU: HU-03 | RF: RF-05, RF-06
 * RN: RN-04, RN-05 | RNF: RNF-01, RNF-02, RNF-03
 * CA: CA-03.1, CA-03.2, CA-03.3
 * Estructuras: arreglo con contador y pila propia.
 * =========================================================== */
import java.io.*;
import java.time.LocalDate;

public class Inscripciones_jornada {
    private int idInscripcion,idUsuario,idJornada;
    private Inscripciones_jornada[] datos=new Inscripciones_jornada[200];
    private int n=0;
    private pila<Inscripciones_jornada> eliminados=new pila<>();
    private final String ARCHIVO="datos/inscripciones.txt";

    public Inscripciones_jornada(){cargarArchivo();}
    private Inscripciones_jornada(int id,int u,int j){idInscripcion=id;idUsuario=u;idJornada=j;}
    private int indice(int id){for(int i=0;i<n;i++)if(datos[i].idInscripcion==id)return i;return -1;}
    private boolean yaInscrito(int u,int j){
        for(int i=0;i<n;i++)if(datos[i].idUsuario==u&&datos[i].idJornada==j)return true;
        return false;
    }
    public boolean crearInscripcion(int id,int usuario,int jornada,Usuario usuarios,Jornadas jornadas){
        if(n==200||id<=0||indice(id)>=0||usuarios==null||!usuarios.esResidente(usuario)
            ||jornadas==null||!jornadas.existeJornada(jornada)){
            System.out.println("Datos inválidos o usuario sin permiso.");return false;
        }
        if(yaInscrito(usuario,jornada)){System.out.println("Ya estás inscrito en esta jornada.");return false;}
        try{if(LocalDate.parse(jornadas.getFechaJornada(jornada)).isBefore(LocalDate.now())){
            System.out.println("No es posible inscribirse a una jornada pasada.");return false;
        }}catch(Exception e){return false;}
        datos[n++]=new Inscripciones_jornada(id,usuario,jornada);guardarArchivo();
        System.out.println("Inscripción registrada.");return true;
    }
    public void consultarInscripcion(int id){int i=indice(id);if(i<0){System.out.println("Inscripción no encontrada.");return;}mostrar(datos[i]);}
    public void buscarPorTexto(String t){
        boolean ok=false;if(t==null)return;
        for(int i=0;i<n;i++)if((String.valueOf(datos[i].idUsuario)+" "+datos[i].idJornada).contains(t)){mostrar(datos[i]);ok=true;}
        if(!ok)System.out.println("Sin coincidencias.");
    }
    public boolean actualizarInscripcion(int id,int nuevoUsuario,int nuevaJornada,Usuario usuarios,Jornadas jornadas){
        int i=indice(id);if(i<0||usuarios==null||!usuarios.esResidente(nuevoUsuario)||jornadas==null||!jornadas.existeJornada(nuevaJornada))return false;
        for(int k=0;k<n;k++)if(k!=i&&datos[k].idUsuario==nuevoUsuario&&datos[k].idJornada==nuevaJornada)return false;
        try{if(LocalDate.parse(jornadas.getFechaJornada(nuevaJornada)).isBefore(LocalDate.now()))return false;}catch(Exception e){return false;}
        datos[i].idUsuario=nuevoUsuario;datos[i].idJornada=nuevaJornada;guardarArchivo();return true;
    }
    public boolean eliminarInscripcion(int id){
        int i=indice(id);if(i<0||!eliminados.apilar(datos[i]))return false;
        for(int k=i;k<n-1;k++)datos[k]=datos[k+1];datos[--n]=null;guardarArchivo();return true;
    }
    public boolean deshacerEliminacion(){
        Inscripciones_jornada x=eliminados.desapilar();if(x==null||n==200||indice(x.idInscripcion)>=0||yaInscrito(x.idUsuario,x.idJornada))return false;
        datos[n++]=x;guardarArchivo();return true;
    }
    public void listarInscripciones(){
        if(n==0){System.out.println("Aún no hay inscripciones.");return;}
        for(int i=0;i<n;i++)mostrar(datos[i]);System.out.println("Total de inscripciones: "+n);
    }
    public void listarPorJornada(int jornada){
        boolean ok=false;for(int i=0;i<n;i++)if(datos[i].idJornada==jornada){mostrar(datos[i]);ok=true;}
        if(!ok)System.out.println("Esta jornada no tiene inscritos.");
    }
    private void mostrar(Inscripciones_jornada x){
        System.out.println("ID inscripción: "+x.idInscripcion+" | ID usuario: "+x.idUsuario+" | ID jornada: "+x.idJornada);
    }
    public void guardarArchivo(){
        new File("datos").mkdirs();
        try(PrintWriter p=new PrintWriter(new FileWriter(ARCHIVO))){
            for(int i=0;i<n;i++)p.println(datos[i].idInscripcion+";"+datos[i].idUsuario+";"+datos[i].idJornada);
        }catch(IOException e){System.out.println("Error guardando inscripciones.");}
    }
    private void cargarArchivo(){
        File f=new File(ARCHIVO);if(!f.exists())return;
        try(BufferedReader b=new BufferedReader(new FileReader(f))){
            String l;while((l=b.readLine())!=null&&n<200){
                String[] d=l.split(";",-1);if(d.length==3){
                    int id=Integer.parseInt(d[0]),u=Integer.parseInt(d[1]),j=Integer.parseInt(d[2]);
                    if(id>0&&indice(id)<0&&!yaInscrito(u,j))datos[n++]=new Inscripciones_jornada(id,u,j);
                }
            }
        }catch(Exception e){System.out.println("Error leyendo inscripciones.");}
    }
}

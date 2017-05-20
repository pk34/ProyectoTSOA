/**
 * Francisco Javier Peguero López
 * Paco
 * 209537864
 */

package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

import java.io.IOException;
import java.net.*;
import java.util.*;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.MicroNucleoBase;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.ProcesoCliente;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.ProcesoServidor;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.ResendThread;
import sistemaDistribuido.util.Convertidor;
import sistemaDistribuido.util.Buzon;
import sistemaDistribuido.util.Pausador;
import com.sun.scenario.effect.impl.sw.sse.SSEBlend_HARD_LIGHTPeer;
import com.sun.xml.internal.ws.encoding.policy.EncodingPolicyValidator;

public final class MicroNucleo extends MicroNucleoBase {
    private static MicroNucleo nucleo = new MicroNucleo();
    private static Hashtable<Integer, ParMaquinaProceso> tablaEmision;
    private Hashtable<Integer, byte[]> tablaRecepcion;
    private LinkedList<DatosProceso> TablaDireccionamientoProcesosRemotos;
    private LinkedList<DatosProceso> TablaDireccionamientoProcesosLocales;
    private Proceso Cliente;
    private Hashtable<Integer, Buzon> buzones; //de buzones

    private MicroNucleo() {
        tablaEmision = new Hashtable<Integer, ParMaquinaProceso>();
        tablaRecepcion = new Hashtable<Integer, byte[]>();
        TablaDireccionamientoProcesosLocales = new LinkedList<DatosProceso>();
        TablaDireccionamientoProcesosRemotos = new LinkedList<DatosProceso>();
        buzones = new Hashtable<Integer, Buzon>(); //de buzones
    }

    public final static MicroNucleo obtenerMicroNucleo() {
        return nucleo;
    }

    public void registrarEnTablaEmision(ParMaquinaProceso asa){
        tablaEmision.put(asa.dameID(), asa);
    }
    // funcion de buzones, la llama el proceso servicor en su constructor
    public boolean registraBuzon(int idProceso){
        imprimeln("-------Registrando buzón del servidor "+idProceso);
        buzones.put(idProceso, new Buzon(idProceso));
        return true;
    }

    /*
     * Metodos para probar el paso de mensajes entre los procesos cliente y
     * servidor en ausencia de datagramas. Esta es una forma incorrecta de
     * programacion "por uso de variables globales" (en este caso atributos de
     * clase) ya que, para empezar, no se usan ambos parametros en los metodos y
     * fallaria si dos procesos invocaran simultaneamente a receiveFalso() al
     * reescribir el atributo mensaje
     */
    byte[] mensaje;

    public void sendFalso(int dest, byte[] message) {
        System.arraycopy(message, 0, mensaje, 0, message.length);
        // Reanuda la ejecucion del proceso que haya invocado a receiveFalso()
        notificarHilos();
    }

    public void receiveFalso(int addr, byte[] message) {
        mensaje = message;
        suspenderProceso();
    }

    protected boolean iniciarModulos() {
        return true;
    }

    protected void sendVerdadero(int dest,byte[] message){
        imprimeln("\nEl proceso invocante es el "+super.dameIdProceso()+"\n");
        int idorigen = 0;
        int iddestino = 0;
        String ip = "";

        if ( tablaEmision.containsKey(new Integer(dest))) {
            imprimeln("Se encontro en la tabla de emision");
            ParMaquinaProceso datos;
            datos = tablaEmision.get( new Integer(dest)  );
            tablaEmision.remove(dest);
            ip = datos.dameIP();
            idorigen = super.dameIdProceso();
            iddestino = dest;


            // Envio del mensaje real
            setOriginBytes(message, idorigen);
            setDestinationBytes(message, iddestino);

            DatagramPacket dp;
            DatagramSocket socketEmision;
            try
            {
                dp = new DatagramPacket(message, message.length, InetAddress.getByName(ip), damePuertoRecepcion() );
                socketEmision = dameSocketEmision();
                imprimeln("Se encontro en la tabla de emision");
                socketEmision.send(dp); 
            }
            catch (SocketException se){
                System.err.println("Error iniciando socket: " + se.getMessage());
            }
            catch (UnknownHostException uhe){
                System.err.println("UnknownHostException: " + uhe.getMessage());
            }
            catch (IOException ioe){
                System.err.println("IOException: " + ioe.getMessage());
            }
        }
        else {
            if(Cliente != null && Cliente.banderaSend)
            {
                Iterator<DatosProceso> lista =
                        TablaDireccionamientoProcesosRemotos.iterator();
                DatosProceso datos;
                idorigen = super.dameIdProceso();
                boolean EncontroServer = false;

                while( lista.hasNext( ) && (EncontroServer == false) )
                {
                    datos = lista.next();
                    if(  dest == datos.dameNumdeServicio()  )
                    {
                        imprimeln("Se encontro en la tabla de procesos remotos");
                        EncontroServer = true;
                        iddestino = datos.dameID();
                        ip = datos.dameIP();
                    }
                }

                if(EncontroServer)
                {
                    // hacer el envio

                    setOriginBytes(message, idorigen);
                    setDestinationBytes(message, iddestino);

                    DatagramPacket dp;
                    DatagramSocket socketEmision;
                    try
                    {
                        dp = new DatagramPacket(message, message.length, InetAddress.getByName(ip), damePuertoRecepcion());
                        socketEmision = dameSocketEmision();
                        socketEmision.send(dp); 
                    }
                    catch(SocketException se){
                        System.err.println("Error iniciando socket: " + se.getMessage());
                    }
                    catch(UnknownHostException uhe){
                        System.err.println("UnknownHostException: " + uhe.getMessage());
                    }
                    catch(IOException exce){
                        System.err.println("IOException: "+exce.getMessage());
                    }
                }
                else
                {
                    LSA hilo;
                    try {
                        imprimeln("Se prepara para crear LSA");
                        hilo = new LSA(idorigen, new DatagramSocket(), damePuertoRecepcion(), TablaDireccionamientoProcesosRemotos, message, this, dameProcesoLocal(super.dameIdProceso()));
                        hilo.start();
                    } catch (SocketException se) {
                        se.printStackTrace();
                    }
                }
            }
        }
    }


    protected void receiveVerdadero(int addr, byte[] message) {
        imprimeln("Registrando proceso cliente");
        tablaRecepcion.put(Integer.valueOf(addr), message);
        //suspenderProceso();
        Buzon buzonServidor;
        if((buzonServidor=buzones.get(addr))==null){
            tablaRecepcion.put(addr, message);
            imprimeln("---------------Agregando proceso cliente "+addr+" a la tabla de recepción");
            suspenderProceso();
        }else if(buzonServidor.vacio()){
            tablaRecepcion.put(addr, message);
            imprimeln("-----------El buzon del servidor esta vacio. Agregando proceso servidor "+addr+"  a la tabla de recepción");
            System.out.println(buzones.get(addr).hayEspacio());
            suspenderProceso();
        } else {
            System.arraycopy(buzonServidor.retira(), 0, message, 0, 1024);
            imprimeln("-----------El servidor  "+addr+"  obtuvo un elemento del cliente "+message[0]+" del buzón");
        }
    }

    /**
     * Para el(la) encargad@ de direccionamiento por servidor de nombres en
     * practica 5
     */
    protected void sendVerdadero(String dest, byte[] message) {
    }

    /**
     * Para el(la) encargad@ de primitivas sin bloqueo en practica 5
     */
    protected void sendNBVerdadero(int dest, byte[] message) {
    }

    /**
     * Para el(la) encargad@ de primitivas sin bloqueo en practica 5
     */
    protected void receiveNBVerdadero(int addr, byte[] message) {
    }

    public void run() {
        byte[] buffer = new byte[1024];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        int origin, destination;
        String originIp;
        Proceso process;

        while (seguirEsperandoDatagramas()) {
            try {
                nucleo.dameSocketRecepcion().receive(packet);
            } catch (IOException ioe) {
                System.err.println("Error en la recepcion del paquete: " +
                ioe.getMessage());
            }

            origin = obtOrigen(packet.getData());
            originIp = packet.getAddress().getHostAddress();
            destination = obtDestino(packet.getData());

            if (packet.getData()[9] == -4) {
                packet.getData()[9] = 0;

                ResendThread resender = new ResendThread(dameSocketRecepcion(), packet);
                resender.start();
            }
            else if (packet.getData()[9] == -2) //LSA
            {
                imprimeln("Recibiendo LSA");
                int numServicio = Convertidor.toInt((Arrays.copyOfRange(buffer,  10, 14)));
                Iterator<DatosProceso> lista = TablaDireccionamientoProcesosLocales.iterator();
                boolean banderaEncontrarServer = false;
                DatosProceso datos;
                String ipServer;
                int idServer = 0;


                while( lista.hasNext( ) && (banderaEncontrarServer == false) )
                {
                    datos = lista.next();
                    if(  248 == datos.dameNumdeServicio()  )
                    {
                        imprimeln("Se encontro en la tabla de procesos locales");
                        banderaEncontrarServer = true;
                        idServer = datos.dameID();
                        ipServer = datos.dameIP();
                    }

                }
                if(banderaEncontrarServer)
                {// Enviar FSA
                    imprimeln("MicroNucleo se prepara un FSA");
                    FSA hiloFSA = new FSA (
                            dameSocketEmision(), damePuertoRecepcion(),
                            origin, idServer,this);
                    hiloFSA.start();
                }
            }
            else {
                imprimeln("Buscando proceso correspondiente al campo recibido");
                process = nucleo.dameProcesoLocal(destination);

                if (process != null) {
                    if (tablaRecepcion.containsKey(destination)) {
                        byte[] array = tablaRecepcion.get(destination);
                        imprimeln("Copiando mensaje al espaco de proceso");
                        System.arraycopy(packet.getData(), 0, array, 0, array.length);
                        tablaEmision.put(new Integer(origin), new MachineProcessPair(originIp, origin));
                        tablaRecepcion.remove(destination);
                        nucleo.reanudarProceso(process);
                    }
                }
                else {
                	
                    imprimeln("AU");
                    imprimeln("Destinatario no encontrado");
                    buffer[9] = (byte) -1;
                   
                    setOriginBytes(buffer, destination);
                    setDestinationBytes(buffer, origin);

                    try {
                        packet = new DatagramPacket(buffer, buffer.length, InetAddress.getByName(originIp), nucleo.damePuertoRecepcion());
                        nucleo.dameSocketEmision().send(packet);
                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public static int registrarParMaquinaProceso(ParMaquinaProceso server) {
        tablaEmision.put(server.dameID(), server);
        return server.dameID();
    }

    public void setOriginBytes(byte[] buffer, int origin) {
        byte[] originBytes = Convertidor.toBytes(origin);
        for (int i = 0; i < Convertidor.SIZE_INT; ++i) {
            buffer[i] = originBytes[i];
        }
    }

    public void setDestinationBytes(byte[] buffer, int destination) {
        byte[] destinationBytes = Convertidor.toBytes(destination);

        for (int i = 0; i < Convertidor.SIZE_INT; ++i) {
            buffer[4 + i] = destinationBytes[i];
        }
    }

    public int obtOrigen(byte[] buffer) {
        return Convertidor.toInt(Arrays.copyOfRange(buffer, 0, Convertidor.SIZE_INT));
    }

    public int obtDestino(byte[] buffer) {
        return Convertidor.toInt(Arrays.copyOfRange(buffer, 4, 4 + Convertidor.SIZE_INT));
    }

    public void invertOriginDestination(byte[] buffer) {
        byte aux;
        for (int i = 0; i < Convertidor.SIZE_INT; ++i) {
            aux = buffer[i];
            buffer[i] = buffer[4 + i];
            buffer[4 + i] = aux;
        }
    }

    public DatosProceso registrarServidor(int servicio, String ip, int id)
    {
        DatosProceso datos = new DatosProceso(servicio,ip,id);
        TablaDireccionamientoProcesosLocales.add(datos);
        return datos;
    }

    public boolean derregistrarServidor(DatosProceso datos)
    {
        TablaDireccionamientoProcesosLocales.remove(datos);
        return true;
    }

    public boolean registrarProcesoRemoto(int servicio, String ip, int id)
    {
        DatosProceso datos = new DatosProceso(servicio,ip,id);
        TablaDireccionamientoProcesosRemotos.add(datos);
        return true;
    }

    public boolean eliminarDatosProcesoRemoto(int id,int servicio){
        DatosProceso datos;
        for(int i=0; i < TablaDireccionamientoProcesosRemotos.size(); i++)
        {
            datos = TablaDireccionamientoProcesosRemotos.get(i);
            if( (datos.dameID() == id) && (datos.dameNumdeServicio() == servicio))
            {
                TablaDireccionamientoProcesosRemotos.remove(datos);
                return true;
            }
            
        }

        return false;
    }

    public boolean establecerCliente(Proceso cliente)
    {
        Cliente = cliente;
        return true;
    }

    /**/
    /**/
    private void enviaPaqueteAU(MaquinaProcesoClass pmp) {
        byte[] mensajeAU = new byte[9];
        mensajeAU[0] = (byte) 0;
        mensajeAU[4] = (byte)pmp.dameID();
        mensajeAU[1] = (byte)-1;
        
        
        try {
            DatagramSocket ds = dameSocketEmision();
            DatagramPacket dpAU = new DatagramPacket(mensajeAU,mensajeAU.length,InetAddress.getByName(pmp.dameIP()),damePuertoRecepcion());
            
            ds.send(dpAU);
        } 
        catch(SocketException ex){
            imprimeln("Error iniciando socket: "+ex.getMessage());
        }catch(UnknownHostException ex){
            imprimeln("UnknownHostException: "+ex.getMessage());
        }catch(IOException ex){
            imprimeln("IOException: "+ex.getMessage());
        }
        
    }
    
    private void enviaPaqueteTA(MaquinaProcesoClass pmp,byte[] mensaje) {
        byte[] mensajeTA = new byte[1024];
        System.arraycopy(mensaje, 0, mensajeTA, 0, 1024);
        
        
        mensajeTA[1] = (byte)-2;
        
        
        try {
            DatagramSocket ds = dameSocketEmision();
            DatagramPacket dpAU = new DatagramPacket(mensajeTA,mensajeTA.length,InetAddress.getByName(pmp.dameIP()),damePuertoRecepcion());
            
            ds.send(dpAU);
        } 
        catch(SocketException ex){
            imprimeln("Error iniciando socket: "+ex.getMessage());
        }catch(UnknownHostException ex){
            imprimeln("UnknownHostException: "+ex.getMessage());
        }catch(IOException ex){
            imprimeln("IOException: "+ex.getMessage());
        }
        
    }
    
    class MaquinaProcesoClass implements ParMaquinaProceso{
        private int id;
        private String ip;
        
        public MaquinaProcesoClass(int id,String ip) {
            this.ip=ip;
            this.id=id;
        }
        @Override
        public String dameIP() {
            // TODO Auto-generated method stub
            return ip;
        }
        @Override
        public int dameID() {
            // TODO Auto-generated method stub
            return id;
        }
        
    }
    
    class ReenviaPaquete extends Thread{
        
        String ip;
        byte[] mensaje;
        
        public ReenviaPaquete(String ip,byte[] mensaje){
            this.mensaje = mensaje;
            this.ip=ip;
        }
        
        public void run(){
            Pausador.pausa(5000);
            mensaje[1]=0;
            try {
                DatagramSocket ds = dameSocketEmision();
                DatagramPacket dpReintenta = new DatagramPacket(mensaje,mensaje.length,InetAddress.getByName(ip),damePuertoRecepcion());
                System.out.println("Reenviando paquete de proceso " + mensaje[0]);
                ds.send(dpReintenta);
            } 
            catch(SocketException ex){
                imprimeln("Error iniciando socket: "+ex.getMessage());
            }catch(UnknownHostException ex){
                imprimeln("UnknownHostException: "+ex.getMessage());
            }catch(IOException ex){
                imprimeln("IOException: "+ex.getMessage());
            }
            
        }
        
    }
}
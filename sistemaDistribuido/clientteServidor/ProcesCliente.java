/**
 * Francisco Javier Peguero López


 * Paco
 * 209537864
 */
//esto es una prueba de github
// esto lo subi en la rama de ediciones nestor
package sistemaDistribuido.clientteServidor;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.awt.Button;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Convertidor;

public class ProcesCliente extends Proceso{

    private String codOp = "";
    private String mensaje = "";
    public String LSAmensaje ="";
    private Button botonSolicitud;
    public byte[] solCliente=new byte[1024];
    public byte[] respCliente=new byte[1024];


    public ProcesCliente(Escribano esc,Button boton){
        super(esc);
        botonSolicitud = boton;
        banderaSend = false;
        start();
    }

    public void capturarSolicitud(String op, String dt)
    {
        codOp = op;
        mensaje = dt;
    }

    public void run(){
        imprimeln("Proceso cliente en ejecucion.");
        imprimeln("Esperando datos para continuar.");

        while(continuar())
        {
            Nucleo.suspenderProceso();
            imprimeln("Generando mensaje");
            solCliente[0]=(byte)10; 
            empaquetarMensaje(solCliente);
            imprimeln("Enviando mensaje");

            imprimeln("Cliente convoca a send");
              
            Nucleo.establecerCliente(this);
            banderaSend = true;
            Nucleo.send(248,solCliente);
            banderaSend = false;

            imprimeln("Cliente convoca receive");
            Nucleo.receive(dameID(),respCliente);
            imprime("Cliente sale del receive");

            String mensajeDeLaRed = desempaquetarMensaje(respCliente);

            while( mensajeDeLaRed.equals("FSA")  || mensajeDeLaRed.equals("AU"))
            {
                imprimeln("Cliente convoca recieve");
                if(mensajeDeLaRed.equals("AU"))
                {
                    imprimeln("Cliente convoca send");
                    banderaSend = true;
                    Nucleo.send(248,solCliente); 
                    banderaSend = false;
                }

                Nucleo.receive(dameID(),respCliente);
                imprimeln("Cliente sale del receive");
                mensajeDeLaRed = desempaquetarMensaje(respCliente);

            }

            if(LSAmensaje.equals(""))
                imprimeln(mensajeDeLaRed);
            else
            {
                imprimeln(LSAmensaje);
                LSAmensaje = "";
            }

            // aqui deberiamos volver hacer verdadero el boton
            botonSolicitud.setEnabled(true);

        }
    }


    public void empaquetarMensaje(byte []paquete)// no necesita return porque solo recibimos el puntero a array
    {
        byte arrayAux[];
        short numeroCheckSum;

        arrayAux = mensaje.getBytes();
        numeroCheckSum = (short)arrayAux.length;

        paquete[10] = (byte)numeroCheckSum;
        numeroCheckSum >>=8;
        paquete[11]= (byte)numeroCheckSum;


        if(codOp.equals("Crear"))
        {
            paquete[9] = 1;
        }
        else
            if(codOp.equals("Eliminar"))
            {
                paquete[9] = 2;
            }
            else
                if(codOp.equals("Leer"))
                {
                    paquete[9] = 3;	
                }
                else
                    if(codOp.equals("Escribir"))
                    {
                        paquete[9] = 4;
                    }

        for (int i=12,j=0; i<paquete.length && j<arrayAux.length ;i++,j++)
        {
            paquete[i] = arrayAux[j];
        }
    }

    public String desempaquetarMensaje(byte [] paquete){
        imprimeln("Desempaquetar mensaje");
        String mensaje="";
        byte numeroenEnBytes[] = new byte[2];
        short tamMensaje;
        numeroenEnBytes[0] = paquete[8];
        numeroenEnBytes[1] = paquete[9];
        tamMensaje = construyeShort(numeroenEnBytes);

       
        
            if(tamMensaje == -3) // se recibio un FSA
            {
                imprimeln("Se recibe un FSA");
                int idServer = construyeInt(Arrays.copyOfRange(paquete,  14, 18));
                String ip ="";
                // registre 248 y la ip local por no programar para obtener realmnte esos datos
                try {
                    ip = InetAddress.getLocalHost().getHostAddress();	 
                } catch (UnknownHostException e) {

                }
                System.out.println("FSA num"+idServer);

                Nucleo.registrarProcesoRemoto(248, ip, idServer);
                return "FSA";
            }
            else
            	 if( paquete[9] == -1 )
                 {
                     imprimeln("Se recibe AU");
                     int ultimoIdProcesoQueSeLeEnvioSolicitud = Convertidor.toInt(Arrays.copyOfRange(paquete,  0, 4));
                     Nucleo.eliminarDatosProcesoRemoto(ultimoIdProcesoQueSeLeEnvioSolicitud,248);
                     return "AU";
                 }
            	 else
            {
                //System.out.println("Cliente el cliente recivio un mensaje normal");
                for(int i=0,j=10; i<tamMensaje; i++,j++)
                {
                    mensaje += (char)paquete[j];
                }

                return mensaje;
            }
    }

    public byte[] ordenaArray(byte [] array)
    {   
        byte[] destino=new byte[array.length];
        int j=destino.length;
        for(int i=0;i<array.length;i++){
            j--;
            destino[j]=array[i];
        }

        return destino;
    }


    public  short construyeShort(byte arreglo [])
    { 
        short valorOriginal;
        short aux;

        arreglo = ordenaArray(arreglo);

        valorOriginal = (short)(arreglo[0] | (short)0x00);
        valorOriginal <<= 8;
        valorOriginal = (short) (valorOriginal & (short)0xFF00);

        aux = (short)(arreglo[1] | (short)0x00);
        aux = (short) (aux & (short)0x00FF);
        valorOriginal = (short) (valorOriginal | aux);


        return valorOriginal;	
    }

    public int construyeInt(byte arreglo[]){
        int valorOriginal = 0;

        arreglo = ordenaArray(arreglo);

        for(int i= 0 ; i< 4; i++)
        {

            valorOriginal = (int)( (arreglo[i] & 0xFF) | valorOriginal);          
            if( i != 3 ){
                valorOriginal = valorOriginal << 8;
            }
        }
        return valorOriginal;


    }
}

/**
 * Francisco Javier Peguero López
 * Paco
 * 209537864
*/
package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

import sistemaDistribuido.clienteServidorPaco.ProcesCliente;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Convertidor;
import java.io.IOException;
import java.net.*;
import java.util.Iterator;
import java.util.LinkedList;

public class LSA  extends Thread{
    int idorigen;
    DatagramSocket socketEmision;
    int puerto;
    byte [] mensajeCliente = new byte[1024];
    LinkedList<DatosProceso> TablaProcesosRemotos;
    MicroNucleo nucleo;
    ProcesCliente cliente;

    public LSA(int idorigen, DatagramSocket socket, int puerto, LinkedList<DatosProceso> tabla, byte [] mensajeOriginal,MicroNucleo nucleo,Proceso cliente)
    {
        this.idorigen = idorigen;
        socketEmision = socket;
        this.puerto = puerto;
        TablaProcesosRemotos = tabla;
        System.arraycopy(mensajeOriginal, 0, mensajeCliente, 0, mensajeCliente.length);
        this.nucleo = nucleo;
        this.cliente =  (ProcesCliente) cliente;
    }

    public void run()
    {
        byte [] messageLSA = new byte[1024];
        DatagramPacket paqueteLSA;
        byte codigoLSA = -2;
        byte arrayAux[] = new byte[4];
        int intento = 0;
        boolean seEncontroServidor = false;

        messageLSA[9]= codigoLSA;

        nucleo.setOriginBytes(messageLSA, idorigen);
        nucleo.setDestinationBytes(messageLSA, 0);

        arrayAux = Convertidor.toBytes(248);
        for (int i=0; i<4; i++)
        {
            messageLSA[10 + i]= arrayAux[i];
        }

        Iterator<DatosProceso> lista;
        DatosProceso datos;
        int iddestino =0;
        String ip ="";

        while((intento < 3) && (seEncontroServidor == false))
        {
            try {
                paqueteLSA = new DatagramPacket(messageLSA, messageLSA.length,
                        InetAddress.getByName(
                        InetAddress.getLocalHost().getHostAddress()),
                        puerto);
                System.out.println("LSA enviado");
                socketEmision.send(paqueteLSA);
            } 
            catch (UnknownHostException e)
            {
                e.printStackTrace();
            }
            catch (IOException e) 
            {
                e.printStackTrace();
            }


            try {
                sleep(5000);
            } 
            catch (InterruptedException e) 
            {
                e.printStackTrace();
            }

            lista = TablaProcesosRemotos.iterator();
            seEncontroServidor = false;

            while( lista.hasNext( ) && (seEncontroServidor == false) )
            {
                datos = lista.next();
                if(  248 == datos.dameNumdeServicio()  )
                {
                    System.out.println("Se encontro en la tabla de procesos remotos");
                    seEncontroServidor = true;
                    iddestino = datos.dameID();
                    ip = datos.dameIP();
                }
            }

            if(seEncontroServidor)
            {
                //hacer envio de la solicitud al server
                nucleo.setOriginBytes(mensajeCliente, idorigen);
                nucleo.setDestinationBytes(mensajeCliente, iddestino);

                DatagramPacket dp;
                try
                {
                    dp = new DatagramPacket(mensajeCliente, mensajeCliente.length, InetAddress.getByName(ip), puerto);
                    System.out.println("Envio de LSA");
                    socketEmision.send(dp); 
                }
                catch(SocketException exce){
                    System.err.println("Error iniciando socket: "+ exce.getMessage());
                }
                catch(UnknownHostException exce){
                    System.err.println("UnknownHostException: "+ exce.getMessage());
                }
                catch(IOException exce){
                    System.err.println("IOException: "+exce.getMessage());
                }
            }
            else
            {
                intento++;
            }
        }
        
        if(seEncontroServidor == false)
        {
            cliente.LSAmensaje = "No hay servidores que puedan atender, intenta de nuevo";
            cliente.respCliente[8] = 0;
            cliente.respCliente[9] = 0;
            nucleo.reanudarProceso(cliente);
        }
    }

    public byte[] empaquetaEntero(int numero)
    {
        byte [] array = new byte[4];

        array[0] = (byte)numero;
        numero >>=8;
                array[1]= (byte)numero;
                numero >>=8;
        array[2]= (byte)numero;
        numero >>=8;
        array[3]= (byte)numero;

        return array;
    }
}

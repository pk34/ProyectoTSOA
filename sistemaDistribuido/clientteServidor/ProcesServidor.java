/**
 * Francisco Javier Peguero lópez
 * Paco
 * 209537864
 */

package sistemaDistribuido.clientteServidor;

import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.DatosProceso;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Convertidor;
import sistemaDistribuido.util.Pausador;
import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.StringTokenizer;

public class ProcesServidor extends Proceso{

    public ProcesServidor(Escribano esc){
        super(esc);
        start();
    }

    public void run(){
        imprimeln("Proceso servidor en ejecucion.");
        byte[] solServidor=new byte[1024];
        byte[] respServidor=new byte[1024];
        int origen;
        String ip="";
        String mensajeDeLaRed;

        try {
            ip = InetAddress.getLocalHost().getHostAddress();	 
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        DatosProceso misdatos= Nucleo.levantarServidor(248, ip , dameID());

        while(continuar()){
            imprimeln("Se convoco a receive");
            Nucleo.receive(dameID(),solServidor);
            mensajeDeLaRed = desempaquetarMensaje(solServidor);
            
            origen = Convertidor.toInt(Arrays.copyOfRange(solServidor,  0, 4));
            
            imprimeln("Enviando "+origen);

            switch(solServidor[9])
            {
            case 1:// crear archivo
                imprimeln("Peticion para crear " + mensajeDeLaRed);
                empaquetarMensaje(respServidor,"Se ha creado correctamente");
                break;
            case 2:// eliminar archivo
                imprimeln("Peticion para eliminar " + mensajeDeLaRed);
                empaquetarMensaje(respServidor,"Archivo eliminado");
                break;
            case 3:// leer archivo
                imprimeln("Peticion para leer " + mensajeDeLaRed);
                empaquetarMensaje(respServidor,"Lectura correcta");

                break;
            case 4:
                StringTokenizer separador = new StringTokenizer(mensajeDeLaRed, "/");
                String nombreArchivo = separador.nextToken();
                String mensaje = separador.nextToken();
                imprimeln("Peticion para escribir: "+mensaje+"\nEn el archivo "+nombreArchivo);
                empaquetarMensaje(respServidor,"Escritura correcta");
                break;

            }

            Pausador.pausa(5000);
            imprimeln("enviando respuesta");
            Nucleo.send(origen,respServidor);
        }

        Nucleo.tumbarServidor(misdatos);
    }

    public void empaquetarMensaje(byte[] paquete, String mensaje)
    {
        byte []arrayAux = mensaje.getBytes();
        short checkSum = (short)arrayAux.length;

        paquete[8] = (byte)checkSum;
        checkSum >>=8;
        paquete[9]= (byte)checkSum;

        for (int i=10,j=0; i<paquete.length && j<arrayAux.length ;i++,j++)
        {
            paquete[i] = arrayAux[j];
        }
    }

    public String desempaquetarMensaje(byte [] paquete){
        String mensaje="";
        byte numeroenEnBytes[] = new byte[2];
        short tamMensaje;
        numeroenEnBytes[0] = paquete[10];
        numeroenEnBytes[1] = paquete[11];
        tamMensaje = construyeShort(numeroenEnBytes);

        for(int i=0,j=12; i<tamMensaje; i++,j++)
        {
            mensaje += (char)paquete[j];
        }

        return mensaje;
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

/**
 * Francisco Javier Peguero lópez
 * Paco
 * 209537864
 */

package sistemaDistribuido.sistema.clienteServidor.modoMonitor;

import java.io.IOException;
import java.net.*;

public class FSA extends Thread{
	DatagramSocket socketEmision;
	int port, dest, serv;
	//int destino;
	//int idServer;
	MicroNucleo nucleo;
	
	public FSA(DatagramSocket socket,int port,int dest,int serv,MicroNucleo nucleo)
	{
		socketEmision = socket;
		this.port = port;
		this.dest = dest;
		this.serv = serv;
		this.nucleo = nucleo;
		
	}
	
	public void run()
	{
		DatagramPacket paqueteFSA;
		byte[] messageFSA = new byte[1024];
		short codigoFSA = -3;
		byte arrayAux[] = new byte[4];
		
		messageFSA[8] = (byte)codigoFSA;
		codigoFSA >>=8;
        messageFSA[9]= (byte)codigoFSA;
        
        nucleo.setOriginBytes(messageFSA, 0);
        nucleo.setDestinationBytes(messageFSA, dest);

		arrayAux = empaquetaEntero(248);
		for(int i =0; i<4; i++)
		{
			messageFSA[i+10]= arrayAux[i];
		}
		arrayAux = empaquetaEntero(serv);
		for(int i =0; i<4; i++)
		{
			messageFSA[i+14]= arrayAux[i];
		}
	    
	    String ip="";
		try {
			 ip = InetAddress.getLocalHost().getHostAddress();	 
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
                
		try
		{
		   paqueteFSA = new DatagramPacket(messageFSA,messageFSA.length,InetAddress.getByName(ip),port );
		   socketEmision.send(paqueteFSA);
		   System.out.println("Envio de FSA");
		   //socketEmision.close();
		  
		}catch(SocketException e){
			System.out.println("Error iniciando socket: "+e.getMessage());
		}
      catch(IOException e){
			System.out.println("IOException: "+e.getMessage());
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

/* ******************************
 * Nestor Ubaldo Gonzalez Alcala*
 * D03                          *
 * Practica 3                   *
 * *****************************/

/* NOTA 1: El separador es *
 * NOTA 2: Tambien incluyo los puntos extras de la practica 1
 * NOTA 3: La extension ".txt" se agrega automaticamente al archivo
 * */
package sistemaDistribuido.util;

public class Paquetes {
	
	public byte[] generaPaqueteSolicitud(int codigoOperacion,int[] operandos){
		byte[] paqueteSolicitud = new byte[1024];
		//System.out.println( "Tamano arreglo "+(operandos.length*4+1));
		byte[] operandosByte=new byte[operandos.length*4+1];
		paqueteSolicitud[9]=(byte) codigoOperacion;
		
		convierteIntArrayABytes(operandos,operandosByte);
		/*for (int i=0;i<paqueteSolicitud.length;i++) {
			System.out.print(paqueteSolicitud[i]+" ");
		}
		System.out.println();*/
		System.arraycopy(operandosByte, 0, paqueteSolicitud, 10, operandosByte.length);
		
		/*for (int i=0;i<operandosByte.length;i++) {
			System.out.print(operandosByte[i]+" ");
		}
		System.out.println();
		for (int i=0;i<paqueteSolicitud.length;i++) {
			System.out.print(paqueteSolicitud[i]+" ");
		}*/
		
		return paqueteSolicitud;
	}
	
	public Solicitud desempaquetaSolicitud(byte[] paqueteSolicitud){
		Solicitud valoresSolicitud = new Solicitud();
		byte[] operandosByte=new byte[(int)paqueteSolicitud[10]*4+1];
		System.arraycopy(paqueteSolicitud, 10, operandosByte, 0, operandosByte.length);
		/*System.out.println();
		System.out.println(operandosByte.length);
		System.out.println(paqueteSolicitud[9]);*/
		valoresSolicitud.setCodigoOperacion((int)paqueteSolicitud[9]);
		valoresSolicitud.setOperandos(convierteBytesAIntArray(operandosByte));
		
		return valoresSolicitud;
	}
	
	public byte[] generaPaqueteRespuesta(int[] resultados){
		byte[] paqueteRespuesta = new byte[1024];
		byte[] resultadosByte = new byte[resultados.length*4+1];
		
		convierteIntArrayABytes(resultados, resultadosByte);
		//System.out.println();
		/*for (int i=0;i<resultadosByte.length;i++) {
			System.out.print(resultadosByte[i]+" ");
		}*/
		System.arraycopy(resultadosByte, 0, paqueteRespuesta, 8, resultadosByte.length);
		/*System.out.println();
		for (int i=0;i<paqueteRespuesta.length;i++) {
			System.out.print(paqueteRespuesta[i]+" ");
		}*/
		return paqueteRespuesta;
	}
	
	public int[] desempaquetaRespuesta(byte[] paqueteRespuesta){
		byte[] resultadosByte = new byte[(int)paqueteRespuesta[8]*4+1];
		
		System.arraycopy(paqueteRespuesta, 8, resultadosByte, 0, resultadosByte.length);
		
		return convierteBytesAIntArray(resultadosByte);
	}
	
	private int[] convierteBytesAIntArray(byte[] byteArray){
		int numInts = byteArray[0];
		int[] intArray = new int[numInts];
		byte[] auxInt = new byte[4];
		
		for(int i=0;i<numInts;i++){
			System.arraycopy(byteArray, i*4+1, auxInt, 0, 4);
			intArray[i]=convierteByteaInt(auxInt);
		}
		
		return intArray;
	}
	
	private void convierteIntArrayABytes(int[] operandos,byte[] paquete){
		int numOperandos=operandos.length;
		
		for(int i=0;i<numOperandos;i++)
			System.arraycopy(convierteIntaByte(operandos[i]), 0, paquete, i*4+1, 4);
		paquete[0]=(byte)numOperandos;

	}
	
	private int convierteByteaInt(byte[] enteroByte){
		
		int entero = 0x00000000;
		
		int auxiliar = enteroByte[3];
		auxiliar = auxiliar&0x000000ff;
		entero = entero | auxiliar;
		
		entero = entero << 8;
		auxiliar = enteroByte[2];
		auxiliar = auxiliar&0x000000ff;
		entero = entero | auxiliar;
		
		entero = entero << 8;
		auxiliar = enteroByte[1];
		auxiliar = auxiliar&0x000000ff;
		entero = entero | auxiliar;
		
		entero = entero << 8;
		auxiliar = enteroByte[0];
		auxiliar = auxiliar&0x000000ff;
		entero = entero | auxiliar;
		
		return entero;
	}
	
	private byte[] convierteIntaByte(int entero){
		
		byte[] byteArrayEntero = new byte[4];
		
		for(int i=0;i<4;i++)
			byteArrayEntero[i] = (byte)(entero>>i*8);
		
		return byteArrayEntero;

	}

	public class Solicitud{
		private int codigoOperacion;
		private int[] operandos;
		
		public int getCodigoOperacion() {
			return codigoOperacion;
		}
		
		public void setCodigoOperacion(int codigoOperacion) {
			this.codigoOperacion = codigoOperacion;
		}
		
		public int[] getOperandos() {
			return operandos;
		}
		
		public void setOperandos(int[] operandos) {
			this.operandos = operandos;
		}
		
	}

}

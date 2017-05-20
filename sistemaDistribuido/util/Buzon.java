/* ******************************
 * Nestor Ubaldo Gonzalez Alcala*
 * D03                          *
 * Practica 5                   *
 * *****************************/
/* NOTA 1: El separador es *
 * NOTA 3: La extension ".txt" se agrega automaticamente al archivo
 * */
package sistemaDistribuido.util;

import java.util.LinkedList;

public class Buzon {
	public int idProceso;
	private LinkedList<byte[]> espera;
	
	
	public Buzon(int idProceso) {
		this.idProceso=idProceso;
		espera = new LinkedList<byte[]>();
	}
	
	
	public boolean agrega(byte[] solicitud){
		espera.offer(solicitud);
		return true;
	}
	
	public boolean hayEspacio(){
		return espera.size()<3;
	}


	public boolean vacio() {
		// TODO Auto-generated method stub
		return espera.isEmpty();
	}


	public byte[] retira() {
		return espera.poll();
		
	}
	
	public int tamano(){
		
		return espera.size();
		
	}
	
}

/*
 * Francisco Javier Peguero López
 * Paco
 * 209537864
 */

package sistemaDistribuido.sistema.clienteServidor.modoUsuario;

import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;

public class ProcesoCliente extends Proceso {

    private byte m_opcode;
    private String m_message;
    public boolean banderaSend = false;
    public String  avisodelHiloLSA = "";

    public ProcesoCliente(Escribano esc) {
        super(esc);

        imprimeln("Inicio de proceso...");
        start();
    }

    public void setCodop(int opcode) {
        m_opcode = (byte) opcode;
    }

    public void setMessage(String message) {
        m_message = message;
    }

    public void run() {
        imprimeln("Proceso cliente en ejecucion, " +
                  "esperando datos para continuar...");
        Nucleo.suspenderProceso();

        solServidor = new byte[1024];
        respServidor = new byte[1024];

        solServidor[8] = m_opcode;
        solServidor[15] = (byte) m_message.length();

        imprimeln("Generando mensaje a ser enviado," +
                  " llenando los campos necesarios...");
        packMessage();

        imprimeln("Senhalamiento al nucleo para envio de mensaje...");
        Nucleo.send(248, solServidor);//

        imprimeln("Invocando a receive...");
        Nucleo.receive(dameID(), respServidor);

        imprimeln("Procesando respuesta recibida del sevidor...");
        switch (respServidor[9]) {
        case 2:
            imprimeln("Creacion exitosa");
            break;
        case 3:
            imprimeln("Eliminacion exitosa");
            break;
        case 0:
            imprimeln("Lectura exitosa");
            imprimeln("Contenido: "
                    + new String(respServidor, 16,
                            (int) respServidor[15]));
            break;
        case 1:
            imprimeln("Escritura exitosa");
            break;
        case 6:
            imprimeln("Error al leer el archivo");
            break;
        case 7:
            imprimeln("Error al eliminar en archivo");
            break;
        case 4:
            imprimeln("Error al leer archivo");
            break;
        case 5:
            imprimeln("Error al elcribir el archivo");
            break;
        case -1:
            imprimeln("Error al enviar peticion: direccion desconocida.");
            break;
        case -4:
            imprimeln("Error al enviar peticion: servidor ocupado");
            break;
        default:
            imprimeln("invalid status");
            break;
        }
    }

    private void packMessage() {
        byte[] messageBytes = m_message.getBytes();
        for (int i = 0; i < messageBytes.length; ++i)
            solServidor[16 + i] = messageBytes[i];
    }
}

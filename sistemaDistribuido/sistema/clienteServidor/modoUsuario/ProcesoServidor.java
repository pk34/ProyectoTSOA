/*
 * Francisco Javier Peguero López
 * Paco
 * 209537864
 */

package sistemaDistribuido.sistema.clienteServidor.modoUsuario;

import java.io.*;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.sistema.clienteServidor.modoUsuario.Proceso;
import sistemaDistribuido.util.Escribano;
import sistemaDistribuido.util.Pausador;
import sistemaDistribuido.visual.clienteServidor.ClienteFrame;

public class ProcesoServidor extends Proceso {

    private String mensaje;
    private String resp;
    private int m_status;

    public ProcesoServidor(Escribano esc) {
        super(esc);
        start();
    }

    public void run() {
        imprimeln("Proceso servidor en ejecucion.");
        solServidor = new byte[1024];
        respServidor = new byte[1024];

        String fileName;
        String argumento;

        while (continuar()) {
            imprimeln("Invocando a receive...");
            Nucleo.receive(dameID(), solServidor);

            imprimeln("Procesando peticion recibida del cliente...");
            Pausador.pausa(5000);

            mensaje = new String(solServidor, 16, (int) solServidor[15]);

            switch (solServidor[8]) {
            case 0:
                imprimeln("Creando archivo: " + mensaje);
                createFile();
                break;
            case 1:
                imprimeln("Eliminando archivo: " + mensaje);
                deleteFile();
                break;
            case 2:
                fileName = mensaje.split(":")[0];
                argumento = mensaje.split(":")[1];
                imprimeln("Escribiendo archivo: " + fileName);
                writeToFile(fileName, argumento);
                break;
            case 3:
                fileName = mensaje.split(":")[0];
                argumento = mensaje.split(":")[1];
                imprimeln("Leyendo archivo: " + fileName);
                resp = readFromFile(fileName, Integer.parseInt(argumento));
                break;
            default:
                imprimeln("Codigo de operacion invalido");
                break;
            }

            imprimeln("Creando mensaje");
            pack();

            imprimeln("Envio de mensaje");
            Pausador.pausa(2000);

            int origin = Nucleo.nucleo.obtOrigen(solServidor);
            int destination = Nucleo.nucleo.obtDestino(solServidor);

            Nucleo.nucleo.setOriginBytes(respServidor, destination);
            Nucleo.nucleo.setDestinationBytes(respServidor, origin);

            Nucleo.send(origin, respServidor);
        }
    }

    private void createFile() {
        String fileName = mensaje;
        try {
            imprimeln("Nombre archivo " + fileName);
            File myFile = new File(fileName);
            if (myFile.createNewFile()) {
                m_status = 2;
                imprimeln("Archivo creado!");
            } else {
                m_status = 6;
                imprimeln("Error creando archivo!");
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void deleteFile() {
        String fileName = mensaje;
        try {
            File myFile = new File(fileName);
            if (myFile.delete()) {
                m_status = 3;
                imprimeln("Archivo eliminado!");
            } else {
                m_status = 7;
                imprimeln("Error eliminando archivo!");
            }
        } catch (SecurityException ioe) {
            ioe.printStackTrace();
        }
    }

    private String readFromFile(String fileName, int lineNo) {
        String contents = null;
        try {
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            String line;
            int i = 1;
            while ((line = in.readLine()) != null) {
                if (lineNo == i) {
                    contents = line + "\n";
                }
                ++i;
            }
            in.close();
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }

        if (contents == null) {
            m_status = 4;
        } else {
            m_status = 0;
        }

        return contents;
    }

    private void writeToFile(String fileName, String contents) {
        try {
            PrintWriter out = new PrintWriter(fileName);
            out.print(contents);
            out.close();
            m_status = 1;
        } catch (FileNotFoundException fnfe) {
            m_status = 5;
            fnfe.printStackTrace();
        }
    }

    private void pack() {
        respServidor[9] = (byte) m_status;

        if (resp != null) {
            byte[] messageBytes = mensaje.getBytes();
            int messageLength = messageBytes.length;
            respServidor[15] = (byte) messageLength;
            for (int i = 0; i < messageLength; ++i) {
                respServidor[16 + i] = messageBytes[i];
            }
        } else {
            respServidor[15] = 0;
        }
    }
}

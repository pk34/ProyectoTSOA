/**
 * Francisco Javier Peguero lópez
 * Paco
 * 209537864
 */

package sistemaDistribuido.visual.clienteServidor;

import sistemaDistribuido.clienteServidorPaco.ProcesCliente;
import sistemaDistribuido.sistema.clienteServidor.modoMonitor.Nucleo;
import sistemaDistribuido.visual.clienteServidor.MicroNucleoFrame;
import sistemaDistribuido.visual.clienteServidor.ProcesoFrame;

import java.awt.*;
import java.awt.event.*;

public class ClienteFrame extends ProcesoFrame {
  
    private Choice codigosOperacion;
    private TextField campoMensaje;
    private Button botonSolicitud;
    private String codop1, codop2, codop3, codop4;
    
    private ProcesCliente proceso;
    
    public ClienteFrame(MicroNucleoFrame frameNucleo) {
        super(frameNucleo, "Cliente de Archivos");
        add("South", construirPanelSolicitud());
        validate();
        proceso = new ProcesCliente(this,botonSolicitud);
        fijarProceso(proceso);
    
    }

    public Panel construirPanelSolicitud() {
        Panel p = new Panel();
        codigosOperacion = new Choice();
        codop1 = "Crear";
        codop2 = "Eliminar";
        codop3 = "Leer";
        codop4 = "Escribir";
        codigosOperacion.add(codop1);
        codigosOperacion.add(codop2);
        codigosOperacion.add(codop3);
        codigosOperacion.add(codop4);
        campoMensaje = new TextField(10);
        botonSolicitud = new Button("Solicitar");
        botonSolicitud.addActionListener(new ManejadorSolicitud());
        p.add(new Label("Operacion:"));
        p.add(codigosOperacion);
        p.add(new Label("Datos:"));
        p.add(campoMensaje);
        p.add(botonSolicitud);
        return p;
    }

    class ManejadorSolicitud implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            String com = e.getActionCommand();
            if (com.equals("Solicitar")) {
                botonSolicitud.setEnabled(false);
                com = codigosOperacion.getSelectedItem();
                imprimeln("Solicitud a enviar: " + com);
                if( proceso != null ){
                    imprimeln("Solicitud a enviar: "+com);
                    imprimeln("Mensaje a enviar: "+campoMensaje.getText());
                    proceso.capturarSolicitud(com,campoMensaje.getText());
                    Nucleo.reanudarProceso(proceso);
                }
            }
        }
    }
}

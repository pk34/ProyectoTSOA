/**
 * Francisco Javier Peguero lópez
 * Paco
 * 209537864
 */

package sistemaDistribuido.visual.clienteServidor;

import sistemaDistribuido.clienteServidorPaco.ProcesServidor;
import sistemaDistribuido.visual.clienteServidor.MicroNucleoFrame;
import sistemaDistribuido.visual.clienteServidor.ProcesoFrame;

public class ServidorFrame extends ProcesoFrame {
    private static final long serialVersionUID = 1;
    private ProcesServidor proc1;
    public ServidorFrame(MicroNucleoFrame frameNucleo) {
        super(frameNucleo, "Servidor de Archivos");
        proc1 = new ProcesServidor(this);
        fijarProceso(proc1);
    }
}

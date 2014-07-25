package co.carlosbernal.utils.javashellcommand;

import java.io.File;
import java.io.IOException;

/**
 * Clase que facilita el llamado de instrucciones en linea de comandos.
 *
 * @author Carlos Bernal <bernalcarvajal@gmail.com>
 */
public class ShellCommand {

    /**
     * Runtime del S.O.
     */
    private Runtime runtime;
    /**
     * Referencia al proceso de la instruccion ejecutada *
     */
    private Process process;
    /**
     * Consume el stream de salida del comando ejecutado.
     */
    private StreamGobbler responseStreamGobbler;
    /**
     * Consume el stream de error del comando ejecutado.
     */
    private StreamGobbler errorStreamGobbler;

    /**
     * Ejecuta una instrucion en la linea de comandos del S.O.
     *
     * Si se quiere conocer que ocurrio al ejecutar la instruccion se pueden
     * usar los metodo 'get' de esta clase.
     *
     * Es importante anotar que cada vez que se ejecute una instruccion nueva,
     * la informacion de la instrucion anterior sera reemplazada, y por ende,
     * perdida.
     *
     * @param command La instruccion a ejecutar en la linea de comandos
     */
    public void runCommand(String command) {
        this.runCommand(command, null, false, false);
    }

    /**
     * Ejecuta una instrucion en la linea de comandos del S.O.
     *
     * Si se quiere conocer que ocurrio al ejecutar la instruccion se pueden
     * usar los metodo 'get' de esta clase.
     *
     * Es importante anotar que cada vez que se ejecute una instruccion nueva,
     * la informacion de la instrucion anterior sera reemplazada, y por ende,
     * perdida.
     *
     * @param command La instruccion a ejecutar en la linea de comandos
     * @param workingDir workingDir Ruta al directorio que se usara para ejecutar
     * la instruccion. Debe incluir '/' al final.
     */
    public void runCommand(String command, String workingDir) {
        this.runCommand(command, workingDir, false, false);
    }
    
    /**
     * Ejecuta una instrucion en la linea de comandos del S.O.
     *
     * Si se quiere conocer que ocurrio al ejecutar la instruccion se pueden
     * usar los metodo 'get' de esta clase.
     *
     * Es importante anotar que cada vez que se ejecute una instruccion nueva,
     * la informacion de la instrucion anterior sera reemplazada, y por ende,
     * perdida.
     *
     * @param command La instruccion a ejecutar en la linea de comandos
     * @param workingDir workingDir Ruta al directorio que se usara para ejecutar
     * la instruccion. Debe incluir '/' al final.
     * @param runAsync 'true' si se quiere que es funcion se ejecute de manera
     * asincronica, 'false' de lo contrario.
     */
    public void runCommand(String command, String workingDir, boolean runAsync) {
        this.runCommand(command, workingDir, runAsync, false);
    }

    /**
     * Ejecuta una instrucion en la linea de comandos del S.O.
     *
     * Si se quiere conocer que ocurrio al ejecutar la instruccion se pueden
     * usar los metodo 'get' de esta clase.
     *
     * Es importante anotar que cada vez que se ejecute una instruccion nueva,
     * la informacion de la instrucion anterior sera reemplazada, y por ende,
     * perdida.
     *
     * @param command La instruccion a ejecutar en la linea de comandos
     * @param workingDir workingDir Ruta al directorio que se usara para ejecutar
     * la instruccion. Debe incluir '/' al final.
     * @param runAsync 'true' si se quiere que es funcion se ejecute de manera
     * asincronica, 'false' de lo contrario.
     * @param printOutputInConsole 'true' si se quiere que el output del comando
     * se imprima en consola, 'false' de lo contrario.
     */
    public void runCommand(String command, String workingDir, boolean runAsync, boolean printOutputInConsole) {
        try {
            //Determinar el S.O.
            String OSName = System.getProperty("os.name");
            OSName = OSName.toLowerCase();

            //Si el sistema operativo es windows, agregar al comando la instruccion necesaria
            if (OSName.contains("windows")) {
                command = "cmd /c " + command;
            }

            //Obtener el runtime del S.O.
            runtime = Runtime.getRuntime();

            //Ejecutar la instruccion
            if (workingDir != null && !workingDir.equalsIgnoreCase("")) {

                File workDir = new File(workingDir);
                if (!workDir.exists()) {
                    System.out.println("Error al llamar una instruccion en linea de comandos...");
                    System.out.println("El Directorio especificado para ejecuta la instruccion no existe");
                    return;
                }

                if (!OSName.contains("windows")) {
                    process = runtime.exec(new String[]{"bash", "-c", command}, null, workDir);
                } else {
                    process = runtime.exec(command, null, workDir);
                }

            } else {
                System.out.println("---------CMD: "+command);
                if (!OSName.contains("windows")) {
                    process = runtime.exec(new String[]{"bash", "-c", command});
                } else {
                    process = runtime.exec(command);
                }
            }

            //Almacenar la respuesta de la instruccion
            if (!printOutputInConsole) {
                responseStreamGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT");
            } else {
                responseStreamGobbler = new StreamGobbler(process.getInputStream(), "OUTPUT", true);
            }

            //Alamacenar el error que puede presentarse
            if (!printOutputInConsole) {
                errorStreamGobbler = new StreamGobbler(process.getErrorStream(), "ERROR");
            } else {
                errorStreamGobbler = new StreamGobbler(process.getErrorStream(), "ERROR", true);
            }
            
            //Iniciar los consumidores de streams
            responseStreamGobbler.start();
            errorStreamGobbler.start();

            //Esperar a que la instruccion termine de ejecutarse si asi se quiere
            if (!runAsync) {
                process.waitFor();
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Error al llamar una instruccion en linea de comandos...");
            e.printStackTrace(System.out);
        }
    }

    //////////////////////////////////////////////
    //            Getters / Setters
    //////////////////////////////////////////////
    /**
     * @return Runtime del S.O. *
     */
    public Runtime getRuntime() {
        return runtime;
    }

    /**
     * @return Referencia al proceso de la instruccion ejecutada.
     */
    public Process getProcess() {
        return process;
    }

    /**
     * @return String que contiene la respuesta de la instruccion ejecutada.
     *
     * Este metodo puede demorarce tanto como se demora la ejecucion del
     * proceso, si el comando se ejecuto de manera asincronoca.
     */
    public String getResponseString() {
        return responseStreamGobbler.getInputString();
    }

    /**
     * @return String que contiene el error, si ocurrio alguno, de la instruccion
     * ejecutada.
     *
     * Este metodo puede demorarce tanto como se demora la ejecucion del
     * proceso, si el comando se ejecuto de manera asincronoca.
     */
    public String getErrorString() {
        return errorStreamGobbler.getInputString();
    }

    /**
     * @return Codigo de salida de la instruccion ejecutada. Si se uso un llamado
     * asyncronico, se deberia verificar que que el proceso ya ha finalizado,
     * usando el metodo, hasProcessFinished
     */
    public int getExitCode() {
        return process.exitValue();
    }

    /**
     * @return Determina si ocurrio algun error a ejecutar la instruccion Si se uso un
     * llamado asyncronico, se deberia verificar que que el proceso ya ha
     * finalizado, usando el metodo, hasProcessFinished.
     */
    public boolean errorOcurred() {
        if (hasProcessFinished()) {
            if (process.exitValue() == 0) {
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }

    /**
     * @return Verifica si el proceso ya ha terminado.
     */
    public boolean hasProcessFinished() {
        if (process != null) {
            try {
                process.exitValue();
                return true;
            } catch (Exception ex) {
                return false;
            }
        } else {
            return false;
        }
    }
}
package co.carlosbernal.utils.javashellcommand;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 * Hilo que lee los streams de error y salida de los comonado externos, que son
 * llamado con el metodo exec()
 *
 * @author Carlos Bernal <bernalcarvajal@gmail.com>
 */
public class StreamGobbler extends Thread {

    InputStream inputStream;
    String type;
    OutputStream outputStream;
    
    boolean printStreamOnConsole;
    StringBuilder inputStringBuilder;

    public StreamGobbler(InputStream is, String type) {
        this(is, type, false, null);
    }
    
    public StreamGobbler(InputStream is, String type, boolean printStreamOnConsole) {
        this(is, type, printStreamOnConsole, null);
    }

    public StreamGobbler(InputStream is, String type, boolean printStreamOnConsole, OutputStream redirect) {
        this.inputStream = is;
        this.type = type;
        this.outputStream = redirect;
        this.printStreamOnConsole = printStreamOnConsole;
    }

    @Override
    public void run() {
        try {
            
            PrintWriter pw = null;
            if (outputStream != null) {
                pw = new PrintWriter(outputStream, true);
            }

            InputStreamReader isr = new InputStreamReader(inputStream);
            BufferedReader br = new BufferedReader(isr);
            
            String line;
            inputStringBuilder = new StringBuilder();
            while ((line = br.readLine()) != null) {
                if (pw != null) {
                    pw.println(type + ">" + line);
                }
                if(printStreamOnConsole){
                    System.out.println(type + ">" + line);
                }
                inputStringBuilder.append(type).append("> ").append(line).append("\n");
            }
            
            if (pw != null) {
                pw.flush();
            }
            
        } catch (Exception ioe) {
            ioe.printStackTrace(System.out);
        }
    }
    
    public String getInputString(){
        return inputStringBuilder.toString();
    }
}
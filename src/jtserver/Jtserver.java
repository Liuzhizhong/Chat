/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jtserver;

import com.server.ServerMain;
import java.io.File;

/**
 *
 * @author wzj
 */
public class Jtserver {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        File logDir = new File(System.getProperty("user.dir") + "/logs");
        if ( !logDir.exists()){
            logDir.mkdir();
        }
        new ServerMain().go(); 
    }
}

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.server;
 
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/** 
 * @author wzj
 */
public class LogJT { 
    public static void LogMsg(String msg){
        PropertyConfigurator.configure(LogJT.class.getResource("log4j.properties"));
        Logger log = Logger.getLogger(LogJT.class.getName());
        log.error(msg);
    }
    public static void LogMsg(String msg, Exception ex){
        PropertyConfigurator.configure(LogJT.class.getResource("log4j.properties"));
        Logger log = Logger.getLogger(LogJT.class.getName());
        log.error(msg, ex);
    }
}

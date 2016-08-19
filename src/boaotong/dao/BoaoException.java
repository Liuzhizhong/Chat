/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package boaotong.dao;
 

/**
 *
 * @author Administrator
 */
public class BoaoException extends Exception {
    private String message ;
    
    public BoaoException(String message){
        super(message);
        this.message = message;
    }
    
    public String toString(){
        return message;
    }
}

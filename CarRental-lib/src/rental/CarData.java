/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rental;

import java.io.Serializable;

public class CarData implements Serializable {
        
    public CarData(int givenID , CarType givenType) {
        this.id = givenID;
        this.type = givenType;
    }
        
    public int id;
    public CarType type;
}
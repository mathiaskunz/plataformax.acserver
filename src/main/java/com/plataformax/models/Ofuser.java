/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plataformax.models;

import java.io.Serializable;
import java.math.BigInteger;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * @author Mathias
 */
@Entity
@Table(name="ofuser")
public class Ofuser implements Serializable{
    
    //@Column (unique = true)
    //private String name;
    
    @Id
    @Column
    private String username;
    
    //@Column
    //private String description;
 
    
    /*public String getName(){
        return name;
    }
    
    public void setName(String name){
        this.name = name;
    }*/
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
   /* public String getDescription(){
        return description;
    }
    
    public void setDescription(String decription){
        this.description = description;
    }*/

    /*public BigInteger getID() {
        return ID;
    }

    public void setID(BigInteger ID) {
        this.ID = ID;
    }*/
    
}

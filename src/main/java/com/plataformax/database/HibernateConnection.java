/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plataformax.database;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Mathias
 */
public class HibernateConnection {
    
    private static EntityManagerFactory factory;
    private static EntityManager manager;

    public static EntityManager getManager() {

        if (manager == null) {
            try{    
            factory = Persistence.createEntityManagerFactory("openfire");
            }catch (Exception ex){
                System.out.println(ex.getMessage());
            }
            manager = factory.createEntityManager();
        }

        return manager;

    }
}

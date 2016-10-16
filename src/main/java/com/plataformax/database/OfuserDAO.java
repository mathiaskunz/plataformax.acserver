/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plataformax.database;

import com.plataformax.models.Ofuser;
import java.math.BigInteger;
import javax.persistence.EntityManager;

/**
 *
 * @author Mathias
 */
public class OfuserDAO {
    
    private final EntityManager manager = HibernateConnection.getManager();

    public Ofuser getUser(BigInteger id) {
        return manager.find(Ofuser.class, id);
    }
    
    public boolean containsUser(String username){
        manager.getTransaction().begin();
        boolean contains = manager.createQuery("FROM Ofuser as u WHERE u.username LIKE ?1")
                .setParameter(1, username)
                .getResultList().isEmpty();
        manager.getTransaction().commit();
        return contains;
    }

    public boolean addUser(Ofuser user) {
        
        if (containsUser(user.getUsername())) {
            manager.getTransaction().begin();
            manager.persist(user);
            manager.getTransaction().commit();
            return true;
        }else{
            return false;
        }
    }

    public Ofuser editUser(Ofuser user) {
        manager.getTransaction().begin();
        user = manager.merge(user);
        manager.getTransaction().commit();
        manager.refresh(user);
        return user;
    }
}

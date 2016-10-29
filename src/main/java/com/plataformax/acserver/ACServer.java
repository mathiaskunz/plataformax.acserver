/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plataformax.acserver;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.core.UriBuilder;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.ssl.SSLContextConfigurator;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

/**
 *
 * @author Mathias
 */
public class ACServer {
    
    private static final String KEYSTORE_SERVER_FILE = ".//security//serverKeystore";
    private static final String KEYSTORE_SERVER_PWD = "123456";
    private static final String TRUSTORE_SERVER_FILE = ".//security//serverTruststore";
    private static final String TRUSTORE_SERVER_PWD = "123456";
    
    public static void main (String args[]){
        
        //LEMBRAR DE FAZER UMA ROTINA PARA ATUALIZAR O CERTIFICADO DA CA, POIS QUANDO 
        //ESTÁ INVÁLIDO DA PROBLEMA PARA FAZER CONEXÃO SERVIDOR
        
        
        // Grizzly ssl configuration
        SSLContextConfigurator sslContext = new SSLContextConfigurator();
        
        // set up security context
        sslContext.setKeyStoreFile(KEYSTORE_SERVER_FILE); // contains server keypair
        sslContext.setKeyStorePass(KEYSTORE_SERVER_PWD);
        sslContext.setTrustStoreFile(TRUSTORE_SERVER_FILE); // contains client certificate
        sslContext.setTrustStorePass(TRUSTORE_SERVER_PWD);
        
        URI baseUri = UriBuilder.fromUri("https://localhost/api/").port(9998).build();
        ResourceConfig rc = new ResourceConfig().packages("com.plataformax.resources");
        rc.register(MultiPartFeature.class);
        HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, rc, true,
                new SSLEngineConfigurator(sslContext).setClientMode(false).setNeedClientAuth(false));
        try {
            server.start();
        } catch (IOException ex) {
            Logger.getLogger(ACServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}

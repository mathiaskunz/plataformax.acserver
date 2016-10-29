/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.plataformax.resources;

import com.plataformax.database.OfuserDAO;
import com.plataformax.models.Ofuser;
import com.sun.org.apache.xml.internal.security.exceptions.Base64DecodingException;
import com.sun.org.apache.xml.internal.security.utils.Base64;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import java.security.cert.CertificateFactory;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;

/**
 *
 * @author Mathias
 */
@Path("/app")
@Produces(MediaType.TEXT_PLAIN)
public class Response {

    private static final int REQUEST_SUCCESS = 1;
    private static final int REQUEST_FAIL = 0;
    private static final String CA_PATH = "C:\\OpenSSL-Win64\\bin\\";

    @GET
    @Path("name/{name}/{username}/{description}")
    public int GetVerb(@PathParam("name") String name, @PathParam("username") String username,
            @PathParam("description") String description) {

        Ofuser user = new Ofuser();
        //user.setName(name);
        user.setUsername(username);
        //user.setDescription(description);
        OfuserDAO userDao = new OfuserDAO();

        if (userDao.containsUser(username)) {
            return REQUEST_SUCCESS;
        } else {
            return REQUEST_FAIL;
        }

    }

    @POST
    @Path("csr")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    public int certreq(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData) {

        try {
            writeToFile(fileInputStream, CA_PATH + fileMetaData.getFileName());

            String fileName = fileMetaData.getFileName().substring(0,
                    fileMetaData.getFileName().indexOf("."));

            createCertificate(fileName);
            return REQUEST_SUCCESS;
        } catch (IOException ex) {
            Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
            return REQUEST_FAIL;
        }
    }

    @GET
    @Path("down/{down}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response downClientCert(@PathParam("down") String name) {

        String FILE_PATH = CA_PATH + name + ".cer";
        File file = new File(FILE_PATH);
        return javax.ws.rs.core.Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .build();
    }

    @GET
    @Path("downcacert")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response downCACert() {
        String FILE_PATH = CA_PATH + "ca-certificate.pem.txt";
        File file = new File(FILE_PATH);
        return javax.ws.rs.core.Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                .build();
    }

    @POST
    @Path("renewcert")
    @Consumes({MediaType.MULTIPART_FORM_DATA})
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public javax.ws.rs.core.Response renewClientCert(@FormDataParam("file") InputStream fileInputStream,
            @FormDataParam("file") FormDataContentDisposition fileMetaData,
            @FormDataParam("serial") String serial,
            @FormDataParam("signature") String signature,
            @FormDataParam("certificate") String certificate) {

        try {

            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(Base64.decode(certificate));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            String serialNumberSendCert = cert.getSerialNumber().toString(16);

            if (serialNumberSendCert.length() == 3) {
                serialNumberSendCert = "0".concat(serialNumberSendCert);
            }

            if (!serialNumberSendCert.equals(serial) || !verifySign(signature, serial, cert)) {
                return javax.ws.rs.core.Response.status(Status.INTERNAL_SERVER_ERROR)
                        .build();
            }

            writeToFile(fileInputStream, CA_PATH + fileMetaData.getFileName());

            String fileName = fileMetaData.getFileName().substring(0,
                    fileMetaData.getFileName().indexOf("."));

            revokeCert(serial);

            createCertificate(fileName);
            File file = new File(CA_PATH + fileName + ".cer");
            return javax.ws.rs.core.Response.ok(file, MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"")
                    .build();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | SignatureException | Base64DecodingException | CertificateException ex) {
            Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
            return javax.ws.rs.core.Response.status(Status.INTERNAL_SERVER_ERROR)
                    .build();
        }

    }

    @POST
    @Path("cancelclientregister")
    public boolean cancelClientRegister(@FormDataParam("serial") String serial,
            @FormDataParam("signature") String signature,
            @FormDataParam("certificate") String certificate) {

        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            InputStream in = new ByteArrayInputStream(Base64.decode(certificate));
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(in);

            String serialNumberSendCert = cert.getSerialNumber().toString(16);

            if (serialNumberSendCert.length() == 3) {
                serialNumberSendCert = "0".concat(serialNumberSendCert);
            }

            if (!serialNumberSendCert.equals(serial) || !verifySign(signature, serial, cert)) {
                return false;
            }
            
            revokeCert(serial);
            return true;
            
        } catch (IOException | CertificateException | Base64DecodingException 
                | NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(Response.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }

    private boolean verifySign(String messageSignature, String messageToCompare, Certificate cert)
            throws NoSuchAlgorithmException, InvalidKeyException, SignatureException, Base64DecodingException {

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(cert);

        sig.update(messageToCompare.getBytes());

        byte[] decodedSignature = Base64.decode(messageSignature);
        System.out.println("SIGNATURE: " + Arrays.toString(decodedSignature));
        return sig.verify(decodedSignature);
    }

    private void revokeCert(String serial) throws IOException {
        List<String> commandList = new ArrayList<>();
        commandList.add(CA_PATH + "openssl.exe");
        commandList.add("ca");
        commandList.add("-revoke");
        commandList.add(".\\CA\\newcerts\\" + serial + ".pem");
        commandList.add("-passin");
        commandList.add("pass:123456");
        commandList.add("-batch");

        ProcessBuilder pb = new ProcessBuilder(commandList);

        pb.directory(new File(CA_PATH));
        pb.redirectErrorStream(true);
        Process p;

        p = pb.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }

    private void writeToFile(InputStream fileInputStream,
            String fileLocation) throws IOException {

        int read;
        byte[] bytes = new byte[1024];

        try (OutputStream out = new FileOutputStream(new File(fileLocation))) {
            while ((read = fileInputStream.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
            out.flush();
        }
    }

    private void createCertificate(String fileName) throws IOException {

        List<String> commandList = new ArrayList<>();
        commandList.add(CA_PATH + "openssl.exe");
        commandList.add("ca");
        commandList.add("-in");
        commandList.add(fileName + ".csr");
        commandList.add("-out");
        commandList.add(fileName + ".cer");
        commandList.add("-passin");
        commandList.add("pass:123456");
        commandList.add("-batch");

        ProcessBuilder pb = new ProcessBuilder(commandList);

        pb.directory(new File(CA_PATH));
        pb.redirectErrorStream(true);
        Process p;

        p = pb.start();
        BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String line;
        while (true) {
            line = r.readLine();
            if (line == null) {
                break;
            }
            System.out.println(line);
        }
    }
}

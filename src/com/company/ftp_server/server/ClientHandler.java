package com.company.ftp_server.server;

import com.company.ftp_server.client.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

public class ClientHandler implements Runnable{

    private final Socket client;
    private final BufferedReader in;
    private final PrintWriter out;
    private ArrayList<ClientHandler> clients;
    private ArrayList<File> files;
    private Client thisClient;
    private final int DATA_PORT = 21;

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients, ArrayList<File> files) throws IOException {
        this.clients = clients;
        this.files = files;
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("INFO <welcome message>");

            while (true){
                String request = in.readLine();
                if (request.startsWith("CONN")) {
                    handleCONN(getRequest(request));

                }else if(request.startsWith("QUIT")){
                    handleQUIT(getRequest(request));

                }else if(request.equals("USERS")) {
                    if (checkLoggedIn()) handleUSERS();
                    else out.println("400 Please login first");

                }else if(request.startsWith("SEND_FILE")) {
                    if (checkLoggedIn()) handleSEND_FILE(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("PM")){
                    if (checkLoggedIn()) handlePM(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("LIST")) {
                    if (checkLoggedIn()) handleLIST(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("RETR_FILE")) {
                    if (checkLoggedIn()) handleRETR_FILE(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("RENAME_FILE")) {
                    if (checkLoggedIn()) handleRENAME_FILE(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("DEL_FILE")) {
                    if (checkLoggedIn()) handleDEL_FILE(getRequest(request));
                    else out.println("400 Please login first");

//                    TODO: To be implemented
                }else if(request.startsWith("STOR")) {
                    if (checkLoggedIn()) handleSEND_FILE(getRequest(request));
                    else out.println("400 Please login first");

                }else{
                    out.println("400 Unknown command");
                }
            }
        }catch (IOException e){
            System.err.println("IO Exception in ClientHandler -- User has disconnected");
            System.err.println(Arrays.toString(e.getStackTrace()));
        } finally{
            try {
                in.close();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleLIST(String request){
        String response = "";
        if (files.isEmpty()){
            out.println("550 Requested action not taken. File unavailable.");
            return;
        }
        out.println("150 File status okay; about to open data connection.");
        for (File file : files) {
            response = response.concat(file.getName()+ " | " + file.length() + '\n');
        }
        out.println(response);
    }

        // Actually the receive file
    private void handleSEND_FILE(String request) {

        try {
            ServerSocket serverSocket = new ServerSocket(DATA_PORT);
            System.out.println("[SERVER] Waiting for Client DATA connection...");
            Socket client_socket = serverSocket.accept();
            System.out.println("[SERVER] Client data connection successful!");
            DataInputStream dataInputStream = new DataInputStream(client_socket.getInputStream());

            int lenght = dataInputStream.readInt();
            byte[] message;
            if (lenght > 0){
                message = new byte[lenght];
                dataInputStream.readFully(message);
                String fileLocation = "D:/Java Projects/ftp-server/src/com/company/ftp_server/server/" + request;
                FileOutputStream fos = new FileOutputStream(fileLocation);
                fos.write(message);
            }

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleDEL_FILE(String request) {

            String fileName = request;
            System.out.println("filename is " + fileName);

            File file = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/" + fileName);
            System.out.println("File is " + file.getAbsolutePath());

            if (file.delete()) {
                out.println("File deleted successfully");
            } else {
                out.println("Failed to delete file");
            }
    }

    private void handleRENAME_FILE(String request) {

        String[] words = request.split(" ");
        String oldName = words[0];
        String newName = words[1];

        File oldFile = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/" + oldName);
        File newFile = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/" + newName);
        System.out.println("File is " + oldFile.getAbsolutePath());

        if (oldFile.renameTo(newFile)) {
            out.println("File renamed successfully");
        } else {
            out.println("Failed to rename file");
        }
    }

    private void handleRETR_FILE(String request) {

        try {
            ServerSocket serverSocket = new ServerSocket(DATA_PORT);
            System.out.println("[SERVER] Waiting for Client DATA connection...");
            Socket client_socket = serverSocket.accept();
            System.out.println("[SERVER] Client data connection successful!");
            DataOutputStream dataOutputStream = new DataOutputStream(client_socket.getOutputStream());

            String fileName = request;
            System.out.println("filename is " + fileName);

            Path path = Paths.get("D:/Java Projects/ftp-server/src/com/company/ftp_server/server", fileName);


            byte[] fileContent = Files.readAllBytes(path);

            System.out.println("File is " + path);

            dataOutputStream.writeInt(fileContent.length);
            dataOutputStream.write(fileContent);

            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handlePM(String request) {
        int firstSpace = request.indexOf(" ");
        String receiver = request.substring(0, firstSpace);
        String message = request.substring(firstSpace + 1);
        ClientHandler receiverHandler = getClient(receiver);
        if(receiverHandler != null){
            receiverHandler.out.println("PM from " + thisClient.getUserName() + " : " + message);
            out.println("200 Message sent successfully");
        }else{
            out.println("404 User not found");
        }
    }

    private void handleUSERS() {
        out.println("200 The current user list is:");
        for (ClientHandler client : clients){
            out.println(client.thisClient.getUserName());
        }
    }
    private void handleQUIT(String request) {
        out.println("221 Service closing control connection");
    }
    private void handleCONN(String request){
        if(request.length() >= 1) {
            Client temp = Server.getClientByName(request);
            if (temp != null) {
                if (!temp.getLoggedInStatus()) {
                    if (temp.login(request)) {
                        out.println("200 " + request + " : login successful");
                        thisClient = temp;
                    } else {
                        out.println("CONN FAILURE");
                    }
                } else {
                    out.println("401 User already logged in");
                }
            } else {
                out.println("404 User not found");
            }
        }else{
            out.println("411 Length of request should be at least 1. Try inputting CONN <user_name>");
        }
    }
    private void handleBCST(String request){
        for(ClientHandler aClient : clients){
            if (aClient.thisClient == this.thisClient) {
                aClient.out.println("200 " + request);
                aClient.out.println("Other clients receive the message as follows:");
            }
            aClient.out.println("BCST " + thisClient.getUserName() + " " + request);
        }
    }
    private String getRequest(String request){
        int firstSpace = request.indexOf(" ");
        return request.substring(firstSpace + 1);
    }
    public String getPersonalClientUserName(){
        return thisClient.getUserName();
    }
    private boolean checkLoggedIn(){
        return (this.thisClient != null);
    }
    private ClientHandler getClient(String userName){
        for (ClientHandler aClient : clients){
            if (aClient.getPersonalClientUserName().equals(userName)){
                return aClient;
            }
        }
        return null;
    }
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if(hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

}

package com.company.ftp_server.server;

import com.company.ftp_server.client.Client;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientHandler implements Runnable{

    private final Socket client;
    private final BufferedReader in;
    private final PrintWriter out;
    private ArrayList<ClientHandler> clients;
    private File files = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/Files");
    private Client thisClient;
    private final int DATA_PORT = 21;

    public ClientHandler(Socket clientSocket, ArrayList<ClientHandler> clients, ArrayList<File> files) throws IOException {
        this.clients = clients;
        this.client = clientSocket;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(clientSocket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            out.println("220 Service ready for new user");

            while (true){
                String request = in.readLine();
                if (request.startsWith("CONN")) {
                    handleCONN(getRequest(request));

                }else if(request.startsWith("QUIT")){
                    handleQUIT(getRequest(request));

                }else if(request.equals("USERS")) {
                    if (checkLoggedIn()) handleUSERS();
                    else out.println("400 Please login first");

                }else if(request.equals("USER")) {
                    handleUSER(request);

                }else if(request.startsWith("STOR")) {
                    if (checkLoggedIn()) handleSTOR(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("PM")){
                    if (checkLoggedIn()) handlePM(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("LIST")) {
                    if (checkLoggedIn()) handleLIST(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("RETR")) {
                    if (checkLoggedIn()) handleRETR(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("RENAME")) {
                    if (checkLoggedIn()) handleRENAME(getRequest(request));
                    else out.println("400 Please login first");

                }else if(request.startsWith("DEL")) {
                    if (checkLoggedIn()) handleDEL(getRequest(request));
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

        File[] filesArray = files.listFiles();

        if(filesArray != null && filesArray.length > 0) {
            String response = "";
            for (File file : filesArray) {
                response = response.concat(file.getName() + " | " + file.length() + '\n');
            }
            out.println("150 File status okay");
            out.println(response);
            out.println("226 Closing data connection. Requested file action successful");
        }else{
            out.println("550 Requested action not taken. File unavailable.");
        }
    }

        // Actually the receive file
    private void handleSTOR(String request) {

        try {
            ServerSocket serverSocket = new ServerSocket(DATA_PORT);
            System.out.println("[SERVER] Waiting for Client DATA connection...");
            Socket client_socket = serverSocket.accept();
            if(client_socket.isConnected()){
                out.println("200 Command okay");
                out.println("[SERVER] Client data connection successful!");
            }else{
                out.println("425 Can't open data connection.");
                return;
            }
            DataInputStream dataInputStream = new DataInputStream(client_socket.getInputStream());

            int lenght = dataInputStream.readInt();
            byte[] message;
            if (lenght > 0){
                File outputFile = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/Files/" + request);
                message = new byte[lenght];
                dataInputStream.readFully(message);
                FileOutputStream fos = new FileOutputStream(outputFile);
                fos.write(message);
            }

            serverSocket.close();
        } catch (IOException e) {
            out.println("451 Requested action aborted: local error in processing.");
            e.printStackTrace();
        }
    }

    private void handleDEL(String request) {

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

    private void handleRENAME(String request) {

        String[] words = request.split(" ");
        String oldName = words[0];
        String newName = words[1];

        File oldFile = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/Files/" + oldName);
        File newFile = new File("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/Files/" + newName);
        System.out.println("File is " + oldFile.getAbsolutePath());

        if (oldFile.renameTo(newFile)) {
            out.println("File renamed successfully");
        } else {
            out.println("Failed to rename file");
        }
    }

    private void handleRETR(String request) {

        try {
            ServerSocket serverSocket = new ServerSocket(DATA_PORT);
            out.println("150 File status okay; about to open data connection.");
            Socket client_socket = serverSocket.accept();
            System.out.println("[SERVER] Client data connection successful!");
            DataOutputStream dataOutputStream = new DataOutputStream(client_socket.getOutputStream());

            String fileName = request;
            System.out.println("filename is " + fileName);

            Path path = Paths.get("D:/Java Projects/ftp-server/src/com/company/ftp_server/server/Files", fileName);


            byte[] fileContent = Files.readAllBytes(path);

            System.out.println("File is " + path);

            dataOutputStream.writeInt(fileContent.length);
            dataOutputStream.write(fileContent);

            out.println("226 Closing data connection. Requested file action successful");
            serverSocket.close();
        } catch (IOException e) {
            out.println("550 Requested action not taken. File unavailable");
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
        String username = request.split(" ")[0];
        String password = request.split(" ")[1];
        if(request.length() >= 1) {
            Client temp = Server.getClientByName(request);
            if (temp != null) {
                if (!temp.getLoggedInStatus()) {
                    if(!password.equals("pass")){
                        if (temp.login(username, password)) {
                            out.println("200 " + request + " : login successful");
                            thisClient = temp;
                        } else {
                            out.println("CONN FAILURE");
                        }
                    }else{
                        if (temp.login(username, "pass")) {
                            out.println("200 " + request + " : login successful");
                            thisClient = temp;
                        } else {
                            out.println("CONN FAILURE");
                        }
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

    private void handleUSER(String request){
        String username = request.split(" ")[0];
        String password = request.split(" ")[1];
        if(request.length() >= 1) {
            Client temp = Server.getClientByName(username);
            if( temp != null ){
                out.println("409 User already exists");
            }else{
                if(!password.equals("pass")){
                    this.thisClient.setUserNameAndPass(username, password);
                }else{
                    this.thisClient.setUserNameAndPass(username);
                }
            }
        }else{
            out.println("411 Length of request should be at least 1. Try inputting USER <user_name> [<password>]");
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
}

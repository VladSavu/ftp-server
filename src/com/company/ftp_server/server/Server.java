package com.company.ftp_server.server;

import com.company.ftp_server.client.Client;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static ArrayList<Client> clients = new ArrayList<>();
    private ArrayList<ClientHandler> clientHandlers = new ArrayList<>();
    private ArrayList<File> files = new ArrayList<>();
    private final int PORT = 20;
    private final ExecutorService clientsPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public static void main(String[] args) {
        new Server().run();
    }

    public void run(){
        ServerSocket listener = null;
        try {
            listener = new ServerSocket(PORT);
            clients.add(new Client("user1"));
            clients.add(new Client("user2"));
            clients.add(new Client("user3"));
            clients.add(new Client());

//            while (true) {
                System.out.println("[SERVER] Waiting for Client connection...");
                Socket client = listener.accept();
                System.out.println("[SERVER] Client connected successfully!");
                ClientHandler clientThread = new ClientHandler(client, clientHandlers, files);
                clientHandlers.add(clientThread);
                clientsPool.execute(clientThread);
//            }
        } catch (IOException e) {
            e.printStackTrace();
        }

/*
        try {
            out.println("INFO <welcome message>");
            while (true){
                String request = in.readLine();
                if (request.contains("CONN")){
                    String userName = request.substring(5);
                    Client temp = getClientByName(userName);
                    if(temp != null){
                        if (!temp.getLoggedInStatus()){
                            if (temp.login(userName)){
                                out.println("200 " + userName);
                            }else{
                                out.println("CONN FAILURE");
                            }
                        }else{
                            out.println("401 User already logged in");
                        }
                    }else{
                        out.println("404 User not found");
                    }
                }
            }
        }finally {
            in.close();
            out.close();
        }
*/
    }

     public static Client getClientByName (String userName){
        for (Client client : clients) {
            if(client.getUserName().equalsIgnoreCase(userName)){
                return client;
            }
        }
        return null;
    }

//    public String generateKeyPair(){
//        try {
//            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
//            KeyPair keyPair = kpg.generateKeyPair();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        }
//    }

    public synchronized ArrayList<ClientHandler> getClients(){
        return clientHandlers;
    }
}

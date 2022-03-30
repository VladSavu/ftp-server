package com.company.ftp_server.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ServerConnection implements Runnable{
    private final Socket server;
    private final BufferedReader in;
    private final PrintWriter out;

    public ServerConnection(Socket server) throws IOException {
        this.server = server;
        in = new BufferedReader(new InputStreamReader(server.getInputStream()));
        out = new PrintWriter(server.getOutputStream(), true);
    }

    @Override
    public void run() {
       try{
            while (true) {
                String serverResponse = in.readLine();

                if (serverResponse == null) break;

                System.out.println(serverResponse);
            }
       } catch (IOException e) {
                e.printStackTrace();
       } finally {
           try {
               in.close();
               out.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
    }


}







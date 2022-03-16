package com.company.ftp_server;

// Example: Server that receive and sends characters. It converts the text to upper case.
// CharacterServer.java

import java.net.*;
import java.io.*;
import java.util.Arrays;

public class ControlServer {

    public static void main(String[] args) {
        final int CTRLPORT = 21;
        ServerSocket sServ;
        Socket sCon;
        BufferedReader input;
        PrintWriter output;

        String data = "";
        String[] commands = new String[] {"QUIT", "LIST", "RETR", "STOR"};

        try {
            // Create the socket
            sServ = new ServerSocket(CTRLPORT);
            System.out.println("Character Server waiting for requests");

            // Accept a connection  and create the socket for the transmission with the client
            sCon = sServ.accept();
            System.out.println("220 Service ready for new user");

            input = new BufferedReader(new InputStreamReader(sCon.getInputStream()));
            output = new PrintWriter(sCon.getOutputStream(), true);


            while(!data.equals("QUIT")) {

                // Get the input/output from the socket
                System.out.println("Please enter a command from the following: LIST, RETR, STOR or QUIT(shuts down the server)");
                // Read the data sent by the client
                data =  input.readLine();
                System.out.println("Server received: " + data);

                if(!Arrays.asList(commands).contains(data)){
                    continue;
                }else if(data.equals("LIST")){

                }else if(data.equals("RETR")){

                }else if(data.equals("STOR")){

                }

                // Send the text
                output.println(data);


            }
            // Close the socket
            sCon.close();

            // Close the server socket
            sServ.close();

        } catch(IOException e) {
            System.out.println("Error: " + e);
        }
    } // main
} // class CharacterServer


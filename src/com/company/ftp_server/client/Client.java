package com.company.ftp_server.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 20;
    private static final int SERVER_DATA_PORT = 21;
    private boolean loggedIn = false;
    private String userName = "Anonymous";

    private static Socket socket = null;

    public Client(String userName) {
        this.userName = userName;
    }
    public Client(){}

    public boolean getLoggedInStatus(){
        return loggedIn;
    }

    public String getUserName() {
        return userName;
    }

    public boolean login(String userName){
        if(userName.equals(this.userName)){
            loggedIn = true;
            return true;
        }
        return false;
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

    public static void main(String[] args) throws IOException {

        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));

//        Setup for the FTP command port
        Socket command_socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
        ServerConnection command_serverConnection = new ServerConnection(command_socket);
        PrintWriter command_out = new PrintWriter(command_socket.getOutputStream(), true);
        new Thread(command_serverConnection).start();

//        Continually listens for client connections on port 20
        while (true) {
            System.out.print("> ");
            String command = keyboard.readLine();

            if (command.contains("QUIT")) break;

//            Example: "SEND_FILE test.json"
            else if (command.startsWith("SEND_FILE")){
//                Notify the server of an upcoming byte connection so it can start the second port
                command_out.println(command);

//                Sets up the connection on the data port
                Socket client_data_socket = new Socket(SERVER_ADDRESS, SERVER_DATA_PORT);

                String[] words = command.split(" ");
                String fileName = words[1];
                System.out.println("filename is " + fileName);

                Path path = Paths.get("D:/Java Projects/ftp-server/src/com/company/ftp_server/client", fileName);


                byte[] fileContent = Files.readAllBytes(path);

                System.out.println("File is " + path);

                DataOutputStream dataOutputStream = new DataOutputStream(client_data_socket.getOutputStream());
                dataOutputStream.writeInt(fileContent.length);
                dataOutputStream.write(fileContent);

//                closing the connection for data transmission
                client_data_socket.close();
            }

//            Example: "RETR_FILE test.json"
            else if (command.startsWith("RETR_FILE")){
//                Notify the server of an upcoming byte connection so it can start the second port
                command_out.println(command);
                String[] words = command.split(" ");
                String fileName = words[1];

//                Sets up the connection on the data port
                Socket client_data_socket = new Socket(SERVER_ADDRESS, SERVER_DATA_PORT);

                DataInputStream dataInputStream = new DataInputStream(client_data_socket.getInputStream());

                int lenght = dataInputStream.readInt();
                byte[] message;
                if (lenght > 0){
                    message = new byte[lenght];
                    dataInputStream.readFully(message);
                    String fileLocation = "D:/Java Projects/ftp-server/src/com/company/ftp_server/client/" + fileName;
                    FileOutputStream fos = new FileOutputStream(fileLocation);
                    fos.write(message);
                }
                System.out.println("filename is " + fileName);

//                closing the connection for data transmission
                client_data_socket.close();
            }

            else{
                command_out.println(command);
            }


        }
        command_socket.close();
        System.exit(0);
    }


}

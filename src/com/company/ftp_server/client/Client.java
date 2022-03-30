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
        Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);

        ServerConnection serverConnection = new ServerConnection(socket);
        BufferedReader keyboard = new BufferedReader(new InputStreamReader(System.in));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        new Thread(serverConnection).start();

        while (true) {
            System.out.print("> ");
            String command = keyboard.readLine();

            if (command.contains("QUIT")) break;

//            Example: "SEND_FILE test.json"
            if (command.startsWith("SEND_FILE")){
//                Notify the server of an upcoming byte connection so it can start the second port
                out.println("SEND_FILE");

                Socket client_data_socket = new Socket(SERVER_ADDRESS, SERVER_DATA_PORT);

                String[] words = command.split(" ");
                String fileName = words[1];
                System.out.println("filename is " + fileName);
//                String userNameReceiver = words[2];

//                FileInputStream inputStream = new FileInputStream(fileName);
                Path path = Paths.get("D:/Java Projects/ftp-server/src/com/company/ftp_server/client", fileName);


                byte[] fileContent = Files.readAllBytes(path);

                System.out.println("File is " + path);
                System.out.println("File parent is " + path.getParent());

                DataOutputStream dataOutputStream = new DataOutputStream(client_data_socket.getOutputStream());
                dataOutputStream.writeInt(fileContent.length);
                dataOutputStream.write(fileContent);

//                closing the connection for data transmission
                client_data_socket.close();
//                inputStream.read(b, 0, b.length);
            }

            if (!command.startsWith("SEND_FILE")){
                out.println(command);
            }


        }
        socket.close();
        System.exit(0);

//        client.connectToServer();
//        ReadFromServerThread readFromServerThread = new ReadFromServerThread(client.reader);
//        Thread t = new Thread(readFromServerThread);
//        t.start();
//        if(client.connectToServer()){
//            client.doStuff();
//        }
    }


}

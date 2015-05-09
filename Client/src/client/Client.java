package client;

import chat.Message;
import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
    private ObjectInputStream ois;
    private ObjectOutputStream oos;
    private Socket socket;
    private String server, username;
    private int port;// default = 23352
    
    Client(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }
    
    public boolean start() {
        try {
            socket = new Socket(server, port);
        }catch (Exception e) {
            System.out.println("Exception creating client socket: " + e);
        }
        System.out.println("Connection accepted " + socket.getInetAddress() + ":" + socket.getPort());
        try {
            ois = new ObjectInputStream(socket.getInputStream());
            oos = new ObjectOutputStream(socket.getOutputStream());            
        }catch (Exception e) {
            System.out.println("Exception getting i/o streams: " + e);
        }
        new MakeThread().start();
        try {
            oos.writeObject(username);
        }catch (Exception e) {
            System.out.println("Exception on sending username: " + e);
            disconnect();
            return false;
        }
        return true;
    }
    
    private void sendmsg(Message msg) {
        try {
            oos.writeObject(msg);
        }catch (Exception e) {
            System.out.println("Exception during seending message: " + e);
        }
    }
    
    private void disconnect() {
        try {
            ois.close();
            oos.close();
            socket.close();
        }catch (Exception e) {
            System.out.println("Exception on disconnect: " + e);
        }
    }

    public static void main(String[] args) {
        int portn = 23352;
        String serverAdress = "localhost";
        String userName = "anonymous";
        if(args.length > 0)
            userName = args[0];
        if(args.length > 1)
            portn = Integer.parseInt(args[1]);
        if(args.length > 2)
            serverAdress = args[2];
        Client client = new Client(serverAdress, portn, userName);
        if(client.start() == false)
            return;
        Scanner sc = new Scanner(System.in);
        while(true) {
            System.out.print("- ");
            String msg = sc.nextLine();
            if(msg.equals("LOGOUT")) {
                Message smsg = new Message(Message.LOGOUT,"");
                client.sendmsg(smsg);
                break;
            }
            else {
                Message smsg = new Message(Message.MESSAGE,msg);
                client.sendmsg(smsg);
            }
        }
        client.disconnect();
    }
    
    class MakeThread extends Thread {
        public void run() {
            while(true) {
                try {
                    String msg = (String) ois.readObject();
                    System.out.println(msg);
                    System.out.print("- ");
                }catch (IOException | ClassNotFoundException e) {
                    System.out.println("Server closed connection: " + e);
                }
            }
        }
    }
    
}

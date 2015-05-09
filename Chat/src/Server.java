
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.*;

public class Server {
    private static int currentId;
    private final ArrayList <ClientThread> clients;
    private final int port;
    private boolean turnOff;
    
    public Server(int port) {
        this.port = port;
        this.clients = new ArrayList <>();
    }
    
    public void start() throws IOException {
        turnOff = false;
        try {
            ServerSocket ss = new ServerSocket(port);
            while(turnOff != true) {
                System.out.println("Waiting...");
                Socket s = ss.accept();
                if(turnOff == true)
                    break;
                ClientThread ct;
                ct = new ClientThread(s);
                clients.add(ct);
                ct.start();
            }
            //server stopped
            try {
                ss.close();
                for (ClientThread client : clients) {
                    client.ois.close();
                    client.oos.close();
                    client.socket.close();
                }
            }
            catch (Exception e) {
                System.out.println("Exception on closing server:" + e);
            }
        }
        catch(Exception e) {
            System.out.println("Exception on server socket: " + e);
        }
    }
    
    private void sendmsgtoall(String message) {
        Date dNow = new Date( );
        SimpleDateFormat ft = new SimpleDateFormat ("yyyy.MM.dd hh:mm:ss");
        String time = ft.format(dNow);
        System.out.println(time + ":" + message);
        for(int i = 0 ; i < clients.size() ; i++) {
            ClientThread client = clients.get(i);
            if(client.sendmsg(message) == false) {
                clients.remove(i);
                System.out.println("Force disconnected " + client.username);
            }
        }
    }
    
    private void remove(int id) {
        for(int i = 0 ; i < clients.size() ; i++) {
            ClientThread client = clients.get(i);
            if(client.id == id) {
                clients.remove(i);
                return;
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        int portn = 23352;
        if(args.length > 0)
            portn = Integer.parseInt(args[0]);
        Server server = new Server(portn);
        server.start();
    }
    
    class ClientThread extends Thread {
        private String username;
        Socket socket;
        ObjectInputStream ois;
        ObjectOutputStream oos;
        private int id;
        Message chatmessage;

        private ClientThread(Socket s) {
            id = ++currentId;
            this.socket = s;
            try {
                ois = new ObjectInputStream(socket.getInputStream());
                oos = new ObjectOutputStream(socket.getOutputStream());
                username = (String)ois.readObject();
                System.out.println(username + "connected to chat.");
            }
            catch (IOException | ClassNotFoundException e) {
                System.out.println("Exception creating client.");
            }
        }
        
        private void close() {
            try {
                oos.close();
                ois.close();
                socket.close();
            }
            catch (Exception e) {
                System.out.println("Could not close client.");
            }
        }
        
        private boolean sendmsg(String message) {
            if(!socket.isConnected()) {
                close();
                return false;
            }
            try {
                oos.writeObject(message);
            }
            catch (Exception e){ 
                System.out.println("Exception writing msg to oos: " + e);
            }
            return true;
        }
        
        public void run() {
            boolean flag = false;
            while(flag != true) {
                try {
                    chatmessage = (Message)ois.readObject();
                }
                catch (IOException | ClassNotFoundException e) {
                    System.out.println("Exception receiving message.");
                }
                String message = chatmessage.getMessage();
                int type = chatmessage.getType();
                if(type == Message.MESSAGE) {
                    sendmsgtoall(username + ": " + message);
                }
                if(type == Message.LOGOUT) {
                    System.out.println(username + "disconnected.");
                    flag = true;
                }
            }
            remove(id);
            close();
            
        }
    }
}

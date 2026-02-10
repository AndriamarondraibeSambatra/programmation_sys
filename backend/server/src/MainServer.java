import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public static void main(String[] args) {
        try{
            ServerSocket serverSocket = new ServerSocket(2121);
            FileManager fm = new FileManager();
            System.out.println("Server running on port 2121...");
            while(true){
                Socket client = serverSocket.accept();
                new Thread(new ClientThread(client,fm)).start();
            }
        } catch(Exception e){ e.printStackTrace(); }
    }
}

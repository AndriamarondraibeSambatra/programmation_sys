import java.io.*;
import java.net.*;
import java.util.*;

public class LoadBalancer {
    private static List<ServerInfo> servers = new ArrayList<>();
    private static Map<ServerInfo, Integer> serverClients = new HashMap<>();

    public static void main(String[] args) throws Exception {
        servers.add(new ServerInfo("127.0.0.1", 2121));
        servers.add(new ServerInfo("127.0.0.1", 2122));
        serverClients.put(servers.get(0), 0);
        serverClients.put(servers.get(1), 0);

        ServerSocket lbSocket = new ServerSocket(2100);
        System.out.println("Load Balancer running on port 2100...");

        while (true) {
            Socket clientSocket = lbSocket.accept();
            ServerInfo target = chooseServer();
            if (target == null) {
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                out.println("All servers busy. Try later.");
                clientSocket.close();
                continue;
            }
            serverClients.put(target, serverClients.get(target) + 1);
            System.out.println("Connexion sur " + target.ip + ":" + target.port +
                    " | Clients connectés: " + serverClients.get(target));
            new Thread(new ClientForwarder(clientSocket, target)).start();
        }
    }

    private static ServerInfo chooseServer() {
        for (ServerInfo s : servers) {
            if (serverClients.get(s) < 3)
                return s;
        }
        return null;
    }

    static class ServerInfo {
        String ip;
        int port;

        ServerInfo(String ip, int port) {
            this.ip = ip;
            this.port = port;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;
            ServerInfo that = (ServerInfo) o;
            return port == that.port && Objects.equals(ip, that.ip);
        }

        @Override
        public int hashCode() {
            return Objects.hash(ip, port);
        }
    }

    static class ClientForwarder implements Runnable {
        Socket clientSocket;
        ServerInfo server;

        ClientForwarder(Socket c, ServerInfo s) {
            clientSocket = c;
            server = s;
        }

        @Override
        public void run() {
            try (
                Socket serverSocket = new Socket(server.ip, server.port)
            ) {
                final Socket sSocket = serverSocket; 
                Thread t1 = new Thread(() -> forwardData(clientSocket, sSocket));
                Thread t2 = new Thread(() -> forwardData(sSocket, clientSocket));
                t1.start();
                t2.start();
                t1.join();
                t2.join();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                synchronized (serverClients) {
                    serverClients.put(server, serverClients.get(server) - 1);
                    System.out.println("Déconnexion sur " + server.ip + ":" + server.port +
                            " | Clients restants: " + serverClients.get(server));
                }
                try {
                    if (clientSocket != null)
                        clientSocket.close();
                } catch (Exception e) {
                }
            }
        }

        private void forwardData(Socket inSocket, Socket outSocket) {
            try {
                InputStream in = inSocket.getInputStream();
                OutputStream out = outSocket.getOutputStream();
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                outSocket.shutdownOutput();
            } catch (Exception e) {
            }
        }
    }
}
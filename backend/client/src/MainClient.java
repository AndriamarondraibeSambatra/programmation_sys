import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class MainClient {
   public static void main(String[] args) {
    try (
        Socket socket = new Socket("127.0.0.1", 2121);
        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
        Scanner sc = new Scanner(System.in)
    ) {
        // Login
        System.out.print("Username: ");
        String username = sc.nextLine().trim();
        System.out.print("Password: ");
        String password = sc.nextLine().trim();

        out.println("LOGIN;" + username + ";" + password);
        out.flush();

        String loginResponse = in.readLine();
        if (loginResponse == null) {
            System.out.println("Connexion fermée par le serveur");
            return;
        }
        System.out.println("Server: " + loginResponse);

        if (!loginResponse.startsWith("Welcome")) {
            System.out.println("Échec login → sortie");
            return;
        }

        // Boucle commandes
        while (true) {
            System.out.print("Command (upload/download/list/exit): ");
            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;

            if (input.equalsIgnoreCase("exit")) {
                break;
            }

            if (input.toLowerCase().startsWith("upload ")) {
                String[] parts = input.split("\\s+", 2);
                if (parts.length < 2) {
                    System.out.println("Usage: upload nom_du_fichier");
                    continue;
                }
                String filePath = parts[1];
                File file = new File(filePath);

                if (!file.exists() || !file.isFile()) {
                    System.out.println("Fichier introuvable : " + filePath);
                    continue;
                }

                String command = "UPLOAD;" + file.getName() + ";" + file.length();
                System.out.println("[DEBUG] Envoi: " + command);
                out.println(command);
                out.flush();

                // Lecture réponse serveur
                String response = in.readLine();
                if (response == null) {
                    System.out.println("Connexion perdue");
                    break;
                }

                System.out.println("Server response: '" + response + "'");

                if ("READY".equals(response.trim())) {
                    System.out.println("Envoi du fichier...");
                    try (FileInputStream fis = new FileInputStream(file)) {
                        OutputStream os = socket.getOutputStream();
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = fis.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        os.flush();
                        System.out.println("Upload terminé");
                    }
                } else {
                    System.out.println("Erreur serveur : " + response);
                }
            }

            else if (input.toLowerCase().startsWith("download ")) {
                String filename = input.split("\\s+", 2)[1];
                out.println("DOWNLOAD;" + filename);
                out.flush();
                String sizeLine = in.readLine();
                System.out.println("[DEBUG] Taille reçue: " + sizeLine);
                // etc.
            }

            else if (input.equalsIgnoreCase("list")) {
                out.println("LIST");
                out.flush();
                String line;
                while ((line = in.readLine()) != null && !line.isEmpty()) {  // attention : protocole LIST à améliorer
                    System.out.println(line);
                }
            }

            else {
                System.out.println("Commande inconnue");
            }
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}
}

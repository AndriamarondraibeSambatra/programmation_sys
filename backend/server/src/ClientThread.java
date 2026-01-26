import java.io.*;
import java.net.Socket;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;

public class ClientThread implements Runnable {

    private Socket socket;
    private FileManager fileManager;
    private String username; // utilisateur authentifié

    public ClientThread(Socket s, FileManager fm) {
        socket = s;
        fileManager = fm;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);

            /* ================= LOGIN ================= */
            String line = in.readLine();
            if (line == null || !line.startsWith("LOGIN")) {
                out.println("ERROR: LOGIN required");
                socket.close();
                return;
            }

            String[] parts = line.split(";");
            username = parts[1];
            String password = parts[2];

            // Charger users.json
            JSONParser parser = new JSONParser();
            JSONObject users = (JSONObject) parser.parse(
                    new FileReader("server/resources/user.json"));

            // Vérification username / password
            if (!users.containsKey(username)
                    || !users.get(username).equals(password)) {
                out.println("ERROR: Invalid username or password");
                socket.close();
                return;
            }

            out.println("Welcome " + username);
            System.out.println("User logged in: " + username);

            /* ============== COMMAND LOOP ============== */
            while ((line = in.readLine()) != null) {

                /* -------- UPLOAD -------- */
                if (line.startsWith("UPLOAD")) {
                    String[] cmd = line.split(";", 3); // sécurité si nom de fichier contient ;
                    if (cmd.length < 3) {
                        out.println("ERROR: Invalid UPLOAD format");
                        continue;
                    }

                    String filename = cmd[1].trim();
                    long size;
                    try {
                        size = Long.parseLong(cmd[2].trim());
                    } catch (NumberFormatException e) {
                        out.println("ERROR: Invalid file size");
                        continue;
                    }

                    System.out.printf("[UPLOAD] %s demande %s (%d octets)%n", username, filename, size);

                    String userDirPath = "shared_storage/users/" + username + "/"; // ← chemin absolu ou configurable !
                    File userDir = new File(userDirPath);
                    if (!userDir.exists() && !userDir.mkdirs()) {
                        out.println("ERROR: Cannot create user directory");
                        continue;
                    }

                    if (!fileManager.canWrite(username)) {
                        System.out.println("[REFUS] Pas de permission d'écriture pour " + username);
                        out.println("ERROR: No write permission");
                        continue;
                    }

                    if (!fileManager.hasEnoughQuota(username, size)) {
                        Long restant = fileManager.getQuota(username); // attention : pas thread-safe ici mais
                                                                         // acceptable pour debug
                        System.out.printf("[QUOTA] Refus - restant: %d   demandé: %d%n", restant != null ? restant : -1,
                                size);
                        out.println("ERROR: Quota exceeded (remaining: " + (restant != null ? restant : "unknown")
                                + " bytes)");
                        continue;
                    }

                    out.println("READY");

                    // Réception du fichier
                    try (FileOutputStream fos = new FileOutputStream(userDirPath + filename)) {
                        InputStream is = socket.getInputStream();
                        byte[] buffer = new byte[8192];
                        long remaining = size;
                        int read;

                        while (remaining > 0
                                && (read = is.read(buffer, 0, (int) Math.min(buffer.length, remaining))) != -1) {
                            fos.write(buffer, 0, read);
                            remaining -= read;
                        }

                        if (remaining > 0) {
                            System.out.println("[WARNING] Upload incomplet pour " + filename + " (" + remaining
                                    + " octets manquants)");
                        } else {
                            fileManager.consumeQuota(username, size);
                            System.out.println(
                                    "[SUCCESS] " + username + " a uploadé " + filename + " (" + size + " octets)");
                        }

                    } catch (IOException e) {
                        System.out.println("[ERREUR] Échec écriture fichier : " + e.getMessage());
                        out.println("ERROR: Upload failed");
                    }
                }
                /* -------- DOWNLOAD -------- */
                else if (line.startsWith("DOWNLOAD")) {
                    String filename = line.split(";")[1];
                    String userDir = "../shared_storage/users/" + username + "/";
                    File file = new File(userDir + filename);

                    if (!file.exists()) {
                        out.println("ERROR: File not found");
                        continue;
                    }

                    out.println(file.length());

                    FileInputStream fis = new FileInputStream(file);
                    OutputStream os = socket.getOutputStream();
                    byte[] buffer = new byte[4096];
                    int read;

                    while ((read = fis.read(buffer)) > 0) {
                        os.write(buffer, 0, read);
                    }

                    os.flush();
                    fis.close();
                } else if (line.equalsIgnoreCase("LIST")) {

                    String userDir = "../shared_storage/users/" + username + "/";
                    File dir = new File(userDir);

                    if (!dir.exists() || dir.listFiles() == null) {
                        out.println("No files");
                        continue;
                    }

                    File[] files = dir.listFiles();
                    if (files.length == 0) {
                        out.println("No files");
                        continue;
                    }

                    StringBuilder sb = new StringBuilder();
                    for (File f : files) {
                        sb.append(f.getName())
                                .append(" (")
                                .append(f.length())
                                .append(" bytes)\n");
                    }

                    out.println(sb.toString());
                }

                else {
                    out.println("ERROR: Unknown command");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

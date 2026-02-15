import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

final class ReplicationService {
    private static final int CONNECT_TIMEOUT_MS = 1200;
    private static final int READ_TIMEOUT_MS = 6000;

    private ReplicationService() {}

    static void replicateUploadAsync(String username, String filename, File sourceFile) {
        if (username == null || username.trim().isEmpty()) return;
        if (!StoragePaths.isSafeFilename(filename)) return;
        if (sourceFile == null || !sourceFile.exists() || !sourceFile.isFile()) return;

        Thread t = new Thread(() -> replicateUpload(username.trim(), filename.trim(), sourceFile),
                "replica-upload-" + username + "-" + System.nanoTime());
        t.setDaemon(true);
        t.start();
    }

    private static void replicateUpload(String username, String filename, File sourceFile) {
        ServerConfig cfg = ServerConfig.load();
        String host = cfg.getSlaveHost();
        int port = cfg.getSlavePort();
        if (host == null || host.trim().isEmpty() || port <= 0) {
            return;
        }

        long size = sourceFile.length();
        if (size <= 0L) {
            return;
        }

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            writeLine(out, "REPLICA;" + username + ";" + filename + ";" + size);
            String ready = readLine(in);
            if (ready == null || !"READY".equalsIgnoreCase(ready.trim())) {
                System.out.println("[REPLICA] " + host + ":" + port + " rejected replication for "
                        + username + "/" + filename + " -> " + ready);
                return;
            }

            try (FileInputStream fis = new FileInputStream(sourceFile)) {
                copyBytes(fis, out, size);
            }

            String result = readLine(in);
            if (result != null && result.startsWith("OK")) {
                System.out.println("[REPLICA] OK " + username + "/" + filename + " -> " + host + ":" + port);
                AuditLogger.log("system", "replica_upload",
                        username + "/" + filename + " -> " + host + ":" + port + " (" + size + " bytes)");
            } else {
                System.out.println("[REPLICA] Failed " + username + "/" + filename + " -> "
                        + host + ":" + port + " resp=" + result);
            }
        } catch (Exception e) {
            System.out.println("[REPLICA] Unreachable " + host + ":" + port + " (" + e.getMessage() + ")");
        }
    }

    private static void writeLine(OutputStream out, String line) throws IOException {
        String payload = (line == null ? "" : line) + "\n";
        out.write(payload.getBytes(StandardCharsets.UTF_8));
        out.flush();
    }

    private static String readLine(InputStream in) throws IOException {
        int b;
        int cap = 256;
        byte[] buf = new byte[cap];
        int len = 0;
        while ((b = in.read()) != -1) {
            if (b == '\n') break;
            if (b == '\r') continue;
            if (len == cap) {
                cap *= 2;
                byte[] n = new byte[cap];
                System.arraycopy(buf, 0, n, 0, len);
                buf = n;
            }
            buf[len++] = (byte) b;
        }
        if (len == 0 && b == -1) return null;
        return new String(buf, 0, len, StandardCharsets.UTF_8);
    }

    private static void copyBytes(InputStream in, OutputStream out, long size) throws IOException {
        byte[] buffer = new byte[8192];
        long remaining = size;
        while (remaining > 0) {
            int read = in.read(buffer, 0, (int) Math.min(buffer.length, remaining));
            if (read == -1) break;
            out.write(buffer, 0, read);
            remaining -= read;
        }
        out.flush();
        if (remaining > 0) {
            throw new IOException("Replication stream ended early: " + remaining + " bytes missing");
        }
    }
}

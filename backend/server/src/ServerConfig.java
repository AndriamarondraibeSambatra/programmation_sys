import java.io.FileReader;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class ServerConfig {
    private static final String DEFAULT_CONFIG_PATH = "resources/server_config.json";

    private String storageRoot = "shared_storage";
    private String slaveHost = "127.0.0.1";
    private int slavePort = 2222;
    private String slaveStorageRoot = "slave_storage";

    public static ServerConfig load() {
        return load(DEFAULT_CONFIG_PATH);
    }

    public static ServerConfig load(String path) {
        ServerConfig cfg = new ServerConfig();
        try {
            JSONParser parser = new JSONParser();
            JSONObject obj = (JSONObject) parser.parse(new FileReader(path));
            Object storage = obj.get("storage_root");
            if (storage instanceof String && !((String) storage).trim().isEmpty()) {
                cfg.storageRoot = ((String) storage).trim();
            }
            Object host = obj.get("slave_host");
            if (host instanceof String && !((String) host).trim().isEmpty()) {
                cfg.slaveHost = ((String) host).trim();
            }
            Object port = obj.get("slave_port");
            if (port instanceof Number) {
                cfg.slavePort = ((Number) port).intValue();
            } else if (port instanceof String) {
                try { cfg.slavePort = Integer.parseInt(((String) port).trim()); } catch (Exception ignored) {}
            }
            Object slaveStorage = obj.get("slave_storage_root");
            if (slaveStorage instanceof String && !((String) slaveStorage).trim().isEmpty()) {
                cfg.slaveStorageRoot = ((String) slaveStorage).trim();
            }
        } catch (Exception e) {
            System.out.println("[Config] Using defaults (" + path + " not found or invalid)");
        }
        cfg.applyEnvironmentOverrides();
        return cfg;
    }

    private void applyEnvironmentOverrides() {
        String envStorage = envOrNull("SMARTDRIVE_STORAGE_ROOT");
        if (envStorage != null) storageRoot = envStorage;

        String envSlaveHost = envOrNull("SMARTDRIVE_SLAVE_HOST");
        if (envSlaveHost != null) slaveHost = envSlaveHost;

        Integer envSlavePort = envIntOrNull("SMARTDRIVE_SLAVE_PORT");
        if (envSlavePort != null && envSlavePort > 0) slavePort = envSlavePort;

        String envSlaveStorage = envOrNull("SMARTDRIVE_SLAVE_STORAGE_ROOT");
        if (envSlaveStorage != null) slaveStorageRoot = envSlaveStorage;
    }

    private static String envOrNull(String key) {
        String v = System.getenv(key);
        if (v == null) return null;
        String out = v.trim();
        return out.isEmpty() ? null : out;
    }

    private static Integer envIntOrNull(String key) {
        String v = envOrNull(key);
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (Exception ignored) {
            return null;
        }
    }

    public String getStorageRoot() {
        return storageRoot;
    }

    public String getSlaveHost() {
        return slaveHost;
    }

    public int getSlavePort() {
        return slavePort;
    }

    public String getSlaveStorageRoot() {
        return slaveStorageRoot;
    }
}

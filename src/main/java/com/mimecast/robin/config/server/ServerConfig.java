package com.mimecast.robin.config.server;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.ConfigFoundation;

import java.io.IOException;
import java.util.*;

/**
 * Server configuration.
 *
 * <p>This class provides type safe access to server configuration.
 * <p>It also maps authentication users and behaviour scenarios to corresponding objects.
 *
 * @see UserConfig
 * @see ScenarioConfig
 */
@SuppressWarnings("unchecked")
public class ServerConfig extends ConfigFoundation {

    /**
     * Constructs a new ServerConfig instance.
     */
    public ServerConfig() {
        super();
    }

    /**
     * Constructs a new ServerConfig instance with configuration path.
     *
     * @param path Path to configuration file.
     * @throws IOException Unable to read file.
     */
    public ServerConfig(String path) throws IOException {
        super(path);
    }

    /**
     * Gets hostname.
     *
     * @return Hostname.
     */
    public String getHostname() {
        return getStringProperty("hostname", "mimecast.com");
    }

    /**
     * Gets bind address.
     *
     * @return Bind address string.
     */
    public String getBind() {
        return getStringProperty("bind", "::");
    }

    /**
     * Gets SMTP port.
     *
     * @return Bind address number.
     */
    public int getPort() {
        return Math.toIntExact(getLongProperty("port", 25L));
    }

    /**
     * Gets SMTPS port.
     *
     * @return Bind address number.
     */
    public int getSecurePort() {
        return Math.toIntExact(getLongProperty("securePort", 465L));
    }

    /**
     * Gets Submission port.
     *
     * @return Bind address number.
     */
    public int getSubmissionPort() {
        return Math.toIntExact(getLongProperty("submissionPort", 587L));
    }

    /**
     * Gets backlog size.
     *
     * @return Backlog size.
     */
    public int getBacklog() {
        return Math.toIntExact(getLongProperty("backlog", 25L));
    }

    /**
     * Gets minimum pool size.
     *
     * @return Thread pool min size.
     */
    public int getMinimumPoolSize() {
        return Math.toIntExact(getLongProperty("minimumPoolSize", 1L));
    }

    /**
     * Gets maximum pool size.
     *
     * @return Thread pool max size.
     */
    public int getMaximumPoolSize() {
        return Math.toIntExact(getLongProperty("maximumPoolSize", 10L));
    }

    /**
     * Gets thread keep alive time.
     *
     * @return Time in seconds.
     */
    public int getThreadKeepAliveTime() {
        return Math.toIntExact(getLongProperty("threadKeepAliveTime", 60L));
    }

    /**
     * Gets transactions limit.
     * <p>This defines how many commands will be processed before breaking receipt loop.
     *
     * @return Error limit.
     */
    public int getTransactionsLimit() {
        return Math.toIntExact(getLongProperty("transactionsLimit", 200L));
    }

    /**
     * Gets error limit.
     * <p>This defines how many syntax errors should be permitted before iterrupting the receipt.
     *
     * @return Error limit.
     */
    public int getErrorLimit() {
        return Math.toIntExact(getLongProperty("errorLimit", 3L));
    }

    /**
     * Is AUTH enabled.
     *
     * @return Boolean.
     */
    public boolean isAuth() {
        return getBooleanProperty("auth", false);
    }

    /**
     * Is STARTTLS enabled.
     *
     * @return Boolean.
     */
    public boolean isStartTls() {
        return getBooleanProperty("starttls", true);
    }

    /**
     * Is CHUNKING enabled.
     *
     * @return Boolean.
     */
    public boolean isChunking() {
        return getBooleanProperty("chunking", true);
    }

    /**
     * Gets key store.
     *
     * @return Key store path.
     */
    public String getKeyStore() {
        return getStringProperty("keystore", "/usr/local/keystore");
    }

    /**
     * Gets key store password.
     *
     * @return Key store password string.
     */
    public String getKeyStorePassword() {
        return getStringProperty("keystorepassword", "");
    }

    /**
     * Gets metrics port.
     *
     * @return Bind address number.
     */
    public int getMetricsPort() {
        return Math.toIntExact(getLongProperty("metricsPort", 8080L));
    }

    /**
     * Gets API port for client submission endpoint.
     *
     * @return Port number.
     */
    public int getApiPort() {
        return Math.toIntExact(getLongProperty("apiPort", 8090L));
    }

    /**
     * Gets storage config.
     *
     * @return BasicConfig instance.
     */
    public BasicConfig getStorage() {
        return new BasicConfig(getMapProperty("storage"));
    }

    /**
     * Gets relay config.
     *
     * @return BasicConfig instance.
     */
    public BasicConfig getRelay() {
        return new BasicConfig(getMapProperty("relay"));
    }

    /**
     * Is Dovecot authentication via UNIX socket enabled.
     *
     * @return Boolean.
     */
    public boolean isDovecotAuth() {
        return getBooleanProperty("dovecotAuth", false);
    }

    /**
     * Gets Dovecot AUTH socket path.
     *
     * @return String.
     */
    public String getDovecotAuthSocket() {
        return getStringProperty("dovecotAuthSocket", "/run/dovecot/auth-userdb");
    }

    /**
     * Gets Dovecot LDA binary path.
     *
     * @return String.
     */
    public String getDovecotLdaBinary() {
        return getStringProperty("dovecotLdaBinary", "/usr/libexec/dovecot/dovecot-lda");
    }

    /**
     * Is users enabled.
     *
     * @return Boolean.
     */
    public boolean isUsersEnabled() {
        return !isDovecotAuth() && getBooleanProperty("usersEnabled", false);
    }

    /**
     * Gets users list.
     *
     * @return Users list.
     */
    public List<UserConfig> getUsers() {
        List<UserConfig> users = new ArrayList<>();
        for (Map<String, String> user : (List<Map<String, String>>) getListProperty("users")) {
            users.add(new UserConfig(user));
        }
        return users;
    }

    /**
     * Gets user by username.
     *
     * @param find Username to find.
     * @return Optional of UserConfig.
     */
    public Optional<UserConfig> getUser(String find) {
        for (UserConfig user : getUsers()) {
            if (user.getName().equals(find)) {
                return Optional.of(user);
            }
        }
        return Optional.empty();
    }

    /**
     * Gets scenarios map.
     *
     * @return Scenarios map.
     */
    @SuppressWarnings("rawtypes")
    public Map<String, ScenarioConfig> getScenarios() {
        Map<String, ScenarioConfig> scenarios = new HashMap<>();
        for (Object object : getMapProperty("scenarios").entrySet()) {
            Map.Entry entry = (Map.Entry) object;
            scenarios.put((String) entry.getKey(), new ScenarioConfig((Map) entry.getValue()));
        }
        return scenarios;
    }
}

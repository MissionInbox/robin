package com.mimecast.robin.config.server;

import com.mimecast.robin.config.BasicConfig;
import com.mimecast.robin.config.ConfigFoundation;

import java.util.List;
import java.util.Map;

/**
 * Server scenario configuration container.
 *
 * <p>This is a container for scenarios defined in the server configuration.
 * <p>One instance will be made for every scenario defined.
 * <p>This can be used to define specific behaviours for the server.
 * <p>As in when to reject a command and with what response.
 *
 * @see ServerConfig
 */
@SuppressWarnings("unchecked")
public class ScenarioConfig extends ConfigFoundation {

    /**
     * Constructs a new ScenarioConfig instance with given map.
     *
     * @param map Properties map.
     */
    @SuppressWarnings("rawtypes")
    public ScenarioConfig(Map map) {
        super(map);
    }

    /**
     * Gets HELO to match.
     * <p>This is used to select the scenario to run.
     * <p>Once the specific HELO is received the server will enact the matching scenario.
     *
     * @return HELO string.
     */
    public String getHelo() {
        return getStringProperty("helo");
    }

    /**
     * Gets LHLO to match.
     * <p>This is used to select the scenario to run.
     * <p>Once the specific LHLO is received the server will enact the matching scenario.
     *
     * @return LHLO string.
     */
    public String getLhlo() {
        return getStringProperty("lhlo");
    }

    /**
     * Gets EHLO to match.
     * <p>This is used to select the scenario to run.
     * <p>Once the specific EHLO is received the server will enact the matching scenario.
     *
     * @return EHLO string.
     */
    public String getEhlo() {
        return getStringProperty("ehlo");
    }

    /**
     * Gets STARTTLS response.
     * <p>If none defined the server will 220.
     * <p>If response doesn't start with 2 it will not handshake.
     *
     * @return BasicConfig instance.
     */
    public BasicConfig getStarTls() {
        return new BasicConfig(getMapProperty("starttls"));
    }

    /**
     * Gets MAIL response.
     * <p>If none defined the server will 250.
     *
     * @return MAIL response string.
     */
    public String getMail() {
        return getStringProperty("mail");
    }

    /**
     * Gets RCPT list.
     * <p>Each entry is a map with a value key and a response key.
     * <p>Value represents the TO address to match.
     * <p>Response is obviously the response to give if value matched.
     * <p>If none defined the server will 250.
     *
     * @return RCPT list.
     */
    public List<Map<String, String>> getRcpt() {
        return getListProperty("rcpt");
    }

    /**
     * Gets DATA response.
     * <p>If none defined the server will 250.
     *
     * @return DATA response string.
     */
    public String getData() {
        return getStringProperty("data");
    }
}

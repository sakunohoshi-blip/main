package dev.moneyadvisor;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Files;
import java.nio.file.Path;

/** Stores the player's own Hypixel API key in the Fabric config folder. */
public final class Config {
    private static final Path FILE =
        FabricLoader.getInstance().getConfigDir().resolve("skyblock-money-advisor.json");

    public static volatile String apiKey = "";

    private Config() {}

    public static void load() {
        try {
            if (Files.exists(FILE)) {
                JsonObject o = JsonParser.parseString(Files.readString(FILE)).getAsJsonObject();
                if (o.has("apiKey")) apiKey = o.get("apiKey").getAsString();
            }
        } catch (Exception e) {
            MoneyAdvisorMod.LOG.warn("Could not read config", e);
        }
    }

    public static void save(String key) {
        apiKey = key;
        try {
            JsonObject o = new JsonObject();
            o.addProperty("apiKey", key);
            Files.writeString(FILE, o.toString());
        } catch (Exception e) {
            MoneyAdvisorMod.LOG.warn("Could not save config", e);
        }
    }
}

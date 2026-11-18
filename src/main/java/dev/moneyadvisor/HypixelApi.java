package dev.moneyadvisor;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class HypixelApi {
    private static final String BASE = "https://api.hypixel.net/v2";
    private static final HttpClient HTTP = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(10)).build();

    public record ProfileResult(String cuteName, JsonObject member, double bank) {}

    private HypixelApi() {}

    private static JsonObject get(String url, String key) throws Exception {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(15))
            .header("User-Agent", "SkyblockMoneyAdvisor/1.0");
        if (key != null) b.header("API-Key", key);
        HttpResponse<String> r = HTTP.send(b.build(), HttpResponse.BodyHandlers.ofString());
        if (r.statusCode() == 403) throw new IllegalStateException("API key refused (403). Check /moneyadvisor key.");
        if (r.statusCode() == 429) throw new IllegalStateException("Rate limited by Hypixel (429). Wait a minute.");
        if (r.statusCode() != 200) throw new IllegalStateException("Hypixel API returned HTTP " + r.statusCode());
        JsonObject o = JsonParser.parseString(r.body()).getAsJsonObject();
        if (!o.has("success") || !o.get("success").getAsBoolean()) {
            throw new IllegalStateException("Hypixel API reported failure.");
        }
        return o;
    }

    /** Fetches the selected profile (or the one named {@code wanted}). */
    public static ProfileResult fetchProfile(String uuid, String key, String wanted) throws Exception {
        JsonObject o = get(BASE + "/skyblock/profiles?uuid=" + uuid, key);
        JsonElement pe = o.get("profiles");
        if (pe == null || pe.isJsonNull()) throw new IllegalStateException("No SkyBlock profiles found for this account.");
        JsonArray arr = pe.getAsJsonArray();

        JsonObject pick = null;
        for (JsonElement e : arr) {
            JsonObject p = e.getAsJsonObject();
            String cute = p.has("cute_name") ? p.get("cute_name").getAsString() : "";
            if (wanted != null) {
                if (cute.equalsIgnoreCase(wanted)) pick = p;
            } else if (p.has("selected") && p.get("selected").getAsBoolean()) {
                pick = p;
            }
        }
        if (pick == null) {
            throw new IllegalStateException(wanted != null
                ? "No profile named '" + wanted + "' found." : "No selected profile found.");
        }
        JsonObject members = pick.getAsJsonObject("members");
        if (members == null || !members.has(uuid)) throw new IllegalStateException("Your data is missing from that profile.");

        double bank = 0;
        if (pick.has("banking") && pick.get("banking").isJsonObject()) {
            JsonObject banking = pick.getAsJsonObject("banking");
            if (banking.has("balance")) bank = banking.get("balance").getAsDouble();
        }
        return new ProfileResult(pick.get("cute_name").getAsString(), members.getAsJsonObject(uuid), bank);
    }

    /** Public endpoint, no key needed. Returns product id -> instant-sell price. */
    public static Map<String, Double> fetchBazaarPrices() throws Exception {
        JsonObject o = get(BASE + "/skyblock/bazaar", null);
        JsonObject products = o.getAsJsonObject("products");
        Map<String, Double> out = new HashMap<>();
        for (Map.Entry<String, JsonElement> en : products.entrySet()) {
            JsonObject qs = en.getValue().getAsJsonObject().getAsJsonObject("quick_status");
            if (qs != null && qs.has("sellPrice")) out.put(en.getKey(), qs.get("sellPrice").getAsDouble());
        }
        return out;
    }
}

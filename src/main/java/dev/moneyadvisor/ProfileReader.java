package dev.moneyadvisor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class ProfileReader {
    private static final String[] INVENTORY_KEYS = {
        "inv_armor", "equipment_contents", "inv_contents", "ender_chest_contents", "wardrobe_contents"
    };

    private ProfileReader() {}

    public static PlayerProfile read(HypixelApi.ProfileResult pr) {
        JsonObject m = pr.member();
        PlayerProfile p = new PlayerProfile();
        p.profileName = pr.cuteName();

        // Skills
        JsonElement exp = path(m, "player_data", "experience");
        if (exp != null && exp.isJsonObject()) {
            p.skillsKnown = true;
            for (String s : SkyblockData.SKILLS) {
                p.skills.put(s, SkyblockData.level(num(exp.getAsJsonObject().get("SKILL_" + s.toUpperCase())),
                    SkyblockData.SKILL_XP));
            }
        }

        // Dungeons
        p.catacombs = SkyblockData.level(
            num(path(m, "dungeons", "dungeon_types", "catacombs", "experience")), SkyblockData.CATACOMBS_XP);

        // Slayers
        for (String s : SkyblockData.SLAYERS) {
            p.slayers.put(s, SkyblockData.level(
                num(path(m, "slayer", "slayer_bosses", s, "xp")), SkyblockData.SLAYER_XP));
        }

        // Money
        p.coins = num(path(m, "currencies", "coin_purse")) + pr.bank();

        // Gear
        p.gearKnown = path(m, "inventory", "inv_contents", "data") != null;
        for (String key : INVENTORY_KEYS) {
            JsonElement d = path(m, "inventory", key, "data");
            if (d != null && d.isJsonPrimitive()) p.items.addAll(Nbt.itemIds(d.getAsString()));
        }
        return p;
    }

    private static JsonElement path(JsonElement root, String... keys) {
        JsonElement cur = root;
        for (String k : keys) {
            if (cur == null || !cur.isJsonObject() || !cur.getAsJsonObject().has(k)) return null;
            cur = cur.getAsJsonObject().get(k);
        }
        return cur;
    }

    private static double num(JsonElement e) {
        return e != null && e.isJsonPrimitive() ? e.getAsDouble() : 0;
    }
}

package dev.moneyadvisor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/** What the advisor knows about the player, read from the Hypixel API. */
public final class PlayerProfile {
    public String profileName = "?";
    public final Map<String, Integer> skills = new HashMap<>();
    public final Map<String, Integer> slayers = new HashMap<>();
    public int catacombs = 0;
    /** Purse + bank. */
    public double coins = 0;
    /** Every SkyBlock item id found in armor, equipment, inventory, ender chest and wardrobe. */
    public final Set<String> items = new HashSet<>();
    /** False when the player's Inventory API is switched off in SkyBlock settings. */
    public boolean gearKnown = false;
    public boolean skillsKnown = false;
}

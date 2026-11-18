package dev.moneyadvisor;

import java.util.List;
import java.util.Map;

/** One money-making method, loaded from methods.json. */
public final class Method {
    public String id;
    public String name;
    public String description;
    public String tips;
    /** Coins/hour with entry-level gear. Used when live Bazaar pricing is unavailable. */
    public double baseline;
    /** Coins/hour from NPC sales etc., added on top of the Bazaar items below. */
    public double flatPerHour;
    /** Bazaar items produced per hour with entry-level gear. */
    public List<ItemRate> items;
    public Requirements requirements;
    /** Better gear you own multiplies the estimate (best matching tier wins). */
    public List<GearTier> gearTiers;

    public static final class ItemRate {
        public String id;
        public double perHour;
    }

    public static final class Requirements {
        public Map<String, Integer> skills;
        public Integer catacombs;
        public Map<String, Integer> slayers;
        public Double minCoins;
        /** Item ids (a trailing * means "starts with"); you need at least one. */
        public List<String> anyOfItems;
    }

    public static final class GearTier {
        public String label;
        public List<String> anyOf;
        public double mult;
    }
}

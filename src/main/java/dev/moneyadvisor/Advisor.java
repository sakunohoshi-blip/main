package dev.moneyadvisor;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class Advisor {
    private Advisor() {}

    public record Result(Method method, boolean eligible, List<String> missing,
                         double coinsPerHour, String gearLabel) {}

    public static List<Result> rank(PlayerProfile p, List<Method> methods, Map<String, Double> prices) {
        List<Result> out = new ArrayList<>();
        for (Method m : methods) {
            List<String> missing = missing(p, m);
            double base = baseRate(m, prices);
            double mult = 1.0;
            String label = p.gearKnown ? "entry-level gear" : "gear unknown";
            if (p.gearKnown && m.gearTiers != null) {
                for (Method.GearTier t : m.gearTiers) {
                    if (t.mult > mult && owns(p.items, t.anyOf)) {
                        mult = t.mult;
                        label = t.label;
                    }
                }
            }
            out.add(new Result(m, missing.isEmpty(), missing, base * mult, label));
        }
        // Eligible first (best earner on top), then the closest-to-unlocking ones.
        out.sort(Comparator
            .comparing((Result r) -> !r.eligible())
            .thenComparingInt(r -> r.missing().size())
            .thenComparing(Comparator.comparingDouble(Result::coinsPerHour).reversed()));
        return out;
    }

    private static double baseRate(Method m, Map<String, Double> prices) {
        if (prices != null && m.items != null && !m.items.isEmpty()) {
            double sum = 0;
            for (Method.ItemRate ir : m.items) {
                Double price = prices.get(ir.id);
                if (price != null) sum += price * ir.perHour;
            }
            if (sum > 0) return sum + m.flatPerHour;
        }
        return m.baseline + m.flatPerHour;
    }

    private static List<String> missing(PlayerProfile p, Method m) {
        List<String> out = new ArrayList<>();
        Method.Requirements r = m.requirements;
        if (r == null) return out;

        if (r.skills != null && p.skillsKnown) {
            for (Map.Entry<String, Integer> e : r.skills.entrySet()) {
                int have = p.skills.getOrDefault(e.getKey(), 0);
                if (have < e.getValue()) {
                    out.add(cap(e.getKey()) + " " + e.getValue() + " (you: " + have + ")");
                }
            }
        }
        if (r.catacombs != null && p.catacombs < r.catacombs) {
            out.add("Catacombs " + r.catacombs + " (you: " + p.catacombs + ")");
        }
        if (r.slayers != null) {
            for (Map.Entry<String, Integer> e : r.slayers.entrySet()) {
                int have = p.slayers.getOrDefault(e.getKey(), 0);
                if (have < e.getValue()) {
                    out.add(cap(e.getKey()) + " slayer " + e.getValue() + " (you: " + have + ")");
                }
            }
        }
        if (r.minCoins != null && p.coins < r.minCoins) {
            out.add(Report.coins(r.minCoins) + " coins to start (you: " + Report.coins(p.coins) + ")");
        }
        if (r.anyOfItems != null && p.gearKnown && !owns(p.items, r.anyOfItems)) {
            out.add("one of: " + String.join(", ", r.anyOfItems.stream().limit(3).toList()));
        }
        return out;
    }

    /** True if the player owns any item matching one of the patterns (trailing * = prefix). */
    static boolean owns(Set<String> items, List<String> patterns) {
        if (patterns == null) return false;
        for (String pat : patterns) {
            if (pat.endsWith("*")) {
                String prefix = pat.substring(0, pat.length() - 1);
                for (String it : items) if (it.startsWith(prefix)) return true;
            } else if (items.contains(pat)) {
                return true;
            }
        }
        return false;
    }

    private static String cap(String s) {
        return s.isEmpty() ? s : s.substring(0, 1).toUpperCase(Locale.ROOT) + s.substring(1);
    }
}

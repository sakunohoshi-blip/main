package dev.moneyadvisor;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Turns ranked results into chat lines. */
public final class Report {
    private Report() {}

    public static List<Component> build(PlayerProfile p, List<Advisor.Result> results,
                                        boolean livePrices, boolean showAll) {
        List<Component> out = new ArrayList<>();
        out.add(Component.literal("--- Money Advisor · profile " + p.profileName + " ---")
            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));

        if (!p.gearKnown) {
            out.add(Component.literal("Gear unreadable: turn on SkyBlock Menu > Settings > Personal > API Settings > Inventory, "
                + "then rerun. Estimates below assume entry-level gear.").withStyle(ChatFormatting.YELLOW));
        }
        if (!p.skillsKnown) {
            out.add(Component.literal("Skill levels unreadable: skill requirements were not checked.")
                .withStyle(ChatFormatting.YELLOW));
        }

        List<Advisor.Result> eligible = results.stream().filter(Advisor.Result::eligible).toList();
        if (eligible.isEmpty()) {
            out.add(Component.literal("No method unlocked yet. Closest ones are listed below.").withStyle(ChatFormatting.RED));
        }

        int shown = showAll ? eligible.size() : Math.min(3, eligible.size());
        for (int i = 0; i < shown; i++) {
            Advisor.Result r = eligible.get(i);
            out.add(Component.literal("#" + (i + 1) + " " + r.method().name)
                .withStyle(i == 0 ? ChatFormatting.GREEN : ChatFormatting.WHITE, ChatFormatting.BOLD)
                .append(Component.literal("  ~" + coins(r.coinsPerHour()) + "/h").withStyle(ChatFormatting.GOLD)));
            out.add(Component.literal("   " + r.gearLabel() + " · " + nz(r.method().description))
                .withStyle(ChatFormatting.GRAY));
            if (i == 0 && r.method().tips != null) {
                out.add(Component.literal("   Tip: " + r.method().tips).withStyle(ChatFormatting.AQUA));
            }
        }

        // Best "almost unlocked" method that would beat what you can do now.
        double bestNow = eligible.isEmpty() ? 0 : eligible.get(0).coinsPerHour();
        results.stream()
            .filter(r -> !r.eligible() && r.missing().size() <= 2 && r.coinsPerHour() > bestNow)
            .max(java.util.Comparator.comparingDouble(Advisor.Result::coinsPerHour))
            .ifPresent(r -> out.add(Component.literal("Next unlock: " + r.method().name + " (~"
                    + coins(r.coinsPerHour()) + "/h) needs " + String.join("; ", r.missing()))
                .withStyle(ChatFormatting.LIGHT_PURPLE)));

        out.add(Component.literal("Rough estimates (" + (livePrices ? "live Bazaar prices" : "Bazaar offline, baseline values")
            + "). Tune them in config/skyblock-money-advisor-methods.json.").withStyle(ChatFormatting.DARK_GRAY));
        return out;
    }

    public static String coins(double v) {
        if (v >= 1e9) return String.format(Locale.ROOT, "%.2fB", v / 1e9);
        if (v >= 1e6) return String.format(Locale.ROOT, "%.1fM", v / 1e6);
        if (v >= 1e3) return String.format(Locale.ROOT, "%.0fk", v / 1e3);
        return String.format(Locale.ROOT, "%.0f", v);
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}

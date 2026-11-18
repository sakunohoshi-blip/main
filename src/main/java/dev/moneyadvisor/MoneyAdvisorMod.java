package dev.moneyadvisor;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Client-side only. Commands are handled locally (never sent to the server),
 * so your API key is never typed into Hypixel chat.
 */
public class MoneyAdvisorMod implements ClientModInitializer {
    public static final String ID = "skyblock-money-advisor";
    public static final Logger LOG = LoggerFactory.getLogger(ID);

    @Override
    public void onInitializeClient() {
        Config.load();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
            dispatcher.register(ClientCommands.literal("moneyadvisor")
                .executes(ctx -> { help(ctx.getSource()); return 1; })
                .then(ClientCommands.literal("key")
                    .then(ClientCommands.argument("apikey", StringArgumentType.word())
                        .executes(ctx -> {
                            Config.save(StringArgumentType.getString(ctx, "apikey"));
                            say(ctx.getSource(), "API key saved. Now run /moneyadvisor analyze", ChatFormatting.GREEN);
                            return 1;
                        })))
                .then(ClientCommands.literal("analyze")
                    .executes(ctx -> run(ctx.getSource(), null, false))
                    .then(ClientCommands.argument("profile", StringArgumentType.word())
                        .executes(ctx -> run(ctx.getSource(),
                            StringArgumentType.getString(ctx, "profile"), false))))
                .then(ClientCommands.literal("list")
                    .executes(ctx -> run(ctx.getSource(), null, true))
                    .then(ClientCommands.argument("profile", StringArgumentType.word())
                        .executes(ctx -> run(ctx.getSource(),
                            StringArgumentType.getString(ctx, "profile"), true))))));
    }

    private static void help(FabricClientCommandSource src) {
        say(src, "SkyBlock Money Advisor", ChatFormatting.GOLD);
        say(src, "/moneyadvisor key <apikey>        - save your Hypixel API key (developer.hypixel.net)", ChatFormatting.GRAY);
        say(src, "/moneyadvisor analyze [profile]   - top 3 methods for your gear", ChatFormatting.GRAY);
        say(src, "/moneyadvisor list [profile]      - every method you qualify for", ChatFormatting.GRAY);
    }

    private static int run(FabricClientCommandSource src, String profileName, boolean all) {
        if (Config.apiKey.isBlank()) {
            say(src, "No API key yet. Create one at developer.hypixel.net then run /moneyadvisor key <apikey>", ChatFormatting.RED);
            return 0;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return 0;
        String uuid = mc.player.getUUID().toString().replace("-", "");
        say(src, "Analysing your profile...", ChatFormatting.GRAY);

        CompletableFuture.runAsync(() -> {
            try {
                HypixelApi.ProfileResult pr = HypixelApi.fetchProfile(uuid, Config.apiKey, profileName);
                Map<String, Double> prices = null;
                try {
                    prices = HypixelApi.fetchBazaarPrices();
                } catch (Exception e) {
                    LOG.warn("Bazaar unavailable, using baseline estimates", e);
                }
                PlayerProfile p = ProfileReader.read(pr);
                List<Advisor.Result> results = Advisor.rank(p, MethodRepo.load(), prices);
                List<Component> lines = Report.build(p, results, prices != null, all);
                mc.execute(() -> lines.forEach(src::sendFeedback));
            } catch (Exception e) {
                LOG.warn("Analysis failed", e);
                mc.execute(() -> say(src, "Error: " + e.getMessage(), ChatFormatting.RED));
            }
        });
        return 1;
    }

    static void say(FabricClientCommandSource src, String msg, ChatFormatting color) {
        src.sendFeedback(Component.literal(msg).withStyle(color));
    }
}

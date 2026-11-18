package dev.moneyadvisor;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Loads the method list. If config/skyblock-money-advisor-methods.json exists it
 * replaces the bundled list, so you can tune numbers or add your own methods.
 */
public final class MethodRepo {
    private static final java.lang.reflect.Type TYPE = new TypeToken<List<Method>>() {}.getType();

    private MethodRepo() {}

    public static List<Method> load() throws Exception {
        Path override = FabricLoader.getInstance().getConfigDir()
            .resolve("skyblock-money-advisor-methods.json");
        if (Files.exists(override)) {
            try (Reader r = Files.newBufferedReader(override, StandardCharsets.UTF_8)) {
                return new Gson().fromJson(r, TYPE);
            }
        }
        try (InputStream in = MethodRepo.class.getResourceAsStream("/methods.json")) {
            if (in == null) throw new IllegalStateException("Bundled methods.json is missing.");
            return new Gson().fromJson(new InputStreamReader(in, StandardCharsets.UTF_8), TYPE);
        }
    }
}

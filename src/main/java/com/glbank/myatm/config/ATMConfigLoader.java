package com.glbank.myatm.config;

import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ATMConfigLoader {

    private static final String CONFIG_FILE = "myATM.cfg";

    private static Path getConfigPath() {
        return FMLPaths.CONFIGDIR.get().resolve(CONFIG_FILE);
    }

    public static String[] getConfig(String blockName) {
        Path path = getConfigPath();
        if (!Files.exists(path)) {
            createDefaultConfig(path);
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(path);
            for (String raw : lines) {
                String line = raw.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split("':'");
                if (parts.length < 3) continue;

                String name = parts[0].replaceAll("^'", "").trim();
                String url  = parts[1].trim();
                String pass = parts[2].replaceAll("'$", "").trim();

                if (name.equalsIgnoreCase(blockName)) {
                    return new String[]{url, pass};
                }
            }
        } catch (IOException e) {
            System.err.println("[MyATM] Failed to read config: " + e.getMessage());
        }
        return null;
    }

    private static void createDefaultConfig(Path path) {
        String defaultContent =
                "# SimpleATM Configuration File\n" +
                "# Format: 'BlockName':'https://your-site.com':'api_password'\n" +
                "# Example:\n" +
                "# 'GLBank':'https://gltest.pythonanywhere.com':'changeme_secret_key'\n";
        try {
            Files.createDirectories(path.getParent());
            Files.writeString(path, defaultContent);
            System.out.println("[SimpleATM] Created default config at: " + path);
        } catch (IOException e) {
            System.err.println("[SimpleATM] Failed to create default config: " + e.getMessage());
        }
    }
}

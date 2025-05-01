package adris.altoclef.player2api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ChatclefConfigPersistantState {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve("chatclef_config.json");
    private static ChatclefConfigPersistantState config = load();

    private boolean sttHintEnabled = true;

    public static boolean isSttHintEnabled() {
        return instance().sttHintEnabled;
    }

    public static void updateSttHint(boolean value) {
        System.out.println("[ChatclefConfigPersistantState]: updateSttHint called with: " + value);
        instance().sttHintEnabled = value;
        save();
    }

    private static ChatclefConfigPersistantState load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                System.out.println("[ChatclefConfigPersistantState]: Reading from file...");
                return GSON.fromJson(json, ChatclefConfigPersistantState.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("[ChatclefConfigPersistantState]: Could not load file, using default.");
        return new ChatclefConfigPersistantState();
    }

    private static void save() {
        System.out.println("[ChatclefConfigPersistantState]: save() called");
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
            System.out.println("[ChatclefConfigPersistantState]: Writing to file...");
        } catch (IOException e) {
            System.err.println("[ChatclefConfigPersistantState]: Writing to file FAILED");
            e.printStackTrace();
        }
    }


    private static ChatclefConfigPersistantState instance() {
        // if (config == null) {
        // config = load();
        // }
        return config;
    }
}

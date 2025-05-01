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

    private boolean sttHintEnabled = false;

    public static boolean isSttHintEnabled() {
        return instance().sttHintEnabled;
    }

    public static void updateSttHint(boolean value) {
        instance().sttHintEnabled = value;
        save();
    }

    private static ChatclefConfigPersistantState config = load();

    private static ChatclefConfigPersistantState load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                return GSON.fromJson(json, ChatclefConfigPersistantState.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ChatclefConfigPersistantState();
    }

    private static void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(config));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static ChatclefConfigPersistantState instance() {
        return config;
    }
}

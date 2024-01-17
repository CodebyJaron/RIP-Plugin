package nl.bamischrijft.rip.config;

import nl.bamischrijft.rip.Main;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public enum Settings {

    DESPAWN_TIME("settings", "despawn", "after"),
    DESPAWN_TIME_UNIT("settings", "despawn", "unit"),
    NOTIFY_TIME("settings", "despawn", "notify-if-not-collected", "every"),
    NOTIFY_TIME_UNIT("settings", "despawn", "notify-if-not-collected", "unit"),
    PUBLIC_NOTIFY_TIME("settings", "despawn", "public-broadcast", "after"),
    PUBLIC_NOTIFY_TIME_UNIT("settings", "despawn", "public-broadcast", "unit"),

    DYNAMIC_SIZE("settings", "dynamic-size"),

    HOLOGRAM("settings", "hologram"),

    DEATHKEY_KEYITEM("settings", "death-key", "key-item"),
    DEATHKEY_ITEMS("settings", "death-key", "items"),
    DEATHKEY_PATTERN("settings", "death-key", "pattern");

    private static final char YAML_SEPERATOR = '.';

    private final String path;

    Settings(String... path) {
        this.path = StringUtils.join(path, YAML_SEPERATOR);
    }

    @SuppressWarnings("all")
    public <T> T getValue(Class<T> clazz) {
        return (T) Main.getInstance().getConfig().get(this.path);
    }

    @SuppressWarnings("all")
    public <T> List<T> getValueList(Class<T> clazz) {
        return (List<T>) Main.getInstance().getConfig().getList(this.path);
    }

    @SuppressWarnings("all")
    public <V, T> HashMap<String, T> getValueMap(Class<V> valueClass, Class<T> targetClass,
                                            Function<V, T> function) {
        Object obj = Main.getInstance().getConfig().get(this.path);
        if (!(obj instanceof ConfigurationSection)) return null;

        ConfigurationSection section = (ConfigurationSection) obj;
        HashMap<String, T> hashMap = new HashMap<>();
        for (String key : section.getKeys(false)) {
            hashMap.put(key, function.apply((V) section.get(key)));
        }

        return hashMap;
    }

    @SuppressWarnings("all")
    public <T, R> R getValue(Class<T> clazz, Class<R> targetClass, Function<T, R> function) {
        return function.apply(getValue(clazz));
    }

}

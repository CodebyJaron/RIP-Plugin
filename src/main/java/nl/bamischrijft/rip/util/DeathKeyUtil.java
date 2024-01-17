package nl.bamischrijft.rip.util;

import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.item.ItemStackReader;
import nl.bamischrijft.rip.reader.ItemKeyReader;
import nl.bamischrijft.rip.config.Settings;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

public class DeathKeyUtil {

    public static boolean isDeathKey(ItemStack itemStack) {
        return ItemStackBuilder.of(itemStack.clone())
                .amount(1).build().equals(getDeathKey());
    }

    public static ItemStack getDeathKey() {
        return Settings.DEATHKEY_KEYITEM.getValue(ConfigurationSection.class,
                ItemStack.class, s -> new ItemKeyReader().read(s, ItemStackReader.VariableReplacer.NOOP).build());
    }

}

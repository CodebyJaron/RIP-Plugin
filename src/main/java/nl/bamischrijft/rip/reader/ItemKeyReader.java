package nl.bamischrijft.rip.reader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.lucko.helper.item.ItemStackBuilder;
import me.lucko.helper.item.ItemStackReader;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ItemKeyReader extends ItemStackReader {

    @Override
    public ItemStackBuilder read(ConfigurationSection config, VariableReplacer variableReplacer) {
        return ItemStackBuilder.of(parseMaterial(config))
                .apply(isb -> {
                    parseData(config).ifPresent(isb::data);
                    parseName(config).map(variableReplacer::replace).ifPresent(isb::name);
                    parseLore(config).map(variableReplacer::replace).ifPresent(isb::lore);
                    parseEnchantments(config).ifPresent(list -> list.forEach(ed -> {
                        isb.enchant(ed.getEnchantment(), ed.getLevel());
                    }));
                });
    }

    protected Optional<List<EnchantmentData>> parseEnchantments(ConfigurationSection config) {
        if (config.contains("enchantments")) {
            List<EnchantmentData> enchantments;
            try {
                enchantments = config.getStringList("enchantments")
                        .stream().map(String::toLowerCase).map(string -> {
                            if (string.split(":").length == 1) {
                                return new EnchantmentData(Enchantment.getByKey(NamespacedKey.fromString(string)), 1);
                            }
                            return new EnchantmentData(
                                    Enchantment.getByKey(NamespacedKey.fromString(string.split(":")[0])),
                                    Integer.parseInt(string.split(":")[1]));
                        }).collect(Collectors.toList());
            } catch (Exception exception) {
                return Optional.empty();
            }

            return Optional.of(enchantments);
        }

        return Optional.empty();
    }

    @RequiredArgsConstructor
    @Getter
    protected static class EnchantmentData {

        private final Enchantment enchantment;
        private final int level;

    }

}

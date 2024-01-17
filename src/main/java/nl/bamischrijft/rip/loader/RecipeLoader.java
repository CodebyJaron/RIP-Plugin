package nl.bamischrijft.rip.loader;

import lombok.RequiredArgsConstructor;
import nl.bamischrijft.rip.config.Settings;
import nl.bamischrijft.rip.Main;
import nl.bamischrijft.rip.util.DeathKeyUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@RequiredArgsConstructor
public class RecipeLoader {

    private final Main plugin;

    public void registerRecipes() {
        ItemStack itemStack = DeathKeyUtil.getDeathKey();
        HashMap<String, Material> items = Settings.DEATHKEY_ITEMS.getValueMap(String.class, Material.class,
                Material::valueOf);
        List<String> pattern = Settings.DEATHKEY_PATTERN.getValueList(String.class);

        NamespacedKey key = new NamespacedKey(this.plugin, "deathkey");
        ShapedRecipe recipe = new ShapedRecipe(key, itemStack);
        recipe.shape(pattern.toArray(new String[0]));

        if (items == null) {
            plugin.getLogger().warning("Check the config for errors because recipe couldn't be loaded.");
            return;
        }

        items.forEach((letter, material) -> recipe.setIngredient(letter.charAt(0), material));
        recipe.setIngredient('X', Material.AIR);

        Bukkit.addRecipe(recipe);
        plugin.getLogger().info("Succesfully registered the Death Key recipe.");
    }

}

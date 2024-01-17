package nl.bamischrijft.rip.util;

import org.bukkit.block.Block;
import org.bukkit.block.data.type.Chest;

import java.util.function.Consumer;

public class ChestUtil {

    public static void editChestData(Block block, Consumer<Chest> consumer) {
        if (!(block.getBlockData() instanceof Chest)) return;

        Chest data = (Chest) block.getBlockData();
        consumer.accept(data);
        block.setBlockData(data, false);
    }

    public static void editChest(Block block, Consumer<org.bukkit.block.Chest> consumer) {
        if (!(block.getState() instanceof org.bukkit.block.Chest)) return;

        org.bukkit.block.Chest chest = (org.bukkit.block.Chest) block.getState();
        consumer.accept(chest);
        chest.update();
    }

}

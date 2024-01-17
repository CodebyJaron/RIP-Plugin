package nl.bamischrijft.rip.objects;

import lombok.Builder;
import lombok.Getter;
import me.lucko.helper.Schedulers;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import nl.bamischrijft.rip.config.Settings;
import nl.bamischrijft.rip.util.ChestUtil;
import nl.bamischrijft.rip.util.TextUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Builder @Getter
public class DeathChest {

    private final CompositeTerminable consumer;

    private final Player player;
    private final Location deathLocation;
    private final List<ItemStack> drops;
    private final long expireAt;

    protected HashMap<Location, Material> validChestLocations;
    protected Inventory deathChestInventory;

    public void spawn() {
        this.validChestLocations = new HashMap<>();

        final Block block = deathLocation.getBlock();

        if (!block.getType().equals(Material.AIR)) {
            this.validChestLocations.put(block.getLocation(), block.getType());
        } else {
            this.validChestLocations.put(block.getLocation(), null);
        }

        block.setType(Material.CHEST);
        ChestUtil.editChest(block, chest -> chest.setCustomName(TextUtil.format("&c{0}'s DeathChest",
                player.getName())));

        boolean doubleChest = drops.size() > 27;
        if (doubleChest && Settings.DYNAMIC_SIZE.getValue(Boolean.class)) {
            Block neighbour = block.getRelative(BlockFace.EAST);

            if (!neighbour.getType().equals(Material.AIR)) {
                this.validChestLocations.put(neighbour.getLocation(), neighbour.getType());
            } else {
                this.validChestLocations.put(neighbour.getLocation(), null);
            }

            neighbour.setType(Material.CHEST);

            ChestUtil.editChestData(block, chest -> chest.setType(Chest.Type.LEFT));
            ChestUtil.editChestData(neighbour, chest -> chest.setType(Chest.Type.RIGHT));

            this.spawnHolograms(block, neighbour);
        } else {
            this.spawnHolograms(block, null);
        }

        this.deathChestInventory = ((org.bukkit.block.Chest) block.getState()).getInventory();
        int i = 0;
        for (ItemStack itemStack : getDrops()) {
            if (this.deathChestInventory.firstEmpty() == -1) {
                if (itemStack == null) continue;
                Objects.requireNonNull(getDeathLocation().getWorld()).dropItemNaturally(getDeathLocation(),
                        itemStack);
            } else {
                if (itemStack == null) {
                    i++;
                    continue;
                }
                this.deathChestInventory.setItem(i, itemStack);
                i++;
            }
        }
        block.getState().update(true);

        this.startSchedulers();
    }

    private void startSchedulers() {
        // This scheduler is for despawning the deathchest after expiry
        Schedulers.builder().sync().after(expireAt - System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                .run(this::despawn).bindWith(consumer);

        // This scheduler is for notify the player
        int length = Settings.NOTIFY_TIME.getValue(Integer.class);
        TimeUnit unit = Settings.NOTIFY_TIME_UNIT.getValue(String.class, TimeUnit.class, TimeUnit::valueOf);
        Schedulers.builder().sync().afterAndEvery(length, unit)
                .run(this::notifyPlayer).bindWith(consumer);

        // This scheduler is for notify everyone
        int lengthPublic = Settings.PUBLIC_NOTIFY_TIME.getValue(Integer.class);
        TimeUnit unitPublic = Settings.PUBLIC_NOTIFY_TIME_UNIT.getValue(String.class, TimeUnit.class, TimeUnit::valueOf);
        Schedulers.builder().sync().after(lengthPublic, unitPublic)
                .run(this::notifyEveryone).bindWith(consumer);
    }

    private void notifyEveryone() {
        Bukkit.getOnlinePlayers().forEach(user -> {
            if (user == player) return;

            user.sendMessage(
                    TextUtil.format("&a{0}'s DeathChest is not looted yet!", player.getName()),
                    TextUtil.format("&aFind it at X: {0} Y: {1} Z: {2}",
                            deathLocation.getBlockX(), deathLocation.getBlockY(),
                            deathLocation.getBlockZ())
            );
        });
    }

    private void notifyPlayer() {
        player.sendMessage(
                TextUtil.format("&aYou've not collected your inventory from the DeathChest yet!"),
                TextUtil.format("&aX: {0} Y: {1} Z: {2}",
                        deathLocation.getBlockX(), deathLocation.getBlockY(), deathLocation.getBlockZ())
        );
    }

    private List<ArmorStand> holograms;

    private void spawnHolograms(Block block1, Block block2) {
        this.holograms = new ArrayList<>();

        Supplier<HashMap<String, String>> placeholderSupplier = () -> {
            HashMap<String, String> placeholders = new HashMap<>();
            placeholders.put("victim", player.getName());
            placeholders.put("timeleft", String.valueOf((int)((getExpireAt() - System.currentTimeMillis()) / 1000)));
            return placeholders;
        };

        List<String> lines = Settings.HOLOGRAM.getValueList(String.class)
                .stream().map(TextUtil::format).collect(Collectors.toList());
        Location startLocation = block2 == null && block1 != null ? block1.getLocation().clone().add(.5d, 0.7, .5d)
                : block2 != null && block1 != null ? block1.getLocation().clone().add(1d, 0.7, .5d) : null;
        if (startLocation == null) return;
        for (int i = lines.size(); i > 0; i--) {
            String line = lines.get(i - 1);

            ArmorStand armorStand = Objects.requireNonNull(deathLocation.getWorld()).spawn(
                    startLocation.add(0, 0.25d, 0), ArmorStand.class);

            Schedulers.builder().sync().every(1, TimeUnit.SECONDS)
                    .run(() -> armorStand.setCustomName(TextUtil.formatMap(line, placeholderSupplier.get())))
                    .bindWith(consumer);
            armorStand.setCustomNameVisible(true);

            armorStand.setGravity(false);
            armorStand.setMarker(true);

            armorStand.setVisible(false);

            holograms.add(armorStand);
        }
    }

    private void despawnHolograms() {
        holograms.forEach(ArmorStand::remove);
    }

    public void despawn() {
        for (Location location : validChestLocations.keySet()) {
            Material material = validChestLocations.get(location);
            location.getBlock().setType(material == null ? Material.AIR : material);
            location.getBlock().getWorld().spawnParticle(Particle.CLOUD, location.clone().add(.5d, 0, .5d), 50);
        }

        validChestLocations.keySet().stream().findFirst()
                .ifPresent(location -> getDeathChestInventory().forEach(item ->
                {
                    if (item == null) return;
                    Objects.requireNonNull(location.getWorld()).dropItemNaturally(location, item);
                }
        ));

        this.despawnHolograms();
        this.consumer.closeAndReportException();
    }

}

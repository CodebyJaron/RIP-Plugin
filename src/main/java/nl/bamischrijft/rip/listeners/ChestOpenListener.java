package nl.bamischrijft.rip.listeners;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import nl.bamischrijft.rip.Main;
import nl.bamischrijft.rip.objects.DeathChest;
import nl.bamischrijft.rip.util.DeathKeyUtil;
import nl.bamischrijft.rip.util.TextUtil;
import org.bukkit.Material;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

import javax.annotation.Nonnull;
import java.util.Objects;

@RequiredArgsConstructor
public class ChestOpenListener implements TerminableModule {

    private final Main plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(PlayerInteractEvent.class)
                .filter(e -> Objects.equals(e.getHand(), EquipmentSlot.HAND))
                .filter(e -> e.getAction().equals(Action.RIGHT_CLICK_BLOCK))
                .filter(e -> Objects.equals(Objects.requireNonNull(e.getClickedBlock()).getType(), Material.CHEST))
                .filter(e -> plugin.getDeathManager().getDeathChests().stream()
                        .anyMatch(deathChest -> deathChest.getValidChestLocations()
                                .containsKey(Objects.requireNonNull(e.getClickedBlock()).getLocation())))
                .handler(e -> {
                    DeathChest chest = plugin.getDeathManager().getDeathChests().stream()
                            .filter(deathChest -> deathChest.getValidChestLocations()
                                    .containsKey(Objects.requireNonNull(e.getClickedBlock()).getLocation()))
                            .findFirst().orElse(null);
                    assert chest != null;

                    if (chest.getPlayer() == e.getPlayer()) return;
                    if (DeathKeyUtil.isDeathKey(e.getPlayer().getInventory().getItemInMainHand())) {
                        e.getPlayer().getInventory().getItemInMainHand()
                                .setAmount(e.getPlayer().getInventory().getItemInMainHand()
                                        .getAmount() - 1);
                        e.getPlayer().sendMessage(TextUtil.format("&aYou've used your Death Key to open {0}'s DeathChest.",
                                chest.getPlayer().getName()));
                        return;
                    }

                    e.setCancelled(true);
                    e.getPlayer().sendMessage(TextUtil.format("&cThis is &4{0}'s &cDeath Chest you need a &4Death Key &cto open another player's Death Chest.",
                            chest.getPlayer().getName()));
                }).bindWith(consumer);
    }

}

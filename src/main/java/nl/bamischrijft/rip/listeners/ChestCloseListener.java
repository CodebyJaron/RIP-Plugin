package nl.bamischrijft.rip.listeners;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.module.TerminableModule;
import nl.bamischrijft.rip.Main;
import nl.bamischrijft.rip.objects.DeathChest;
import org.bukkit.event.inventory.InventoryCloseEvent;

import javax.annotation.Nonnull;

@RequiredArgsConstructor
public class ChestCloseListener implements TerminableModule {

    private final Main plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        Events.subscribe(InventoryCloseEvent.class)
                .filter(e -> plugin.getDeathManager().getDeathChests()
                        .stream().anyMatch(deathChest -> deathChest.getDeathChestInventory()
                                .equals(e.getInventory())))
                .handler(e -> {
                    DeathChest chest = plugin.getDeathManager().getDeathChests().stream()
                            .filter(deathChest -> deathChest.getDeathChestInventory()
                                    .equals(e.getInventory())).findFirst().orElse(null);
                    assert chest != null;
                    chest.despawn();
                    plugin.getDeathManager().getDeathChests().remove(chest);
                });
    }

}

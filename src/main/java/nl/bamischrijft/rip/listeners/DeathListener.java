package nl.bamischrijft.rip.listeners;

import lombok.RequiredArgsConstructor;
import me.lucko.helper.Events;
import me.lucko.helper.terminable.TerminableConsumer;
import me.lucko.helper.terminable.composite.CompositeTerminable;
import me.lucko.helper.terminable.module.TerminableModule;
import nl.bamischrijft.rip.Main;
import nl.bamischrijft.rip.config.Settings;
import nl.bamischrijft.rip.objects.DeathChest;
import nl.bamischrijft.rip.util.*;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DeathListener implements TerminableModule {

    private final Main plugin;

    @Override
    public void setup(@Nonnull TerminableConsumer consumer) {
        int length = Settings.DESPAWN_TIME.getValue(Integer.class);
        TimeUnit timeUnit = Settings.DESPAWN_TIME_UNIT.getValue(String.class, TimeUnit.class, TimeUnit::valueOf);
        long millis = timeUnit.toMillis(length);

        System.out.println(millis);

        Events.subscribe(PlayerDeathEvent.class)
                // make sure that keep inventory is off
                // to prevent dupe
                .filter(e -> !e.getKeepInventory())
                .handler(e -> {
                    Player deathPlayer = e.getEntity();

                    e.setKeepInventory(true);
                    e.getDrops().clear();

                    List<ItemStack> drops = Arrays.stream(e.getEntity().getInventory().getContents()).filter(Objects::nonNull)
                                    .collect(Collectors.toList());
                    e.getEntity().getInventory().clear();

                    DeathChest deathChest = DeathChestUtil.createDeathChest()
                            .player(deathPlayer)
                            .deathLocation(deathPlayer.getLocation().clone())
                            .drops(drops)
                            .expireAt(System.currentTimeMillis() + millis)
                            .consumer(CompositeTerminable.create())
                            .build();
                    deathChest.spawn();

                    SQLiteUtil.update("INSERT INTO rip (uuid, location) VALUES (?, ?)",
                            deathPlayer.getUniqueId().toString(),
                            LocationUtil.locationToString(deathChest.getDeathLocation())
                    );

                    plugin.getDeathManager().getDeathChests().add(deathChest);

                    Function<Double, String> format = d -> NumberUtil.format(d, "0.0");

                    deathPlayer.sendMessage(TextUtil.format("&aYou've died!"),
                            TextUtil.format("&aA chest with your inventory contents was spawned at X: {0} Y: {1} Z: {2}",
                                    deathChest.getDeathLocation().getBlockX(),
                                    deathChest.getDeathLocation().getBlockY(),
                                    deathChest.getDeathLocation().getBlockZ()),
                            TextUtil.format("&aYour DeathChest will be despawned in {0} {1}.",
                                    length, timeUnit.toString().toLowerCase()));
                }).bindWith(consumer);
    }

}

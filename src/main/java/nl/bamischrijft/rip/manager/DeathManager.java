package nl.bamischrijft.rip.manager;

import lombok.Getter;
import nl.bamischrijft.rip.objects.DeathChest;

import java.util.ArrayList;
import java.util.List;

public class DeathManager {

    @Getter
    private final List<DeathChest> deathChests = new ArrayList<>();

}

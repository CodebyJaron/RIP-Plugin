package nl.bamischrijft.rip.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LocationUtil {
    public static Location locationFromString(String locationString) {
        int[] coordinates = Arrays.stream(locationString.split(",")).limit(3).mapToInt(Integer::parseInt).toArray();
        String worldName = Arrays.stream(locationString.split(",")).skip(3).collect(Collectors.joining(","));

        return new Location(Bukkit.getWorld(worldName), coordinates[0], coordinates[1], coordinates[2]);
    }

    public static String locationToString(Location location) {
        return String.format("%d,%d,%d,%s", location.getBlockX(), location.getBlockY(), location.getBlockZ(),
                location.getWorld().getName());
    }
}

package com.songoda.ultimatekits.kit.object;

import org.bukkit.Location;

import java.util.*;

public final class KitManager {

    private final Set<Kit> registeredKits = new HashSet<>();
    private final Map<Location, KitBlockData> kitsAtLocations = new HashMap<>();

    public boolean addKit(Kit kit) {
        if (kit == null) return false;
        return registeredKits.add(kit);
    }

    public void removeKit(Kit kit) {
        registeredKits.remove(kit);
        removeLocationsFromKit(kit);
    }

    public void removeLocationsFromKit(Kit kit) {
        for (Map.Entry<Location, KitBlockData> entry : kitsAtLocations.entrySet()) {
            if (entry.getValue().getKit() == kit) {
                entry.getValue().reset();
                kitsAtLocations.remove(entry.getKey());
            }
        }
    }

    public void addKitToLocation(Kit kit, Location location) {
        kitsAtLocations.put(roundLocation(location), new KitBlockData(kit, location));
    }

    public void addKitToLocation(Kit kit, Location location, boolean hologram, boolean particles, boolean items, boolean itemOverride) {
        kitsAtLocations.put(roundLocation(location), new KitBlockData(kit, location, hologram, particles, items, itemOverride));
    }

    public Kit removeKitFromLocation(Location location) {
        getKit(roundLocation(location)).reset();

        KitBlockData removed = kitsAtLocations.remove(roundLocation(location));
        return (removed != null ? removed.getKit() : null);
    }

    public Kit getKit(String name) {
        for (Kit kit : registeredKits)
            if (kit.getName().equalsIgnoreCase(name)) return kit;
        return null;
    }

    public KitBlockData getKit(Location location) {
        return kitsAtLocations.get(roundLocation(location));
    }

    public Set<Kit> getKits() {
        return Collections.unmodifiableSet(registeredKits);
    }

    public Map<Location, KitBlockData> getKitLocations() {
        return Collections.unmodifiableMap(kitsAtLocations);
    }

    public void clearKits() {
        this.registeredKits.clear();
        this.kitsAtLocations.clear();
    }

    private Location roundLocation(Location location) {
        location = location.clone();
        location.setX(location.getBlockX());
        location.setY(location.getBlockY());
        location.setZ(location.getBlockZ());
        return location;
    }
}

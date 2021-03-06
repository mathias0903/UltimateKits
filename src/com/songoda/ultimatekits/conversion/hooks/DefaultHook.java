package com.songoda.ultimatekits.conversion.hooks;

import com.songoda.ultimatekits.conversion.Hook;
import com.songoda.ultimatekits.utils.Methods;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultHook implements Hook {

    public enum Kits {
        TOOLS(10, "STONE_PICKAXE 1", "STONE_AXE 1", "STONE_SPADE 1", "STONE_HOE 1"),
        BETTER_TOOLS(300, "DIAMOND_PICKAXE 1 DIG_SPEED:5 DURABILITY:2", "DIAMOND_AXE 1 DIG_SPEED:2 DURABILITY:2", "DIAMOND_SPADE 1 DIG_SPEED:1", "DIAMOND_HOE 1 DURABILITY:3"),
        BRIANNA(0, "SKULL_ITEM:3 1 player:Songoda");

        public String[] items;
        public int delay;

        Kits(int delay, String... items) {
            this.items = items;
            this.delay = delay;
        }
    }

    public Set<ItemStack> getItems(String kitName) {
        Set<ItemStack> items = new HashSet<>();

        for (Kits kit : Kits.values()) {
            if (!kit.name().equalsIgnoreCase(kitName)) continue;
            for (String string : kit.items) {
                items.add(Methods.deserializeItemStack(string));
            }
        }

        return items;
    }

    public Set<String> getKits() {
        Set<String> kits = new HashSet<>();

        for (Kits kit : Kits.values()) {
            kits.add(kit.name().toLowerCase());
        }

        return kits;
    }

    public long getDelay(String kitName) {
        for (Kits kit : Kits.values()) {
            if (!kit.name().equalsIgnoreCase(kitName)) continue;
            return kit.delay;
        }
        return 0;
    }
}

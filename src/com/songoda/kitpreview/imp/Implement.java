package com.songoda.kitpreview.imp;

import com.songoda.kitpreview.KitPreview;
import com.songoda.kitpreview.imp.hooks.EssentialsHook;
import com.songoda.kitpreview.imp.hooks.UltimateCoreHook;
import com.songoda.kitpreview.utils.Methods;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Implement {

    private final KitPreview instance;

    private Hook hook;

    public Implement(KitPreview instance) {
        this.instance = instance;
        if (instance.getServer().getPluginManager().getPlugin("Essentials") != null) {
            hook = new EssentialsHook();
        } else if (instance.getServer().getPluginManager().getPlugin("UltimateCore") != null) {
            hook = new UltimateCoreHook();
        }
        convertKits();
    }


    public void convertKits() {
        List<String> kits = hook.getKits();

        if (instance.getKitFile().getConfig().contains("Kits")) return;

        for (String kit : kits) {
            List<String> serializedItems = new ArrayList<>();
            for (ItemStack item : hook.getItems(kit)) {
                serializedItems.add(Methods.serializeItemStack(item));
            }
            instance.getKitFile().getConfig().set("Kits." + kit + ".items", serializedItems);
        }
        instance.getKitFile().saveConfig();
    }

    public Hook getHook() {
        return hook;
    }
}

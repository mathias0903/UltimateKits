package com.songoda.kitpreview.imp.hooks;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.Kit;
import com.songoda.kitpreview.KitPreview;
import com.songoda.kitpreview.imp.Hook;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class EssentialsHook extends Hook {

    private Essentials essentials;

    public EssentialsHook() {
        super();
        essentials = (Essentials) KitPreview.getInstance().getServer().getPluginManager().getPlugin("Essentials");
    }

    public List<ItemStack> getItems(String kitName) {
        List<ItemStack> stacks = new ArrayList();
        try {
            Kit kit = new Kit(kitName, essentials);

            for (String nonParse : kit.getItems()) {
                String[] parts = nonParse.split(" +");
                stacks.add(essentials.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1));
            }
        } catch (Exception e) {
            System.out.println(e);
        }
        return stacks;
    }

    public List<String> getKits() {
        ConfigurationSection cs = essentials.getSettings().getKits();
        List<String> kits = new ArrayList<>();
        for (String kitItem : cs.getKeys(false)) {
            kits.add(kitItem);
        }
        return kits;
    }

}

package com.songoda.kitpreview.imp;

import org.bukkit.inventory.ItemStack;

import java.util.List;

public abstract class Hook {

    protected Hook() {}

    public abstract List<String> getKits();

    public abstract List<ItemStack> getItems(String kit);

}
package com.songoda.kitpreview.hooks;

import com.earth2me.essentials.*;
import com.earth2me.essentials.craftbukkit.InventoryWorkaround;
import com.earth2me.essentials.textreader.KeywordReplacer;
import com.earth2me.essentials.textreader.SimpleTextInput;
import com.songoda.kitpreview.KitPreview;
import com.songoda.kitpreview.Lang;
import com.songoda.kitpreview.utils.Debugger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;

/**
 * Created by songoda on 3/16/2017.
 */
public class EssentialsHook implements Hooks {

    private Essentials essentials = (Essentials) KitPreview.getInstance().getServer().getPluginManager().getPlugin("Essentials");

    @Override
    public List<String> getKits() {
        ConfigurationSection cs = essentials.getSettings().getKits();
        List<String> list = new ArrayList<>();
        try {
            for (String kitItem : cs.getKeys(false)) {
                list.add(kitItem);
            }
        } catch (Exception e) {
            if (Debugger.isDebug())
                Debugger.runReport(e);
        }
        return list;
    }

    @Override
    public long getNextUse(String kitName, Player player) {
        try {
            Kit kit = new Kit(kitName, essentials);
            return kit.getNextUse(essentials.getUser(player));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return 0;
    }

    @Override
    public List<ItemStack> getItems(Player player, String kitName, boolean commands) {
        Kit kit;
        List<String> items;
        List<ItemStack> stacks = new ArrayList();
        try {
            kit = new Kit(kitName, essentials);
            items = kit.getItems(essentials.getUser(player));
            for (String str : items) {
                if (!str.startsWith("/") || commands) {
                    String[] parts = str.split(" +");
                    ItemStack parseStack;
                    if (str.startsWith("/")) {
                        parseStack = new ItemStack(Material.PAPER, 1);
                        ItemMeta meta = parseStack.getItemMeta();

                        ArrayList<String> lore = new ArrayList<>();

                        int index = 0;
                        while (index < str.length()) {
                            lore.add("§a" + str.substring(index, Math.min(index + 30, str.length())));
                            index += 30;
                        }
                        meta.setLore(lore);
                        meta.setDisplayName(Lang.COMMAND.getConfigValue());
                        parseStack.setItemMeta(meta);
                    } else {
                        parseStack = essentials.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
                    }
                    MetaItemStack metaStack = new MetaItemStack(parseStack);
                    if (parts.length > 2 != str.startsWith("/")) {
                        try {
                            metaStack.parseStringMeta(null, true, parts, 2, essentials);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ItemStack fin = metaStack.getItemStack();
                    if (Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI") && metaStack.getItemStack().getItemMeta().getLore() != null) {
                        ArrayList<String> lore2 = new ArrayList<String>();
                        ItemMeta meta2 = metaStack.getItemStack().getItemMeta();
                        for (String lor : metaStack.getItemStack().getItemMeta().getLore()) {
                            lor = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, lor.replace(" ", "_")).replace("_", " ");
                            lore2.add(lor);
                        }
                        meta2.setLore(lore2);
                        fin.setItemMeta(meta2);
                    }
                    stacks.add(fin);
                }
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return stacks;
    }

    @Override
    public boolean canGiveKit(Player player) {
        try {
            if (player.hasPermission("essentials.kit.others"))
                return true;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public int kitSize(String kitName) {
        List<String> items = null;
        try {
            Kit kit = new Kit(kitName, essentials);
            items = kit.getItems(essentials.getUser(""));
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return items.size();
    }

    @Override
    public void createKit(Player player) {
        try {
            User user = new User(player, essentials);
            //long delay = Long.valueOf(args[1]);
            long delay = 0;
            String kitname = "test";
            ItemStack[] items = user.getBase().getInventory().getContents();
            List<String> list = new ArrayList<>();
            for (ItemStack is : items) {
                if (is != null && is.getType() != null && is.getType() != Material.AIR) {
                    String serialized = essentials.getItemDb().serialize(is);
                    list.add(serialized);
                }
            }
            essentials.getSettings().addKit(kitname, list, delay);
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public void saveKit(Player player, String kitName, ItemStack[] items) {
        try {
            List<String> list = new ArrayList<>();
            for (ItemStack is : items) {
                if (is != null && is.getType() != null && is.getType() != Material.AIR) {
                    if (is.getType() == Material.PAPER && ChatColor.stripColor(is.getItemMeta().getDisplayName()).equals("Command")) {
                        String command = "";
                        for (String line : is.getItemMeta().getLore()) {
                            command += line;
                        }
                        list.add(ChatColor.stripColor(command));
                    } else {
                        String serialized = essentials.getItemDb().serialize(is);
                        list.add(serialized);
                    }
                }
            }
            Map<String, Object> kitt = essentials.getSettings().getKit(kitName);

            String delay = "-1";
            try {
                delay = kitt.get("delay").toString();
            } catch (Exception e) {
            }

            ConfigurationSection test = essentials.getSettings().getKits();

            if (KitPreview.getInstance().hooks.doesKitExist(kitName))
                kitName = test.getConfigurationSection(kitName).getName();

            essentials.getSettings().addKit(kitName, list, Integer.parseInt(delay));
        } catch (Exception e) {
            player.sendMessage("You need EssentialsX to use this feature.");
            Debugger.runReport(e);
        }
    }

    @Override
    public boolean hasPermission(Player player, String kitName) {
        try {
            if (player.hasPermission("essentials.kits." + kitName.toLowerCase()))
                return true;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public void giveKit(Player player, String kitName) {
        try {
            Kit kit = new Kit(kitName, essentials);
            List<String> items = kit.getItems(essentials.getUser(player));
            givePartKit(player, kitName, items.size());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public void givePartKit(Player player, String kitName, int amt) {
        try {
            User user = new User(player, essentials);
            Kit kit = new Kit(kitName, essentials);
            List<String> items = kit.getItems(essentials.getUser(player));

            amt = items.size() - amt;

            while (amt != 0) {
                int num = ThreadLocalRandom.current().nextInt(0, items.size());
                items.remove(num);
                amt--;
            }
            try {
                SimpleTextInput e = new SimpleTextInput(items);
                KeywordReplacer output = new KeywordReplacer(e, user.getSource(), essentials);
                boolean spew = false;
                boolean allowUnsafe = essentials.getSettings().allowUnsafeEnchantments();
                Iterator var7 = output.getLines().iterator();

                while (var7.hasNext()) {
                    String kitItem = (String) var7.next();
                    if (kitItem.startsWith(essentials.getSettings().getCurrencySymbol())) {
                        BigDecimal parts2 = new BigDecimal(kitItem.substring(essentials.getSettings().getCurrencySymbol().length()).trim());
                        Trade parseStack2 = new Trade(parts2, essentials);
                        parseStack2.pay(user, Trade.OverflowType.DROP);
                    } else if (kitItem.startsWith("/")) {
                        String parts1 = kitItem.substring(1);
                        String parseStack1 = user.getName();
                        parts1 = parts1.replace("{player}", parseStack1);
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), parts1);
                        continue;
                    }

                    String[] parts = kitItem.split(" +");
                    ItemStack parseStack = essentials.getItemDb().get(parts[0], parts.length > 1 ? Integer.parseInt(parts[1]) : 1);
                    if (parseStack.getType() == Material.AIR) {
                        continue;
                    }
                    MetaItemStack metaStack = new MetaItemStack(parseStack);
                    if (parts.length > 2) {
                        metaStack.parseStringMeta((CommandSource) null, allowUnsafe, parts, 2, this.essentials);
                    }

                    boolean allowOversizedStacks = user.isAuthorized("essentials.oversizedstacks");
                    Map overfilled;
                    if (allowOversizedStacks) {
                        overfilled = InventoryWorkaround.addOversizedItems(user.getBase().getInventory(), essentials.getSettings().getOversizedStackSize(), new ItemStack[]{metaStack.getItemStack()});
                    } else {
                        overfilled = InventoryWorkaround.addItems(user.getBase().getInventory(), new ItemStack[]{metaStack.getItemStack()});
                    }

                    for (Iterator var14 = overfilled.values().iterator(); var14.hasNext(); spew = true) {
                        ItemStack itemStack = (ItemStack) var14.next();
                        int spillAmount = itemStack.getAmount();
                        if (!allowOversizedStacks) {
                            itemStack.setAmount(spillAmount < itemStack.getMaxStackSize() ? spillAmount : itemStack.getMaxStackSize());
                        }

                        while (spillAmount > 0) {
                            user.getWorld().dropItemNaturally(user.getLocation(), itemStack);
                            spillAmount -= itemStack.getAmount();
                        }
                    }
                }

                user.getBase().updateInventory();
                if (spew) {
                    user.sendMessage(I18n.tl("kitInvFull", new Object[0]));
                }
            } catch (Exception var17) {
                user.getBase().updateInventory();
                essentials.getLogger().log(Level.WARNING, var17.getMessage());
                throw new Exception(I18n.tl("kitError2", new Object[0]), var17);
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }


    @Override
    public void updateDelay(Player player, String kitName) {
        try {
            essentials.getUser(player).setKitTimestamp(kitName, System.currentTimeMillis());
        } catch (Exception e) {
            Debugger.runReport(e);
        }
    }

    @Override
    public boolean isReady(Player player, String kitName) {
        try {
            Kit kit = new Kit(kitName, essentials);
            User user = new User(player, essentials);

            long next = kit.getNextUse(user);

            if (next == 0L) {
                return true;
            }
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    @Override
    public boolean doesKitExist(String kit) {
        try {
            if (essentials.getSettings().getKit(kit) != null)
                return true;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }
}

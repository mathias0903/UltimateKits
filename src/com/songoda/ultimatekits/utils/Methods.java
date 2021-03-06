package com.songoda.ultimatekits.utils;

import com.songoda.arconix.Arconix;
import com.songoda.arconix.method.formatting.TextComponent;
import com.songoda.ultimatekits.UltimateKits;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.*;
import org.bukkit.block.Banner;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Created by songoda on 2/24/2017.
 */
public class Methods {


    public static ItemStack getGlass() {
        UltimateKits plugin = UltimateKits.getInstance();
        return Arconix.pl().getGUI().getGlass(plugin.getConfig().getBoolean("Interfaces.Replace Glass Type 1 With Rainbow Glass"), plugin.getConfig().getInt("Interfaces.Glass Type 1"));
    }

    public static ItemStack getBackgroundGlass(boolean type) {
        UltimateKits plugin = UltimateKits.getInstance();
        if (type)
            return Arconix.pl().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 2"));
        else
            return Arconix.pl().getGUI().getGlass(false, plugin.getConfig().getInt("Interfaces.Glass Type 3"));
    }

    public static void fillGlass(Inventory i) {
        int nu = 0;
        while (nu != 27) {
            ItemStack glass = getGlass();
            i.setItem(nu, glass);
            nu++;
        }
    }

    public static boolean canGiveKit(Player player) {
        try {
            if (player.hasPermission("ultimatekits.cangive")) return true;

            if (player.hasPermission("essentials.kit.others")) return true;
        } catch (Exception e) {
            Debugger.runReport(e);
        }
        return false;
    }

    public static boolean doesKitExist(String kit) {
        return UltimateKits.getInstance().getKitFile().getConfig().contains("Kits." + kit);
    }

    public static Collection<Entity> getNearbyEntities(Location location, double x, double y, double z) {
        if (!UltimateKits.getInstance().v1_7) return location.getWorld().getNearbyEntities(location, x, y, z);

        if (location == null) return Collections.emptyList();

        World world = location.getWorld();
        net.minecraft.server.v1_7_R4.AxisAlignedBB aabb = net.minecraft.server.v1_7_R4.AxisAlignedBB
                .a(location.getX() - x, location.getY() - y, location.getZ() - z, location.getX() + x, location.getY() + y, location.getZ() + z);
        List<net.minecraft.server.v1_7_R4.Entity> entityList = ((org.bukkit.craftbukkit.v1_7_R4.CraftWorld) world).getHandle().getEntities(null, aabb, null);
        List<Entity> bukkitEntityList = new ArrayList<>();

        for (Object entity : entityList) {
            bukkitEntityList.add(((net.minecraft.server.v1_7_R4.Entity) entity).getBukkitEntity());
        }

        return bukkitEntityList;
    }

    public static boolean pay(Player p, double amount) {
        if (UltimateKits.getInstance().getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = UltimateKits.getInstance().getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);

        net.milkbowl.vault.economy.Economy econ = rsp.getProvider();

        econ.depositPlayer(p, amount);
        return true;
    }

    public static String serializeItemStack(ItemStack item) {
        String str = item.getType().name();
        if (item.getDurability() != 0)
            str += ":" + item.getDurability() + " ";
        else
            str += " ";

        str += item.getAmount() + " ";

        if (item.hasItemMeta()) {
            ItemMeta meta = item.getItemMeta();
            if (meta.hasDisplayName())
                str += "title:" + fixLine(meta.getDisplayName()) + " ";
            if (meta.hasLore()) {
                str += "lore:";
                int num = 0;
                for (String line : meta.getLore()) {
                    num++;
                    str += fixLine(line);
                    if (meta.getLore().size() != num)
                        str += "|";
                }
                str += " ";
            }

            for (Enchantment ench : item.getEnchantments().keySet()) {
                str += ench.getName() + ":" + item.getEnchantmentLevel(ench) + " ";
            }

            Set<ItemFlag> flags = meta.getItemFlags();
            if (flags != null && !flags.isEmpty()) {
                str += "itemflags:";
                boolean first = true;
                for (ItemFlag flag : flags) {
                    if (!first) {
                        str += ",";
                    }
                    str += flag.name();
                    first = false;
                }
            }
        }

        switch (item.getType()) {
            case WRITTEN_BOOK:
                BookMeta bookMeta = (BookMeta) item.getItemMeta();
                if (bookMeta.hasTitle()) {
                    str += "title:" + bookMeta.getTitle().replace(" ", "_") + " ";
                }
                if (bookMeta.hasAuthor()) {
                    str += "author:" + bookMeta.getAuthor() + " ";
                }
                if (bookMeta.hasPages()) {
                    String title = bookMeta.getAuthor();
                    int num = 0;
                    while (UltimateKits.getInstance().getDataFile().getConfig().contains("Books.pages." + title)) {
                        title += num;
                    }
                    str += "id:" + bookMeta.getAuthor() + " ";
                    int pNum = 0;
                    for (String page : bookMeta.getPages()) {
                        pNum++;
                        UltimateKits.getInstance().getDataFile().getConfig().set("Books.pages." + title + "." + pNum, page);
                    }
                }
                break;
            case ENCHANTED_BOOK:
                EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) item.getItemMeta();
                for (Enchantment e : enchantmentStorageMeta.getStoredEnchants().keySet()) {
                    str += e.getName().toLowerCase() + ":" + enchantmentStorageMeta.getStoredEnchantLevel(e) + " ";
                }
                break;
            case FIREWORK:
                FireworkMeta fireworkMeta = (FireworkMeta) item.getItemMeta();
                if (fireworkMeta.hasEffects()) {
                    for (FireworkEffect effect : fireworkMeta.getEffects()) {
                        if (effect.getColors() != null && !effect.getColors().isEmpty()) {
                            str += "color:";
                            boolean first = true;
                            for (Color c : effect.getColors()) {
                                if (!first) {
                                    str += ",";
                                }
                                str += c.asRGB();
                                first = false;
                            }
                            str += " ";
                        }

                        str += "shape: " + effect.getType().name() + " ";
                        if (effect.getFadeColors() != null && !effect.getFadeColors().isEmpty()) {
                            str += "fade:";
                            boolean first = true;
                            for (Color c : effect.getFadeColors()) {
                                if (!first) {
                                    str += ",";
                                }
                                str += c.asRGB();
                                first = false;
                            }
                            str += " ";
                        }
                    }
                    str += "power: " + fireworkMeta.getPower() + " ";
                }
                break;
            case POTION:
                PotionMeta potion = ((PotionMeta) item.getItemMeta());
                if (!UltimateKits.getInstance().v1_8 && !UltimateKits.getInstance().v1_7) {
                    if (potion.hasColor()) {
                        str += "color:" + potion.getColor().asRGB() + " ";
                    }
                    if (potion.getBasePotionData() != null
                            && potion.getBasePotionData().getType() != null
                            && potion.getBasePotionData().getType().getEffectType() != null) {
                        PotionEffectType e = potion.getBasePotionData().getType().getEffectType();
                        str += "effect:" + e.getName().toLowerCase() + " " + "duration:" + e.getDurationModifier() + " ";
                    }
                }
                break;
            case SKULL_ITEM:
                SkullMeta skullMeta = (SkullMeta) item.getItemMeta();
                if (skullMeta != null && skullMeta.hasOwner()) {
                    str += "player:" + skullMeta.getOwner() + " ";
                }
                break;
            case LEATHER_HELMET:
            case LEATHER_CHESTPLATE:
            case LEATHER_LEGGINGS:
            case LEATHER_BOOTS:
                LeatherArmorMeta leatherArmorMeta = (LeatherArmorMeta) item.getItemMeta();
                int rgb = leatherArmorMeta.getColor().asRGB();
                str += "color:" + rgb + " ";
                break;
            case BANNER:
                BannerMeta bannerMeta = (BannerMeta) item.getItemMeta();
                if (bannerMeta != null) {
                    int basecolor = bannerMeta.getBaseColor().getColor().asRGB();
                    str += "basecolor:" + basecolor + " ";
                    for (org.bukkit.block.banner.Pattern p : bannerMeta.getPatterns()) {
                        String type = p.getPattern().getIdentifier();
                        int color = p.getColor().getColor().asRGB();
                        str += type + "," + color + " ";
                    }
                }
                break;
            case SHIELD:
                BlockStateMeta shieldMeta = (BlockStateMeta) item.getItemMeta();
                Banner shieldBannerMeta = (Banner) shieldMeta.getBlockState();
                int basecolor = shieldBannerMeta.getBaseColor().getColor().asRGB();
                str += "basecolor:" + basecolor + " ";
                for (org.bukkit.block.banner.Pattern p : shieldBannerMeta.getPatterns()) {
                    String type = p.getPattern().getIdentifier();
                    int color = p.getColor().getColor().asRGB();
                    str += type + "," + color + " ";
                }
                break;
        }
        return str.replace("§", "&").trim();
    }

    public static ItemStack deserializeItemStack(String string) {
        string = string.replace("&", "§");
        String[] splited = string.split("\\s+");

        String[] val = splited[0].split(":");
        ItemStack item;
        if (Arconix.pl().doMath().isNumeric(val[0])) {
            item = new ItemStack(Integer.parseInt(val[0]));
        } else {
            item = new ItemStack(Material.valueOf(val[0]));
        }

        if (item.getType() == Material.SKULL_ITEM) {
            item = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        }

        ItemMeta meta = item.getItemMeta();

        if (val.length == 2) {
            item.setDurability(Short.parseShort(val[1]));
        }
        if (splited.length >= 2) {
            if (Arconix.pl().doMath().isNumeric(splited[1])) {
                item.setAmount(Integer.parseInt(splited[1]));
            }

            for (String st : splited) {
                String str = unfixLine(st);
                if (!str.contains(":")) continue;
                String[] ops = str.split(":", 2);

                String option = ops[0];
                String value = ops[1];

                if (Enchantment.getByName(option.replace(" ", "_").toUpperCase()) != null) {
                    Enchantment enchantment = Enchantment.getByName(option.replace(" ", "_").toUpperCase());
                    if (item.getType() != Material.ENCHANTED_BOOK) {
                        meta.addEnchant(enchantment, Integer.parseInt(value), true);
                    } else {
                        ((EnchantmentStorageMeta) meta).addStoredEnchant(enchantment, Integer.parseInt(value), true);
                    }
                }

                String effect = "";
                int duration = 0;
                int hit = 0;

                value = value.replace("_", " ");
                switch (option) {
                    case "title":
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            ((BookMeta) meta).setTitle(value);
                        } else meta.setDisplayName(value);
                        break;
                    case "lore":
                        String[] parts = value.split("\\|");
                        ArrayList<String> lore = new ArrayList<>();
                        for (String line : parts)
                            lore.add(TextComponent.formatText(line));
                        meta.setLore(lore);
                        break;
                    case "player":
                        if (item.getType() == Material.SKULL_ITEM) {
                            ((SkullMeta) meta).setOwner(value);
                        }
                        break;
                    case "author":
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            ((BookMeta) meta).setAuthor(value);
                        }
                        break;
                    case "effect":
                    case "duration":
                        hit++;
                        if (option.equalsIgnoreCase("effect")) {
                            effect = value;
                        } else if (option.equalsIgnoreCase("duration")) {
                            duration = Integer.parseInt(value);
                        }

                        if (hit == 2) {
                            PotionEffect effect2 = PotionEffectType.getByName(effect).createEffect(duration, 0);
                            ((PotionMeta) meta).addCustomEffect(effect2, false);
                        }

                        break;
                    case "id":
                        if (item.getType() == Material.WRITTEN_BOOK) {
                            if (!UltimateKits.getInstance().getDataFile().getConfig().contains("Books.pages." + value))
                                continue;
                            ConfigurationSection cs = UltimateKits.getInstance().getDataFile().getConfig().getConfigurationSection("Books.pages." + value);
                            for (String key : cs.getKeys(false)) {
                                ((BookMeta) meta).addPage(UltimateKits.getInstance().getDataFile().getConfig().getString("Books.pages." + value + "." + key));
                            }
                        }
                        break;
                    case "color":
                        switch (item.getType()) {
                            case POTION:
                                if (!UltimateKits.getInstance().v1_8 && !UltimateKits.getInstance().v1_7) {
                                    ((PotionMeta) meta).setColor(Color.fromRGB(Integer.parseInt(value)));
                                }
                                break;
                            case LEATHER_HELMET:
                            case LEATHER_CHESTPLATE:
                            case LEATHER_LEGGINGS:
                            case LEATHER_BOOTS:
                                ((LeatherArmorMeta) meta).setColor(Color.fromRGB(Integer.parseInt(value)));
                                break;
                        }
                        break;
                }
            }
        }
        item.setItemMeta(meta);
        return item;
    }

    public static String fixLine(String line) {
        line = line.replace(" ", "_");
        return line;
    }

    public static String unfixLine(String line) {
        line = line.replace("_", " ");
        return line;
    }


    public static String getKitFromLocation(Location location) {
        return UltimateKits.getInstance().getConfig().getString("data.block." + Arconix.pl().serialize().serializeLocation(location));
    }

}

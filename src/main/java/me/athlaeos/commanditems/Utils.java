package me.athlaeos.commanditems;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Utils {

    private static Random random = null;

    public static Random getRandom(){
        if (random == null){
            random = new Random();
        }
        return random;
    }

    public static boolean doesPathExist(YamlConfiguration config, String root, String key){
        ConfigurationSection section = config.getConfigurationSection(root);
        if (section != null){
            return section.getKeys(false).contains(key);
        }
        return false;
    }

    public static boolean isItemEmptyOrNull(ItemStack i){
        if (i == null) return true;
        return i.getType().isAir();
    }

    public static void sendMessage(CommandSender whomst, String message){
        if (message != null){
            if (!message.equals("")){
                whomst.sendMessage(chat(message));
            }
        }
    }

    public static String chat (String s) {
        if (s == null) return "";
        return newChat(s);
    }

    public static Map<Integer, ArrayList<String>> paginateTextList(int pageSize, List<String> allEntries) {
        Map<Integer, ArrayList<String>> pages = new HashMap<>();
        int stepper = 0;

        for (int pageNumber = 0; pageNumber < Math.ceil((double)allEntries.size()/(double)pageSize); pageNumber++) {
            ArrayList<String> pageEntries = new ArrayList<>();
            for (int pageEntry = 0; pageEntry < pageSize && stepper < allEntries.size(); pageEntry++, stepper++) {
                pageEntries.add(allEntries.get(stepper));
            }
            pages.put(pageNumber, pageEntries);
        }
        return pages;
    }

    public static String oldChat(String message) {
        return ChatColor.translateAlternateColorCodes('&', message + "");
    }
    static final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public static String newChat(String message) {
        char COLOR_CHAR = ChatColor.COLOR_CHAR;
        Matcher matcher = hexPattern.matcher(message);
        StringBuffer buffer = new StringBuffer(message.length() + 4 * 8);
        while (matcher.find())
        {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        return Utils.oldChat(matcher.appendTail(buffer).toString());
    }

    public static double round(double value, int places) {
        if (places < 0) places = 2;

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public static Map<Integer, ArrayList<ItemStack>> paginateItemStackList(int pageSize, List<ItemStack> allEntries) {
        Map<Integer, ArrayList<ItemStack>> pages = new HashMap<>();
        int stepper = 0;

        for (int pageNumber = 0; pageNumber < Math.ceil((double)allEntries.size()/(double)pageSize); pageNumber++) {
            ArrayList<ItemStack> pageEntries = new ArrayList<>();
            for (int pageEntry = 0; pageEntry < pageSize && stepper < allEntries.size(); pageEntry++, stepper++) {
                pageEntries.add(allEntries.get(stepper));
            }
            pages.put(pageNumber, pageEntries);
        }
        return pages;
    }

    public static String toTimeStamp(int ticks, int base){
        if (ticks == 0) return "0:00";
        if (ticks < 0) return "∞";
        int hours = (int) Math.floor(ticks / (3600D * base));
        ticks %= (base * 3600);
        int minutes = (int) Math.floor(ticks / (60D * base));
        ticks %= (base * 60);
        int seconds = (int) Math.floor(ticks / (double) base);
        if (hours > 0){
            if (seconds < 10){
                if (minutes < 10){
                    return String.format("%d:0%d:0%d", hours, minutes, seconds);
                } else {
                    return String.format("%d:%d:0%d", hours, minutes, seconds);
                }
            } else {
                if (minutes < 10){
                    return String.format("%d:0%d:%d", hours, minutes, seconds);
                } else {
                    return String.format("%d:%d:%d", hours, minutes, seconds);
                }
            }
        } else {
            if (seconds < 10){
                return String.format("%d:0%d", minutes, seconds);
            } else {
                return String.format("%d:%d", minutes, seconds);
            }
        }
    }
}

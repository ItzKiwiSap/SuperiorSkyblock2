package com.bgsoftware.superiorskyblock.menu;

import com.bgsoftware.superiorskyblock.api.island.Island;
import com.bgsoftware.superiorskyblock.api.wrappers.SuperiorPlayer;
import com.bgsoftware.superiorskyblock.utils.FileUtils;
import com.bgsoftware.superiorskyblock.utils.items.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public final class MenuVisitors extends SuperiorMenu {

    private static int previousSlot, currentSlot, nextSlot;
    private static List<Integer> slots = new ArrayList<>();

    private List<SuperiorPlayer> visitors;
    private Island island;
    private int currentPage = 1;

    private MenuVisitors(SuperiorPlayer superiorPlayer, Island island){
        super("menuVisitors", superiorPlayer);
        this.island = island;
    }

    @Override
    public void onPlayerClick(InventoryClickEvent e) {
        int clickedSlot = e.getRawSlot();

        if(clickedSlot == previousSlot || clickedSlot == nextSlot || clickedSlot == currentSlot){
            int nextPage;

            if(clickedSlot == previousSlot){
                nextPage = currentPage == 1 ? -1 : currentPage - 1;
            }
            else if(clickedSlot == nextSlot){
                nextPage = visitors.size() > currentPage * slots.size() ? currentPage + 1 : -1;
            }
            else return;

            if(nextPage == -1)
                return;

            previousMove = false;
            open(previousMenu);
        }

        else{
            if(e.getCurrentItem() == null)
                return;

            int indexOf = slots.indexOf(e.getRawSlot());

            if(indexOf < 0 || indexOf >= visitors.size())
                return;

            SuperiorPlayer targetPlayer = visitors.get(indexOf);

            if (targetPlayer != null) {
                if (e.getClick().name().contains("RIGHT")) {
                    Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island invite " + targetPlayer.getName());
                } else if (e.getClick().name().contains("LEFT")) {
                    Bukkit.dispatchCommand(superiorPlayer.asPlayer(), "island expel " + targetPlayer.getName());
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        Inventory inventory = super.getInventory();

        this.visitors = island.getIslandVisitors();

        for(int i = 0; i < slots.size(); i++){
            int visitorsIndex = i + (slots.size() * (currentPage - 1));

            if(visitorsIndex < visitors.size()) {
                SuperiorPlayer _superiorPlayer = visitors.get(i + (slots.size() * (currentPage - 1)));
                Island island = _superiorPlayer.getIsland();
                String islandOwner = "None";
                String islandName = "None";
                if (island != null) {
                    islandOwner = island.getOwner().getName();
                    islandName = island.getName().isEmpty() ? islandOwner : island.getName();
                }
                inventory.setItem(slots.get(i), new ItemBuilder(inventory.getItem(slots.get(i)))
                        .replaceAll("{0}", _superiorPlayer.getName())
                        .replaceAll("{1}", islandOwner)
                        .replaceAll("{2}", islandName)
                        .asSkullOf(_superiorPlayer).build(superiorPlayer));
            }
            else{
                inventory.setItem(slots.get(i), new ItemStack(Material.AIR));
            }
        }

        inventory.setItem(previousSlot, new ItemBuilder(inventory.getItem(previousSlot))
                .replaceAll("{0}", (currentPage == 1 ? "&c" : "&a")).build(superiorPlayer));

        inventory.setItem(currentSlot, new ItemBuilder(inventory.getItem(currentSlot))
                .replaceAll("{0}", currentPage + "").build(superiorPlayer));

        inventory.setItem(nextSlot, new ItemBuilder(inventory.getItem(nextSlot))
                .replaceAll("{0}", (visitors.size() > currentPage * slots.size() ? "&a" : "&c")).build(superiorPlayer));

        return inventory;
    }

    public static void init(){
        MenuVisitors menuVisitors = new MenuVisitors(null, null);

        File file = new File(plugin.getDataFolder(), "menus/visitors.yml");

        if(!file.exists())
            FileUtils.saveResource("menus/visitors.yml");

        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);

        Map<Character, List<Integer>> charSlots = FileUtils.loadGUI(menuVisitors, "visitors.yml", cfg);

        previousSlot = charSlots.getOrDefault(cfg.getString("previous-page", "%").charAt(0), Collections.singletonList(-1)).get(0);
        currentSlot = charSlots.getOrDefault(cfg.getString("current-page", "*").charAt(0), Collections.singletonList(-1)).get(0);
        nextSlot = charSlots.getOrDefault(cfg.getString("next-page", "^").charAt(0), Collections.singletonList(-1)).get(0);

        slots = charSlots.getOrDefault(cfg.getString("slots", "@").charAt(0), Collections.singletonList(-1));
        slots.sort(Integer::compareTo);
    }

    public static void openInventory(SuperiorPlayer superiorPlayer, SuperiorMenu previousMenu, Island island){
        new MenuVisitors(superiorPlayer, island).open(previousMenu);
    }

    public static void refreshMenus(){
        refreshMenus(MenuVisitors.class);
    }

}
package com.firestartermc.festivities.item;

import com.firestartermc.festivities.Festivities;
import com.firestartermc.festivities.api.ItemArchetype;
import com.firestartermc.kerosene.Kerosene;
import com.firestartermc.kerosene.item.ItemBuilder;
import com.firestartermc.kerosene.item.SkullBuilder;
import com.firestartermc.kerosene.util.MessageUtils;
import com.firestartermc.kerosene.util.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SnowmanScanner extends ItemArchetype implements Listener {

    private final List<Location> requiredBlocks = new ArrayList<>();

    public SnowmanScanner() {
        super("snowman_scanner", "Snowman Scanner");
        var world = Bukkit.getWorld("world");
        requiredBlocks.add(world.getBlockAt(-36085, 79, 44993).getLocation()); // shop
        requiredBlocks.add(world.getBlockAt(23240, 67, -44716).getLocation()); // spawn
        requiredBlocks.add(world.getBlockAt(23179, 73, -44752).getLocation()); // spawn
        requiredBlocks.add(world.getBlockAt(9974, 220, -33967).getLocation()); // grinder
        requiredBlocks.add(world.getBlockAt(26787, 58, -28796).getLocation()); // community garden
        requiredBlocks.add(world.getBlockAt(-298, 39, -28155).getLocation()); // warp bed
    }

    @Override
    @NotNull
    public ItemStack getItem() {
        return new SkullBuilder()
                .name("&#cfe1ff&lSNOWMAN &#c2cdff&lSCANNER")
                .lore(
                        "&fFind 5 out of 6 snowman heads in",
                        "&fpopular warps and locations and click",
                        "&fon them with this scanner. Find",
                        "&fthe 6th one for a bigger reward.",
                        "&r ",
                        getFoundString(0)
                )
                .texture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMz" +
                        "JhNDVjY2RjZWUxZjZmNmFjM2U4ZmZmMDkzYzIyZTkwZWFlOTU5ZmI2MDkyYzliYjJlOTg2NDNhOWYyZDQ0In19fQ==")
                .persistData(TYPE_KEY, PersistentDataType.STRING, getId())
                .build();
    }

    @NotNull
    private String getFoundString(int found) {
        return MessageUtils.formatColors("&f&lYou've found &#d4f8ff&l" + found, true);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !matches(event.getItem())) {
            return;
        } else {
            event.setCancelled(true);
        }

        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.PLAYER_HEAD) {
            return;
        }

        var location = event.getClickedBlock().getLocation();
        if (!requiredBlocks.contains(location)) {
            return;
        }

        var item = event.getItem();
        var player = event.getPlayer();
        var index = requiredBlocks.indexOf(location);
        var key = new NamespacedKey("snowman_scanner", "found");
        var meta = item.getItemMeta();
        var data = meta.getPersistentDataContainer();
        var stored = data.getOrDefault(key, PersistentDataType.BYTE_ARRAY, new byte[7]);

        if (stored[index] == 1) {
            return;
        }

        var found = (byte) (stored[6] + 1);
        stored[index] = 1;
        stored[6] = found;
        data.set(key, PersistentDataType.BYTE_ARRAY, stored);

        if (found < 5) {
            player.sendMessage(MessageUtils.formatColors("&#cfe1ff&lSCANNER: &fYou found " + found + " snowmen.", true));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
        } else if (found == 5) {
            player.sendMessage(MessageUtils.formatColors("&#cfe1ff&lSCANNER: &f&lYou found 5 snowmen and received $2K! Find one more for an extra epic reward.", true));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            Kerosene.getKerosene().getEconomy().depositPlayer(player, 2000);
        } else if (found == 6) {
            player.sendMessage(MessageUtils.formatColors("&#cfe1ff&lSCANNER: &e&lYou found all 6 snowmen! Enjoy your Unbreaking IV book.", true));
            player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);

            var book = ItemBuilder.of(Material.ENCHANTED_BOOK)
                    .name("&#cfe1ff&lFROSTY &#c2cdff&lBOOK")
                    .storeEnchantment(Enchantment.DURABILITY, 4)
                    .build();
            PlayerUtils.giveOrDropItem(player, book);
        }

        var lore = item.getLore();
        lore.set(5, getFoundString(found));
        meta.setLore(lore);
        item.setItemMeta(meta);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), item);
        player.updateInventory();
    }
}

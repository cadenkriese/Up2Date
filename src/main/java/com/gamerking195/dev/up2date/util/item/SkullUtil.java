package com.gamerking195.dev.up2date.util.item;

import com.gamerking195.dev.up2date.Up2Date;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * @author myles
 * @since 16/06/2017
 */
public class SkullUtil {

    private static final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static ItemStack makeSkull(String skinURL, String name, List<String> lore) {
        ItemStack skull = new ItemStack(Material.SKULL_ITEM, 1, (short) 3);
        skull = Up2Date.getInstance().getServer().getUnsafe().modifyItemStack(skull, "{SkullOwner:{Id:\"" + UUID.randomUUID() + "\",Name:\"" + getRandomString(16) + "\",Properties:{textures:[{Value:\"" + skinURL + "\"}]}}}");
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        skullMeta.setDisplayName(name);
        skullMeta.setLore(lore);
        skull.setItemMeta(skullMeta);

        return skull;
    }

    private static String getRandomString(int length) {
        StringBuilder b = new StringBuilder(length);
        for (int j = 0; j < length; j++)
            b.append(chars.charAt(new Random().nextInt(chars.length())));
        return b.toString();
    }
}

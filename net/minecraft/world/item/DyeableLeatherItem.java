/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DyeableLeatherItem {
    public static final String TAG_COLOR = "color";
    public static final String TAG_DISPLAY = "display";
    public static final int DEFAULT_LEATHER_COLOR = 10511680;

    default public boolean hasCustomColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        return compoundTag != null && compoundTag.contains(TAG_COLOR, 99);
    }

    default public int getColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_COLOR, 99)) {
            return compoundTag.getInt(TAG_COLOR);
        }
        return 10511680;
    }

    default public void clearColor(ItemStack itemStack) {
        CompoundTag compoundTag = itemStack.getTagElement(TAG_DISPLAY);
        if (compoundTag != null && compoundTag.contains(TAG_COLOR)) {
            compoundTag.remove(TAG_COLOR);
        }
    }

    default public void setColor(ItemStack itemStack, int i) {
        itemStack.getOrCreateTagElement(TAG_DISPLAY).putInt(TAG_COLOR, i);
    }

    public static ItemStack dyeArmor(ItemStack itemStack, List<DyeItem> list) {
        int n;
        float h;
        ItemStack itemStack2 = ItemStack.EMPTY;
        int[] is = new int[3];
        int i = 0;
        int j = 0;
        DyeableLeatherItem dyeableLeatherItem = null;
        Item item = itemStack.getItem();
        if (item instanceof DyeableLeatherItem) {
            dyeableLeatherItem = (DyeableLeatherItem)((Object)item);
            itemStack2 = itemStack.copy();
            itemStack2.setCount(1);
            if (dyeableLeatherItem.hasCustomColor(itemStack)) {
                int k = dyeableLeatherItem.getColor(itemStack2);
                float f = (float)(k >> 16 & 0xFF) / 255.0f;
                float g = (float)(k >> 8 & 0xFF) / 255.0f;
                h = (float)(k & 0xFF) / 255.0f;
                i += (int)(Math.max(f, Math.max(g, h)) * 255.0f);
                is[0] = is[0] + (int)(f * 255.0f);
                is[1] = is[1] + (int)(g * 255.0f);
                is[2] = is[2] + (int)(h * 255.0f);
                ++j;
            }
            for (DyeItem dyeItem : list) {
                float[] fs = dyeItem.getDyeColor().getTextureDiffuseColors();
                int l = (int)(fs[0] * 255.0f);
                int m = (int)(fs[1] * 255.0f);
                n = (int)(fs[2] * 255.0f);
                i += Math.max(l, Math.max(m, n));
                is[0] = is[0] + l;
                is[1] = is[1] + m;
                is[2] = is[2] + n;
                ++j;
            }
        }
        if (dyeableLeatherItem == null) {
            return ItemStack.EMPTY;
        }
        int k = is[0] / j;
        int o = is[1] / j;
        int p = is[2] / j;
        h = (float)i / (float)j;
        float q = Math.max(k, Math.max(o, p));
        k = (int)((float)k * h / q);
        o = (int)((float)o * h / q);
        p = (int)((float)p * h / q);
        n = k;
        n = (n << 8) + o;
        n = (n << 8) + p;
        dyeableLeatherItem.setColor(itemStack2, n);
        return itemStack2;
    }
}


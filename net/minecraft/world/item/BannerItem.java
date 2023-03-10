/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBannerBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.Nullable;

public class BannerItem
extends StandingAndWallBlockItem {
    private static final String PATTERN_PREFIX = "block.minecraft.banner.";

    public BannerItem(Block block, Block block2, Item.Properties properties) {
        super(block, block2, properties, Direction.DOWN);
        Validate.isInstanceOf(AbstractBannerBlock.class, block);
        Validate.isInstanceOf(AbstractBannerBlock.class, block2);
    }

    public static void appendHoverTextFromBannerBlockEntityTag(ItemStack itemStack, List<Component> list) {
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag == null || !compoundTag.contains("Patterns")) {
            return;
        }
        ListTag listTag = compoundTag.getList("Patterns", 10);
        for (int i = 0; i < listTag.size() && i < 6; ++i) {
            CompoundTag compoundTag2 = listTag.getCompound(i);
            DyeColor dyeColor = DyeColor.byId(compoundTag2.getInt("Color"));
            Holder<BannerPattern> holder = BannerPattern.byHash(compoundTag2.getString("Pattern"));
            if (holder == null) continue;
            holder.unwrapKey().map(resourceKey -> resourceKey.location().toShortLanguageKey()).ifPresent(string -> list.add(Component.translatable(PATTERN_PREFIX + string + "." + dyeColor.getName()).withStyle(ChatFormatting.GRAY)));
        }
    }

    public DyeColor getColor() {
        return ((AbstractBannerBlock)this.getBlock()).getColor();
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        BannerItem.appendHoverTextFromBannerBlockEntityTag(itemStack, list);
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerPattern;
import org.jetbrains.annotations.Nullable;

public class BannerPatternItem
extends Item {
    private final BannerPattern bannerPattern;

    public BannerPatternItem(BannerPattern bannerPattern, Item.Properties properties) {
        super(properties);
        this.bannerPattern = bannerPattern;
    }

    public BannerPattern getBannerPattern() {
        return this.bannerPattern;
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        list.add(this.getDisplayName().withStyle(ChatFormatting.GRAY));
    }

    @Environment(value=EnvType.CLIENT)
    public Component getDisplayName() {
        return new TranslatableComponent(this.getDescriptionId() + ".desc", new Object[0]);
    }
}


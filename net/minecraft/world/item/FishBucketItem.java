/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.TropicalFish;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.Nullable;

public class FishBucketItem
extends BucketItem {
    private final EntityType<?> type;

    public FishBucketItem(EntityType<?> entityType, Fluid fluid, Item.Properties properties) {
        super(fluid, properties);
        this.type = entityType;
    }

    @Override
    public void checkExtraContent(Level level, ItemStack itemStack, BlockPos blockPos) {
        if (!level.isClientSide) {
            this.spawn(level, itemStack, blockPos);
        }
    }

    @Override
    protected void playEmptySound(@Nullable Player player, LevelAccessor levelAccessor, BlockPos blockPos) {
        levelAccessor.playSound(player, blockPos, SoundEvents.BUCKET_EMPTY_FISH, SoundSource.NEUTRAL, 1.0f, 1.0f);
    }

    private void spawn(Level level, ItemStack itemStack, BlockPos blockPos) {
        Entity entity = this.type.spawn(level, itemStack, null, blockPos, MobSpawnType.BUCKET, true, false);
        if (entity != null) {
            ((AbstractFish)entity).setFromBucket(true);
        }
    }

    @Override
    @Environment(value=EnvType.CLIENT)
    public void appendHoverText(ItemStack itemStack, @Nullable Level level, List<Component> list, TooltipFlag tooltipFlag) {
        CompoundTag compoundTag;
        if (this.type == EntityType.TROPICAL_FISH && (compoundTag = itemStack.getTag()) != null && compoundTag.contains("BucketVariantTag", 3)) {
            int i = compoundTag.getInt("BucketVariantTag");
            ChatFormatting[] chatFormattings = new ChatFormatting[]{ChatFormatting.ITALIC, ChatFormatting.GRAY};
            String string = "color.minecraft." + TropicalFish.getBaseColor(i);
            String string2 = "color.minecraft." + TropicalFish.getPatternColor(i);
            for (int j = 0; j < TropicalFish.COMMON_VARIANTS.length; ++j) {
                if (i != TropicalFish.COMMON_VARIANTS[j]) continue;
                list.add(new TranslatableComponent(TropicalFish.getPredefinedName(j), new Object[0]).withStyle(chatFormattings));
                return;
            }
            list.add(new TranslatableComponent(TropicalFish.getFishTypeName(i), new Object[0]).withStyle(chatFormattings));
            TranslatableComponent component = new TranslatableComponent(string, new Object[0]);
            if (!string.equals(string2)) {
                component.append(", ").append(new TranslatableComponent(string2, new Object[0]));
            }
            component.withStyle(chatFormattings);
            list.add(component);
        }
    }
}


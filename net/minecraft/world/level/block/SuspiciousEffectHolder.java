/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.flag.FeatureElement;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;

public interface SuspiciousEffectHolder {
    public MobEffect getSuspiciousEffect();

    public int getEffectDuration();

    public static List<SuspiciousEffectHolder> getAllEffectHolders() {
        return BuiltInRegistries.ITEM.stream().map(SuspiciousEffectHolder::tryGet).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Nullable
    public static SuspiciousEffectHolder tryGet(ItemLike itemLike) {
        BlockItem blockItem;
        FeatureElement featureElement = itemLike.asItem();
        if (featureElement instanceof BlockItem && (featureElement = (blockItem = (BlockItem)featureElement).getBlock()) instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciousEffectHolder = (SuspiciousEffectHolder)((Object)featureElement);
            return suspiciousEffectHolder;
        }
        Item item = itemLike.asItem();
        if (item instanceof SuspiciousEffectHolder) {
            SuspiciousEffectHolder suspiciousEffectHolder2 = (SuspiciousEffectHolder)((Object)item);
            return suspiciousEffectHolder2;
        }
        return null;
    }
}


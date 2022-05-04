/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block.entity;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import org.jetbrains.annotations.Nullable;

public class BannerPattern {
    final String hashname;

    public BannerPattern(String string) {
        this.hashname = string;
    }

    public static ResourceLocation location(ResourceKey<BannerPattern> resourceKey, boolean bl) {
        String string = bl ? "banner" : "shield";
        ResourceLocation resourceLocation = resourceKey.location();
        return new ResourceLocation(resourceLocation.getNamespace(), "entity/" + string + "/" + resourceLocation.getPath());
    }

    public String getHashname() {
        return this.hashname;
    }

    @Nullable
    public static Holder<BannerPattern> byHash(String string) {
        return Registry.BANNER_PATTERN.holders().filter(reference -> ((BannerPattern)reference.value()).hashname.equals(string)).findAny().orElse(null);
    }

    public static class Builder {
        private final List<Pair<Holder<BannerPattern>, DyeColor>> patterns = Lists.newArrayList();

        public Builder addPattern(ResourceKey<BannerPattern> resourceKey, DyeColor dyeColor) {
            return this.addPattern(Registry.BANNER_PATTERN.getHolderOrThrow(resourceKey), dyeColor);
        }

        public Builder addPattern(Holder<BannerPattern> holder, DyeColor dyeColor) {
            return this.addPattern(Pair.of(holder, dyeColor));
        }

        public Builder addPattern(Pair<Holder<BannerPattern>, DyeColor> pair) {
            this.patterns.add(pair);
            return this;
        }

        public ListTag toListTag() {
            ListTag listTag = new ListTag();
            for (Pair<Holder<BannerPattern>, DyeColor> pair : this.patterns) {
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("Pattern", pair.getFirst().value().hashname);
                compoundTag.putInt("Color", pair.getSecond().getId());
                listTag.add(compoundTag);
            }
            return listTag;
        }
    }
}


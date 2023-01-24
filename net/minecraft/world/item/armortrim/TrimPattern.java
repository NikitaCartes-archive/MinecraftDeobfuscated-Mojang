/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.armortrim;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.armortrim.TrimMaterial;

public record TrimPattern(ResourceLocation assetId, Holder<Item> templateItem, Component description) {
    public static final Codec<TrimPattern> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)ResourceLocation.CODEC.fieldOf("asset_id")).forGetter(TrimPattern::assetId), ((MapCodec)RegistryFixedCodec.create(Registries.ITEM).fieldOf("template_item")).forGetter(TrimPattern::templateItem), ((MapCodec)ExtraCodecs.COMPONENT.fieldOf("description")).forGetter(TrimPattern::description)).apply((Applicative<TrimPattern, ?>)instance, TrimPattern::new));
    public static final Codec<Holder<TrimPattern>> CODEC = RegistryFileCodec.create(Registries.TRIM_PATTERN, DIRECT_CODEC);

    public Component copyWithStyle(Holder<TrimMaterial> holder) {
        return this.description.copy().withStyle(holder.value().description().getStyle());
    }
}


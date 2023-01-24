/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.armortrim;

import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.RegistryFixedCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;

public record TrimMaterial(String assetName, Holder<Item> ingredient, float itemModelIndex, Optional<ArmorMaterials> incompatibleArmorMaterial, Component description) {
    public static final Codec<TrimMaterial> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)Codec.STRING.fieldOf("asset_name")).forGetter(TrimMaterial::assetName), ((MapCodec)RegistryFixedCodec.create(Registries.ITEM).fieldOf("ingredient")).forGetter(TrimMaterial::ingredient), ((MapCodec)Codec.FLOAT.fieldOf("item_model_index")).forGetter(TrimMaterial::itemModelIndex), ArmorMaterials.CODEC.optionalFieldOf("incompatible_armor_material").forGetter(TrimMaterial::incompatibleArmorMaterial), ((MapCodec)ExtraCodecs.COMPONENT.fieldOf("description")).forGetter(TrimMaterial::description)).apply((Applicative<TrimMaterial, ?>)instance, TrimMaterial::new));
    public static final Codec<Holder<TrimMaterial>> CODEC = RegistryFileCodec.create(Registries.TRIM_MATERIAL, DIRECT_CODEC);

    public static TrimMaterial create(String string, Item item, float f, Optional<ArmorMaterials> optional, Component component) {
        return new TrimMaterial(string, BuiltInRegistries.ITEM.wrapAsHolder(item), f, optional, component);
    }
}


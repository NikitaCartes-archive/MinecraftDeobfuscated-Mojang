package net.minecraft.world.item.armortrim;

import com.mojang.serialization.Codec;
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

public record TrimMaterial(
	String assetName, Holder<Item> ingredient, float itemModelIndex, Optional<ArmorMaterials> incompatibleArmorMaterial, Component description
) {
	public static final Codec<TrimMaterial> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.STRING.fieldOf("asset_name").forGetter(TrimMaterial::assetName),
					RegistryFixedCodec.create(Registries.ITEM).fieldOf("ingredient").forGetter(TrimMaterial::ingredient),
					Codec.FLOAT.fieldOf("item_model_index").forGetter(TrimMaterial::itemModelIndex),
					ArmorMaterials.CODEC.optionalFieldOf("incompatible_armor_material").forGetter(TrimMaterial::incompatibleArmorMaterial),
					ExtraCodecs.COMPONENT.fieldOf("description").forGetter(TrimMaterial::description)
				)
				.apply(instance, TrimMaterial::new)
	);
	public static final Codec<Holder<TrimMaterial>> CODEC = RegistryFileCodec.create(Registries.TRIM_MATERIAL, DIRECT_CODEC);

	public static TrimMaterial create(String string, Item item, float f, Optional<ArmorMaterials> optional, Component component) {
		return new TrimMaterial(string, BuiltInRegistries.ITEM.wrapAsHolder(item), f, optional, component);
	}
}

package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public record WolfVariant(ResourceLocation texture, ResourceLocation tameTexture, ResourceLocation angryTexture, HolderSet<Biome> biomes) {
	public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("texture").forGetter(WolfVariant::texture),
					ResourceLocation.CODEC.fieldOf("tame_texture").forGetter(WolfVariant::tameTexture),
					ResourceLocation.CODEC.fieldOf("angry_texture").forGetter(WolfVariant::angryTexture),
					RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(WolfVariant::biomes)
				)
				.apply(instance, WolfVariant::new)
	);
}

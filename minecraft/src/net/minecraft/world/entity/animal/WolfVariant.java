package net.minecraft.world.entity.animal;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Objects;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;

public final class WolfVariant {
	public static final Codec<WolfVariant> DIRECT_CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("wild_texture").forGetter(wolfVariant -> wolfVariant.wildTexture),
					ResourceLocation.CODEC.fieldOf("tame_texture").forGetter(wolfVariant -> wolfVariant.tameTexture),
					ResourceLocation.CODEC.fieldOf("angry_texture").forGetter(wolfVariant -> wolfVariant.angryTexture),
					RegistryCodecs.homogeneousList(Registries.BIOME).fieldOf("biomes").forGetter(WolfVariant::biomes)
				)
				.apply(instance, WolfVariant::new)
	);
	public static final Codec<Holder<WolfVariant>> CODEC = RegistryFileCodec.create(Registries.WOLF_VARIANT, DIRECT_CODEC);
	private final ResourceLocation wildTexture;
	private final ResourceLocation tameTexture;
	private final ResourceLocation angryTexture;
	private final ResourceLocation wildTextureFull;
	private final ResourceLocation tameTextureFull;
	private final ResourceLocation angryTextureFull;
	private final HolderSet<Biome> biomes;

	public WolfVariant(ResourceLocation resourceLocation, ResourceLocation resourceLocation2, ResourceLocation resourceLocation3, HolderSet<Biome> holderSet) {
		this.wildTexture = resourceLocation;
		this.wildTextureFull = fullTextureId(resourceLocation);
		this.tameTexture = resourceLocation2;
		this.tameTextureFull = fullTextureId(resourceLocation2);
		this.angryTexture = resourceLocation3;
		this.angryTextureFull = fullTextureId(resourceLocation3);
		this.biomes = holderSet;
	}

	private static ResourceLocation fullTextureId(ResourceLocation resourceLocation) {
		return resourceLocation.withPath((UnaryOperator<String>)(string -> "textures/" + string + ".png"));
	}

	public ResourceLocation wildTexture() {
		return this.wildTextureFull;
	}

	public ResourceLocation tameTexture() {
		return this.tameTextureFull;
	}

	public ResourceLocation angryTexture() {
		return this.angryTextureFull;
	}

	public HolderSet<Biome> biomes() {
		return this.biomes;
	}

	public boolean equals(Object object) {
		if (object == this) {
			return true;
		} else {
			return !(object instanceof WolfVariant wolfVariant)
				? false
				: Objects.equals(this.wildTexture, wolfVariant.wildTexture)
					&& Objects.equals(this.tameTexture, wolfVariant.tameTexture)
					&& Objects.equals(this.angryTexture, wolfVariant.angryTexture)
					&& Objects.equals(this.biomes, wolfVariant.biomes);
		}
	}

	public int hashCode() {
		int i = 1;
		i = 31 * i + this.wildTexture.hashCode();
		i = 31 * i + this.tameTexture.hashCode();
		i = 31 * i + this.angryTexture.hashCode();
		return 31 * i + this.biomes.hashCode();
	}
}

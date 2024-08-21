package net.minecraft.world.item;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

public record ArmorMaterial(
	Map<ArmorItem.Type, Integer> defense,
	Holder<SoundEvent> equipSound,
	Predicate<ItemStack> repairIngredient,
	List<ArmorMaterial.Layer> layers,
	float toughness,
	float knockbackResistance
) {
	public static final Codec<Holder<ArmorMaterial>> CODEC = BuiltInRegistries.ARMOR_MATERIAL.holderByNameCodec();

	public int getDefense(ArmorItem.Type type) {
		return (Integer)this.defense.getOrDefault(type, 0);
	}

	public static final class Layer {
		private final ResourceLocation assetName;
		private final String suffix;
		private final boolean dyeable;
		private final ResourceLocation innerTexture;
		private final ResourceLocation outerTexture;

		public Layer(ResourceLocation resourceLocation, String string, boolean bl) {
			this.assetName = resourceLocation;
			this.suffix = string;
			this.dyeable = bl;
			this.innerTexture = this.resolveTexture(true);
			this.outerTexture = this.resolveTexture(false);
		}

		public Layer(ResourceLocation resourceLocation) {
			this(resourceLocation, "", false);
		}

		private ResourceLocation resolveTexture(boolean bl) {
			return this.assetName
				.withPath((UnaryOperator<String>)(string -> "textures/models/armor/" + this.assetName.getPath() + "_layer_" + (bl ? 2 : 1) + this.suffix + ".png"));
		}

		public ResourceLocation texture(boolean bl) {
			return bl ? this.innerTexture : this.outerTexture;
		}

		public boolean dyeable() {
			return this.dyeable;
		}
	}
}

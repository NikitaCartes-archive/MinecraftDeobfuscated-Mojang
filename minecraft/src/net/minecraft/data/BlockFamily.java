package net.minecraft.data;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.flag.FeatureFlag;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.StringUtils;

public class BlockFamily {
	private final Block baseBlock;
	final Map<BlockFamily.Variant, Block> variants = Maps.<BlockFamily.Variant, Block>newHashMap();
	FeatureFlagSet requiredFeatures = FeatureFlags.VANILLA_SET;
	boolean generateModel = true;
	boolean generateRecipe = true;
	@Nullable
	String recipeGroupPrefix;
	@Nullable
	String recipeUnlockedBy;

	BlockFamily(Block block) {
		this.baseBlock = block;
	}

	public Block getBaseBlock() {
		return this.baseBlock;
	}

	public Map<BlockFamily.Variant, Block> getVariants() {
		return this.variants;
	}

	public Block get(BlockFamily.Variant variant) {
		return (Block)this.variants.get(variant);
	}

	public boolean shouldGenerateModel() {
		return this.generateModel;
	}

	public boolean shouldGenerateRecipe(FeatureFlagSet featureFlagSet) {
		return this.generateRecipe && this.requiredFeatures.isSubsetOf(featureFlagSet);
	}

	public Optional<String> getRecipeGroupPrefix() {
		return StringUtils.isBlank(this.recipeGroupPrefix) ? Optional.empty() : Optional.of(this.recipeGroupPrefix);
	}

	public Optional<String> getRecipeUnlockedBy() {
		return StringUtils.isBlank(this.recipeUnlockedBy) ? Optional.empty() : Optional.of(this.recipeUnlockedBy);
	}

	public static class Builder {
		private final BlockFamily family;

		public Builder(Block block) {
			this.family = new BlockFamily(block);
		}

		public BlockFamily getFamily() {
			return this.family;
		}

		public BlockFamily.Builder button(Block block) {
			this.family.variants.put(BlockFamily.Variant.BUTTON, block);
			return this;
		}

		public BlockFamily.Builder chiseled(Block block) {
			this.family.variants.put(BlockFamily.Variant.CHISELED, block);
			return this;
		}

		public BlockFamily.Builder mosaic(Block block) {
			this.family.variants.put(BlockFamily.Variant.MOSAIC, block);
			return this;
		}

		public BlockFamily.Builder cracked(Block block) {
			this.family.variants.put(BlockFamily.Variant.CRACKED, block);
			return this;
		}

		public BlockFamily.Builder cut(Block block) {
			this.family.variants.put(BlockFamily.Variant.CUT, block);
			return this;
		}

		public BlockFamily.Builder door(Block block) {
			this.family.variants.put(BlockFamily.Variant.DOOR, block);
			return this;
		}

		public BlockFamily.Builder customFence(Block block) {
			this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE, block);
			return this;
		}

		public BlockFamily.Builder fence(Block block) {
			this.family.variants.put(BlockFamily.Variant.FENCE, block);
			return this;
		}

		public BlockFamily.Builder customFenceGate(Block block) {
			this.family.variants.put(BlockFamily.Variant.CUSTOM_FENCE_GATE, block);
			return this;
		}

		public BlockFamily.Builder fenceGate(Block block) {
			this.family.variants.put(BlockFamily.Variant.FENCE_GATE, block);
			return this;
		}

		public BlockFamily.Builder sign(Block block, Block block2) {
			this.family.variants.put(BlockFamily.Variant.SIGN, block);
			this.family.variants.put(BlockFamily.Variant.WALL_SIGN, block2);
			return this;
		}

		public BlockFamily.Builder slab(Block block) {
			this.family.variants.put(BlockFamily.Variant.SLAB, block);
			return this;
		}

		public BlockFamily.Builder stairs(Block block) {
			this.family.variants.put(BlockFamily.Variant.STAIRS, block);
			return this;
		}

		public BlockFamily.Builder pressurePlate(Block block) {
			this.family.variants.put(BlockFamily.Variant.PRESSURE_PLATE, block);
			return this;
		}

		public BlockFamily.Builder polished(Block block) {
			this.family.variants.put(BlockFamily.Variant.POLISHED, block);
			return this;
		}

		public BlockFamily.Builder trapdoor(Block block) {
			this.family.variants.put(BlockFamily.Variant.TRAPDOOR, block);
			return this;
		}

		public BlockFamily.Builder wall(Block block) {
			this.family.variants.put(BlockFamily.Variant.WALL, block);
			return this;
		}

		public BlockFamily.Builder dontGenerateModel() {
			this.family.generateModel = false;
			return this;
		}

		public BlockFamily.Builder dontGenerateRecipe() {
			this.family.generateRecipe = false;
			return this;
		}

		public BlockFamily.Builder featureLockedBehind(FeatureFlag... featureFlags) {
			this.family.requiredFeatures = FeatureFlags.REGISTRY.subset(featureFlags);
			return this;
		}

		public BlockFamily.Builder recipeGroupPrefix(String string) {
			this.family.recipeGroupPrefix = string;
			return this;
		}

		public BlockFamily.Builder recipeUnlockedBy(String string) {
			this.family.recipeUnlockedBy = string;
			return this;
		}
	}

	public static enum Variant {
		BUTTON("button"),
		CHISELED("chiseled"),
		CRACKED("cracked"),
		CUT("cut"),
		DOOR("door"),
		CUSTOM_FENCE("custom_fence"),
		FENCE("fence"),
		CUSTOM_FENCE_GATE("custom_fence_gate"),
		FENCE_GATE("fence_gate"),
		MOSAIC("mosaic"),
		SIGN("sign"),
		SLAB("slab"),
		STAIRS("stairs"),
		PRESSURE_PLATE("pressure_plate"),
		POLISHED("polished"),
		TRAPDOOR("trapdoor"),
		WALL("wall"),
		WALL_SIGN("wall_sign");

		private final String name;

		private Variant(String string2) {
			this.name = string2;
		}

		public String getName() {
			return this.name;
		}
	}
}

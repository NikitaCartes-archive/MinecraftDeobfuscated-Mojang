package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class VegetationPatchConfiguration implements FeatureConfiguration {
	public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					TagKey.hashedCodec(Registry.BLOCK_REGISTRY).fieldOf("replaceable").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.replaceable),
					BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.groundState),
					PlacedFeature.CODEC.fieldOf("vegetation_feature").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationFeature),
					CaveSurface.CODEC.fieldOf("surface").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.surface),
					IntProvider.codec(1, 128).fieldOf("depth").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.depth),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("extra_bottom_block_chance")
						.forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.extraBottomBlockChance),
					Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.verticalRange),
					Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationChance),
					IntProvider.CODEC.fieldOf("xz_radius").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.xzRadius),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("extra_edge_column_chance")
						.forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.extraEdgeColumnChance)
				)
				.apply(instance, VegetationPatchConfiguration::new)
	);
	public final TagKey<Block> replaceable;
	public final BlockStateProvider groundState;
	public final Holder<PlacedFeature> vegetationFeature;
	public final CaveSurface surface;
	public final IntProvider depth;
	public final float extraBottomBlockChance;
	public final int verticalRange;
	public final float vegetationChance;
	public final IntProvider xzRadius;
	public final float extraEdgeColumnChance;

	public VegetationPatchConfiguration(
		TagKey<Block> tagKey,
		BlockStateProvider blockStateProvider,
		Holder<PlacedFeature> holder,
		CaveSurface caveSurface,
		IntProvider intProvider,
		float f,
		int i,
		float g,
		IntProvider intProvider2,
		float h
	) {
		this.replaceable = tagKey;
		this.groundState = blockStateProvider;
		this.vegetationFeature = holder;
		this.surface = caveSurface;
		this.depth = intProvider;
		this.extraBottomBlockChance = f;
		this.verticalRange = i;
		this.vegetationChance = g;
		this.xzRadius = intProvider2;
		this.extraEdgeColumnChance = h;
	}
}

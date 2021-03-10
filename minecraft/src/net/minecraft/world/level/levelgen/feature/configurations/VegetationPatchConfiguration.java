package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class VegetationPatchConfiguration implements FeatureConfiguration {
	public static final Codec<VegetationPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("replaceable").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.replaceable),
					BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.groundState),
					ConfiguredFeature.CODEC.fieldOf("vegetation_feature").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationFeature),
					CaveSurface.CODEC.fieldOf("surface").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.surface),
					UniformInt.codec(1, 64, 64).fieldOf("depth").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.depth),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("extra_bottom_block_chance")
						.forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.extraBottomBlockChance),
					Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.verticalRange),
					Codec.floatRange(0.0F, 1.0F).fieldOf("vegetation_chance").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.vegetationChance),
					UniformInt.CODEC.fieldOf("xz_radius").forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.xzRadius),
					Codec.floatRange(0.0F, 1.0F)
						.fieldOf("extra_edge_column_chance")
						.forGetter(vegetationPatchConfiguration -> vegetationPatchConfiguration.extraEdgeColumnChance)
				)
				.apply(instance, VegetationPatchConfiguration::new)
	);
	public final ResourceLocation replaceable;
	public final BlockStateProvider groundState;
	public final Supplier<ConfiguredFeature<?, ?>> vegetationFeature;
	public final CaveSurface surface;
	public final UniformInt depth;
	public final float extraBottomBlockChance;
	public final int verticalRange;
	public final float vegetationChance;
	public final UniformInt xzRadius;
	public final float extraEdgeColumnChance;

	public VegetationPatchConfiguration(
		ResourceLocation resourceLocation,
		BlockStateProvider blockStateProvider,
		Supplier<ConfiguredFeature<?, ?>> supplier,
		CaveSurface caveSurface,
		UniformInt uniformInt,
		float f,
		int i,
		float g,
		UniformInt uniformInt2,
		float h
	) {
		this.replaceable = resourceLocation;
		this.groundState = blockStateProvider;
		this.vegetationFeature = supplier;
		this.surface = caveSurface;
		this.depth = uniformInt;
		this.extraBottomBlockChance = f;
		this.verticalRange = i;
		this.vegetationChance = g;
		this.xzRadius = uniformInt2;
		this.extraEdgeColumnChance = h;
	}
}

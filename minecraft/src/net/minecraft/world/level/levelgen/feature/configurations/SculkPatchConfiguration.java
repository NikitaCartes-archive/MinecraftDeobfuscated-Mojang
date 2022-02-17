package net.minecraft.world.level.levelgen.feature.configurations;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.function.Supplier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.CaveSurface;

public class SculkPatchConfiguration implements FeatureConfiguration {
	public static final Codec<SculkPatchConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					ResourceLocation.CODEC.fieldOf("replaceable").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.replaceable),
					BlockStateProvider.CODEC.fieldOf("ground_state").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.groundState),
					ConfiguredFeature.CODEC.fieldOf("growth_feature").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.growthFeature),
					Codec.floatRange(0.0F, 1.0F).fieldOf("growth_chance").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.growthChance),
					CaveSurface.CODEC.fieldOf("surface").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.surface),
					Codec.intRange(1, 256).fieldOf("vertical_range").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.verticalRange),
					IntProvider.CODEC.fieldOf("xz_radius").forGetter(sculkPatchConfiguration -> sculkPatchConfiguration.xzRadius)
				)
				.apply(instance, SculkPatchConfiguration::new)
	);
	public final ResourceLocation replaceable;
	public final BlockStateProvider groundState;
	public final Supplier<ConfiguredFeature<?, ?>> growthFeature;
	public final float growthChance;
	public final CaveSurface surface;
	public final int verticalRange;
	public final IntProvider xzRadius;

	public SculkPatchConfiguration(
		ResourceLocation resourceLocation,
		BlockStateProvider blockStateProvider,
		Supplier<ConfiguredFeature<?, ?>> supplier,
		float f,
		CaveSurface caveSurface,
		int i,
		IntProvider intProvider
	) {
		this.replaceable = resourceLocation;
		this.groundState = blockStateProvider;
		this.growthFeature = supplier;
		this.growthChance = f;
		this.surface = caveSurface;
		this.verticalRange = i;
		this.xzRadius = intProvider;
	}
}

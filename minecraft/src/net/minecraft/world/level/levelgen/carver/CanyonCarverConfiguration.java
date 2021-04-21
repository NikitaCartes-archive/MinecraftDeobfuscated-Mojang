package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;

public class CanyonCarverConfiguration extends CarverConfiguration {
	public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					CarverConfiguration.CODEC.forGetter(canyonCarverConfiguration -> canyonCarverConfiguration),
					FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.verticalRotation),
					CanyonCarverConfiguration.CanyonShapeConfiguration.CODEC.fieldOf("shape").forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.shape)
				)
				.apply(instance, CanyonCarverConfiguration::new)
	);
	public final FloatProvider verticalRotation;
	public final CanyonCarverConfiguration.CanyonShapeConfiguration shape;

	public CanyonCarverConfiguration(
		float f,
		HeightProvider heightProvider,
		FloatProvider floatProvider,
		VerticalAnchor verticalAnchor,
		boolean bl,
		CarverDebugSettings carverDebugSettings,
		FloatProvider floatProvider2,
		CanyonCarverConfiguration.CanyonShapeConfiguration canyonShapeConfiguration
	) {
		super(f, heightProvider, floatProvider, verticalAnchor, bl, carverDebugSettings);
		this.verticalRotation = floatProvider2;
		this.shape = canyonShapeConfiguration;
	}

	public CanyonCarverConfiguration(
		CarverConfiguration carverConfiguration, FloatProvider floatProvider, CanyonCarverConfiguration.CanyonShapeConfiguration canyonShapeConfiguration
	) {
		this(
			carverConfiguration.probability,
			carverConfiguration.y,
			carverConfiguration.yScale,
			carverConfiguration.lavaLevel,
			carverConfiguration.aquifersEnabled,
			carverConfiguration.debugSettings,
			floatProvider,
			canyonShapeConfiguration
		);
	}

	public static class CanyonShapeConfiguration {
		public static final Codec<CanyonCarverConfiguration.CanyonShapeConfiguration> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						FloatProvider.CODEC.fieldOf("distance_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.distanceFactor),
						FloatProvider.CODEC.fieldOf("thickness").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.thickness),
						ExtraCodecs.NON_NEGATIVE_INT.fieldOf("width_smoothness").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.widthSmoothness),
						FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.horizontalRadiusFactor),
						Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.verticalRadiusDefaultFactor),
						Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(canyonShapeConfiguration -> canyonShapeConfiguration.verticalRadiusCenterFactor)
					)
					.apply(instance, CanyonCarverConfiguration.CanyonShapeConfiguration::new)
		);
		public final FloatProvider distanceFactor;
		public final FloatProvider thickness;
		public final int widthSmoothness;
		public final FloatProvider horizontalRadiusFactor;
		public final float verticalRadiusDefaultFactor;
		public final float verticalRadiusCenterFactor;

		public CanyonShapeConfiguration(FloatProvider floatProvider, FloatProvider floatProvider2, int i, FloatProvider floatProvider3, float f, float g) {
			this.widthSmoothness = i;
			this.horizontalRadiusFactor = floatProvider3;
			this.verticalRadiusDefaultFactor = f;
			this.verticalRadiusCenterFactor = g;
			this.distanceFactor = floatProvider;
			this.thickness = floatProvider2;
		}
	}
}

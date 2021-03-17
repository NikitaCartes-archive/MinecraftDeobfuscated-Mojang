package net.minecraft.world.level.levelgen.carver;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.FloatProvider;
import net.minecraft.util.UniformInt;
import net.minecraft.world.level.levelgen.VerticalAnchor;

public class CanyonCarverConfiguration extends CarverConfiguration {
	public static final Codec<CanyonCarverConfiguration> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.floatRange(0.0F, 1.0F).fieldOf("probability").forGetter(canyonCarverConfiguration -> canyonCarverConfiguration.probability),
					CarverDebugSettings.CODEC.optionalFieldOf("debug_settings", CarverDebugSettings.DEFAULT).forGetter(CarverConfiguration::getDebugSettings),
					VerticalAnchor.CODEC.fieldOf("bottom_inclusive").forGetter(CanyonCarverConfiguration::getBottomInclusive),
					VerticalAnchor.CODEC.fieldOf("top_inclusive").forGetter(CanyonCarverConfiguration::getTopInclusive),
					UniformInt.CODEC.fieldOf("y_scale").forGetter(CanyonCarverConfiguration::getYScale),
					FloatProvider.codec(0.0F, 1.0F).fieldOf("distance_factor").forGetter(CanyonCarverConfiguration::getDistanceFactor),
					FloatProvider.CODEC.fieldOf("vertical_rotation").forGetter(CanyonCarverConfiguration::getVerticalRotation),
					FloatProvider.CODEC.fieldOf("thickness").forGetter(CanyonCarverConfiguration::getThickness),
					Codec.intRange(0, Integer.MAX_VALUE).fieldOf("width_smoothness").forGetter(CanyonCarverConfiguration::getWidthSmoothness),
					FloatProvider.CODEC.fieldOf("horizontal_radius_factor").forGetter(CanyonCarverConfiguration::getHorizontalRadiusFactor),
					Codec.FLOAT.fieldOf("vertical_radius_default_factor").forGetter(CanyonCarverConfiguration::getVerticalRadiusDefaultFactor),
					Codec.FLOAT.fieldOf("vertical_radius_center_factor").forGetter(CanyonCarverConfiguration::getVerticalRadiusCenterFactor)
				)
				.apply(instance, CanyonCarverConfiguration::new)
	);
	private final VerticalAnchor bottomInclusive;
	private final VerticalAnchor topInclusive;
	private final UniformInt yScale;
	private final FloatProvider distanceFactor;
	private final FloatProvider verticalRotation;
	private final FloatProvider thickness;
	private final int widthSmoothness;
	private final FloatProvider horizontalRadiusFactor;
	private final float verticalRadiusDefaultFactor;
	private final float verticalRadiusCenterFactor;

	public CanyonCarverConfiguration(
		float f,
		CarverDebugSettings carverDebugSettings,
		VerticalAnchor verticalAnchor,
		VerticalAnchor verticalAnchor2,
		UniformInt uniformInt,
		FloatProvider floatProvider,
		FloatProvider floatProvider2,
		FloatProvider floatProvider3,
		int i,
		FloatProvider floatProvider4,
		float g,
		float h
	) {
		super(f, carverDebugSettings);
		this.bottomInclusive = verticalAnchor;
		this.topInclusive = verticalAnchor2;
		this.yScale = uniformInt;
		this.distanceFactor = floatProvider;
		this.verticalRotation = floatProvider2;
		this.thickness = floatProvider3;
		this.widthSmoothness = i;
		this.horizontalRadiusFactor = floatProvider4;
		this.verticalRadiusDefaultFactor = g;
		this.verticalRadiusCenterFactor = h;
	}

	public VerticalAnchor getBottomInclusive() {
		return this.bottomInclusive;
	}

	public VerticalAnchor getTopInclusive() {
		return this.topInclusive;
	}

	public UniformInt getYScale() {
		return this.yScale;
	}

	public FloatProvider getDistanceFactor() {
		return this.distanceFactor;
	}

	public FloatProvider getVerticalRotation() {
		return this.verticalRotation;
	}

	public FloatProvider getThickness() {
		return this.thickness;
	}

	public int getWidthSmoothness() {
		return this.widthSmoothness;
	}

	public FloatProvider getHorizontalRadiusFactor() {
		return this.horizontalRadiusFactor;
	}

	public float getVerticalRadiusDefaultFactor() {
		return this.verticalRadiusDefaultFactor;
	}

	public float getVerticalRadiusCenterFactor() {
		return this.verticalRadiusCenterFactor;
	}
}

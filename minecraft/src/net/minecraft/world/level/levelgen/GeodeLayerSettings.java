package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class GeodeLayerSettings {
	private static final Codec<Double> LAYER_RANGE = Codec.doubleRange(0.01, 50.0);
	public static final Codec<GeodeLayerSettings> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					LAYER_RANGE.fieldOf("filling").orElse(1.7).forGetter(geodeLayerSettings -> geodeLayerSettings.filling),
					LAYER_RANGE.fieldOf("inner_layer").orElse(2.2).forGetter(geodeLayerSettings -> geodeLayerSettings.innerLayer),
					LAYER_RANGE.fieldOf("middle_layer").orElse(3.2).forGetter(geodeLayerSettings -> geodeLayerSettings.middleLayer),
					LAYER_RANGE.fieldOf("outer_layer").orElse(4.2).forGetter(geodeLayerSettings -> geodeLayerSettings.outerLayer)
				)
				.apply(instance, GeodeLayerSettings::new)
	);
	public final double filling;
	public final double innerLayer;
	public final double middleLayer;
	public final double outerLayer;

	public GeodeLayerSettings(double d, double e, double f, double g) {
		this.filling = d;
		this.innerLayer = e;
		this.middleLayer = f;
		this.outerLayer = g;
	}
}

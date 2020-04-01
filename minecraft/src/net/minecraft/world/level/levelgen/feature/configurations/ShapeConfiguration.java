package net.minecraft.world.level.levelgen.feature.configurations;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.Random;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProviderType;

public class ShapeConfiguration implements FeatureConfiguration {
	public final BlockStateProvider material;
	public final ShapeConfiguration.Metric metric;
	public final float radiusMin;
	public final float radiusMax;

	public ShapeConfiguration(BlockStateProvider blockStateProvider, ShapeConfiguration.Metric metric, float f, float g) {
		this.material = blockStateProvider;
		this.metric = metric;
		this.radiusMin = f;
		this.radiusMax = g;
	}

	@Override
	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(
			dynamicOps,
			dynamicOps.createMap(
				ImmutableMap.of(
					dynamicOps.createString("metric"),
					dynamicOps.createString(this.metric.name),
					dynamicOps.createString("material"),
					this.material.serialize(dynamicOps),
					dynamicOps.createString("radiusMin"),
					dynamicOps.createFloat(this.radiusMin),
					dynamicOps.createString("radiusMax"),
					dynamicOps.createFloat(this.radiusMax)
				)
			)
		);
	}

	public static <T> ShapeConfiguration deserialize(Dynamic<T> dynamic) {
		ShapeConfiguration.Metric metric = (ShapeConfiguration.Metric)dynamic.get("metric").asString().map(ShapeConfiguration.Metric::fromId).get();
		BlockStateProvider blockStateProvider = (BlockStateProvider)dynamic.get("material")
			.map(
				dynamicx -> {
					ResourceLocation resourceLocation = (ResourceLocation)dynamicx.get("type").asString().map(ResourceLocation::new).get();
					BlockStateProviderType<?> blockStateProviderType = (BlockStateProviderType<?>)Registry.BLOCKSTATE_PROVIDER_TYPES
						.getOptional(resourceLocation)
						.orElseThrow(() -> new IllegalStateException(resourceLocation.toString()));
					return blockStateProviderType.deserialize(dynamicx);
				}
			)
			.orElseThrow(IllegalStateException::new);
		float f = dynamic.get("radiusMin").asFloat(0.0F);
		float g = dynamic.get("radiusMax").asFloat(0.0F);
		return new ShapeConfiguration(blockStateProvider, metric, f, g);
	}

	public static ShapeConfiguration random(Random random) {
		ShapeConfiguration.Metric metric = Util.randomObject(random, ShapeConfiguration.Metric.values());
		BlockStateProvider blockStateProvider = BlockStateProvider.random(random);
		float f = 1.0F + random.nextFloat() * 5.0F;
		float g = Math.min(f + random.nextFloat() * 10.0F, 15.0F);
		return new ShapeConfiguration(blockStateProvider, metric, f, g);
	}

	public static enum Metric {
		EUCLIDIAN("euclidian") {
			@Override
			public float distance(BlockPos blockPos, BlockPos blockPos2) {
				return Mth.sqrt(blockPos.distSqr(blockPos2));
			}
		},
		TAXICAB("taxicab") {
			@Override
			public float distance(BlockPos blockPos, BlockPos blockPos2) {
				return (float)blockPos.distManhattan(blockPos2);
			}
		},
		CHESSBOARD("chessboard") {
			@Override
			public float distance(BlockPos blockPos, BlockPos blockPos2) {
				float f = (float)Math.abs(blockPos2.getX() - blockPos.getX());
				float g = (float)Math.abs(blockPos2.getY() - blockPos.getY());
				float h = (float)Math.abs(blockPos2.getZ() - blockPos.getZ());
				return Math.max(f, Math.max(g, h));
			}
		};

		private final String name;

		private Metric(String string2) {
			this.name = string2;
		}

		public abstract float distance(BlockPos blockPos, BlockPos blockPos2);

		public static ShapeConfiguration.Metric fromId(String string) {
			return (ShapeConfiguration.Metric)Stream.of(values())
				.filter(metric -> metric.name.equals(string))
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(string));
		}
	}
}

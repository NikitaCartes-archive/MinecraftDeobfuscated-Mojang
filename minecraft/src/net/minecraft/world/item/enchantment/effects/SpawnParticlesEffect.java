package net.minecraft.world.item.enchantment.effects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.util.valueproviders.ConstantFloat;
import net.minecraft.util.valueproviders.FloatProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.phys.Vec3;

public record SpawnParticlesEffect(
	ParticleOptions particle,
	SpawnParticlesEffect.PositionSource horizontalPosition,
	SpawnParticlesEffect.PositionSource verticalPosition,
	SpawnParticlesEffect.VelocitySource horizontalVelocity,
	SpawnParticlesEffect.VelocitySource verticalVelocity,
	FloatProvider speed
) implements EnchantmentEntityEffect {
	public static final MapCodec<SpawnParticlesEffect> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					ParticleTypes.CODEC.fieldOf("particle").forGetter(SpawnParticlesEffect::particle),
					SpawnParticlesEffect.PositionSource.CODEC.fieldOf("horizontal_position").forGetter(SpawnParticlesEffect::horizontalPosition),
					SpawnParticlesEffect.PositionSource.CODEC.fieldOf("vertical_position").forGetter(SpawnParticlesEffect::verticalPosition),
					SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("horizontal_velocity").forGetter(SpawnParticlesEffect::horizontalVelocity),
					SpawnParticlesEffect.VelocitySource.CODEC.fieldOf("vertical_velocity").forGetter(SpawnParticlesEffect::verticalVelocity),
					FloatProvider.CODEC.optionalFieldOf("speed", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect::speed)
				)
				.apply(instance, SpawnParticlesEffect::new)
	);

	public static SpawnParticlesEffect.PositionSource offsetFromEntityPosition(float f) {
		return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.ENTITY_POSITION, f, 1.0F);
	}

	public static SpawnParticlesEffect.PositionSource inBoundingBox() {
		return new SpawnParticlesEffect.PositionSource(SpawnParticlesEffect.PositionSourceType.BOUNDING_BOX, 0.0F, 1.0F);
	}

	public static SpawnParticlesEffect.VelocitySource movementScaled(float f) {
		return new SpawnParticlesEffect.VelocitySource(f, ConstantFloat.ZERO);
	}

	public static SpawnParticlesEffect.VelocitySource fixedVelocity(FloatProvider floatProvider) {
		return new SpawnParticlesEffect.VelocitySource(0.0F, floatProvider);
	}

	@Override
	public void apply(ServerLevel serverLevel, int i, EnchantedItemInUse enchantedItemInUse, Entity entity, Vec3 vec3) {
		RandomSource randomSource = entity.getRandom();
		Vec3 vec32 = entity.getKnownMovement();
		float f = entity.getBbWidth();
		float g = entity.getBbHeight();
		serverLevel.sendParticles(
			this.particle,
			this.horizontalPosition.getCoordinate(vec3.x(), vec3.x(), f, randomSource),
			this.verticalPosition.getCoordinate(vec3.y(), vec3.y() + (double)(g / 2.0F), g, randomSource),
			this.horizontalPosition.getCoordinate(vec3.z(), vec3.z(), f, randomSource),
			0,
			this.horizontalVelocity.getVelocity(vec32.x(), randomSource),
			this.verticalVelocity.getVelocity(vec32.y(), randomSource),
			this.horizontalVelocity.getVelocity(vec32.z(), randomSource),
			(double)this.speed.sample(randomSource)
		);
	}

	@Override
	public MapCodec<SpawnParticlesEffect> codec() {
		return CODEC;
	}

	public static record PositionSource(SpawnParticlesEffect.PositionSourceType type, float offset, float scale) {
		public static final MapCodec<SpawnParticlesEffect.PositionSource> CODEC = RecordCodecBuilder.<SpawnParticlesEffect.PositionSource>mapCodec(
				instance -> instance.group(
							SpawnParticlesEffect.PositionSourceType.CODEC.fieldOf("type").forGetter(SpawnParticlesEffect.PositionSource::type),
							Codec.FLOAT.optionalFieldOf("offset", Float.valueOf(0.0F)).forGetter(SpawnParticlesEffect.PositionSource::offset),
							ExtraCodecs.POSITIVE_FLOAT.optionalFieldOf("scale", 1.0F).forGetter(SpawnParticlesEffect.PositionSource::scale)
						)
						.apply(instance, SpawnParticlesEffect.PositionSource::new)
			)
			.validate(
				positionSource -> positionSource.type() == SpawnParticlesEffect.PositionSourceType.ENTITY_POSITION && positionSource.scale() != 1.0F
						? DataResult.error(() -> "Cannot scale an entity position coordinate source")
						: DataResult.success(positionSource)
			);

		public double getCoordinate(double d, double e, float f, RandomSource randomSource) {
			return this.type.getCoordinate(d, e, f * this.scale, randomSource) + (double)this.offset;
		}
	}

	public static enum PositionSourceType implements StringRepresentable {
		ENTITY_POSITION("entity_position", (d, e, f, randomSource) -> d),
		BOUNDING_BOX("in_bounding_box", (d, e, f, randomSource) -> e + (randomSource.nextDouble() - 0.5) * (double)f);

		public static final Codec<SpawnParticlesEffect.PositionSourceType> CODEC = StringRepresentable.fromEnum(SpawnParticlesEffect.PositionSourceType::values);
		private final String id;
		private final SpawnParticlesEffect.PositionSourceType.CoordinateSource source;

		private PositionSourceType(final String string2, final SpawnParticlesEffect.PositionSourceType.CoordinateSource coordinateSource) {
			this.id = string2;
			this.source = coordinateSource;
		}

		public double getCoordinate(double d, double e, float f, RandomSource randomSource) {
			return this.source.getCoordinate(d, e, f, randomSource);
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}

		@FunctionalInterface
		interface CoordinateSource {
			double getCoordinate(double d, double e, float f, RandomSource randomSource);
		}
	}

	public static record VelocitySource(float movementScale, FloatProvider base) {
		public static final MapCodec<SpawnParticlesEffect.VelocitySource> CODEC = RecordCodecBuilder.mapCodec(
			instance -> instance.group(
						Codec.FLOAT.optionalFieldOf("movement_scale", Float.valueOf(0.0F)).forGetter(SpawnParticlesEffect.VelocitySource::movementScale),
						FloatProvider.CODEC.optionalFieldOf("base", ConstantFloat.ZERO).forGetter(SpawnParticlesEffect.VelocitySource::base)
					)
					.apply(instance, SpawnParticlesEffect.VelocitySource::new)
		);

		public double getVelocity(double d, RandomSource randomSource) {
			return d * (double)this.movementScale + (double)this.base.sample(randomSource);
		}
	}
}

package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.Mth;

public record MovementPredicate(
	MinMaxBounds.Doubles x,
	MinMaxBounds.Doubles y,
	MinMaxBounds.Doubles z,
	MinMaxBounds.Doubles speed,
	MinMaxBounds.Doubles horizontalSpeed,
	MinMaxBounds.Doubles verticalSpeed,
	MinMaxBounds.Doubles fallDistance
) {
	public static final Codec<MovementPredicate> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("x", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::x),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("y", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::y),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("z", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::z),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::speed),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("horizontal_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::horizontalSpeed),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("vertical_speed", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::verticalSpeed),
					MinMaxBounds.Doubles.CODEC.optionalFieldOf("fall_distance", MinMaxBounds.Doubles.ANY).forGetter(MovementPredicate::fallDistance)
				)
				.apply(instance, MovementPredicate::new)
	);

	public static MovementPredicate speed(MinMaxBounds.Doubles doubles) {
		return new MovementPredicate(
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			doubles,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY
		);
	}

	public static MovementPredicate horizontalSpeed(MinMaxBounds.Doubles doubles) {
		return new MovementPredicate(
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			doubles,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY
		);
	}

	public static MovementPredicate verticalSpeed(MinMaxBounds.Doubles doubles) {
		return new MovementPredicate(
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			doubles,
			MinMaxBounds.Doubles.ANY
		);
	}

	public static MovementPredicate fallDistance(MinMaxBounds.Doubles doubles) {
		return new MovementPredicate(
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			MinMaxBounds.Doubles.ANY,
			doubles
		);
	}

	public boolean matches(double d, double e, double f, double g) {
		if (this.x.matches(d) && this.y.matches(e) && this.z.matches(f)) {
			double h = Mth.lengthSquared(d, e, f);
			if (!this.speed.matchesSqr(h)) {
				return false;
			} else {
				double i = Mth.lengthSquared(d, f);
				if (!this.horizontalSpeed.matchesSqr(i)) {
					return false;
				} else {
					double j = Math.abs(e);
					return !this.verticalSpeed.matches(j) ? false : this.fallDistance.matches(g);
				}
			}
		} else {
			return false;
		}
	}
}

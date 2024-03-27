package net.minecraft.world.item;

import java.util.OptionalInt;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.phys.Vec3;

public interface ProjectileItem {
	Projectile asProjectile(Level level, Position position, ItemStack itemStack, Direction direction);

	default ProjectileItem.DispenseConfig createDispenseConfig() {
		return ProjectileItem.DispenseConfig.DEFAULT;
	}

	default void shoot(Projectile projectile, double d, double e, double f, float g, float h) {
		projectile.shoot(d, e, f, g, h);
	}

	public static record DispenseConfig(ProjectileItem.PositionFunction positionFunction, float uncertainty, float power, OptionalInt overrideDispenseEvent) {
		public static final ProjectileItem.DispenseConfig DEFAULT = builder().build();

		public static ProjectileItem.DispenseConfig.Builder builder() {
			return new ProjectileItem.DispenseConfig.Builder();
		}

		public static class Builder {
			private ProjectileItem.PositionFunction positionFunction = (blockSource, direction) -> DispenserBlock.getDispensePosition(
					blockSource, 0.7, new Vec3(0.0, 0.1, 0.0)
				);
			private float uncertainty = 6.0F;
			private float power = 1.1F;
			private OptionalInt overrideDispenseEvent = OptionalInt.empty();

			public ProjectileItem.DispenseConfig.Builder positionFunction(ProjectileItem.PositionFunction positionFunction) {
				this.positionFunction = positionFunction;
				return this;
			}

			public ProjectileItem.DispenseConfig.Builder uncertainty(float f) {
				this.uncertainty = f;
				return this;
			}

			public ProjectileItem.DispenseConfig.Builder power(float f) {
				this.power = f;
				return this;
			}

			public ProjectileItem.DispenseConfig.Builder overrideDispenseEvent(int i) {
				this.overrideDispenseEvent = OptionalInt.of(i);
				return this;
			}

			public ProjectileItem.DispenseConfig build() {
				return new ProjectileItem.DispenseConfig(this.positionFunction, this.uncertainty, this.power, this.overrideDispenseEvent);
			}
		}
	}

	@FunctionalInterface
	public interface PositionFunction {
		Position getDispensePosition(BlockSource blockSource, Direction direction);
	}
}

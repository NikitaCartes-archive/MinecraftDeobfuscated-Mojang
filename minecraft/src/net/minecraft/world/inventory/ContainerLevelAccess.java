package net.minecraft.world.inventory;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ContainerLevelAccess {
	ContainerLevelAccess NULL = new ContainerLevelAccess() {
		@Override
		public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
			return Optional.empty();
		}
	};

	static ContainerLevelAccess create(Level level, BlockPos blockPos) {
		return new ContainerLevelAccess() {
			@Override
			public <T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction) {
				return Optional.of(biFunction.apply(level, blockPos));
			}
		};
	}

	<T> Optional<T> evaluate(BiFunction<Level, BlockPos, T> biFunction);

	default <T> T evaluate(BiFunction<Level, BlockPos, T> biFunction, T object) {
		return (T)this.evaluate(biFunction).orElse(object);
	}

	default void execute(BiConsumer<Level, BlockPos> biConsumer) {
		this.evaluate((level, blockPos) -> {
			biConsumer.accept(level, blockPos);
			return Optional.empty();
		});
	}
}

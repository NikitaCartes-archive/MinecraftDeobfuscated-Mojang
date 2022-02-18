package net.minecraft.world.level;

import java.util.function.Supplier;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;

public interface WorldGenLevel extends ServerLevelAccessor {
	long getSeed();

	default boolean ensureCanWrite(BlockPos blockPos) {
		return true;
	}

	default void setCurrentlyGenerating(@Nullable Supplier<String> supplier) {
	}
}

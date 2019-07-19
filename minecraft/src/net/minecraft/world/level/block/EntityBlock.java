package net.minecraft.world.level.block;

import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface EntityBlock {
	@Nullable
	BlockEntity newBlockEntity(BlockGetter blockGetter);
}

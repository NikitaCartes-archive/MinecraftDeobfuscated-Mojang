package net.minecraft.client.color.block;

import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public interface BlockColor {
	int getColor(BlockState blockState, @Nullable BlockAndBiomeGetter blockAndBiomeGetter, @Nullable BlockPos blockPos, int i);
}

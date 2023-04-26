package net.minecraft.world.level.chunk;

import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.lighting.ChunkSkyLightSources;

public interface LightChunk extends BlockGetter {
	void findBlockLightSources(BiConsumer<BlockPos, BlockState> biConsumer);

	ChunkSkyLightSources getSkyLightSources();
}

package net.minecraft.world.level.chunk;

import javax.annotation.Nullable;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LightLayer;

public interface LightChunkGetter {
	@Nullable
	LightChunk getChunkForLighting(int i, int j);

	default void onLightUpdate(LightLayer lightLayer, SectionPos sectionPos) {
	}

	BlockGetter getLevel();
}

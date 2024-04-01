package net.minecraft.world.grid;

import javax.annotation.Nullable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.lighting.LayerLightEventListener;
import net.minecraft.world.level.lighting.LevelLightEngine;

public class SubGridLightEngine extends LevelLightEngine {
	public static final SubGridLightEngine INSTANCE = new SubGridLightEngine();

	private SubGridLightEngine() {
		super(new LightChunkGetter() {
			@Nullable
			@Override
			public LightChunk getChunkForLighting(int i, int j) {
				return null;
			}

			@Override
			public BlockGetter getLevel() {
				return EmptyBlockGetter.INSTANCE;
			}
		}, false, false);
	}

	@Override
	public LayerLightEventListener getLayerListener(LightLayer lightLayer) {
		return lightLayer == LightLayer.BLOCK ? LayerLightEventListener.ConstantLayer.ZERO : LayerLightEventListener.ConstantLayer.FULL_BRIGHT;
	}
}

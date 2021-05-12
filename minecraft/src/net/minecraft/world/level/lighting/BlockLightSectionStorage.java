package net.minecraft.world.level.lighting;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.chunk.DataLayer;
import net.minecraft.world.level.chunk.LightChunkGetter;

public class BlockLightSectionStorage extends LayerLightSectionStorage<BlockLightSectionStorage.BlockDataLayerStorageMap> {
	protected BlockLightSectionStorage(LightChunkGetter lightChunkGetter) {
		super(LightLayer.BLOCK, lightChunkGetter, new BlockLightSectionStorage.BlockDataLayerStorageMap(new Long2ObjectOpenHashMap<>()));
	}

	@Override
	protected int getLightValue(long l) {
		long m = SectionPos.blockToSection(l);
		DataLayer dataLayer = this.getDataLayer(m, false);
		return dataLayer == null
			? 0
			: dataLayer.get(SectionPos.sectionRelative(BlockPos.getX(l)), SectionPos.sectionRelative(BlockPos.getY(l)), SectionPos.sectionRelative(BlockPos.getZ(l)));
	}

	protected static final class BlockDataLayerStorageMap extends DataLayerStorageMap<BlockLightSectionStorage.BlockDataLayerStorageMap> {
		public BlockDataLayerStorageMap(Long2ObjectOpenHashMap<DataLayer> long2ObjectOpenHashMap) {
			super(long2ObjectOpenHashMap);
		}

		public BlockLightSectionStorage.BlockDataLayerStorageMap copy() {
			return new BlockLightSectionStorage.BlockDataLayerStorageMap(this.map.clone());
		}
	}
}

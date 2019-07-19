package net.minecraft.world.level.lighting;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

public interface LightEventListener {
	default void updateSectionStatus(BlockPos blockPos, boolean bl) {
		this.updateSectionStatus(SectionPos.of(blockPos), bl);
	}

	void updateSectionStatus(SectionPos sectionPos, boolean bl);
}

package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

@Environment(EnvType.CLIENT)
public class VillageSectionsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
	private static final int MAX_RENDER_DIST_FOR_VILLAGE_SECTIONS = 60;
	private final Set<SectionPos> villageSections = Sets.<SectionPos>newHashSet();

	VillageSectionsDebugRenderer() {
	}

	@Override
	public void clear() {
		this.villageSections.clear();
	}

	public void setVillageSection(SectionPos sectionPos) {
		this.villageSections.add(sectionPos);
	}

	public void setNotVillageSection(SectionPos sectionPos) {
		this.villageSections.remove(sectionPos);
	}

	@Override
	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, double d, double e, double f) {
		BlockPos blockPos = BlockPos.containing(d, e, f);
		this.villageSections.forEach(sectionPos -> {
			if (blockPos.closerThan(sectionPos.center(), 60.0)) {
				highlightVillageSection(poseStack, multiBufferSource, sectionPos);
			}
		});
	}

	private static void highlightVillageSection(PoseStack poseStack, MultiBufferSource multiBufferSource, SectionPos sectionPos) {
		DebugRenderer.renderFilledUnitCube(poseStack, multiBufferSource, sectionPos.center(), 0.2F, 1.0F, 0.2F, 0.15F);
	}
}

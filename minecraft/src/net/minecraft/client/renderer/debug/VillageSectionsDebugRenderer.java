package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

@Environment(EnvType.CLIENT)
public class VillageSectionsDebugRenderer implements DebugRenderer.SimpleDebugRenderer {
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
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.disableTexture();
		this.doRender(d, e, f);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	private void doRender(double d, double e, double f) {
		BlockPos blockPos = new BlockPos(d, e, f);
		this.villageSections.forEach(sectionPos -> {
			if (blockPos.closerThan(sectionPos.center(), 60.0)) {
				highlightVillageSection(sectionPos);
			}
		});
	}

	private static void highlightVillageSection(SectionPos sectionPos) {
		float f = 1.0F;
		BlockPos blockPos = sectionPos.center();
		BlockPos blockPos2 = blockPos.offset(-1.0, -1.0, -1.0);
		BlockPos blockPos3 = blockPos.offset(1.0, 1.0, 1.0);
		DebugRenderer.renderFilledBox(blockPos2, blockPos3, 0.2F, 1.0F, 0.2F, 0.15F);
	}
}

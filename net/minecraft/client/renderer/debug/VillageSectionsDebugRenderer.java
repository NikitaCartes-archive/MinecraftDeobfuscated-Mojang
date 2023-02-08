/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.debug;

import com.google.common.collect.Sets;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.Set;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.debug.DebugRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;

@Environment(value=EnvType.CLIENT)
public class VillageSectionsDebugRenderer
implements DebugRenderer.SimpleDebugRenderer {
    private static final int MAX_RENDER_DIST_FOR_VILLAGE_SECTIONS = 60;
    private final Set<SectionPos> villageSections = Sets.newHashSet();

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
        BlockPos blockPos = new BlockPos(d, e, f);
        this.villageSections.forEach(sectionPos -> {
            if (blockPos.closerThan(sectionPos.center(), 60.0)) {
                VillageSectionsDebugRenderer.highlightVillageSection(poseStack, multiBufferSource, sectionPos);
            }
        });
    }

    private static void highlightVillageSection(PoseStack poseStack, MultiBufferSource multiBufferSource, SectionPos sectionPos) {
        float f = 1.0f;
        BlockPos blockPos = sectionPos.center();
        BlockPos blockPos2 = blockPos.offset(-1.0, -1.0, -1.0);
        BlockPos blockPos3 = blockPos.offset(1.0, 1.0, 1.0);
        DebugRenderer.renderFilledBox(poseStack, multiBufferSource, blockPos2, blockPos3, 0.2f, 1.0f, 0.2f, 0.15f);
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.PufferfishBigModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.PufferfishSmallModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

@Environment(value=EnvType.CLIENT)
public class PufferfishRenderer
extends MobRenderer<Pufferfish, EntityModel<Pufferfish>> {
    private static final ResourceLocation PUFFER_LOCATION = new ResourceLocation("textures/entity/fish/pufferfish.png");
    private int puffStateO = 3;
    private final PufferfishSmallModel<Pufferfish> small = new PufferfishSmallModel();
    private final PufferfishMidModel<Pufferfish> mid = new PufferfishMidModel();
    private final PufferfishBigModel<Pufferfish> big = new PufferfishBigModel();

    public PufferfishRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new PufferfishBigModel(), 0.2f);
    }

    @Override
    public ResourceLocation getTextureLocation(Pufferfish pufferfish) {
        return PUFFER_LOCATION;
    }

    @Override
    public void render(Pufferfish pufferfish, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        int j = pufferfish.getPuffState();
        if (j != this.puffStateO) {
            this.model = j == 0 ? this.small : (j == 1 ? this.mid : this.big);
        }
        this.puffStateO = j;
        this.shadowRadius = 0.1f + 0.1f * (float)j;
        super.render(pufferfish, f, g, poseStack, multiBufferSource, i);
    }

    @Override
    protected void setupRotations(Pufferfish pufferfish, PoseStack poseStack, float f, float g, float h) {
        poseStack.translate(0.0, Mth.cos(f * 0.05f) * 0.08f, 0.0);
        super.setupRotations(pufferfish, poseStack, f, g, h);
    }
}


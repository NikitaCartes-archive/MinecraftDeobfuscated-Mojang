/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.IllagerRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.phys.Vec3;

@Environment(value=EnvType.CLIENT)
public class IllusionerRenderer
extends IllagerRenderer<Illusioner> {
    private static final ResourceLocation ILLUSIONER = new ResourceLocation("textures/entity/illager/illusioner.png");

    public IllusionerRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher, new IllagerModel(0.0f, 0.0f, 64, 64), 0.5f);
        this.addLayer(new ItemInHandLayer<Illusioner, IllagerModel<Illusioner>>((RenderLayerParent)this){

            @Override
            public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, Illusioner illusioner, float f, float g, float h, float j, float k, float l, float m) {
                if (illusioner.isCastingSpell() || illusioner.isAggressive()) {
                    super.render(poseStack, multiBufferSource, i, illusioner, f, g, h, j, k, l, m);
                }
            }
        });
        ((IllagerModel)this.model).getHat().visible = true;
    }

    @Override
    public ResourceLocation getTextureLocation(Illusioner illusioner) {
        return ILLUSIONER;
    }

    @Override
    public void render(Illusioner illusioner, double d, double e, double f, float g, float h, PoseStack poseStack, MultiBufferSource multiBufferSource) {
        if (illusioner.isInvisible()) {
            Vec3[] vec3s = illusioner.getIllusionOffsets(h);
            float i = this.getBob(illusioner, h);
            for (int j = 0; j < vec3s.length; ++j) {
                poseStack.pushPose();
                poseStack.translate(vec3s[j].x + (double)Mth.cos((float)j + i * 0.5f) * 0.025, vec3s[j].y + (double)Mth.cos((float)j + i * 0.75f) * 0.0125, vec3s[j].z + (double)Mth.cos((float)j + i * 0.7f) * 0.025);
                super.render(illusioner, d, e, f, g, h, poseStack, multiBufferSource);
                poseStack.popPose();
            }
        } else {
            super.render(illusioner, d, e, f, g, h, poseStack, multiBufferSource);
        }
    }

    @Override
    protected boolean isVisible(Illusioner illusioner, boolean bl) {
        return true;
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.LlamaSpitModel;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.projectile.LlamaSpit;

@Environment(value=EnvType.CLIENT)
public class LlamaSpitRenderer
extends EntityRenderer<LlamaSpit> {
    private static final ResourceLocation LLAMA_SPIT_LOCATION = new ResourceLocation("textures/entity/llama/spit.png");
    private final LlamaSpitModel<LlamaSpit> model = new LlamaSpitModel();

    public LlamaSpitRenderer(EntityRenderDispatcher entityRenderDispatcher) {
        super(entityRenderDispatcher);
    }

    @Override
    public void render(LlamaSpit llamaSpit, double d, double e, double f, float g, float h) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d, (float)e + 0.15f, (float)f);
        RenderSystem.rotatef(Mth.lerp(h, llamaSpit.yRotO, llamaSpit.yRot) - 90.0f, 0.0f, 1.0f, 0.0f);
        RenderSystem.rotatef(Mth.lerp(h, llamaSpit.xRotO, llamaSpit.xRot), 0.0f, 0.0f, 1.0f);
        this.bindTexture(llamaSpit);
        if (this.solidRender) {
            RenderSystem.enableColorMaterial();
            RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(llamaSpit));
        }
        this.model.render(llamaSpit, h, 0.0f, -0.1f, 0.0f, 0.0f, 0.0625f);
        if (this.solidRender) {
            RenderSystem.tearDownSolidRenderingTextureCombine();
            RenderSystem.disableColorMaterial();
        }
        RenderSystem.popMatrix();
        super.render(llamaSpit, d, e, f, g, h);
    }

    @Override
    protected ResourceLocation getTextureLocation(LlamaSpit llamaSpit) {
        return LLAMA_SPIT_LOCATION;
    }
}


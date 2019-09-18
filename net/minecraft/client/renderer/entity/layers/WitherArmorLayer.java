/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.WitherBossModel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

@Environment(value=EnvType.CLIENT)
public class WitherArmorLayer
extends RenderLayer<WitherBoss, WitherBossModel<WitherBoss>> {
    private static final ResourceLocation WITHER_ARMOR_LOCATION = new ResourceLocation("textures/entity/wither/wither_armor.png");
    private final WitherBossModel<WitherBoss> model = new WitherBossModel(0.5f);

    public WitherArmorLayer(RenderLayerParent<WitherBoss, WitherBossModel<WitherBoss>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(WitherBoss witherBoss, float f, float g, float h, float i, float j, float k, float l) {
        if (!witherBoss.isPowered()) {
            return;
        }
        RenderSystem.depthMask(!witherBoss.isInvisible());
        this.bindTexture(WITHER_ARMOR_LOCATION);
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float m = (float)witherBoss.tickCount + h;
        float n = Mth.cos(m * 0.02f) * 3.0f;
        float o = m * 0.01f;
        RenderSystem.translatef(n, o, 0.0f);
        RenderSystem.matrixMode(5888);
        RenderSystem.enableBlend();
        float p = 0.5f;
        RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);
        RenderSystem.disableLighting();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        this.model.prepareMobModel(witherBoss, f, g, h);
        ((WitherBossModel)this.getParentModel()).copyPropertiesTo(this.model);
        FogRenderer.resetFogColor(true);
        this.model.render(witherBoss, f, g, i, j, k, l);
        FogRenderer.resetFogColor(false);
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        RenderSystem.matrixMode(5888);
        RenderSystem.enableLighting();
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}


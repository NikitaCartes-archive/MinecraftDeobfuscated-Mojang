/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CreeperModel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Creeper;

@Environment(value=EnvType.CLIENT)
public class CreeperPowerLayer
extends RenderLayer<Creeper, CreeperModel<Creeper>> {
    private static final ResourceLocation POWER_LOCATION = new ResourceLocation("textures/entity/creeper/creeper_armor.png");
    private final CreeperModel<Creeper> model = new CreeperModel(2.0f);

    public CreeperPowerLayer(RenderLayerParent<Creeper, CreeperModel<Creeper>> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(Creeper creeper, float f, float g, float h, float i, float j, float k, float l) {
        if (!creeper.isPowered()) {
            return;
        }
        boolean bl = creeper.isInvisible();
        GlStateManager.depthMask(!bl);
        this.bindTexture(POWER_LOCATION);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        float m = (float)creeper.tickCount + h;
        GlStateManager.translatef(m * 0.01f, m * 0.01f, 0.0f);
        GlStateManager.matrixMode(5888);
        GlStateManager.enableBlend();
        float n = 0.5f;
        GlStateManager.color4f(0.5f, 0.5f, 0.5f, 1.0f);
        GlStateManager.disableLighting();
        GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        ((CreeperModel)this.getParentModel()).copyPropertiesTo(this.model);
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.resetFogColor(true);
        this.model.render(creeper, f, g, i, j, k, l);
        gameRenderer.resetFogColor(false);
        GlStateManager.matrixMode(5890);
        GlStateManager.loadIdentity();
        GlStateManager.matrixMode(5888);
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.depthMask(true);
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}


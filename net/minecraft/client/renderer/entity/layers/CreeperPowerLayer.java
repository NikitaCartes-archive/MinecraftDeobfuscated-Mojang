/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
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
        RenderSystem.depthMask(!bl);
        this.bindTexture(POWER_LOCATION);
        RenderSystem.matrixMode(5890);
        RenderSystem.loadIdentity();
        float m = (float)creeper.tickCount + h;
        RenderSystem.translatef(m * 0.01f, m * 0.01f, 0.0f);
        RenderSystem.matrixMode(5888);
        RenderSystem.enableBlend();
        float n = 0.5f;
        RenderSystem.color4f(0.5f, 0.5f, 0.5f, 1.0f);
        RenderSystem.disableLighting();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        ((CreeperModel)this.getParentModel()).copyPropertiesTo(this.model);
        GameRenderer gameRenderer = Minecraft.getInstance().gameRenderer;
        gameRenderer.resetFogColor(true);
        this.model.render(creeper, f, g, i, j, k, l);
        gameRenderer.resetFogColor(false);
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


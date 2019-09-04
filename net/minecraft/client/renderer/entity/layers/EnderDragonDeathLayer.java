/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.dragon.DragonModel;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;

@Environment(value=EnvType.CLIENT)
public class EnderDragonDeathLayer
extends RenderLayer<EnderDragon, DragonModel> {
    public EnderDragonDeathLayer(RenderLayerParent<EnderDragon, DragonModel> renderLayerParent) {
        super(renderLayerParent);
    }

    @Override
    public void render(EnderDragon enderDragon, float f, float g, float h, float i, float j, float k, float l) {
        if (enderDragon.dragonDeathTime <= 0) {
            return;
        }
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();
        Lighting.turnOff();
        float m = ((float)enderDragon.dragonDeathTime + h) / 200.0f;
        float n = 0.0f;
        if (m > 0.8f) {
            n = (m - 0.8f) / 0.2f;
        }
        Random random = new Random(432L);
        RenderSystem.disableTexture();
        RenderSystem.shadeModel(7425);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.disableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.depthMask(false);
        RenderSystem.pushMatrix();
        RenderSystem.translatef(0.0f, -1.0f, -2.0f);
        int o = 0;
        while ((float)o < (m + m * m) / 2.0f * 60.0f) {
            RenderSystem.rotatef(random.nextFloat() * 360.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(random.nextFloat() * 360.0f, 0.0f, 1.0f, 0.0f);
            RenderSystem.rotatef(random.nextFloat() * 360.0f, 0.0f, 0.0f, 1.0f);
            RenderSystem.rotatef(random.nextFloat() * 360.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.rotatef(random.nextFloat() * 360.0f, 0.0f, 1.0f, 0.0f);
            RenderSystem.rotatef(random.nextFloat() * 360.0f + m * 90.0f, 0.0f, 0.0f, 1.0f);
            float p = random.nextFloat() * 20.0f + 5.0f + n * 10.0f;
            float q = random.nextFloat() * 2.0f + 1.0f + n * 2.0f;
            bufferBuilder.begin(6, DefaultVertexFormat.POSITION_COLOR);
            bufferBuilder.vertex(0.0, 0.0, 0.0).color(255, 255, 255, (int)(255.0f * (1.0f - n))).endVertex();
            bufferBuilder.vertex(-0.866 * (double)q, p, -0.5f * q).color(255, 0, 255, 0).endVertex();
            bufferBuilder.vertex(0.866 * (double)q, p, -0.5f * q).color(255, 0, 255, 0).endVertex();
            bufferBuilder.vertex(0.0, p, 1.0f * q).color(255, 0, 255, 0).endVertex();
            bufferBuilder.vertex(-0.866 * (double)q, p, -0.5f * q).color(255, 0, 255, 0).endVertex();
            tesselator.end();
            ++o;
        }
        RenderSystem.popMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableCull();
        RenderSystem.disableBlend();
        RenderSystem.shadeModel(7424);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.enableTexture();
        RenderSystem.enableAlphaTest();
        Lighting.turnOn();
    }

    @Override
    public boolean colorsOnDamage() {
        return false;
    }
}


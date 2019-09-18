/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;

@Environment(value=EnvType.CLIENT)
public class SpawnerRenderer
extends BlockEntityRenderer<SpawnerBlockEntity> {
    @Override
    public void render(SpawnerBlockEntity spawnerBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef((float)d + 0.5f, (float)e, (float)f + 0.5f);
        SpawnerRenderer.render(spawnerBlockEntity.getSpawner(), d, e, f, g);
        RenderSystem.popMatrix();
    }

    public static void render(BaseSpawner baseSpawner, double d, double e, double f, float g) {
        Entity entity = baseSpawner.getOrCreateDisplayEntity();
        if (entity != null) {
            float h = 0.53125f;
            float i = Math.max(entity.getBbWidth(), entity.getBbHeight());
            if ((double)i > 1.0) {
                h /= i;
            }
            RenderSystem.translatef(0.0f, 0.4f, 0.0f);
            RenderSystem.rotatef((float)Mth.lerp((double)g, baseSpawner.getoSpin(), baseSpawner.getSpin()) * 10.0f, 0.0f, 1.0f, 0.0f);
            RenderSystem.translatef(0.0f, -0.2f, 0.0f);
            RenderSystem.rotatef(-30.0f, 1.0f, 0.0f, 0.0f);
            RenderSystem.scalef(h, h, h);
            entity.moveTo(d, e, f, 0.0f, 0.0f);
            Minecraft.getInstance().getEntityRenderDispatcher().render(entity, 0.0, 0.0, 0.0, 0.0f, g, false);
        }
    }
}


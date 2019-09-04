/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BellModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BellRenderer
extends BlockEntityRenderer<BellBlockEntity> {
    private static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("textures/entity/bell/bell_body.png");
    private final BellModel bellModel = new BellModel();

    @Override
    public void render(BellBlockEntity bellBlockEntity, double d, double e, double f, float g, int i) {
        RenderSystem.pushMatrix();
        RenderSystem.enableRescaleNormal();
        this.bindTexture(BELL_RESOURCE_LOCATION);
        RenderSystem.translatef((float)d, (float)e, (float)f);
        float h = (float)bellBlockEntity.ticks + g;
        float j = 0.0f;
        float k = 0.0f;
        if (bellBlockEntity.shaking) {
            float l = Mth.sin(h / (float)Math.PI) / (4.0f + h / 3.0f);
            if (bellBlockEntity.clickDirection == Direction.NORTH) {
                j = -l;
            } else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
                j = l;
            } else if (bellBlockEntity.clickDirection == Direction.EAST) {
                k = -l;
            } else if (bellBlockEntity.clickDirection == Direction.WEST) {
                k = l;
            }
        }
        this.bellModel.render(j, k, 0.0625f);
        RenderSystem.color4f(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.popMatrix();
    }
}


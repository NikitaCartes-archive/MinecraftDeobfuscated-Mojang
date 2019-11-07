/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.entity.BellBlockEntity;

@Environment(value=EnvType.CLIENT)
public class BellRenderer
extends BlockEntityRenderer<BellBlockEntity> {
    public static final ResourceLocation BELL_RESOURCE_LOCATION = new ResourceLocation("entity/bell/bell_body");
    private final ModelPart bellBody = new ModelPart(32, 32, 0, 0);

    public BellRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
        this.bellBody.addBox(-3.0f, -6.0f, -3.0f, 6.0f, 7.0f, 6.0f);
        this.bellBody.setPos(8.0f, 12.0f, 8.0f);
        ModelPart modelPart = new ModelPart(32, 32, 0, 13);
        modelPart.addBox(4.0f, 4.0f, 4.0f, 8.0f, 2.0f, 8.0f);
        modelPart.setPos(-8.0f, -12.0f, -8.0f);
        this.bellBody.addChild(modelPart);
    }

    @Override
    public void render(BellBlockEntity bellBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float g = (float)bellBlockEntity.ticks + f;
        float h = 0.0f;
        float k = 0.0f;
        if (bellBlockEntity.shaking) {
            float l = Mth.sin(g / (float)Math.PI) / (4.0f + g / 3.0f);
            if (bellBlockEntity.clickDirection == Direction.NORTH) {
                h = -l;
            } else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
                h = l;
            } else if (bellBlockEntity.clickDirection == Direction.EAST) {
                k = -l;
            } else if (bellBlockEntity.clickDirection == Direction.WEST) {
                k = l;
            }
        }
        this.bellBody.xRot = h;
        this.bellBody.zRot = k;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.bellBody.render(poseStack, vertexConsumer, i, j, this.getSprite(BELL_RESOURCE_LOCATION));
    }
}


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
    public void render(BellBlockEntity bellBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float h = (float)bellBlockEntity.ticks + g;
        float k = 0.0f;
        float l = 0.0f;
        if (bellBlockEntity.shaking) {
            float m = Mth.sin(h / (float)Math.PI) / (4.0f + h / 3.0f);
            if (bellBlockEntity.clickDirection == Direction.NORTH) {
                k = -m;
            } else if (bellBlockEntity.clickDirection == Direction.SOUTH) {
                k = m;
            } else if (bellBlockEntity.clickDirection == Direction.EAST) {
                l = -m;
            } else if (bellBlockEntity.clickDirection == Direction.WEST) {
                l = m;
            }
        }
        this.bellBody.xRot = k;
        this.bellBody.zRot = l;
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.bellBody.render(poseStack, vertexConsumer, 0.0625f, i, j, this.getSprite(BELL_RESOURCE_LOCATION));
    }
}


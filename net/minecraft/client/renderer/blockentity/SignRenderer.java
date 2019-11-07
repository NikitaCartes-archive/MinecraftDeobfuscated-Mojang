/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.ComponentRenderUtils;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(value=EnvType.CLIENT)
public class SignRenderer
extends BlockEntityRenderer<SignBlockEntity> {
    private final SignModel signModel = new SignModel();

    public SignRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(SignBlockEntity signBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float h;
        BlockState blockState = signBlockEntity.getBlockState();
        poseStack.pushPose();
        float g = 0.6666667f;
        if (blockState.getBlock() instanceof StandingSignBlock) {
            poseStack.translate(0.5, 0.5, 0.5);
            h = -((float)(blockState.getValue(StandingSignBlock.ROTATION) * 360) / 16.0f);
            poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
            this.signModel.stick.visible = true;
        } else {
            poseStack.translate(0.5, 0.5, 0.5);
            h = -blockState.getValue(WallSignBlock.FACING).toYRot();
            poseStack.mulPose(Vector3f.YP.rotationDegrees(h));
            poseStack.translate(0.0, -0.3125, -0.4375);
            this.signModel.stick.visible = false;
        }
        TextureAtlasSprite textureAtlasSprite = this.getSprite(SignRenderer.getTexture(blockState.getBlock()));
        poseStack.pushPose();
        poseStack.scale(0.6666667f, -0.6666667f, -0.6666667f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.entitySolid(TextureAtlas.LOCATION_BLOCKS));
        this.signModel.sign.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        this.signModel.stick.render(poseStack, vertexConsumer, i, j, textureAtlasSprite);
        poseStack.popPose();
        Font font = this.renderer.getFont();
        float k = 0.010416667f;
        poseStack.translate(0.0, 0.3333333432674408, 0.046666666865348816);
        poseStack.scale(0.010416667f, -0.010416667f, 0.010416667f);
        int l = signBlockEntity.getColor().getTextColor();
        for (int m = 0; m < 4; ++m) {
            String string = signBlockEntity.getRenderMessage(m, component -> {
                List<Component> list = ComponentRenderUtils.wrapComponents(component, 90, font, false, true);
                return list.isEmpty() ? "" : list.get(0).getColoredString();
            });
            if (string == null) continue;
            float n = -font.width(string) / 2;
            font.drawInBatch(string, n, m * 10 - signBlockEntity.messages.length * 5, l, false, poseStack.last().pose(), multiBufferSource, false, 0, i);
        }
        poseStack.popPose();
    }

    public static ResourceLocation getTexture(Block block) {
        if (block == Blocks.OAK_SIGN || block == Blocks.OAK_WALL_SIGN) {
            return ModelBakery.OAK_SIGN_TEXTURE;
        }
        if (block == Blocks.SPRUCE_SIGN || block == Blocks.SPRUCE_WALL_SIGN) {
            return ModelBakery.SPRUCE_SIGN_TEXTURE;
        }
        if (block == Blocks.BIRCH_SIGN || block == Blocks.BIRCH_WALL_SIGN) {
            return ModelBakery.BIRCH_SIGN_TEXTURE;
        }
        if (block == Blocks.ACACIA_SIGN || block == Blocks.ACACIA_WALL_SIGN) {
            return ModelBakery.ACACIA_SIGN_TEXTURE;
        }
        if (block == Blocks.JUNGLE_SIGN || block == Blocks.JUNGLE_WALL_SIGN) {
            return ModelBakery.JUNGLE_SIGN_TEXTURE;
        }
        if (block == Blocks.DARK_OAK_SIGN || block == Blocks.DARK_OAK_WALL_SIGN) {
            return ModelBakery.DARK_OAK_SIGN_TEXTURE;
        }
        return ModelBakery.OAK_SIGN_TEXTURE;
    }

    @Environment(value=EnvType.CLIENT)
    public static final class SignModel
    extends Model {
        public final ModelPart sign = new ModelPart(64, 32, 0, 0);
        public final ModelPart stick;

        public SignModel() {
            super(RenderType::entityCutoutNoCull);
            this.sign.addBox(-12.0f, -14.0f, -1.0f, 24.0f, 12.0f, 2.0f, 0.0f);
            this.stick = new ModelPart(64, 32, 0, 14);
            this.stick.addBox(-1.0f, -2.0f, -1.0f, 2.0f, 14.0f, 2.0f, 0.0f);
        }

        @Override
        public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int i, int j, float f, float g, float h) {
            this.sign.render(poseStack, vertexConsumer, i, j, null, f, g, h);
            this.stick.render(poseStack, vertexConsumer, i, j, null, f, g, h);
        }
    }
}


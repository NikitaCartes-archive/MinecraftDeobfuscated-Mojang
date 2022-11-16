/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PiglinHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkullBlockRenderer
implements BlockEntityRenderer<SkullBlockEntity> {
    private final Map<SkullBlock.Type, SkullModelBase> modelByType;
    private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
        hashMap.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
        hashMap.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
        hashMap.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
        hashMap.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
        hashMap.put(SkullBlock.Types.PIGLIN, new ResourceLocation("textures/entity/piglin/piglin.png"));
        hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
    });

    public static Map<SkullBlock.Type, SkullModelBase> createSkullRenderers(EntityModelSet entityModelSet) {
        ImmutableMap.Builder<SkullBlock.Types, SkullModelBase> builder = ImmutableMap.builder();
        builder.put(SkullBlock.Types.SKELETON, new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
        builder.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(entityModelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
        builder.put(SkullBlock.Types.PLAYER, new SkullModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_HEAD)));
        builder.put(SkullBlock.Types.ZOMBIE, new SkullModel(entityModelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
        builder.put(SkullBlock.Types.CREEPER, new SkullModel(entityModelSet.bakeLayer(ModelLayers.CREEPER_HEAD)));
        builder.put(SkullBlock.Types.DRAGON, new DragonHeadModel(entityModelSet.bakeLayer(ModelLayers.DRAGON_SKULL)));
        builder.put(SkullBlock.Types.PIGLIN, new PiglinHeadModel(entityModelSet.bakeLayer(ModelLayers.PIGLIN_HEAD)));
        return builder.build();
    }

    public SkullBlockRenderer(BlockEntityRendererProvider.Context context) {
        this.modelByType = SkullBlockRenderer.createSkullRenderers(context.getModelSet());
    }

    @Override
    public void render(SkullBlockEntity skullBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float g = skullBlockEntity.getAnimation(f);
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        int k = bl ? RotationSegment.convertToSegment(direction) : blockState.getValue(SkullBlock.ROTATION);
        float h = RotationSegment.convertToDegrees(k);
        SkullBlock.Type type = ((AbstractSkullBlock)blockState.getBlock()).getType();
        SkullModelBase skullModelBase = this.modelByType.get(type);
        RenderType renderType = SkullBlockRenderer.getRenderType(type, skullBlockEntity.getOwnerProfile());
        SkullBlockRenderer.renderSkull(direction, h, g, poseStack, multiBufferSource, i, skullModelBase, renderType);
    }

    public static void renderSkull(@Nullable Direction direction, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, SkullModelBase skullModelBase, RenderType renderType) {
        poseStack.pushPose();
        if (direction == null) {
            poseStack.translate(0.5f, 0.0f, 0.5f);
        } else {
            float h = 0.25f;
            poseStack.translate(0.5f - (float)direction.getStepX() * 0.25f, 0.25f, 0.5f - (float)direction.getStepZ() * 0.25f);
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
        skullModelBase.setupAnim(g, f, 0.0f);
        skullModelBase.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    public static RenderType getRenderType(SkullBlock.Type type, @Nullable GameProfile gameProfile) {
        ResourceLocation resourceLocation = SKIN_BY_TYPE.get(type);
        if (type != SkullBlock.Types.PLAYER || gameProfile == null) {
            return RenderType.entityCutoutNoCullZOffset(resourceLocation);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        if (map.containsKey((Object)MinecraftProfileTexture.Type.SKIN)) {
            return RenderType.entityTranslucent(minecraft.getSkinManager().registerTexture(map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN));
        }
        return RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(UUIDUtil.getOrCreatePlayerUUID(gameProfile)));
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

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
import net.minecraft.client.model.HumanoidHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.WallSkullBlock;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class SkullBlockRenderer
extends BlockEntityRenderer<SkullBlockEntity> {
    private static final Map<SkullBlock.Type, SkullModel> MODEL_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        SkullModel skullModel = new SkullModel(0, 0, 64, 32);
        HumanoidHeadModel skullModel2 = new HumanoidHeadModel();
        DragonHeadModel dragonHeadModel = new DragonHeadModel(0.0f);
        hashMap.put(SkullBlock.Types.SKELETON, skullModel);
        hashMap.put(SkullBlock.Types.WITHER_SKELETON, skullModel);
        hashMap.put(SkullBlock.Types.PLAYER, skullModel2);
        hashMap.put(SkullBlock.Types.ZOMBIE, skullModel2);
        hashMap.put(SkullBlock.Types.CREEPER, skullModel);
        hashMap.put(SkullBlock.Types.DRAGON, dragonHeadModel);
    });
    private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
        hashMap.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
        hashMap.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
        hashMap.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
        hashMap.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
        hashMap.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
        hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
    });

    public SkullBlockRenderer(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super(blockEntityRenderDispatcher);
    }

    @Override
    public void render(SkullBlockEntity skullBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
        float h = skullBlockEntity.getMouthAnimation(g);
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        float k = 22.5f * (float)(bl ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        SkullBlockRenderer.renderSkull(direction, k, ((AbstractSkullBlock)blockState.getBlock()).getType(), skullBlockEntity.getOwnerProfile(), h, poseStack, multiBufferSource, i);
    }

    public static void renderSkull(@Nullable Direction direction, float f, SkullBlock.Type type, @Nullable GameProfile gameProfile, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
        SkullModel skullModel = MODEL_BY_TYPE.get(type);
        poseStack.pushPose();
        if (direction == null) {
            poseStack.translate(0.5, 0.0, 0.5);
        } else {
            switch (direction) {
                case NORTH: {
                    poseStack.translate(0.5, 0.25, 0.74f);
                    break;
                }
                case SOUTH: {
                    poseStack.translate(0.5, 0.25, 0.26f);
                    break;
                }
                case WEST: {
                    poseStack.translate(0.74f, 0.25, 0.5);
                    break;
                }
                default: {
                    poseStack.translate(0.26f, 0.25, 0.5);
                }
            }
        }
        poseStack.scale(-1.0f, -1.0f, 1.0f);
        VertexConsumer vertexConsumer = multiBufferSource.getBuffer(SkullBlockRenderer.getRenderType(type, gameProfile));
        skullModel.setupAnim(g, f, 0.0f);
        skullModel.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0f, 1.0f, 1.0f);
        poseStack.popPose();
    }

    private static RenderType getRenderType(SkullBlock.Type type, @Nullable GameProfile gameProfile) {
        ResourceLocation resourceLocation = SKIN_BY_TYPE.get(type);
        if (type != SkullBlock.Types.PLAYER || gameProfile == null) {
            return RenderType.entityCutout(resourceLocation);
        }
        Minecraft minecraft = Minecraft.getInstance();
        Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
        if (map.containsKey((Object)MinecraftProfileTexture.Type.SKIN)) {
            return RenderType.entityTranslucent(minecraft.getSkinManager().registerTexture(map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN));
        }
        return RenderType.entityCutout(DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(gameProfile)));
    }
}


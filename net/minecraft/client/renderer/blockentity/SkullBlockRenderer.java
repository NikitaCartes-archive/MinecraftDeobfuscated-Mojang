/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Map;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
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
    public static SkullBlockRenderer instance;
    private static final Map<SkullBlock.Type, SkullModel> MODEL_BY_TYPE;
    private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE;

    @Override
    public void render(SkullBlockEntity skullBlockEntity, double d, double e, double f, float g, int i, RenderType renderType) {
        float h = skullBlockEntity.getMouthAnimation(g);
        BlockState blockState = skullBlockEntity.getBlockState();
        boolean bl = blockState.getBlock() instanceof WallSkullBlock;
        Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
        float j = 22.5f * (float)(bl ? (2 + direction.get2DDataValue()) * 4 : blockState.getValue(SkullBlock.ROTATION));
        this.renderSkull((float)d, (float)e, (float)f, direction, j, ((AbstractSkullBlock)blockState.getBlock()).getType(), skullBlockEntity.getOwnerProfile(), i, h);
    }

    @Override
    public void init(BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
        super.init(blockEntityRenderDispatcher);
        instance = this;
    }

    public void renderSkull(float f, float g, float h, @Nullable Direction direction, float i, SkullBlock.Type type, @Nullable GameProfile gameProfile, int j, float k) {
        SkullModel skullModel = MODEL_BY_TYPE.get(type);
        if (j >= 0) {
            this.bindTexture((ResourceLocation)BREAKING_LOCATIONS.get(j));
            RenderSystem.matrixMode(5890);
            RenderSystem.pushMatrix();
            RenderSystem.scalef(4.0f, 2.0f, 1.0f);
            RenderSystem.translatef(0.0625f, 0.0625f, 0.0625f);
            RenderSystem.matrixMode(5888);
        } else {
            this.bindTexture(this.getLocation(type, gameProfile));
        }
        RenderSystem.pushMatrix();
        if (direction == null) {
            RenderSystem.translatef(f + 0.5f, g, h + 0.5f);
        } else {
            switch (direction) {
                case NORTH: {
                    RenderSystem.translatef(f + 0.5f, g + 0.25f, h + 0.74f);
                    break;
                }
                case SOUTH: {
                    RenderSystem.translatef(f + 0.5f, g + 0.25f, h + 0.26f);
                    break;
                }
                case WEST: {
                    RenderSystem.translatef(f + 0.74f, g + 0.25f, h + 0.5f);
                    break;
                }
                default: {
                    RenderSystem.translatef(f + 0.26f, g + 0.25f, h + 0.5f);
                }
            }
        }
        RenderSystem.enableRescaleNormal();
        RenderSystem.scalef(-1.0f, -1.0f, 1.0f);
        RenderSystem.enableAlphaTest();
        if (type == SkullBlock.Types.PLAYER) {
            RenderSystem.setProfile(RenderSystem.Profile.PLAYER_SKIN);
        }
        skullModel.render(k, 0.0f, 0.0f, i, 0.0f, 0.0625f);
        RenderSystem.popMatrix();
        if (j >= 0) {
            RenderSystem.matrixMode(5890);
            RenderSystem.popMatrix();
            RenderSystem.matrixMode(5888);
        }
    }

    private ResourceLocation getLocation(SkullBlock.Type type, @Nullable GameProfile gameProfile) {
        ResourceLocation resourceLocation = SKIN_BY_TYPE.get(type);
        if (type == SkullBlock.Types.PLAYER && gameProfile != null) {
            Minecraft minecraft = Minecraft.getInstance();
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
            resourceLocation = map.containsKey((Object)MinecraftProfileTexture.Type.SKIN) ? minecraft.getSkinManager().registerTexture(map.get((Object)MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN) : DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(gameProfile));
        }
        return resourceLocation;
    }

    static {
        MODEL_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
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
        SKIN_BY_TYPE = Util.make(Maps.newHashMap(), hashMap -> {
            hashMap.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
            hashMap.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
            hashMap.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
            hashMap.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
            hashMap.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
            hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
        });
    }
}


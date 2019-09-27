package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.Map;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidHeadModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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

@Environment(EnvType.CLIENT)
public class SkullBlockRenderer extends BlockEntityRenderer<SkullBlockEntity> {
	private static final Map<SkullBlock.Type, SkullModel> MODEL_BY_TYPE = Util.make(Maps.<SkullBlock.Type, SkullModel>newHashMap(), hashMap -> {
		SkullModel skullModel = new SkullModel(0, 0, 64, 32);
		SkullModel skullModel2 = new HumanoidHeadModel();
		DragonHeadModel dragonHeadModel = new DragonHeadModel(0.0F);
		hashMap.put(SkullBlock.Types.SKELETON, skullModel);
		hashMap.put(SkullBlock.Types.WITHER_SKELETON, skullModel);
		hashMap.put(SkullBlock.Types.PLAYER, skullModel2);
		hashMap.put(SkullBlock.Types.ZOMBIE, skullModel2);
		hashMap.put(SkullBlock.Types.CREEPER, skullModel);
		hashMap.put(SkullBlock.Types.DRAGON, dragonHeadModel);
	});
	private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.<SkullBlock.Type, ResourceLocation>newHashMap(), hashMap -> {
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

	public void render(SkullBlockEntity skullBlockEntity, double d, double e, double f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		float h = skullBlockEntity.getMouthAnimation(g);
		BlockState blockState = skullBlockEntity.getBlockState();
		boolean bl = blockState.getBlock() instanceof WallSkullBlock;
		Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
		float j = 22.5F * (float)(bl ? (2 + direction.get2DDataValue()) * 4 : (Integer)blockState.getValue(SkullBlock.ROTATION));
		renderSkull(direction, j, ((AbstractSkullBlock)blockState.getBlock()).getType(), skullBlockEntity.getOwnerProfile(), h, poseStack, multiBufferSource, i);
	}

	public static void renderSkull(
		@Nullable Direction direction,
		float f,
		SkullBlock.Type type,
		@Nullable GameProfile gameProfile,
		float g,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i
	) {
		SkullModel skullModel = (SkullModel)MODEL_BY_TYPE.get(type);
		poseStack.pushPose();
		if (direction == null) {
			poseStack.translate(0.5, 0.0, 0.5);
		} else {
			switch (direction) {
				case NORTH:
					poseStack.translate(0.5, 0.25, 0.74F);
					break;
				case SOUTH:
					poseStack.translate(0.5, 0.25, 0.26F);
					break;
				case WEST:
					poseStack.translate(0.74F, 0.25, 0.5);
					break;
				case EAST:
				default:
					poseStack.translate(0.26F, 0.25, 0.5);
			}
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(RenderType.NEW_ENTITY(getLocation(type, gameProfile)));
		OverlayTexture.setDefault(vertexConsumer);
		skullModel.render(poseStack, vertexConsumer, g, f, 0.0F, 0.0625F, i);
		vertexConsumer.unsetDefaultOverlayCoords();
		poseStack.popPose();
	}

	private static ResourceLocation getLocation(SkullBlock.Type type, @Nullable GameProfile gameProfile) {
		ResourceLocation resourceLocation = (ResourceLocation)SKIN_BY_TYPE.get(type);
		if (type == SkullBlock.Types.PLAYER && gameProfile != null) {
			Minecraft minecraft = Minecraft.getInstance();
			Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
			if (map.containsKey(Type.SKIN)) {
				resourceLocation = minecraft.getSkinManager().registerTexture((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN);
			} else {
				resourceLocation = DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(gameProfile));
			}
		}

		return resourceLocation;
	}
}

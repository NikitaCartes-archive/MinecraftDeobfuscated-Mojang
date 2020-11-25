package net.minecraft.client.renderer.blockentity;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.ImmutableMap.Builder;
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
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.dragon.DragonHeadModel;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
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
public class SkullBlockRenderer implements BlockEntityRenderer<SkullBlockEntity> {
	private final Map<SkullBlock.Type, SkullModelBase> modelByType;
	private static final Map<SkullBlock.Type, ResourceLocation> SKIN_BY_TYPE = Util.make(Maps.<SkullBlock.Type, ResourceLocation>newHashMap(), hashMap -> {
		hashMap.put(SkullBlock.Types.SKELETON, new ResourceLocation("textures/entity/skeleton/skeleton.png"));
		hashMap.put(SkullBlock.Types.WITHER_SKELETON, new ResourceLocation("textures/entity/skeleton/wither_skeleton.png"));
		hashMap.put(SkullBlock.Types.ZOMBIE, new ResourceLocation("textures/entity/zombie/zombie.png"));
		hashMap.put(SkullBlock.Types.CREEPER, new ResourceLocation("textures/entity/creeper/creeper.png"));
		hashMap.put(SkullBlock.Types.DRAGON, new ResourceLocation("textures/entity/enderdragon/dragon.png"));
		hashMap.put(SkullBlock.Types.PLAYER, DefaultPlayerSkin.getDefaultSkin());
	});

	public static Map<SkullBlock.Type, SkullModelBase> createSkullRenderers(EntityModelSet entityModelSet) {
		Builder<SkullBlock.Type, SkullModelBase> builder = ImmutableMap.builder();
		builder.put(SkullBlock.Types.SKELETON, new SkullModel(entityModelSet.bakeLayer(ModelLayers.SKELETON_SKULL)));
		builder.put(SkullBlock.Types.WITHER_SKELETON, new SkullModel(entityModelSet.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL)));
		builder.put(SkullBlock.Types.PLAYER, new SkullModel(entityModelSet.bakeLayer(ModelLayers.PLAYER_HEAD)));
		builder.put(SkullBlock.Types.ZOMBIE, new SkullModel(entityModelSet.bakeLayer(ModelLayers.ZOMBIE_HEAD)));
		builder.put(SkullBlock.Types.CREEPER, new SkullModel(entityModelSet.bakeLayer(ModelLayers.CREEPER_HEAD)));
		builder.put(SkullBlock.Types.DRAGON, new DragonHeadModel(entityModelSet.bakeLayer(ModelLayers.DRAGON_SKULL)));
		return builder.build();
	}

	public SkullBlockRenderer(BlockEntityRendererProvider.Context context) {
		this.modelByType = createSkullRenderers(context.getModelSet());
	}

	public void render(SkullBlockEntity skullBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		float g = skullBlockEntity.getMouthAnimation(f);
		BlockState blockState = skullBlockEntity.getBlockState();
		boolean bl = blockState.getBlock() instanceof WallSkullBlock;
		Direction direction = bl ? blockState.getValue(WallSkullBlock.FACING) : null;
		float h = 22.5F * (float)(bl ? (2 + direction.get2DDataValue()) * 4 : (Integer)blockState.getValue(SkullBlock.ROTATION));
		SkullBlock.Type type = ((AbstractSkullBlock)blockState.getBlock()).getType();
		SkullModelBase skullModelBase = (SkullModelBase)this.modelByType.get(type);
		RenderType renderType = getRenderType(type, skullBlockEntity.getOwnerProfile());
		renderSkull(direction, h, g, poseStack, multiBufferSource, i, skullModelBase, renderType);
	}

	public static void renderSkull(
		@Nullable Direction direction,
		float f,
		float g,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		SkullModelBase skullModelBase,
		RenderType renderType
	) {
		poseStack.pushPose();
		if (direction == null) {
			poseStack.translate(0.5, 0.0, 0.5);
		} else {
			float h = 0.25F;
			poseStack.translate((double)(0.5F - (float)direction.getStepX() * 0.25F), 0.25, (double)(0.5F - (float)direction.getStepZ() * 0.25F));
		}

		poseStack.scale(-1.0F, -1.0F, 1.0F);
		VertexConsumer vertexConsumer = multiBufferSource.getBuffer(renderType);
		skullModelBase.setupAnim(g, f, 0.0F);
		skullModelBase.renderToBuffer(poseStack, vertexConsumer, i, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
		poseStack.popPose();
	}

	public static RenderType getRenderType(SkullBlock.Type type, @Nullable GameProfile gameProfile) {
		ResourceLocation resourceLocation = (ResourceLocation)SKIN_BY_TYPE.get(type);
		if (type == SkullBlock.Types.PLAYER && gameProfile != null) {
			Minecraft minecraft = Minecraft.getInstance();
			Map<Type, MinecraftProfileTexture> map = minecraft.getSkinManager().getInsecureSkinInformation(gameProfile);
			return map.containsKey(Type.SKIN)
				? RenderType.entityTranslucent(minecraft.getSkinManager().registerTexture((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN))
				: RenderType.entityCutoutNoCull(DefaultPlayerSkin.getDefaultSkin(Player.createPlayerUUID(gameProfile)));
		} else {
			return RenderType.entityCutoutNoCullZOffset(resourceLocation);
		}
	}
}

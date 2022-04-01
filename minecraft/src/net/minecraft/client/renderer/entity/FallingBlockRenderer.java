package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import java.util.Random;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GenericItemBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class FallingBlockRenderer extends EntityRenderer<FallingBlockEntity> {
	private final ItemRenderer itemRenderer;

	public FallingBlockRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemRenderer = context.getItemRenderer();
		this.shadowRadius = 0.5F;
	}

	public void render(FallingBlockEntity fallingBlockEntity, float f, float g, PoseStack poseStack, MultiBufferSource multiBufferSource, int i) {
		BlockState blockState = fallingBlockEntity.getBlockState();
		if (blockState.is(Blocks.LAVA)) {
			blockState = GenericItemBlock.genericBlockFromItem(Items.LAVA_BUCKET);
		} else if (blockState.is(Blocks.WATER)) {
			blockState = GenericItemBlock.genericBlockFromItem(Items.WATER_BUCKET);
		}

		if (blockState.getRenderShape() == RenderShape.MODEL) {
			Level level = fallingBlockEntity.getLevel();
			if ((blockState.is(Blocks.GENERIC_ITEM_BLOCK) || blockState != level.getBlockState(fallingBlockEntity.blockPosition()))
				&& blockState.getRenderShape() != RenderShape.INVISIBLE) {
				poseStack.pushPose();
				BlockPos blockPos = new BlockPos(fallingBlockEntity.getX(), fallingBlockEntity.getBoundingBox().maxY, fallingBlockEntity.getZ());
				Item item = GenericItemBlock.itemFromGenericBlock(blockState);
				if (item != null) {
					ItemStack itemStack = new ItemStack(item);
					BakedModel bakedModel = this.itemRenderer.getModel(itemStack, fallingBlockEntity.level, null, fallingBlockEntity.getId());
					float h = fallingBlockEntity.getSpin(g);
					poseStack.pushPose();
					poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
					poseStack.mulPose(Vector3f.ZP.rotationDegrees(h));
					this.itemRenderer.render(itemStack, ItemTransforms.TransformType.FIXED, false, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY, bakedModel);
					poseStack.popPose();
				} else {
					poseStack.translate(-0.5, 0.0, -0.5);
					BlockRenderDispatcher blockRenderDispatcher = Minecraft.getInstance().getBlockRenderer();
					blockRenderDispatcher.getModelRenderer()
						.tesselateBlock(
							level,
							blockRenderDispatcher.getBlockModel(blockState),
							blockState,
							blockPos,
							poseStack,
							multiBufferSource.getBuffer(ItemBlockRenderTypes.getMovingBlockRenderType(blockState)),
							false,
							new Random(),
							blockState.getSeed(fallingBlockEntity.getStartPos()),
							OverlayTexture.NO_OVERLAY
						);
				}

				poseStack.popPose();
				super.render(fallingBlockEntity, f, g, poseStack, multiBufferSource, i);
			}
		}
	}

	public ResourceLocation getTextureLocation(FallingBlockEntity fallingBlockEntity) {
		return TextureAtlas.LOCATION_BLOCKS;
	}
}

package net.minecraft.client.renderer.entity.layers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

@Environment(EnvType.CLIENT)
public class BarrelLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
	public BarrelLayer(RenderLayerParent<T, M> renderLayerParent) {
		super(renderLayerParent);
	}

	public void render(PoseStack poseStack, MultiBufferSource multiBufferSource, int i, T livingEntity, float f, float g, float h, float j, float k, float l) {
		ItemStack itemStack = livingEntity.getItemBySlot(EquipmentSlot.HEAD);
		if (!itemStack.isEmpty() && itemStack.is(Items.BARREL)) {
			BlockState blockState = Blocks.BARREL.defaultBlockState();
			if (blockState.hasProperty(BlockStateProperties.OPEN)) {
				boolean var10002;
				label35: {
					if (livingEntity instanceof Player player && player.isCrouching()) {
						var10002 = true;
						break label35;
					}

					var10002 = false;
				}

				blockState = blockState.setValue(BlockStateProperties.OPEN, Boolean.valueOf(!var10002));
			}

			if (blockState.hasProperty(BlockStateProperties.FACING)) {
				blockState = blockState.setValue(BlockStateProperties.FACING, Direction.UP);
			}

			label29: {
				poseStack.pushPose();
				if (livingEntity instanceof Player player && player.isCrouching()) {
					poseStack.scale(1.07F, 1.07F, 1.07F);
					poseStack.translate(-0.5, 0.28, -0.5);
					break label29;
				}

				poseStack.translate(-0.5, -0.25, -0.5);
			}

			Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, poseStack, multiBufferSource, i, OverlayTexture.NO_OVERLAY);
			poseStack.popPose();
		}
	}
}

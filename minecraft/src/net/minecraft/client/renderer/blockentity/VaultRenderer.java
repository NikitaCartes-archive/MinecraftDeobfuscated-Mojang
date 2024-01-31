package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.vault.VaultBlockEntity;
import net.minecraft.world.level.block.entity.vault.VaultClientData;

@Environment(EnvType.CLIENT)
public class VaultRenderer implements BlockEntityRenderer<VaultBlockEntity> {
	private final ItemRenderer itemRenderer;
	private final RandomSource random = RandomSource.create();

	public VaultRenderer(BlockEntityRendererProvider.Context context) {
		this.itemRenderer = context.getItemRenderer();
	}

	public void render(VaultBlockEntity vaultBlockEntity, float f, PoseStack poseStack, MultiBufferSource multiBufferSource, int i, int j) {
		if (VaultBlockEntity.Client.shouldDisplayActiveEffects(vaultBlockEntity.getSharedData())) {
			Level level = vaultBlockEntity.getLevel();
			if (level != null) {
				ItemStack itemStack = vaultBlockEntity.getSharedData().getDisplayItem();
				if (!itemStack.isEmpty()) {
					this.random.setSeed((long)ItemEntityRenderer.getSeedForItemStack(itemStack));
					VaultClientData vaultClientData = vaultBlockEntity.getClientData();
					renderItemInside(
						f, level, poseStack, multiBufferSource, i, itemStack, this.itemRenderer, vaultClientData.previousSpin(), vaultClientData.currentSpin(), this.random
					);
				}
			}
		}
	}

	public static void renderItemInside(
		float f,
		Level level,
		PoseStack poseStack,
		MultiBufferSource multiBufferSource,
		int i,
		ItemStack itemStack,
		ItemRenderer itemRenderer,
		float g,
		float h,
		RandomSource randomSource
	) {
		poseStack.pushPose();
		poseStack.translate(0.5F, 0.4F, 0.5F);
		poseStack.mulPose(Axis.YP.rotationDegrees(Mth.rotLerp(f, g, h)));
		ItemEntityRenderer.renderMultipleFromCount(itemRenderer, poseStack, multiBufferSource, i, itemStack, randomSource, level);
		poseStack.popPose();
	}
}

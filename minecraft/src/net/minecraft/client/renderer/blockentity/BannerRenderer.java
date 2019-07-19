package net.minecraft.client.renderer.blockentity;

import com.mojang.blaze3d.platform.GlStateManager;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.BannerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.banner.BannerTextures;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Environment(EnvType.CLIENT)
public class BannerRenderer extends BlockEntityRenderer<BannerBlockEntity> {
	private final BannerModel bannerModel = new BannerModel();

	public void render(BannerBlockEntity bannerBlockEntity, double d, double e, double f, float g, int i) {
		float h = 0.6666667F;
		boolean bl = bannerBlockEntity.getLevel() == null;
		GlStateManager.pushMatrix();
		ModelPart modelPart = this.bannerModel.getPole();
		long l;
		if (bl) {
			l = 0L;
			GlStateManager.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
			modelPart.visible = true;
		} else {
			l = bannerBlockEntity.getLevel().getGameTime();
			BlockState blockState = bannerBlockEntity.getBlockState();
			if (blockState.getBlock() instanceof BannerBlock) {
				GlStateManager.translatef((float)d + 0.5F, (float)e + 0.5F, (float)f + 0.5F);
				GlStateManager.rotatef((float)(-(Integer)blockState.getValue(BannerBlock.ROTATION) * 360) / 16.0F, 0.0F, 1.0F, 0.0F);
				modelPart.visible = true;
			} else {
				GlStateManager.translatef((float)d + 0.5F, (float)e - 0.16666667F, (float)f + 0.5F);
				GlStateManager.rotatef(-((Direction)blockState.getValue(WallBannerBlock.FACING)).toYRot(), 0.0F, 1.0F, 0.0F);
				GlStateManager.translatef(0.0F, -0.3125F, -0.4375F);
				modelPart.visible = false;
			}
		}

		BlockPos blockPos = bannerBlockEntity.getBlockPos();
		float j = (float)((long)(blockPos.getX() * 7 + blockPos.getY() * 9 + blockPos.getZ() * 13) + l) + g;
		this.bannerModel.getFlag().xRot = (-0.0125F + 0.01F * Mth.cos(j * (float) Math.PI * 0.02F)) * (float) Math.PI;
		GlStateManager.enableRescaleNormal();
		ResourceLocation resourceLocation = this.getTextureLocation(bannerBlockEntity);
		if (resourceLocation != null) {
			this.bindTexture(resourceLocation);
			GlStateManager.pushMatrix();
			GlStateManager.scalef(0.6666667F, -0.6666667F, -0.6666667F);
			this.bannerModel.render();
			GlStateManager.popMatrix();
		}

		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.popMatrix();
	}

	@Nullable
	private ResourceLocation getTextureLocation(BannerBlockEntity bannerBlockEntity) {
		return BannerTextures.BANNER_CACHE.getTextureLocation(bannerBlockEntity.getTextureHashName(), bannerBlockEntity.getPatterns(), bannerBlockEntity.getColors());
	}
}

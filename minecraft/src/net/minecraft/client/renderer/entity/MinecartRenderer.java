package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.MinecartModel;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class MinecartRenderer<T extends AbstractMinecart> extends EntityRenderer<T> {
	private static final ResourceLocation MINECART_LOCATION = new ResourceLocation("textures/entity/minecart.png");
	protected final EntityModel<T> model = new MinecartModel<>();

	public MinecartRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
		this.shadowRadius = 0.7F;
	}

	public void render(T abstractMinecart, double d, double e, double f, float g, float h) {
		RenderSystem.pushMatrix();
		this.bindTexture(abstractMinecart);
		long l = (long)abstractMinecart.getId() * 493286711L;
		l = l * l * 4392167121L + l * 98761L;
		float i = (((float)(l >> 16 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float j = (((float)(l >> 20 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		float k = (((float)(l >> 24 & 7L) + 0.5F) / 8.0F - 0.5F) * 0.004F;
		RenderSystem.translatef(i, j, k);
		double m = Mth.lerp((double)h, abstractMinecart.xOld, abstractMinecart.x);
		double n = Mth.lerp((double)h, abstractMinecart.yOld, abstractMinecart.y);
		double o = Mth.lerp((double)h, abstractMinecart.zOld, abstractMinecart.z);
		double p = 0.3F;
		Vec3 vec3 = abstractMinecart.getPos(m, n, o);
		float q = Mth.lerp(h, abstractMinecart.xRotO, abstractMinecart.xRot);
		if (vec3 != null) {
			Vec3 vec32 = abstractMinecart.getPosOffs(m, n, o, 0.3F);
			Vec3 vec33 = abstractMinecart.getPosOffs(m, n, o, -0.3F);
			if (vec32 == null) {
				vec32 = vec3;
			}

			if (vec33 == null) {
				vec33 = vec3;
			}

			d += vec3.x - m;
			e += (vec32.y + vec33.y) / 2.0 - n;
			f += vec3.z - o;
			Vec3 vec34 = vec33.add(-vec32.x, -vec32.y, -vec32.z);
			if (vec34.length() != 0.0) {
				vec34 = vec34.normalize();
				g = (float)(Math.atan2(vec34.z, vec34.x) * 180.0 / Math.PI);
				q = (float)(Math.atan(vec34.y) * 73.0);
			}
		}

		RenderSystem.translatef((float)d, (float)e + 0.375F, (float)f);
		RenderSystem.rotatef(180.0F - g, 0.0F, 1.0F, 0.0F);
		RenderSystem.rotatef(-q, 0.0F, 0.0F, 1.0F);
		float r = (float)abstractMinecart.getHurtTime() - h;
		float s = abstractMinecart.getDamage() - h;
		if (s < 0.0F) {
			s = 0.0F;
		}

		if (r > 0.0F) {
			RenderSystem.rotatef(Mth.sin(r) * r * s / 10.0F * (float)abstractMinecart.getHurtDir(), 1.0F, 0.0F, 0.0F);
		}

		int t = abstractMinecart.getDisplayOffset();
		if (this.solidRender) {
			RenderSystem.enableColorMaterial();
			RenderSystem.setupSolidRenderingTextureCombine(this.getTeamColor(abstractMinecart));
		}

		BlockState blockState = abstractMinecart.getDisplayBlockState();
		if (blockState.getRenderShape() != RenderShape.INVISIBLE) {
			RenderSystem.pushMatrix();
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			float u = 0.75F;
			RenderSystem.scalef(0.75F, 0.75F, 0.75F);
			RenderSystem.translatef(-0.5F, (float)(t - 8) / 16.0F, 0.5F);
			this.renderMinecartContents(abstractMinecart, h, blockState);
			RenderSystem.popMatrix();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			this.bindTexture(abstractMinecart);
		}

		RenderSystem.scalef(-1.0F, -1.0F, 1.0F);
		this.model.render(abstractMinecart, 0.0F, 0.0F, -0.1F, 0.0F, 0.0F, 0.0625F);
		RenderSystem.popMatrix();
		if (this.solidRender) {
			RenderSystem.tearDownSolidRenderingTextureCombine();
			RenderSystem.disableColorMaterial();
		}

		super.render(abstractMinecart, d, e, f, g, h);
	}

	protected ResourceLocation getTextureLocation(T abstractMinecart) {
		return MINECART_LOCATION;
	}

	protected void renderMinecartContents(T abstractMinecart, float f, BlockState blockState) {
		RenderSystem.pushMatrix();
		Minecraft.getInstance().getBlockRenderer().renderSingleBlock(blockState, abstractMinecart.getBrightness());
		RenderSystem.popMatrix();
	}
}

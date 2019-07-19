package net.minecraft.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.culling.Culler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.scores.PlayerTeam;

@Environment(EnvType.CLIENT)
public abstract class EntityRenderer<T extends Entity> {
	private static final ResourceLocation SHADOW_LOCATION = new ResourceLocation("textures/misc/shadow.png");
	protected final EntityRenderDispatcher entityRenderDispatcher;
	protected float shadowRadius;
	protected float shadowStrength = 1.0F;
	protected boolean solidRender;

	protected EntityRenderer(EntityRenderDispatcher entityRenderDispatcher) {
		this.entityRenderDispatcher = entityRenderDispatcher;
	}

	public void setSolidRender(boolean bl) {
		this.solidRender = bl;
	}

	public boolean shouldRender(T entity, Culler culler, double d, double e, double f) {
		if (!entity.shouldRender(d, e, f)) {
			return false;
		} else if (entity.noCulling) {
			return true;
		} else {
			AABB aABB = entity.getBoundingBoxForCulling().inflate(0.5);
			if (aABB.hasNaN() || aABB.getSize() == 0.0) {
				aABB = new AABB(entity.x - 2.0, entity.y - 2.0, entity.z - 2.0, entity.x + 2.0, entity.y + 2.0, entity.z + 2.0);
			}

			return culler.isVisible(aABB);
		}
	}

	public void render(T entity, double d, double e, double f, float g, float h) {
		if (!this.solidRender) {
			this.renderName(entity, d, e, f);
		}
	}

	protected int getTeamColor(T entity) {
		PlayerTeam playerTeam = (PlayerTeam)entity.getTeam();
		return playerTeam != null && playerTeam.getColor().getColor() != null ? playerTeam.getColor().getColor() : 16777215;
	}

	protected void renderName(T entity, double d, double e, double f) {
		if (this.shouldShowName(entity)) {
			this.renderNameTag(entity, entity.getDisplayName().getColoredString(), d, e, f, 64);
		}
	}

	protected boolean shouldShowName(T entity) {
		return entity.shouldShowName() && entity.hasCustomName();
	}

	protected void renderNameTags(T entity, double d, double e, double f, String string, double g) {
		this.renderNameTag(entity, string, d, e, f, 64);
	}

	@Nullable
	protected abstract ResourceLocation getTextureLocation(T entity);

	protected boolean bindTexture(T entity) {
		ResourceLocation resourceLocation = this.getTextureLocation(entity);
		if (resourceLocation == null) {
			return false;
		} else {
			this.bindTexture(resourceLocation);
			return true;
		}
	}

	public void bindTexture(ResourceLocation resourceLocation) {
		this.entityRenderDispatcher.textureManager.bind(resourceLocation);
	}

	private void renderFlame(Entity entity, double d, double e, double f, float g) {
		GlStateManager.disableLighting();
		TextureAtlas textureAtlas = Minecraft.getInstance().getTextureAtlas();
		TextureAtlasSprite textureAtlasSprite = textureAtlas.getSprite(ModelBakery.FIRE_0);
		TextureAtlasSprite textureAtlasSprite2 = textureAtlas.getSprite(ModelBakery.FIRE_1);
		GlStateManager.pushMatrix();
		GlStateManager.translatef((float)d, (float)e, (float)f);
		float h = entity.getBbWidth() * 1.4F;
		GlStateManager.scalef(h, h, h);
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		float i = 0.5F;
		float j = 0.0F;
		float k = entity.getBbHeight() / h;
		float l = (float)(entity.y - entity.getBoundingBox().minY);
		GlStateManager.rotatef(-this.entityRenderDispatcher.playerRotY, 0.0F, 1.0F, 0.0F);
		GlStateManager.translatef(0.0F, 0.0F, -0.3F + (float)((int)k) * 0.02F);
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float m = 0.0F;
		int n = 0;
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX);

		while (k > 0.0F) {
			TextureAtlasSprite textureAtlasSprite3 = n % 2 == 0 ? textureAtlasSprite : textureAtlasSprite2;
			this.bindTexture(TextureAtlas.LOCATION_BLOCKS);
			float o = textureAtlasSprite3.getU0();
			float p = textureAtlasSprite3.getV0();
			float q = textureAtlasSprite3.getU1();
			float r = textureAtlasSprite3.getV1();
			if (n / 2 % 2 == 0) {
				float s = q;
				q = o;
				o = s;
			}

			bufferBuilder.vertex((double)(i - 0.0F), (double)(0.0F - l), (double)m).uv((double)q, (double)r).endVertex();
			bufferBuilder.vertex((double)(-i - 0.0F), (double)(0.0F - l), (double)m).uv((double)o, (double)r).endVertex();
			bufferBuilder.vertex((double)(-i - 0.0F), (double)(1.4F - l), (double)m).uv((double)o, (double)p).endVertex();
			bufferBuilder.vertex((double)(i - 0.0F), (double)(1.4F - l), (double)m).uv((double)q, (double)p).endVertex();
			k -= 0.45F;
			l -= 0.45F;
			i *= 0.9F;
			m += 0.03F;
			n++;
		}

		tesselator.end();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}

	private void renderShadow(Entity entity, double d, double e, double f, float g, float h) {
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.entityRenderDispatcher.textureManager.bind(SHADOW_LOCATION);
		LevelReader levelReader = this.getLevel();
		GlStateManager.depthMask(false);
		float i = this.shadowRadius;
		if (entity instanceof Mob) {
			Mob mob = (Mob)entity;
			if (mob.isBaby()) {
				i *= 0.5F;
			}
		}

		double j = Mth.lerp((double)h, entity.xOld, entity.x);
		double k = Mth.lerp((double)h, entity.yOld, entity.y);
		double l = Mth.lerp((double)h, entity.zOld, entity.z);
		int m = Mth.floor(j - (double)i);
		int n = Mth.floor(j + (double)i);
		int o = Mth.floor(k - (double)i);
		int p = Mth.floor(k);
		int q = Mth.floor(l - (double)i);
		int r = Mth.floor(l + (double)i);
		double s = d - j;
		double t = e - k;
		double u = f - l;
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_TEX_COLOR);

		for (BlockPos blockPos : BlockPos.betweenClosed(new BlockPos(m, o, q), new BlockPos(n, p, r))) {
			BlockPos blockPos2 = blockPos.below();
			BlockState blockState = levelReader.getBlockState(blockPos2);
			if (blockState.getRenderShape() != RenderShape.INVISIBLE && levelReader.getMaxLocalRawBrightness(blockPos) > 3) {
				this.renderBlockShadow(blockState, levelReader, blockPos2, d, e, f, blockPos, g, i, s, t, u);
			}
		}

		tesselator.end();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.disableBlend();
		GlStateManager.depthMask(true);
	}

	private LevelReader getLevel() {
		return this.entityRenderDispatcher.level;
	}

	private void renderBlockShadow(
		BlockState blockState,
		LevelReader levelReader,
		BlockPos blockPos,
		double d,
		double e,
		double f,
		BlockPos blockPos2,
		float g,
		float h,
		double i,
		double j,
		double k
	) {
		if (blockState.isCollisionShapeFullBlock(levelReader, blockPos)) {
			VoxelShape voxelShape = blockState.getShape(this.getLevel(), blockPos2.below());
			if (!voxelShape.isEmpty()) {
				Tesselator tesselator = Tesselator.getInstance();
				BufferBuilder bufferBuilder = tesselator.getBuilder();
				double l = ((double)g - (e - ((double)blockPos2.getY() + j)) / 2.0) * 0.5 * (double)this.getLevel().getBrightness(blockPos2);
				if (!(l < 0.0)) {
					if (l > 1.0) {
						l = 1.0;
					}

					AABB aABB = voxelShape.bounds();
					double m = (double)blockPos2.getX() + aABB.minX + i;
					double n = (double)blockPos2.getX() + aABB.maxX + i;
					double o = (double)blockPos2.getY() + aABB.minY + j + 0.015625;
					double p = (double)blockPos2.getZ() + aABB.minZ + k;
					double q = (double)blockPos2.getZ() + aABB.maxZ + k;
					float r = (float)((d - m) / 2.0 / (double)h + 0.5);
					float s = (float)((d - n) / 2.0 / (double)h + 0.5);
					float t = (float)((f - p) / 2.0 / (double)h + 0.5);
					float u = (float)((f - q) / 2.0 / (double)h + 0.5);
					bufferBuilder.vertex(m, o, p).uv((double)r, (double)t).color(1.0F, 1.0F, 1.0F, (float)l).endVertex();
					bufferBuilder.vertex(m, o, q).uv((double)r, (double)u).color(1.0F, 1.0F, 1.0F, (float)l).endVertex();
					bufferBuilder.vertex(n, o, q).uv((double)s, (double)u).color(1.0F, 1.0F, 1.0F, (float)l).endVertex();
					bufferBuilder.vertex(n, o, p).uv((double)s, (double)t).color(1.0F, 1.0F, 1.0F, (float)l).endVertex();
				}
			}
		}
	}

	public static void render(AABB aABB, double d, double e, double f) {
		GlStateManager.disableTexture();
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferBuilder = tesselator.getBuilder();
		GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		bufferBuilder.offset(d, e, f);
		bufferBuilder.begin(7, DefaultVertexFormat.POSITION_NORMAL);
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.minZ).normal(0.0F, 0.0F, -1.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.maxZ).normal(0.0F, 0.0F, 1.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.minZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.maxZ).normal(0.0F, -1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.maxZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.minZ).normal(0.0F, 1.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.maxZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.maxY, aABB.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.minX, aABB.minY, aABB.minZ).normal(-1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.minZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.maxY, aABB.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		bufferBuilder.vertex(aABB.maxX, aABB.minY, aABB.maxZ).normal(1.0F, 0.0F, 0.0F).endVertex();
		tesselator.end();
		bufferBuilder.offset(0.0, 0.0, 0.0);
		GlStateManager.enableTexture();
	}

	public void postRender(Entity entity, double d, double e, double f, float g, float h) {
		if (this.entityRenderDispatcher.options != null) {
			if (this.entityRenderDispatcher.options.entityShadows
				&& this.shadowRadius > 0.0F
				&& !entity.isInvisible()
				&& this.entityRenderDispatcher.shouldRenderShadow()) {
				double i = this.entityRenderDispatcher.distanceToSqr(entity.x, entity.y, entity.z);
				float j = (float)((1.0 - i / 256.0) * (double)this.shadowStrength);
				if (j > 0.0F) {
					this.renderShadow(entity, d, e, f, j, h);
				}
			}

			if (entity.displayFireAnimation() && !entity.isSpectator()) {
				this.renderFlame(entity, d, e, f, h);
			}
		}
	}

	public Font getFont() {
		return this.entityRenderDispatcher.getFont();
	}

	protected void renderNameTag(T entity, String string, double d, double e, double f, int i) {
		double g = entity.distanceToSqr(this.entityRenderDispatcher.camera.getPosition());
		if (!(g > (double)(i * i))) {
			boolean bl = entity.isVisuallySneaking();
			float h = this.entityRenderDispatcher.playerRotY;
			float j = this.entityRenderDispatcher.playerRotX;
			float k = entity.getBbHeight() + 0.5F - (bl ? 0.25F : 0.0F);
			int l = "deadmau5".equals(string) ? -10 : 0;
			GameRenderer.renderNameTagInWorld(this.getFont(), string, (float)d, (float)e + k, (float)f, l, h, j, bl);
		}
	}

	public EntityRenderDispatcher getDispatcher() {
		return this.entityRenderDispatcher;
	}

	public boolean hasSecondPass() {
		return false;
	}

	public void renderSecondPass(T entity, double d, double e, double f, float g, float h) {
	}

	public void setLightColor(T entity) {
		int i = entity.getLightColor();
		int j = i % 65536;
		int k = i / 65536;
		GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);
	}
}

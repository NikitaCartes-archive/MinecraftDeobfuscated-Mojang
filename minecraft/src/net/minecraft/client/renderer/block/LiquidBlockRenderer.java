package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndBiomeGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StainedGlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

@Environment(EnvType.CLIENT)
public class LiquidBlockRenderer {
	private final TextureAtlasSprite[] lavaIcons = new TextureAtlasSprite[2];
	private final TextureAtlasSprite[] waterIcons = new TextureAtlasSprite[2];
	private TextureAtlasSprite waterOverlay;

	protected void setupSprites() {
		TextureAtlas textureAtlas = Minecraft.getInstance().getTextureAtlas();
		this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
		this.lavaIcons[1] = textureAtlas.getSprite(ModelBakery.LAVA_FLOW);
		this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
		this.waterIcons[1] = textureAtlas.getSprite(ModelBakery.WATER_FLOW);
		this.waterOverlay = textureAtlas.getSprite(ModelBakery.WATER_OVERLAY);
	}

	private static boolean isNeighborSameFluid(BlockGetter blockGetter, BlockPos blockPos, Direction direction, FluidState fluidState) {
		BlockPos blockPos2 = blockPos.relative(direction);
		FluidState fluidState2 = blockGetter.getFluidState(blockPos2);
		return fluidState2.getType().isSame(fluidState.getType());
	}

	private static boolean isFaceOccluded(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		if (blockState.canOcclude()) {
			VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, (double)f, 1.0);
			VoxelShape voxelShape2 = blockState.getOcclusionShape(blockGetter, blockPos2);
			return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
		} else {
			return false;
		}
	}

	public boolean tesselate(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos, VertexConsumer vertexConsumer, FluidState fluidState) {
		boolean bl = fluidState.is(FluidTags.LAVA);
		TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
		int i = bl ? 16777215 : BiomeColors.getAverageWaterColor(blockAndBiomeGetter, blockPos);
		float f = (float)(i >> 16 & 0xFF) / 255.0F;
		float g = (float)(i >> 8 & 0xFF) / 255.0F;
		float h = (float)(i & 0xFF) / 255.0F;
		boolean bl2 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.UP, fluidState);
		boolean bl3 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.DOWN, fluidState)
			&& !isFaceOccluded(blockAndBiomeGetter, blockPos, Direction.DOWN, 0.8888889F);
		boolean bl4 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.NORTH, fluidState);
		boolean bl5 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.SOUTH, fluidState);
		boolean bl6 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.WEST, fluidState);
		boolean bl7 = !isNeighborSameFluid(blockAndBiomeGetter, blockPos, Direction.EAST, fluidState);
		if (!bl2 && !bl3 && !bl7 && !bl6 && !bl4 && !bl5) {
			return false;
		} else {
			boolean bl8 = false;
			float j = 0.5F;
			float k = 1.0F;
			float l = 0.8F;
			float m = 0.6F;
			float n = this.getWaterHeight(blockAndBiomeGetter, blockPos, fluidState.getType());
			float o = this.getWaterHeight(blockAndBiomeGetter, blockPos.south(), fluidState.getType());
			float p = this.getWaterHeight(blockAndBiomeGetter, blockPos.east().south(), fluidState.getType());
			float q = this.getWaterHeight(blockAndBiomeGetter, blockPos.east(), fluidState.getType());
			double d = (double)(blockPos.getX() & 15);
			double e = (double)(blockPos.getY() & 15);
			double r = (double)(blockPos.getZ() & 15);
			float s = 0.001F;
			if (bl2 && !isFaceOccluded(blockAndBiomeGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
				bl8 = true;
				n -= 0.001F;
				o -= 0.001F;
				p -= 0.001F;
				q -= 0.001F;
				Vec3 vec3 = fluidState.getFlow(blockAndBiomeGetter, blockPos);
				float t;
				float v;
				float x;
				float z;
				float u;
				float w;
				float y;
				float aa;
				if (vec3.x == 0.0 && vec3.z == 0.0) {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[0];
					t = textureAtlasSprite.getU(0.0);
					u = textureAtlasSprite.getV(0.0);
					v = t;
					w = textureAtlasSprite.getV(16.0);
					x = textureAtlasSprite.getU(16.0);
					y = w;
					z = x;
					aa = u;
				} else {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[1];
					float ab = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
					float ac = Mth.sin(ab) * 0.25F;
					float ad = Mth.cos(ab) * 0.25F;
					float ae = 8.0F;
					t = textureAtlasSprite.getU((double)(8.0F + (-ad - ac) * 16.0F));
					u = textureAtlasSprite.getV((double)(8.0F + (-ad + ac) * 16.0F));
					v = textureAtlasSprite.getU((double)(8.0F + (-ad + ac) * 16.0F));
					w = textureAtlasSprite.getV((double)(8.0F + (ad + ac) * 16.0F));
					x = textureAtlasSprite.getU((double)(8.0F + (ad + ac) * 16.0F));
					y = textureAtlasSprite.getV((double)(8.0F + (ad - ac) * 16.0F));
					z = textureAtlasSprite.getU((double)(8.0F + (ad - ac) * 16.0F));
					aa = textureAtlasSprite.getV((double)(8.0F + (-ad - ac) * 16.0F));
				}

				float af = (t + v + x + z) / 4.0F;
				float ab = (u + w + y + aa) / 4.0F;
				float ac = (float)textureAtlasSprites[0].getWidth() / (textureAtlasSprites[0].getU1() - textureAtlasSprites[0].getU0());
				float ad = (float)textureAtlasSprites[0].getHeight() / (textureAtlasSprites[0].getV1() - textureAtlasSprites[0].getV0());
				float ae = 4.0F / Math.max(ad, ac);
				t = Mth.lerp(ae, t, af);
				v = Mth.lerp(ae, v, af);
				x = Mth.lerp(ae, x, af);
				z = Mth.lerp(ae, z, af);
				u = Mth.lerp(ae, u, ab);
				w = Mth.lerp(ae, w, ab);
				y = Mth.lerp(ae, y, ab);
				aa = Mth.lerp(ae, aa, ab);
				int ag = this.getLightColor(blockAndBiomeGetter, blockPos);
				float ah = 1.0F * f;
				float ai = 1.0F * g;
				float aj = 1.0F * h;
				this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, t, u, ag);
				this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, v, w, ag);
				this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, x, y, ag);
				this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, z, aa, ag);
				if (fluidState.shouldRenderBackwardUpFace(blockAndBiomeGetter, blockPos.above())) {
					this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ah, ai, aj, t, u, ag);
					this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ah, ai, aj, z, aa, ag);
					this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ah, ai, aj, x, y, ag);
					this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ah, ai, aj, v, w, ag);
				}
			}

			if (bl3) {
				float tx = textureAtlasSprites[0].getU0();
				float vx = textureAtlasSprites[0].getU1();
				float xx = textureAtlasSprites[0].getV0();
				float zx = textureAtlasSprites[0].getV1();
				int ak = this.getLightColor(blockAndBiomeGetter, blockPos.below());
				float wx = 0.5F * f;
				float yx = 0.5F * g;
				float aax = 0.5F * h;
				this.vertex(vertexConsumer, d, e, r + 1.0, wx, yx, aax, tx, zx, ak);
				this.vertex(vertexConsumer, d, e, r, wx, yx, aax, tx, xx, ak);
				this.vertex(vertexConsumer, d + 1.0, e, r, wx, yx, aax, vx, xx, ak);
				this.vertex(vertexConsumer, d + 1.0, e, r + 1.0, wx, yx, aax, vx, zx, ak);
				bl8 = true;
			}

			for (int al = 0; al < 4; al++) {
				float vx;
				float xx;
				double am;
				double ao;
				double an;
				double ap;
				Direction direction;
				boolean bl9;
				if (al == 0) {
					vx = n;
					xx = q;
					am = d;
					an = d + 1.0;
					ao = r + 0.001F;
					ap = r + 0.001F;
					direction = Direction.NORTH;
					bl9 = bl4;
				} else if (al == 1) {
					vx = p;
					xx = o;
					am = d + 1.0;
					an = d;
					ao = r + 1.0 - 0.001F;
					ap = r + 1.0 - 0.001F;
					direction = Direction.SOUTH;
					bl9 = bl5;
				} else if (al == 2) {
					vx = o;
					xx = n;
					am = d + 0.001F;
					an = d + 0.001F;
					ao = r + 1.0;
					ap = r;
					direction = Direction.WEST;
					bl9 = bl6;
				} else {
					vx = q;
					xx = p;
					am = d + 1.0 - 0.001F;
					an = d + 1.0 - 0.001F;
					ao = r;
					ap = r + 1.0;
					direction = Direction.EAST;
					bl9 = bl7;
				}

				if (bl9 && !isFaceOccluded(blockAndBiomeGetter, blockPos, direction, Math.max(vx, xx))) {
					bl8 = true;
					BlockPos blockPos2 = blockPos.relative(direction);
					TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
					if (!bl) {
						Block block = blockAndBiomeGetter.getBlockState(blockPos2).getBlock();
						if (block == Blocks.GLASS || block instanceof StainedGlassBlock) {
							textureAtlasSprite2 = this.waterOverlay;
						}
					}

					float ah = textureAtlasSprite2.getU(0.0);
					float ai = textureAtlasSprite2.getU(8.0);
					float aj = textureAtlasSprite2.getV((double)((1.0F - vx) * 16.0F * 0.5F));
					float aq = textureAtlasSprite2.getV((double)((1.0F - xx) * 16.0F * 0.5F));
					float ar = textureAtlasSprite2.getV(8.0);
					int as = this.getLightColor(blockAndBiomeGetter, blockPos2);
					float at = al < 2 ? 0.8F : 0.6F;
					float au = 1.0F * at * f;
					float av = 1.0F * at * g;
					float aw = 1.0F * at * h;
					this.vertex(vertexConsumer, am, e + (double)vx, ao, au, av, aw, ah, aj, as);
					this.vertex(vertexConsumer, an, e + (double)xx, ap, au, av, aw, ai, aq, as);
					this.vertex(vertexConsumer, an, e + 0.0, ap, au, av, aw, ai, ar, as);
					this.vertex(vertexConsumer, am, e + 0.0, ao, au, av, aw, ah, ar, as);
					if (textureAtlasSprite2 != this.waterOverlay) {
						this.vertex(vertexConsumer, am, e + 0.0, ao, au, av, aw, ah, ar, as);
						this.vertex(vertexConsumer, an, e + 0.0, ap, au, av, aw, ai, ar, as);
						this.vertex(vertexConsumer, an, e + (double)xx, ap, au, av, aw, ai, aq, as);
						this.vertex(vertexConsumer, am, e + (double)vx, ao, au, av, aw, ah, aj, as);
					}
				}
			}

			return bl8;
		}
	}

	private void vertex(VertexConsumer vertexConsumer, double d, double e, double f, float g, float h, float i, float j, float k, int l) {
		vertexConsumer.vertex(d, e, f).color(g, h, i, 1.0F).uv(j, k).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private int getLightColor(BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
		int i = blockAndBiomeGetter.getLightColor(blockPos);
		int j = blockAndBiomeGetter.getLightColor(blockPos.above());
		int k = i & 0xFF;
		int l = j & 0xFF;
		int m = i >> 16 & 0xFF;
		int n = j >> 16 & 0xFF;
		return (k > l ? k : l) | (m > n ? m : n) << 16;
	}

	private float getWaterHeight(BlockGetter blockGetter, BlockPos blockPos, Fluid fluid) {
		int i = 0;
		float f = 0.0F;

		for (int j = 0; j < 4; j++) {
			BlockPos blockPos2 = blockPos.offset(-(j & 1), 0, -(j >> 1 & 1));
			if (blockGetter.getFluidState(blockPos2.above()).getType().isSame(fluid)) {
				return 1.0F;
			}

			FluidState fluidState = blockGetter.getFluidState(blockPos2);
			if (fluidState.getType().isSame(fluid)) {
				float g = fluidState.getHeight(blockGetter, blockPos2);
				if (g >= 0.8F) {
					f += g * 10.0F;
					i += 10;
				} else {
					f += g;
					i++;
				}
			} else if (!blockGetter.getBlockState(blockPos2).getMaterial().isSolid()) {
				i++;
			}
		}

		return f / (float)i;
	}
}

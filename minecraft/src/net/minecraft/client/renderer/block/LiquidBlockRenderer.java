package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.LeavesBlock;
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
		this.lavaIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.LAVA.defaultBlockState()).getParticleIcon();
		this.lavaIcons[1] = ModelBakery.LAVA_FLOW.sprite();
		this.waterIcons[0] = Minecraft.getInstance().getModelManager().getBlockModelShaper().getBlockModel(Blocks.WATER.defaultBlockState()).getParticleIcon();
		this.waterIcons[1] = ModelBakery.WATER_FLOW.sprite();
		this.waterOverlay = ModelBakery.WATER_OVERLAY.sprite();
	}

	private static boolean isNeighborSameFluid(BlockGetter blockGetter, BlockPos blockPos, Direction direction, FluidState fluidState) {
		BlockPos blockPos2 = blockPos.relative(direction);
		FluidState fluidState2 = blockGetter.getFluidState(blockPos2);
		return fluidState2.getType().isSame(fluidState.getType());
	}

	private static boolean isFaceOccludedByState(BlockGetter blockGetter, Direction direction, float f, BlockPos blockPos, BlockState blockState) {
		if (blockState.canOcclude()) {
			VoxelShape voxelShape = Shapes.box(0.0, 0.0, 0.0, 1.0, (double)f, 1.0);
			VoxelShape voxelShape2 = blockState.getOcclusionShape(blockGetter, blockPos);
			return Shapes.blockOccudes(voxelShape, voxelShape2, direction);
		} else {
			return false;
		}
	}

	private static boolean isFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f) {
		BlockPos blockPos2 = blockPos.relative(direction);
		BlockState blockState = blockGetter.getBlockState(blockPos2);
		return isFaceOccludedByState(blockGetter, direction, f, blockPos2, blockState);
	}

	private static boolean isFaceOccludedBySelf(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
		return isFaceOccludedByState(blockGetter, direction.getOpposite(), 1.0F, blockPos, blockState);
	}

	public static boolean shouldRenderFace(
		BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction
	) {
		return !isFaceOccludedBySelf(blockAndTintGetter, blockPos, blockState, direction)
			&& !isNeighborSameFluid(blockAndTintGetter, blockPos, direction, fluidState);
	}

	public boolean tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, FluidState fluidState) {
		boolean bl = fluidState.is(FluidTags.LAVA);
		TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
		BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
		int i = bl ? 16777215 : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
		float f = (float)(i >> 16 & 0xFF) / 255.0F;
		float g = (float)(i >> 8 & 0xFF) / 255.0F;
		float h = (float)(i & 0xFF) / 255.0F;
		boolean bl2 = !isNeighborSameFluid(blockAndTintGetter, blockPos, Direction.UP, fluidState);
		boolean bl3 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.DOWN)
			&& !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889F);
		boolean bl4 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.NORTH);
		boolean bl5 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.SOUTH);
		boolean bl6 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.WEST);
		boolean bl7 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.EAST);
		if (!bl2 && !bl3 && !bl7 && !bl6 && !bl4 && !bl5) {
			return false;
		} else {
			boolean bl8 = false;
			float j = blockAndTintGetter.getShade(Direction.DOWN, true);
			float k = blockAndTintGetter.getShade(Direction.UP, true);
			float l = blockAndTintGetter.getShade(Direction.NORTH, true);
			float m = blockAndTintGetter.getShade(Direction.WEST, true);
			float n = this.getWaterHeight(blockAndTintGetter, blockPos, fluidState.getType());
			float o = this.getWaterHeight(blockAndTintGetter, blockPos.south(), fluidState.getType());
			float p = this.getWaterHeight(blockAndTintGetter, blockPos.east().south(), fluidState.getType());
			float q = this.getWaterHeight(blockAndTintGetter, blockPos.east(), fluidState.getType());
			double d = (double)(blockPos.getX() & 15);
			double e = (double)(blockPos.getY() & 15);
			double r = (double)(blockPos.getZ() & 15);
			float s = 0.001F;
			float t = bl3 ? 0.001F : 0.0F;
			if (bl2 && !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(n, o), Math.min(p, q)))) {
				bl8 = true;
				n -= 0.001F;
				o -= 0.001F;
				p -= 0.001F;
				q -= 0.001F;
				Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
				float u;
				float w;
				float y;
				float aa;
				float v;
				float x;
				float z;
				float ab;
				if (vec3.x == 0.0 && vec3.z == 0.0) {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[0];
					u = textureAtlasSprite.getU(0.0);
					v = textureAtlasSprite.getV(0.0);
					w = u;
					x = textureAtlasSprite.getV(16.0);
					y = textureAtlasSprite.getU(16.0);
					z = x;
					aa = y;
					ab = v;
				} else {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[1];
					float ac = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
					float ad = Mth.sin(ac) * 0.25F;
					float ae = Mth.cos(ac) * 0.25F;
					float af = 8.0F;
					u = textureAtlasSprite.getU((double)(8.0F + (-ae - ad) * 16.0F));
					v = textureAtlasSprite.getV((double)(8.0F + (-ae + ad) * 16.0F));
					w = textureAtlasSprite.getU((double)(8.0F + (-ae + ad) * 16.0F));
					x = textureAtlasSprite.getV((double)(8.0F + (ae + ad) * 16.0F));
					y = textureAtlasSprite.getU((double)(8.0F + (ae + ad) * 16.0F));
					z = textureAtlasSprite.getV((double)(8.0F + (ae - ad) * 16.0F));
					aa = textureAtlasSprite.getU((double)(8.0F + (ae - ad) * 16.0F));
					ab = textureAtlasSprite.getV((double)(8.0F + (-ae - ad) * 16.0F));
				}

				float ag = (u + w + y + aa) / 4.0F;
				float ac = (v + x + z + ab) / 4.0F;
				float ad = (float)textureAtlasSprites[0].getWidth() / (textureAtlasSprites[0].getU1() - textureAtlasSprites[0].getU0());
				float ae = (float)textureAtlasSprites[0].getHeight() / (textureAtlasSprites[0].getV1() - textureAtlasSprites[0].getV0());
				float af = 4.0F / Math.max(ae, ad);
				u = Mth.lerp(af, u, ag);
				w = Mth.lerp(af, w, ag);
				y = Mth.lerp(af, y, ag);
				aa = Mth.lerp(af, aa, ag);
				v = Mth.lerp(af, v, ac);
				x = Mth.lerp(af, x, ac);
				z = Mth.lerp(af, z, ac);
				ab = Mth.lerp(af, ab, ac);
				int ah = this.getLightColor(blockAndTintGetter, blockPos);
				float ai = k * f;
				float aj = k * g;
				float ak = k * h;
				this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ai, aj, ak, u, v, ah);
				this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ai, aj, ak, w, x, ah);
				this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ai, aj, ak, y, z, ah);
				this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ai, aj, ak, aa, ab, ah);
				if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
					this.vertex(vertexConsumer, d + 0.0, e + (double)n, r + 0.0, ai, aj, ak, u, v, ah);
					this.vertex(vertexConsumer, d + 1.0, e + (double)q, r + 0.0, ai, aj, ak, aa, ab, ah);
					this.vertex(vertexConsumer, d + 1.0, e + (double)p, r + 1.0, ai, aj, ak, y, z, ah);
					this.vertex(vertexConsumer, d + 0.0, e + (double)o, r + 1.0, ai, aj, ak, w, x, ah);
				}
			}

			if (bl3) {
				float ux = textureAtlasSprites[0].getU0();
				float wx = textureAtlasSprites[0].getU1();
				float yx = textureAtlasSprites[0].getV0();
				float aax = textureAtlasSprites[0].getV1();
				int al = this.getLightColor(blockAndTintGetter, blockPos.below());
				float xx = j * f;
				float zx = j * g;
				float abx = j * h;
				this.vertex(vertexConsumer, d, e + (double)t, r + 1.0, xx, zx, abx, ux, aax, al);
				this.vertex(vertexConsumer, d, e + (double)t, r, xx, zx, abx, ux, yx, al);
				this.vertex(vertexConsumer, d + 1.0, e + (double)t, r, xx, zx, abx, wx, yx, al);
				this.vertex(vertexConsumer, d + 1.0, e + (double)t, r + 1.0, xx, zx, abx, wx, aax, al);
				bl8 = true;
			}

			for (int am = 0; am < 4; am++) {
				float wx;
				float yx;
				double an;
				double ap;
				double ao;
				double aq;
				Direction direction;
				boolean bl9;
				if (am == 0) {
					wx = n;
					yx = q;
					an = d;
					ao = d + 1.0;
					ap = r + 0.001F;
					aq = r + 0.001F;
					direction = Direction.NORTH;
					bl9 = bl4;
				} else if (am == 1) {
					wx = p;
					yx = o;
					an = d + 1.0;
					ao = d;
					ap = r + 1.0 - 0.001F;
					aq = r + 1.0 - 0.001F;
					direction = Direction.SOUTH;
					bl9 = bl5;
				} else if (am == 2) {
					wx = o;
					yx = n;
					an = d + 0.001F;
					ao = d + 0.001F;
					ap = r + 1.0;
					aq = r;
					direction = Direction.WEST;
					bl9 = bl6;
				} else {
					wx = q;
					yx = p;
					an = d + 1.0 - 0.001F;
					ao = d + 1.0 - 0.001F;
					ap = r;
					aq = r + 1.0;
					direction = Direction.EAST;
					bl9 = bl7;
				}

				if (bl9 && !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(wx, yx))) {
					bl8 = true;
					BlockPos blockPos2 = blockPos.relative(direction);
					TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
					if (!bl) {
						Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
						if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
							textureAtlasSprite2 = this.waterOverlay;
						}
					}

					float ai = textureAtlasSprite2.getU(0.0);
					float aj = textureAtlasSprite2.getU(8.0);
					float ak = textureAtlasSprite2.getV((double)((1.0F - wx) * 16.0F * 0.5F));
					float ar = textureAtlasSprite2.getV((double)((1.0F - yx) * 16.0F * 0.5F));
					float as = textureAtlasSprite2.getV(8.0);
					int at = this.getLightColor(blockAndTintGetter, blockPos2);
					float au = am < 2 ? l : m;
					float av = k * au * f;
					float aw = k * au * g;
					float ax = k * au * h;
					this.vertex(vertexConsumer, an, e + (double)wx, ap, av, aw, ax, ai, ak, at);
					this.vertex(vertexConsumer, ao, e + (double)yx, aq, av, aw, ax, aj, ar, at);
					this.vertex(vertexConsumer, ao, e + (double)t, aq, av, aw, ax, aj, as, at);
					this.vertex(vertexConsumer, an, e + (double)t, ap, av, aw, ax, ai, as, at);
					if (textureAtlasSprite2 != this.waterOverlay) {
						this.vertex(vertexConsumer, an, e + (double)t, ap, av, aw, ax, ai, as, at);
						this.vertex(vertexConsumer, ao, e + (double)t, aq, av, aw, ax, aj, as, at);
						this.vertex(vertexConsumer, ao, e + (double)yx, aq, av, aw, ax, aj, ar, at);
						this.vertex(vertexConsumer, an, e + (double)wx, ap, av, aw, ax, ai, ak, at);
					}
				}
			}

			return bl8;
		}
	}

	private void vertex(VertexConsumer vertexConsumer, double d, double e, double f, float g, float h, float i, float j, float k, int l) {
		vertexConsumer.vertex(d, e, f).color(g, h, i, 1.0F).uv(j, k).uv2(l).normal(0.0F, 1.0F, 0.0F).endVertex();
	}

	private int getLightColor(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
		int i = LevelRenderer.getLightColor(blockAndTintGetter, blockPos);
		int j = LevelRenderer.getLightColor(blockAndTintGetter, blockPos.above());
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

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
	private static final float MAX_FLUID_HEIGHT = 0.8888889F;
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

	private static boolean isNeighborSameFluid(FluidState fluidState, FluidState fluidState2) {
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

	private static boolean isFaceOccludedByNeighbor(BlockGetter blockGetter, BlockPos blockPos, Direction direction, float f, BlockState blockState) {
		return isFaceOccludedByState(blockGetter, direction, f, blockPos.relative(direction), blockState);
	}

	private static boolean isFaceOccludedBySelf(BlockGetter blockGetter, BlockPos blockPos, BlockState blockState, Direction direction) {
		return isFaceOccludedByState(blockGetter, direction.getOpposite(), 1.0F, blockPos, blockState);
	}

	public static boolean shouldRenderFace(
		BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2
	) {
		return !isFaceOccludedBySelf(blockAndTintGetter, blockPos, blockState, direction) && !isNeighborSameFluid(fluidState, fluidState2);
	}

	public void tesselate(BlockAndTintGetter blockAndTintGetter, BlockPos blockPos, VertexConsumer vertexConsumer, BlockState blockState, FluidState fluidState) {
		boolean bl = fluidState.is(FluidTags.LAVA);
		TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
		int i = bl ? 16777215 : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
		float f = (float)(i >> 16 & 0xFF) / 255.0F;
		float g = (float)(i >> 8 & 0xFF) / 255.0F;
		float h = (float)(i & 0xFF) / 255.0F;
		BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.DOWN));
		FluidState fluidState2 = blockState2.getFluidState();
		BlockState blockState3 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.UP));
		FluidState fluidState3 = blockState3.getFluidState();
		BlockState blockState4 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.NORTH));
		FluidState fluidState4 = blockState4.getFluidState();
		BlockState blockState5 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.SOUTH));
		FluidState fluidState5 = blockState5.getFluidState();
		BlockState blockState6 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.WEST));
		FluidState fluidState6 = blockState6.getFluidState();
		BlockState blockState7 = blockAndTintGetter.getBlockState(blockPos.relative(Direction.EAST));
		FluidState fluidState7 = blockState7.getFluidState();
		boolean bl2 = !isNeighborSameFluid(fluidState, fluidState3);
		boolean bl3 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.DOWN, fluidState2)
			&& !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.DOWN, 0.8888889F, blockState2);
		boolean bl4 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.NORTH, fluidState4);
		boolean bl5 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.SOUTH, fluidState5);
		boolean bl6 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.WEST, fluidState6);
		boolean bl7 = shouldRenderFace(blockAndTintGetter, blockPos, fluidState, blockState, Direction.EAST, fluidState7);
		if (bl2 || bl3 || bl7 || bl6 || bl4 || bl5) {
			float j = blockAndTintGetter.getShade(Direction.DOWN, true);
			float k = blockAndTintGetter.getShade(Direction.UP, true);
			float l = blockAndTintGetter.getShade(Direction.NORTH, true);
			float m = blockAndTintGetter.getShade(Direction.WEST, true);
			Fluid fluid = fluidState.getType();
			float n = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);
			float o;
			float p;
			float q;
			float r;
			if (n >= 1.0F) {
				o = 1.0F;
				p = 1.0F;
				q = 1.0F;
				r = 1.0F;
			} else {
				float s = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), blockState4, fluidState4);
				float t = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), blockState5, fluidState5);
				float u = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), blockState7, fluidState7);
				float v = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), blockState6, fluidState6);
				o = this.calculateAverageHeight(blockAndTintGetter, fluid, n, s, u, blockPos.relative(Direction.NORTH).relative(Direction.EAST));
				p = this.calculateAverageHeight(blockAndTintGetter, fluid, n, s, v, blockPos.relative(Direction.NORTH).relative(Direction.WEST));
				q = this.calculateAverageHeight(blockAndTintGetter, fluid, n, t, u, blockPos.relative(Direction.SOUTH).relative(Direction.EAST));
				r = this.calculateAverageHeight(blockAndTintGetter, fluid, n, t, v, blockPos.relative(Direction.SOUTH).relative(Direction.WEST));
			}

			double d = (double)(blockPos.getX() & 15);
			double e = (double)(blockPos.getY() & 15);
			double w = (double)(blockPos.getZ() & 15);
			float x = 0.001F;
			float y = bl3 ? 0.001F : 0.0F;
			if (bl2 && !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), blockState3)) {
				p -= 0.001F;
				r -= 0.001F;
				q -= 0.001F;
				o -= 0.001F;
				Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
				float z;
				float ab;
				float ad;
				float af;
				float aa;
				float ac;
				float ae;
				float ag;
				if (vec3.x == 0.0 && vec3.z == 0.0) {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[0];
					z = textureAtlasSprite.getU(0.0);
					aa = textureAtlasSprite.getV(0.0);
					ab = z;
					ac = textureAtlasSprite.getV(16.0);
					ad = textureAtlasSprite.getU(16.0);
					ae = ac;
					af = ad;
					ag = aa;
				} else {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[1];
					float ah = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
					float ai = Mth.sin(ah) * 0.25F;
					float aj = Mth.cos(ah) * 0.25F;
					float ak = 8.0F;
					z = textureAtlasSprite.getU((double)(8.0F + (-aj - ai) * 16.0F));
					aa = textureAtlasSprite.getV((double)(8.0F + (-aj + ai) * 16.0F));
					ab = textureAtlasSprite.getU((double)(8.0F + (-aj + ai) * 16.0F));
					ac = textureAtlasSprite.getV((double)(8.0F + (aj + ai) * 16.0F));
					ad = textureAtlasSprite.getU((double)(8.0F + (aj + ai) * 16.0F));
					ae = textureAtlasSprite.getV((double)(8.0F + (aj - ai) * 16.0F));
					af = textureAtlasSprite.getU((double)(8.0F + (aj - ai) * 16.0F));
					ag = textureAtlasSprite.getV((double)(8.0F + (-aj - ai) * 16.0F));
				}

				float al = (z + ab + ad + af) / 4.0F;
				float ah = (aa + ac + ae + ag) / 4.0F;
				float ai = textureAtlasSprites[0].uvShrinkRatio();
				z = Mth.lerp(ai, z, al);
				ab = Mth.lerp(ai, ab, al);
				ad = Mth.lerp(ai, ad, al);
				af = Mth.lerp(ai, af, al);
				aa = Mth.lerp(ai, aa, ah);
				ac = Mth.lerp(ai, ac, ah);
				ae = Mth.lerp(ai, ae, ah);
				ag = Mth.lerp(ai, ag, ah);
				int am = this.getLightColor(blockAndTintGetter, blockPos);
				float ak = k * f;
				float an = k * g;
				float ao = k * h;
				this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
				this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
				this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
				this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
				if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
					this.vertex(vertexConsumer, d + 0.0, e + (double)p, w + 0.0, ak, an, ao, z, aa, am);
					this.vertex(vertexConsumer, d + 1.0, e + (double)o, w + 0.0, ak, an, ao, af, ag, am);
					this.vertex(vertexConsumer, d + 1.0, e + (double)q, w + 1.0, ak, an, ao, ad, ae, am);
					this.vertex(vertexConsumer, d + 0.0, e + (double)r, w + 1.0, ak, an, ao, ab, ac, am);
				}
			}

			if (bl3) {
				float zx = textureAtlasSprites[0].getU0();
				float abx = textureAtlasSprites[0].getU1();
				float adx = textureAtlasSprites[0].getV0();
				float afx = textureAtlasSprites[0].getV1();
				int ap = this.getLightColor(blockAndTintGetter, blockPos.below());
				float acx = j * f;
				float aex = j * g;
				float agx = j * h;
				this.vertex(vertexConsumer, d, e + (double)y, w + 1.0, acx, aex, agx, zx, afx, ap);
				this.vertex(vertexConsumer, d, e + (double)y, w, acx, aex, agx, zx, adx, ap);
				this.vertex(vertexConsumer, d + 1.0, e + (double)y, w, acx, aex, agx, abx, adx, ap);
				this.vertex(vertexConsumer, d + 1.0, e + (double)y, w + 1.0, acx, aex, agx, abx, afx, ap);
			}

			int aq = this.getLightColor(blockAndTintGetter, blockPos);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				float afx;
				float aax;
				double ar;
				double at;
				double as;
				double au;
				boolean bl8;
				switch (direction) {
					case NORTH:
						afx = p;
						aax = o;
						ar = d;
						as = d + 1.0;
						at = w + 0.001F;
						au = w + 0.001F;
						bl8 = bl4;
						break;
					case SOUTH:
						afx = q;
						aax = r;
						ar = d + 1.0;
						as = d;
						at = w + 1.0 - 0.001F;
						au = w + 1.0 - 0.001F;
						bl8 = bl5;
						break;
					case WEST:
						afx = r;
						aax = p;
						ar = d + 0.001F;
						as = d + 0.001F;
						at = w + 1.0;
						au = w;
						bl8 = bl6;
						break;
					default:
						afx = o;
						aax = q;
						ar = d + 1.0 - 0.001F;
						as = d + 1.0 - 0.001F;
						at = w;
						au = w + 1.0;
						bl8 = bl7;
				}

				if (bl8
					&& !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(afx, aax), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) {
					BlockPos blockPos2 = blockPos.relative(direction);
					TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
					if (!bl) {
						Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
						if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
							textureAtlasSprite2 = this.waterOverlay;
						}
					}

					float av = textureAtlasSprite2.getU(0.0);
					float aw = textureAtlasSprite2.getU(8.0);
					float ax = textureAtlasSprite2.getV((double)((1.0F - afx) * 16.0F * 0.5F));
					float ay = textureAtlasSprite2.getV((double)((1.0F - aax) * 16.0F * 0.5F));
					float az = textureAtlasSprite2.getV(8.0);
					float ba = direction.getAxis() == Direction.Axis.Z ? l : m;
					float bb = k * ba * f;
					float bc = k * ba * g;
					float bd = k * ba * h;
					this.vertex(vertexConsumer, ar, e + (double)afx, at, bb, bc, bd, av, ax, aq);
					this.vertex(vertexConsumer, as, e + (double)aax, au, bb, bc, bd, aw, ay, aq);
					this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
					this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
					if (textureAtlasSprite2 != this.waterOverlay) {
						this.vertex(vertexConsumer, ar, e + (double)y, at, bb, bc, bd, av, az, aq);
						this.vertex(vertexConsumer, as, e + (double)y, au, bb, bc, bd, aw, az, aq);
						this.vertex(vertexConsumer, as, e + (double)aax, au, bb, bc, bd, aw, ay, aq);
						this.vertex(vertexConsumer, ar, e + (double)afx, at, bb, bc, bd, av, ax, aq);
					}
				}
			}
		}
	}

	private float calculateAverageHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, float f, float g, float h, BlockPos blockPos) {
		if (!(h >= 1.0F) && !(g >= 1.0F)) {
			float[] fs = new float[2];
			if (h > 0.0F || g > 0.0F) {
				float i = this.getHeight(blockAndTintGetter, fluid, blockPos);
				if (i >= 1.0F) {
					return 1.0F;
				}

				this.addWeightedHeight(fs, i);
			}

			this.addWeightedHeight(fs, f);
			this.addWeightedHeight(fs, h);
			this.addWeightedHeight(fs, g);
			return fs[0] / fs[1];
		} else {
			return 1.0F;
		}
	}

	private void addWeightedHeight(float[] fs, float f) {
		if (f >= 0.8F) {
			fs[0] += f * 10.0F;
			fs[1] += 10.0F;
		} else if (f >= 0.0F) {
			fs[0] += f;
			fs[1]++;
		}
	}

	private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos) {
		BlockState blockState = blockAndTintGetter.getBlockState(blockPos);
		return this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, blockState.getFluidState());
	}

	private float getHeight(BlockAndTintGetter blockAndTintGetter, Fluid fluid, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
		if (fluid.isSame(fluidState.getType())) {
			BlockState blockState2 = blockAndTintGetter.getBlockState(blockPos.above());
			return fluid.isSame(blockState2.getFluidState().getType()) ? 1.0F : fluidState.getOwnHeight();
		} else {
			return !blockState.isSolid() ? 0.0F : -1.0F;
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
}

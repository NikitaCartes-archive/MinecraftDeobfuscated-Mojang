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

	public void tesselate(
		BlockAndTintGetter blockAndTintGetter,
		BlockPos blockPos,
		VertexConsumer vertexConsumer,
		BlockState blockState,
		FluidState fluidState,
		double d,
		double e,
		double f
	) {
		boolean bl = fluidState.is(FluidTags.LAVA);
		TextureAtlasSprite[] textureAtlasSprites = bl ? this.lavaIcons : this.waterIcons;
		int i = bl ? 16777215 : BiomeColors.getAverageWaterColor(blockAndTintGetter, blockPos);
		float g = (float)(i >> 16 & 0xFF) / 255.0F;
		float h = (float)(i >> 8 & 0xFF) / 255.0F;
		float j = (float)(i & 0xFF) / 255.0F;
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
			float k = blockAndTintGetter.getShade(Direction.DOWN, true);
			float l = blockAndTintGetter.getShade(Direction.UP, true);
			float m = blockAndTintGetter.getShade(Direction.NORTH, true);
			float n = blockAndTintGetter.getShade(Direction.WEST, true);
			Fluid fluid = fluidState.getType();
			float o = this.getHeight(blockAndTintGetter, fluid, blockPos, blockState, fluidState);
			float p;
			float q;
			float r;
			float s;
			if (o >= 1.0F) {
				p = 1.0F;
				q = 1.0F;
				r = 1.0F;
				s = 1.0F;
			} else {
				float t = this.getHeight(blockAndTintGetter, fluid, blockPos.north(), blockState4, fluidState4);
				float u = this.getHeight(blockAndTintGetter, fluid, blockPos.south(), blockState5, fluidState5);
				float v = this.getHeight(blockAndTintGetter, fluid, blockPos.east(), blockState7, fluidState7);
				float w = this.getHeight(blockAndTintGetter, fluid, blockPos.west(), blockState6, fluidState6);
				p = this.calculateAverageHeight(blockAndTintGetter, fluid, o, t, v, blockPos.relative(Direction.NORTH).relative(Direction.EAST));
				q = this.calculateAverageHeight(blockAndTintGetter, fluid, o, t, w, blockPos.relative(Direction.NORTH).relative(Direction.WEST));
				r = this.calculateAverageHeight(blockAndTintGetter, fluid, o, u, v, blockPos.relative(Direction.SOUTH).relative(Direction.EAST));
				s = this.calculateAverageHeight(blockAndTintGetter, fluid, o, u, w, blockPos.relative(Direction.SOUTH).relative(Direction.WEST));
			}

			float t = 0.001F;
			float u = bl3 ? 0.001F : 0.0F;
			if (bl2 && !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, Direction.UP, Math.min(Math.min(q, s), Math.min(r, p)), blockState3)) {
				q -= 0.001F;
				s -= 0.001F;
				r -= 0.001F;
				p -= 0.001F;
				Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
				float z;
				float ab;
				float x;
				float y;
				float aa;
				float ac;
				float v;
				float w;
				if (vec3.x == 0.0 && vec3.z == 0.0) {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[0];
					v = textureAtlasSprite.getU(0.0F);
					x = textureAtlasSprite.getV(0.0F);
					w = v;
					y = textureAtlasSprite.getV(1.0F);
					z = textureAtlasSprite.getU(1.0F);
					aa = y;
					ab = z;
					ac = x;
				} else {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[1];
					float ad = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
					float ae = Mth.sin(ad) * 0.25F;
					float af = Mth.cos(ad) * 0.25F;
					float ag = 0.5F;
					v = textureAtlasSprite.getU(0.5F + (-af - ae));
					x = textureAtlasSprite.getV(0.5F + -af + ae);
					w = textureAtlasSprite.getU(0.5F + -af + ae);
					y = textureAtlasSprite.getV(0.5F + af + ae);
					z = textureAtlasSprite.getU(0.5F + af + ae);
					aa = textureAtlasSprite.getV(0.5F + (af - ae));
					ab = textureAtlasSprite.getU(0.5F + (af - ae));
					ac = textureAtlasSprite.getV(0.5F + (-af - ae));
				}

				float ah = (v + w + z + ab) / 4.0F;
				float ad = (x + y + aa + ac) / 4.0F;
				float ae = textureAtlasSprites[0].uvShrinkRatio();
				v = Mth.lerp(ae, v, ah);
				w = Mth.lerp(ae, w, ah);
				z = Mth.lerp(ae, z, ah);
				ab = Mth.lerp(ae, ab, ah);
				x = Mth.lerp(ae, x, ad);
				y = Mth.lerp(ae, y, ad);
				aa = Mth.lerp(ae, aa, ad);
				ac = Mth.lerp(ae, ac, ad);
				int ai = this.getLightColor(blockAndTintGetter, blockPos);
				float ag = l * g;
				float aj = l * h;
				float ak = l * j;
				this.vertex(vertexConsumer, d + 0.0, e + (double)q, f + 0.0, ag, aj, ak, v, x, ai);
				this.vertex(vertexConsumer, d + 0.0, e + (double)s, f + 1.0, ag, aj, ak, w, y, ai);
				this.vertex(vertexConsumer, d + 1.0, e + (double)r, f + 1.0, ag, aj, ak, z, aa, ai);
				this.vertex(vertexConsumer, d + 1.0, e + (double)p, f + 0.0, ag, aj, ak, ab, ac, ai);
				if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
					this.vertex(vertexConsumer, d + 0.0, e + (double)q, f + 0.0, ag, aj, ak, v, x, ai);
					this.vertex(vertexConsumer, d + 1.0, e + (double)p, f + 0.0, ag, aj, ak, ab, ac, ai);
					this.vertex(vertexConsumer, d + 1.0, e + (double)r, f + 1.0, ag, aj, ak, z, aa, ai);
					this.vertex(vertexConsumer, d + 0.0, e + (double)s, f + 1.0, ag, aj, ak, w, y, ai);
				}
			}

			if (bl3) {
				float vx = textureAtlasSprites[0].getU0();
				float wx = textureAtlasSprites[0].getU1();
				float zx = textureAtlasSprites[0].getV0();
				float abx = textureAtlasSprites[0].getV1();
				int al = this.getLightColor(blockAndTintGetter, blockPos.below());
				float yx = k * g;
				float aax = k * h;
				float acx = k * j;
				this.vertex(vertexConsumer, d, e + (double)u, f + 1.0, yx, aax, acx, vx, abx, al);
				this.vertex(vertexConsumer, d, e + (double)u, f, yx, aax, acx, vx, zx, al);
				this.vertex(vertexConsumer, d + 1.0, e + (double)u, f, yx, aax, acx, wx, zx, al);
				this.vertex(vertexConsumer, d + 1.0, e + (double)u, f + 1.0, yx, aax, acx, wx, abx, al);
			}

			int am = this.getLightColor(blockAndTintGetter, blockPos);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				float abx;
				float xx;
				double an;
				double ap;
				double ao;
				double aq;
				boolean bl8;
				switch (direction) {
					case NORTH:
						abx = q;
						xx = p;
						an = d;
						ao = d + 1.0;
						ap = f + 0.001F;
						aq = f + 0.001F;
						bl8 = bl4;
						break;
					case SOUTH:
						abx = r;
						xx = s;
						an = d + 1.0;
						ao = d;
						ap = f + 1.0 - 0.001F;
						aq = f + 1.0 - 0.001F;
						bl8 = bl5;
						break;
					case WEST:
						abx = s;
						xx = q;
						an = d + 0.001F;
						ao = d + 0.001F;
						ap = f + 1.0;
						aq = f;
						bl8 = bl6;
						break;
					default:
						abx = p;
						xx = r;
						an = d + 1.0 - 0.001F;
						ao = d + 1.0 - 0.001F;
						ap = f;
						aq = f + 1.0;
						bl8 = bl7;
				}

				if (bl8
					&& !isFaceOccludedByNeighbor(blockAndTintGetter, blockPos, direction, Math.max(abx, xx), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) {
					BlockPos blockPos2 = blockPos.relative(direction);
					TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
					if (!bl) {
						Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
						if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
							textureAtlasSprite2 = this.waterOverlay;
						}
					}

					float ar = textureAtlasSprite2.getU(0.0F);
					float as = textureAtlasSprite2.getU(0.5F);
					float at = textureAtlasSprite2.getV((1.0F - abx) * 0.5F);
					float au = textureAtlasSprite2.getV((1.0F - xx) * 0.5F);
					float av = textureAtlasSprite2.getV(0.5F);
					float aw = direction.getAxis() == Direction.Axis.Z ? m : n;
					float ax = l * aw * g;
					float ay = l * aw * h;
					float az = l * aw * j;
					this.vertex(vertexConsumer, an, e + (double)abx, ap, ax, ay, az, ar, at, am);
					this.vertex(vertexConsumer, ao, e + (double)xx, aq, ax, ay, az, as, au, am);
					this.vertex(vertexConsumer, ao, e + (double)u, aq, ax, ay, az, as, av, am);
					this.vertex(vertexConsumer, an, e + (double)u, ap, ax, ay, az, ar, av, am);
					if (textureAtlasSprite2 != this.waterOverlay) {
						this.vertex(vertexConsumer, an, e + (double)u, ap, ax, ay, az, ar, av, am);
						this.vertex(vertexConsumer, ao, e + (double)u, aq, ax, ay, az, as, av, am);
						this.vertex(vertexConsumer, ao, e + (double)xx, aq, ax, ay, az, as, au, am);
						this.vertex(vertexConsumer, an, e + (double)abx, ap, ax, ay, az, ar, at, am);
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

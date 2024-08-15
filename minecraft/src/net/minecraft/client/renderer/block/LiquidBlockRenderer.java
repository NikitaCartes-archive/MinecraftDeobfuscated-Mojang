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

	private static boolean isFaceOccludedByState(Direction direction, float f, BlockState blockState) {
		VoxelShape voxelShape = blockState.getFaceOcclusionShape(direction.getOpposite());
		if (voxelShape == Shapes.empty()) {
			return false;
		} else if (voxelShape == Shapes.block()) {
			boolean bl = f == 1.0F;
			return direction != Direction.UP || bl;
		} else {
			VoxelShape voxelShape2 = Shapes.box(0.0, 0.0, 0.0, 1.0, (double)f, 1.0);
			return Shapes.blockOccudes(voxelShape2, voxelShape, direction);
		}
	}

	private static boolean isFaceOccludedByNeighbor(Direction direction, float f, BlockState blockState) {
		return isFaceOccludedByState(direction, f, blockState);
	}

	private static boolean isFaceOccludedBySelf(BlockState blockState, Direction direction) {
		return isFaceOccludedByState(direction.getOpposite(), 1.0F, blockState);
	}

	public static boolean shouldRenderFace(FluidState fluidState, BlockState blockState, Direction direction, FluidState fluidState2) {
		return !isFaceOccludedBySelf(blockState, direction) && !isNeighborSameFluid(fluidState, fluidState2);
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
		boolean bl3 = shouldRenderFace(fluidState, blockState, Direction.DOWN, fluidState2) && !isFaceOccludedByNeighbor(Direction.DOWN, 0.8888889F, blockState2);
		boolean bl4 = shouldRenderFace(fluidState, blockState, Direction.NORTH, fluidState4);
		boolean bl5 = shouldRenderFace(fluidState, blockState, Direction.SOUTH, fluidState5);
		boolean bl6 = shouldRenderFace(fluidState, blockState, Direction.WEST, fluidState6);
		boolean bl7 = shouldRenderFace(fluidState, blockState, Direction.EAST, fluidState7);
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

			float s = (float)(blockPos.getX() & 15);
			float t = (float)(blockPos.getY() & 15);
			float u = (float)(blockPos.getZ() & 15);
			float v = 0.001F;
			float w = bl3 ? 0.001F : 0.0F;
			if (bl2 && !isFaceOccludedByNeighbor(Direction.UP, Math.min(Math.min(p, r), Math.min(q, o)), blockState3)) {
				p -= 0.001F;
				r -= 0.001F;
				q -= 0.001F;
				o -= 0.001F;
				Vec3 vec3 = fluidState.getFlow(blockAndTintGetter, blockPos);
				float x;
				float z;
				float ab;
				float ad;
				float y;
				float aa;
				float ac;
				float ae;
				if (vec3.x == 0.0 && vec3.z == 0.0) {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[0];
					x = textureAtlasSprite.getU(0.0F);
					y = textureAtlasSprite.getV(0.0F);
					z = x;
					aa = textureAtlasSprite.getV(1.0F);
					ab = textureAtlasSprite.getU(1.0F);
					ac = aa;
					ad = ab;
					ae = y;
				} else {
					TextureAtlasSprite textureAtlasSprite = textureAtlasSprites[1];
					float af = (float)Mth.atan2(vec3.z, vec3.x) - (float) (Math.PI / 2);
					float ag = Mth.sin(af) * 0.25F;
					float ah = Mth.cos(af) * 0.25F;
					float ai = 0.5F;
					x = textureAtlasSprite.getU(0.5F + (-ah - ag));
					y = textureAtlasSprite.getV(0.5F + -ah + ag);
					z = textureAtlasSprite.getU(0.5F + -ah + ag);
					aa = textureAtlasSprite.getV(0.5F + ah + ag);
					ab = textureAtlasSprite.getU(0.5F + ah + ag);
					ac = textureAtlasSprite.getV(0.5F + (ah - ag));
					ad = textureAtlasSprite.getU(0.5F + (ah - ag));
					ae = textureAtlasSprite.getV(0.5F + (-ah - ag));
				}

				float aj = (x + z + ab + ad) / 4.0F;
				float af = (y + aa + ac + ae) / 4.0F;
				float ag = textureAtlasSprites[0].uvShrinkRatio();
				x = Mth.lerp(ag, x, aj);
				z = Mth.lerp(ag, z, aj);
				ab = Mth.lerp(ag, ab, aj);
				ad = Mth.lerp(ag, ad, aj);
				y = Mth.lerp(ag, y, af);
				aa = Mth.lerp(ag, aa, af);
				ac = Mth.lerp(ag, ac, af);
				ae = Mth.lerp(ag, ae, af);
				int ak = this.getLightColor(blockAndTintGetter, blockPos);
				float ai = k * f;
				float al = k * g;
				float am = k * h;
				this.vertex(vertexConsumer, s + 0.0F, t + p, u + 0.0F, ai, al, am, x, y, ak);
				this.vertex(vertexConsumer, s + 0.0F, t + r, u + 1.0F, ai, al, am, z, aa, ak);
				this.vertex(vertexConsumer, s + 1.0F, t + q, u + 1.0F, ai, al, am, ab, ac, ak);
				this.vertex(vertexConsumer, s + 1.0F, t + o, u + 0.0F, ai, al, am, ad, ae, ak);
				if (fluidState.shouldRenderBackwardUpFace(blockAndTintGetter, blockPos.above())) {
					this.vertex(vertexConsumer, s + 0.0F, t + p, u + 0.0F, ai, al, am, x, y, ak);
					this.vertex(vertexConsumer, s + 1.0F, t + o, u + 0.0F, ai, al, am, ad, ae, ak);
					this.vertex(vertexConsumer, s + 1.0F, t + q, u + 1.0F, ai, al, am, ab, ac, ak);
					this.vertex(vertexConsumer, s + 0.0F, t + r, u + 1.0F, ai, al, am, z, aa, ak);
				}
			}

			if (bl3) {
				float xx = textureAtlasSprites[0].getU0();
				float zx = textureAtlasSprites[0].getU1();
				float abx = textureAtlasSprites[0].getV0();
				float adx = textureAtlasSprites[0].getV1();
				int an = this.getLightColor(blockAndTintGetter, blockPos.below());
				float aax = j * f;
				float acx = j * g;
				float aex = j * h;
				this.vertex(vertexConsumer, s, t + w, u + 1.0F, aax, acx, aex, xx, adx, an);
				this.vertex(vertexConsumer, s, t + w, u, aax, acx, aex, xx, abx, an);
				this.vertex(vertexConsumer, s + 1.0F, t + w, u, aax, acx, aex, zx, abx, an);
				this.vertex(vertexConsumer, s + 1.0F, t + w, u + 1.0F, aax, acx, aex, zx, adx, an);
			}

			int ao = this.getLightColor(blockAndTintGetter, blockPos);

			for (Direction direction : Direction.Plane.HORIZONTAL) {
				float adx;
				float yx;
				float aax;
				float acx;
				float aex;
				float ap;
				boolean bl8;
				switch (direction) {
					case NORTH:
						adx = p;
						yx = o;
						aax = s;
						aex = s + 1.0F;
						acx = u + 0.001F;
						ap = u + 0.001F;
						bl8 = bl4;
						break;
					case SOUTH:
						adx = q;
						yx = r;
						aax = s + 1.0F;
						aex = s;
						acx = u + 1.0F - 0.001F;
						ap = u + 1.0F - 0.001F;
						bl8 = bl5;
						break;
					case WEST:
						adx = r;
						yx = p;
						aax = s + 0.001F;
						aex = s + 0.001F;
						acx = u + 1.0F;
						ap = u;
						bl8 = bl6;
						break;
					default:
						adx = o;
						yx = q;
						aax = s + 1.0F - 0.001F;
						aex = s + 1.0F - 0.001F;
						acx = u;
						ap = u + 1.0F;
						bl8 = bl7;
				}

				if (bl8 && !isFaceOccludedByNeighbor(direction, Math.max(adx, yx), blockAndTintGetter.getBlockState(blockPos.relative(direction)))) {
					BlockPos blockPos2 = blockPos.relative(direction);
					TextureAtlasSprite textureAtlasSprite2 = textureAtlasSprites[1];
					if (!bl) {
						Block block = blockAndTintGetter.getBlockState(blockPos2).getBlock();
						if (block instanceof HalfTransparentBlock || block instanceof LeavesBlock) {
							textureAtlasSprite2 = this.waterOverlay;
						}
					}

					float ah = textureAtlasSprite2.getU(0.0F);
					float ai = textureAtlasSprite2.getU(0.5F);
					float al = textureAtlasSprite2.getV((1.0F - adx) * 0.5F);
					float am = textureAtlasSprite2.getV((1.0F - yx) * 0.5F);
					float aq = textureAtlasSprite2.getV(0.5F);
					float ar = direction.getAxis() == Direction.Axis.Z ? l : m;
					float as = k * ar * f;
					float at = k * ar * g;
					float au = k * ar * h;
					this.vertex(vertexConsumer, aax, t + adx, acx, as, at, au, ah, al, ao);
					this.vertex(vertexConsumer, aex, t + yx, ap, as, at, au, ai, am, ao);
					this.vertex(vertexConsumer, aex, t + w, ap, as, at, au, ai, aq, ao);
					this.vertex(vertexConsumer, aax, t + w, acx, as, at, au, ah, aq, ao);
					if (textureAtlasSprite2 != this.waterOverlay) {
						this.vertex(vertexConsumer, aax, t + w, acx, as, at, au, ah, aq, ao);
						this.vertex(vertexConsumer, aex, t + w, ap, as, at, au, ai, aq, ao);
						this.vertex(vertexConsumer, aex, t + yx, ap, as, at, au, ai, am, ao);
						this.vertex(vertexConsumer, aax, t + adx, acx, as, at, au, ah, al, ao);
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

	private void vertex(VertexConsumer vertexConsumer, float f, float g, float h, float i, float j, float k, float l, float m, int n) {
		vertexConsumer.addVertex(f, g, h).setColor(i, j, k, 1.0F).setUv(l, m).setLight(n).setNormal(0.0F, 1.0F, 0.0F);
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

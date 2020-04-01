package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import it.unimi.dsi.fastutil.longs.Long2FloatLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class ModelBlockRenderer {
	private final BlockColors blockColors;
	private static final ThreadLocal<ModelBlockRenderer.Cache> CACHE = ThreadLocal.withInitial(() -> new ModelBlockRenderer.Cache());

	public ModelBlockRenderer(BlockColors blockColors) {
		this.blockColors = blockColors;
	}

	public boolean tesselateBlock(
		BlockAndTintGetter blockAndTintGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l,
		int i
	) {
		boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
		Vec3 vec3 = blockState.getOffset(blockAndTintGetter, blockPos);
		poseStack.translate(vec3.x, vec3.y, vec3.z);

		try {
			return bl2
				? this.tesselateWithAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i)
				: this.tesselateWithoutAO(blockAndTintGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l, i);
		} catch (Throwable var17) {
			CrashReport crashReport = CrashReport.forThrowable(var17, "Tesselating block model");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
			crashReportCategory.setDetail("Using AO", bl2);
			throw new ReportedException(crashReport);
		}
	}

	public boolean tesselateWithAO(
		BlockAndTintGetter blockAndTintGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l,
		int i
	) {
		boolean bl2 = false;
		float[] fs = new float[Direction.values().length * 2];
		BitSet bitSet = new BitSet(3);
		ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionFace = new ModelBlockRenderer.AmbientOcclusionFace();

		for (Direction direction : Direction.values()) {
			random.setSeed(l);
			List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
			if (!list.isEmpty() && (!bl || Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction))) {
				this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list, fs, bitSet, ambientOcclusionFace, i);
				bl2 = true;
			}
		}

		random.setSeed(l);
		List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
		if (!list2.isEmpty()) {
			this.renderModelFaceAO(blockAndTintGetter, blockState, blockPos, poseStack, vertexConsumer, list2, fs, bitSet, ambientOcclusionFace, i);
			bl2 = true;
		}

		return bl2;
	}

	public boolean tesselateWithoutAO(
		BlockAndTintGetter blockAndTintGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l,
		int i
	) {
		boolean bl2 = false;
		BitSet bitSet = new BitSet(3);

		for (Direction direction : Direction.values()) {
			random.setSeed(l);
			List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
			if (!list.isEmpty() && (!bl || Block.shouldRenderFace(blockState, blockAndTintGetter, blockPos, direction))) {
				int j = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos.relative(direction));
				this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, j, i, false, poseStack, vertexConsumer, list, bitSet);
				bl2 = true;
			}
		}

		random.setSeed(l);
		List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
		if (!list2.isEmpty()) {
			this.renderModelFaceFlat(blockAndTintGetter, blockState, blockPos, -1, i, true, poseStack, vertexConsumer, list2, bitSet);
			bl2 = true;
		}

		return bl2;
	}

	private void renderModelFaceAO(
		BlockAndTintGetter blockAndTintGetter,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		List<BakedQuad> list,
		float[] fs,
		BitSet bitSet,
		ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionFace,
		int i
	) {
		for (BakedQuad bakedQuad : list) {
			this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), fs, bitSet);
			ambientOcclusionFace.calculate(blockAndTintGetter, blockState, blockPos, bakedQuad.getDirection(), fs, bitSet, bakedQuad.isShade());
			this.putQuadData(
				blockAndTintGetter,
				blockState,
				blockPos,
				vertexConsumer,
				poseStack.last(),
				bakedQuad,
				ambientOcclusionFace.brightness[0],
				ambientOcclusionFace.brightness[1],
				ambientOcclusionFace.brightness[2],
				ambientOcclusionFace.brightness[3],
				ambientOcclusionFace.lightmap[0],
				ambientOcclusionFace.lightmap[1],
				ambientOcclusionFace.lightmap[2],
				ambientOcclusionFace.lightmap[3],
				i
			);
		}
	}

	private void putQuadData(
		BlockAndTintGetter blockAndTintGetter,
		BlockState blockState,
		BlockPos blockPos,
		VertexConsumer vertexConsumer,
		PoseStack.Pose pose,
		BakedQuad bakedQuad,
		float f,
		float g,
		float h,
		float i,
		int j,
		int k,
		int l,
		int m,
		int n
	) {
		float p;
		float q;
		float r;
		if (bakedQuad.isTinted()) {
			int o = this.blockColors.getColor(blockState, blockAndTintGetter, blockPos, bakedQuad.getTintIndex());
			p = (float)(o >> 16 & 0xFF) / 255.0F;
			q = (float)(o >> 8 & 0xFF) / 255.0F;
			r = (float)(o & 0xFF) / 255.0F;
		} else {
			p = 1.0F;
			q = 1.0F;
			r = 1.0F;
		}

		Vector3f vector3f = blockAndTintGetter.getExtraTint(blockState, blockPos);
		p *= vector3f.x();
		q *= vector3f.y();
		r *= vector3f.z();
		vertexConsumer.putBulkData(pose, bakedQuad, new float[]{f, g, h, i}, p, q, r, new int[]{j, k, l, m}, n, true);
	}

	private void calculateShape(
		BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, int[] is, Direction direction, @Nullable float[] fs, BitSet bitSet
	) {
		float f = 32.0F;
		float g = 32.0F;
		float h = 32.0F;
		float i = -32.0F;
		float j = -32.0F;
		float k = -32.0F;

		for (int l = 0; l < 4; l++) {
			float m = Float.intBitsToFloat(is[l * 8]);
			float n = Float.intBitsToFloat(is[l * 8 + 1]);
			float o = Float.intBitsToFloat(is[l * 8 + 2]);
			f = Math.min(f, m);
			g = Math.min(g, n);
			h = Math.min(h, o);
			i = Math.max(i, m);
			j = Math.max(j, n);
			k = Math.max(k, o);
		}

		if (fs != null) {
			fs[Direction.WEST.get3DDataValue()] = f;
			fs[Direction.EAST.get3DDataValue()] = i;
			fs[Direction.DOWN.get3DDataValue()] = g;
			fs[Direction.UP.get3DDataValue()] = j;
			fs[Direction.NORTH.get3DDataValue()] = h;
			fs[Direction.SOUTH.get3DDataValue()] = k;
			int l = Direction.values().length;
			fs[Direction.WEST.get3DDataValue() + l] = 1.0F - f;
			fs[Direction.EAST.get3DDataValue() + l] = 1.0F - i;
			fs[Direction.DOWN.get3DDataValue() + l] = 1.0F - g;
			fs[Direction.UP.get3DDataValue() + l] = 1.0F - j;
			fs[Direction.NORTH.get3DDataValue() + l] = 1.0F - h;
			fs[Direction.SOUTH.get3DDataValue() + l] = 1.0F - k;
		}

		float p = 1.0E-4F;
		float m = 0.9999F;
		switch (direction) {
			case DOWN:
				bitSet.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, g == j && (g < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
				break;
			case UP:
				bitSet.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, g == j && (j > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
				break;
			case NORTH:
				bitSet.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
				bitSet.set(0, h == k && (h < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
				break;
			case SOUTH:
				bitSet.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
				bitSet.set(0, h == k && (k > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
				break;
			case WEST:
				bitSet.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, f == i && (f < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
				break;
			case EAST:
				bitSet.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, f == i && (i > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndTintGetter, blockPos)));
		}
	}

	private void renderModelFaceFlat(
		BlockAndTintGetter blockAndTintGetter,
		BlockState blockState,
		BlockPos blockPos,
		int i,
		int j,
		boolean bl,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		List<BakedQuad> list,
		BitSet bitSet
	) {
		for (BakedQuad bakedQuad : list) {
			if (bl) {
				this.calculateShape(blockAndTintGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, bitSet);
				BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(bakedQuad.getDirection()) : blockPos;
				i = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos2);
			}

			float f = blockAndTintGetter.getShade(bakedQuad.getDirection(), bakedQuad.isShade());
			this.putQuadData(blockAndTintGetter, blockState, blockPos, vertexConsumer, poseStack.last(), bakedQuad, f, f, f, f, i, i, i, i, j);
		}
	}

	public void renderModel(
		PoseStack.Pose pose, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i, int j
	) {
		Random random = new Random();
		long l = 42L;

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			renderQuadList(pose, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, direction, random), i, j);
		}

		random.setSeed(42L);
		renderQuadList(pose, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, null, random), i, j);
	}

	private static void renderQuadList(PoseStack.Pose pose, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i, int j) {
		for (BakedQuad bakedQuad : list) {
			float k;
			float l;
			float m;
			if (bakedQuad.isTinted()) {
				k = Mth.clamp(f, 0.0F, 1.0F);
				l = Mth.clamp(g, 0.0F, 1.0F);
				m = Mth.clamp(h, 0.0F, 1.0F);
			} else {
				k = 1.0F;
				l = 1.0F;
				m = 1.0F;
			}

			vertexConsumer.putBulkData(pose, bakedQuad, k, l, m, i, j);
		}
	}

	public static void enableCaching() {
		((ModelBlockRenderer.Cache)CACHE.get()).enable();
	}

	public static void clearCache() {
		((ModelBlockRenderer.Cache)CACHE.get()).disable();
	}

	@Environment(EnvType.CLIENT)
	public static enum AdjacencyInfo {
		DOWN(
			new Direction[]{Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH},
			0.5F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.SOUTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.SOUTH
			}
		),
		UP(
			new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH},
			1.0F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.SOUTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.SOUTH
			}
		),
		NORTH(
			new Direction[]{Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST},
			0.8F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_WEST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_EAST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_EAST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_WEST
			}
		),
		SOUTH(
			new Direction[]{Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP},
			0.8F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.WEST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_WEST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.WEST,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.WEST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.EAST
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_EAST,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.EAST,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.EAST
			}
		),
		WEST(
			new Direction[]{Direction.UP, Direction.DOWN, Direction.NORTH, Direction.SOUTH},
			0.6F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.SOUTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.SOUTH
			}
		),
		EAST(
			new Direction[]{Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH},
			0.6F,
			true,
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.SOUTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.DOWN,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.NORTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_NORTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.NORTH
			},
			new ModelBlockRenderer.SizeInfo[]{
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.SOUTH,
				ModelBlockRenderer.SizeInfo.FLIP_UP,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.FLIP_SOUTH,
				ModelBlockRenderer.SizeInfo.UP,
				ModelBlockRenderer.SizeInfo.SOUTH
			}
		);

		private final Direction[] corners;
		private final boolean doNonCubicWeight;
		private final ModelBlockRenderer.SizeInfo[] vert0Weights;
		private final ModelBlockRenderer.SizeInfo[] vert1Weights;
		private final ModelBlockRenderer.SizeInfo[] vert2Weights;
		private final ModelBlockRenderer.SizeInfo[] vert3Weights;
		private static final ModelBlockRenderer.AdjacencyInfo[] BY_FACING = Util.make(new ModelBlockRenderer.AdjacencyInfo[6], adjacencyInfos -> {
			adjacencyInfos[Direction.DOWN.get3DDataValue()] = DOWN;
			adjacencyInfos[Direction.UP.get3DDataValue()] = UP;
			adjacencyInfos[Direction.NORTH.get3DDataValue()] = NORTH;
			adjacencyInfos[Direction.SOUTH.get3DDataValue()] = SOUTH;
			adjacencyInfos[Direction.WEST.get3DDataValue()] = WEST;
			adjacencyInfos[Direction.EAST.get3DDataValue()] = EAST;
		});

		private AdjacencyInfo(
			Direction[] directions,
			float f,
			boolean bl,
			ModelBlockRenderer.SizeInfo[] sizeInfos,
			ModelBlockRenderer.SizeInfo[] sizeInfos2,
			ModelBlockRenderer.SizeInfo[] sizeInfos3,
			ModelBlockRenderer.SizeInfo[] sizeInfos4
		) {
			this.corners = directions;
			this.doNonCubicWeight = bl;
			this.vert0Weights = sizeInfos;
			this.vert1Weights = sizeInfos2;
			this.vert2Weights = sizeInfos3;
			this.vert3Weights = sizeInfos4;
		}

		public static ModelBlockRenderer.AdjacencyInfo fromFacing(Direction direction) {
			return BY_FACING[direction.get3DDataValue()];
		}
	}

	@Environment(EnvType.CLIENT)
	class AmbientOcclusionFace {
		private final float[] brightness = new float[4];
		private final int[] lightmap = new int[4];

		public AmbientOcclusionFace() {
		}

		public void calculate(
			BlockAndTintGetter blockAndTintGetter, BlockState blockState, BlockPos blockPos, Direction direction, float[] fs, BitSet bitSet, boolean bl
		) {
			BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(direction) : blockPos;
			ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			ModelBlockRenderer.Cache cache = (ModelBlockRenderer.Cache)ModelBlockRenderer.CACHE.get();
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[0]);
			BlockState blockState2 = blockAndTintGetter.getBlockState(mutableBlockPos);
			int i = cache.getLightColor(blockState2, blockAndTintGetter, mutableBlockPos);
			float f = cache.getShadeBrightness(blockState2, blockAndTintGetter, mutableBlockPos);
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[1]);
			BlockState blockState3 = blockAndTintGetter.getBlockState(mutableBlockPos);
			int j = cache.getLightColor(blockState3, blockAndTintGetter, mutableBlockPos);
			float g = cache.getShadeBrightness(blockState3, blockAndTintGetter, mutableBlockPos);
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[2]);
			BlockState blockState4 = blockAndTintGetter.getBlockState(mutableBlockPos);
			int k = cache.getLightColor(blockState4, blockAndTintGetter, mutableBlockPos);
			float h = cache.getShadeBrightness(blockState4, blockAndTintGetter, mutableBlockPos);
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[3]);
			BlockState blockState5 = blockAndTintGetter.getBlockState(mutableBlockPos);
			int l = cache.getLightColor(blockState5, blockAndTintGetter, mutableBlockPos);
			float m = cache.getShadeBrightness(blockState5, blockAndTintGetter, mutableBlockPos);
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[0]).move(direction);
			boolean bl2 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[1]).move(direction);
			boolean bl3 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[2]).move(direction);
			boolean bl4 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
			mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[3]).move(direction);
			boolean bl5 = blockAndTintGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndTintGetter, mutableBlockPos) == 0;
			float n;
			int o;
			if (!bl4 && !bl2) {
				n = f;
				o = i;
			} else {
				mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
				BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
				n = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
				o = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
			}

			float p;
			int q;
			if (!bl5 && !bl2) {
				p = f;
				q = i;
			} else {
				mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
				BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
				p = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
				q = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
			}

			float r;
			int s;
			if (!bl4 && !bl3) {
				r = f;
				s = i;
			} else {
				mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
				BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
				r = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
				s = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
			}

			float t;
			int u;
			if (!bl5 && !bl3) {
				t = f;
				u = i;
			} else {
				mutableBlockPos.setWithOffset(blockPos2, adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
				BlockState blockState6 = blockAndTintGetter.getBlockState(mutableBlockPos);
				t = cache.getShadeBrightness(blockState6, blockAndTintGetter, mutableBlockPos);
				u = cache.getLightColor(blockState6, blockAndTintGetter, mutableBlockPos);
			}

			int v = cache.getLightColor(blockState, blockAndTintGetter, blockPos);
			mutableBlockPos.setWithOffset(blockPos, direction);
			BlockState blockState7 = blockAndTintGetter.getBlockState(mutableBlockPos);
			if (bitSet.get(0) || !blockState7.isSolidRender(blockAndTintGetter, mutableBlockPos)) {
				v = cache.getLightColor(blockState7, blockAndTintGetter, mutableBlockPos);
			}

			float w = bitSet.get(0)
				? cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos2), blockAndTintGetter, blockPos2)
				: cache.getShadeBrightness(blockAndTintGetter.getBlockState(blockPos), blockAndTintGetter, blockPos);
			ModelBlockRenderer.AmbientVertexRemap ambientVertexRemap = ModelBlockRenderer.AmbientVertexRemap.fromFacing(direction);
			if (bitSet.get(1) && adjacencyInfo.doNonCubicWeight) {
				float x = (m + f + p + w) * 0.25F;
				float y = (h + f + n + w) * 0.25F;
				float z = (h + g + r + w) * 0.25F;
				float aa = (m + g + t + w) * 0.25F;
				float ab = fs[adjacencyInfo.vert0Weights[0].shape] * fs[adjacencyInfo.vert0Weights[1].shape];
				float ac = fs[adjacencyInfo.vert0Weights[2].shape] * fs[adjacencyInfo.vert0Weights[3].shape];
				float ad = fs[adjacencyInfo.vert0Weights[4].shape] * fs[adjacencyInfo.vert0Weights[5].shape];
				float ae = fs[adjacencyInfo.vert0Weights[6].shape] * fs[adjacencyInfo.vert0Weights[7].shape];
				float af = fs[adjacencyInfo.vert1Weights[0].shape] * fs[adjacencyInfo.vert1Weights[1].shape];
				float ag = fs[adjacencyInfo.vert1Weights[2].shape] * fs[adjacencyInfo.vert1Weights[3].shape];
				float ah = fs[adjacencyInfo.vert1Weights[4].shape] * fs[adjacencyInfo.vert1Weights[5].shape];
				float ai = fs[adjacencyInfo.vert1Weights[6].shape] * fs[adjacencyInfo.vert1Weights[7].shape];
				float aj = fs[adjacencyInfo.vert2Weights[0].shape] * fs[adjacencyInfo.vert2Weights[1].shape];
				float ak = fs[adjacencyInfo.vert2Weights[2].shape] * fs[adjacencyInfo.vert2Weights[3].shape];
				float al = fs[adjacencyInfo.vert2Weights[4].shape] * fs[adjacencyInfo.vert2Weights[5].shape];
				float am = fs[adjacencyInfo.vert2Weights[6].shape] * fs[adjacencyInfo.vert2Weights[7].shape];
				float an = fs[adjacencyInfo.vert3Weights[0].shape] * fs[adjacencyInfo.vert3Weights[1].shape];
				float ao = fs[adjacencyInfo.vert3Weights[2].shape] * fs[adjacencyInfo.vert3Weights[3].shape];
				float ap = fs[adjacencyInfo.vert3Weights[4].shape] * fs[adjacencyInfo.vert3Weights[5].shape];
				float aq = fs[adjacencyInfo.vert3Weights[6].shape] * fs[adjacencyInfo.vert3Weights[7].shape];
				this.brightness[ambientVertexRemap.vert0] = x * ab + y * ac + z * ad + aa * ae;
				this.brightness[ambientVertexRemap.vert1] = x * af + y * ag + z * ah + aa * ai;
				this.brightness[ambientVertexRemap.vert2] = x * aj + y * ak + z * al + aa * am;
				this.brightness[ambientVertexRemap.vert3] = x * an + y * ao + z * ap + aa * aq;
				int ar = this.blend(l, i, q, v);
				int as = this.blend(k, i, o, v);
				int at = this.blend(k, j, s, v);
				int au = this.blend(l, j, u, v);
				this.lightmap[ambientVertexRemap.vert0] = this.blend(ar, as, at, au, ab, ac, ad, ae);
				this.lightmap[ambientVertexRemap.vert1] = this.blend(ar, as, at, au, af, ag, ah, ai);
				this.lightmap[ambientVertexRemap.vert2] = this.blend(ar, as, at, au, aj, ak, al, am);
				this.lightmap[ambientVertexRemap.vert3] = this.blend(ar, as, at, au, an, ao, ap, aq);
			} else {
				float x = (m + f + p + w) * 0.25F;
				float y = (h + f + n + w) * 0.25F;
				float z = (h + g + r + w) * 0.25F;
				float aa = (m + g + t + w) * 0.25F;
				this.lightmap[ambientVertexRemap.vert0] = this.blend(l, i, q, v);
				this.lightmap[ambientVertexRemap.vert1] = this.blend(k, i, o, v);
				this.lightmap[ambientVertexRemap.vert2] = this.blend(k, j, s, v);
				this.lightmap[ambientVertexRemap.vert3] = this.blend(l, j, u, v);
				this.brightness[ambientVertexRemap.vert0] = x;
				this.brightness[ambientVertexRemap.vert1] = y;
				this.brightness[ambientVertexRemap.vert2] = z;
				this.brightness[ambientVertexRemap.vert3] = aa;
			}

			float x = blockAndTintGetter.getShade(direction, bl);

			for (int av = 0; av < this.brightness.length; av++) {
				this.brightness[av] = this.brightness[av] * x;
			}
		}

		private int blend(int i, int j, int k, int l) {
			if (i == 0) {
				i = l;
			}

			if (j == 0) {
				j = l;
			}

			if (k == 0) {
				k = l;
			}

			return i + j + k + l >> 2 & 16711935;
		}

		private int blend(int i, int j, int k, int l, float f, float g, float h, float m) {
			int n = (int)((float)(i >> 16 & 0xFF) * f + (float)(j >> 16 & 0xFF) * g + (float)(k >> 16 & 0xFF) * h + (float)(l >> 16 & 0xFF) * m) & 0xFF;
			int o = (int)((float)(i & 0xFF) * f + (float)(j & 0xFF) * g + (float)(k & 0xFF) * h + (float)(l & 0xFF) * m) & 0xFF;
			return n << 16 | o;
		}
	}

	@Environment(EnvType.CLIENT)
	static enum AmbientVertexRemap {
		DOWN(0, 1, 2, 3),
		UP(2, 3, 0, 1),
		NORTH(3, 0, 1, 2),
		SOUTH(0, 1, 2, 3),
		WEST(3, 0, 1, 2),
		EAST(1, 2, 3, 0);

		private final int vert0;
		private final int vert1;
		private final int vert2;
		private final int vert3;
		private static final ModelBlockRenderer.AmbientVertexRemap[] BY_FACING = Util.make(new ModelBlockRenderer.AmbientVertexRemap[6], ambientVertexRemaps -> {
			ambientVertexRemaps[Direction.DOWN.get3DDataValue()] = DOWN;
			ambientVertexRemaps[Direction.UP.get3DDataValue()] = UP;
			ambientVertexRemaps[Direction.NORTH.get3DDataValue()] = NORTH;
			ambientVertexRemaps[Direction.SOUTH.get3DDataValue()] = SOUTH;
			ambientVertexRemaps[Direction.WEST.get3DDataValue()] = WEST;
			ambientVertexRemaps[Direction.EAST.get3DDataValue()] = EAST;
		});

		private AmbientVertexRemap(int j, int k, int l, int m) {
			this.vert0 = j;
			this.vert1 = k;
			this.vert2 = l;
			this.vert3 = m;
		}

		public static ModelBlockRenderer.AmbientVertexRemap fromFacing(Direction direction) {
			return BY_FACING[direction.get3DDataValue()];
		}
	}

	@Environment(EnvType.CLIENT)
	static class Cache {
		private boolean enabled;
		private final Long2IntLinkedOpenHashMap colorCache = Util.make(() -> {
			Long2IntLinkedOpenHashMap long2IntLinkedOpenHashMap = new Long2IntLinkedOpenHashMap(100, 0.25F) {
				@Override
				protected void rehash(int i) {
				}
			};
			long2IntLinkedOpenHashMap.defaultReturnValue(Integer.MAX_VALUE);
			return long2IntLinkedOpenHashMap;
		});
		private final Long2FloatLinkedOpenHashMap brightnessCache = Util.make(() -> {
			Long2FloatLinkedOpenHashMap long2FloatLinkedOpenHashMap = new Long2FloatLinkedOpenHashMap(100, 0.25F) {
				@Override
				protected void rehash(int i) {
				}
			};
			long2FloatLinkedOpenHashMap.defaultReturnValue(Float.NaN);
			return long2FloatLinkedOpenHashMap;
		});

		private Cache() {
		}

		public void enable() {
			this.enabled = true;
		}

		public void disable() {
			this.enabled = false;
			this.colorCache.clear();
			this.brightnessCache.clear();
		}

		public int getLightColor(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
			long l = blockPos.asLong();
			if (this.enabled) {
				int i = this.colorCache.get(l);
				if (i != Integer.MAX_VALUE) {
					return i;
				}
			}

			int i = LevelRenderer.getLightColor(blockAndTintGetter, blockState, blockPos);
			if (this.enabled) {
				if (this.colorCache.size() == 100) {
					this.colorCache.removeFirstInt();
				}

				this.colorCache.put(l, i);
			}

			return i;
		}

		public float getShadeBrightness(BlockState blockState, BlockAndTintGetter blockAndTintGetter, BlockPos blockPos) {
			long l = blockPos.asLong();
			if (this.enabled) {
				float f = this.brightnessCache.get(l);
				if (!Float.isNaN(f)) {
					return f;
				}
			}

			float f = blockState.getShadeBrightness(blockAndTintGetter, blockPos);
			if (this.enabled) {
				if (this.brightnessCache.size() == 100) {
					this.brightnessCache.removeFirstFloat();
				}

				this.brightnessCache.put(l, f);
			}

			return f;
		}
	}

	@Environment(EnvType.CLIENT)
	public static enum SizeInfo {
		DOWN(Direction.DOWN, false),
		UP(Direction.UP, false),
		NORTH(Direction.NORTH, false),
		SOUTH(Direction.SOUTH, false),
		WEST(Direction.WEST, false),
		EAST(Direction.EAST, false),
		FLIP_DOWN(Direction.DOWN, true),
		FLIP_UP(Direction.UP, true),
		FLIP_NORTH(Direction.NORTH, true),
		FLIP_SOUTH(Direction.SOUTH, true),
		FLIP_WEST(Direction.WEST, true),
		FLIP_EAST(Direction.EAST, true);

		private final int shape;

		private SizeInfo(Direction direction, boolean bl) {
			this.shape = direction.get3DDataValue() + (bl ? Direction.values().length : 0);
		}
	}
}

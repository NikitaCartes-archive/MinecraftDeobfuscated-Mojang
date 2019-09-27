package net.minecraft.client.renderer.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
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
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.BlockAndBiomeGetter;
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
		BlockAndBiomeGetter blockAndBiomeGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l
	) {
		boolean bl2 = Minecraft.useAmbientOcclusion() && blockState.getLightEmission() == 0 && bakedModel.useAmbientOcclusion();
		Vec3 vec3 = blockState.getOffset(blockAndBiomeGetter, blockPos);
		poseStack.pushPose();
		poseStack.translate((double)(blockPos.getX() & 15) + vec3.x, (double)(blockPos.getY() & 15) + vec3.y, (double)(blockPos.getZ() & 15) + vec3.z);

		boolean throwable;
		try {
			if (!bl2) {
				return this.tesselateWithoutAO(blockAndBiomeGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l);
			}

			throwable = this.tesselateWithAO(blockAndBiomeGetter, bakedModel, blockState, blockPos, poseStack, vertexConsumer, bl, random, l);
		} catch (Throwable var19) {
			CrashReport crashReport = CrashReport.forThrowable(var19, "Tesselating block model");
			CrashReportCategory crashReportCategory = crashReport.addCategory("Block model being tesselated");
			CrashReportCategory.populateBlockDetails(crashReportCategory, blockPos, blockState);
			crashReportCategory.setDetail("Using AO", bl2);
			throw new ReportedException(crashReport);
		} finally {
			poseStack.popPose();
		}

		return throwable;
	}

	public boolean tesselateWithAO(
		BlockAndBiomeGetter blockAndBiomeGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l
	) {
		boolean bl2 = false;
		float[] fs = new float[Direction.values().length * 2];
		BitSet bitSet = new BitSet(3);
		ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionFace = new ModelBlockRenderer.AmbientOcclusionFace();

		for (Direction direction : Direction.values()) {
			random.setSeed(l);
			List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
			if (!list.isEmpty() && (!bl || Block.shouldRenderFace(blockState, blockAndBiomeGetter, blockPos, direction))) {
				this.renderModelFaceAO(blockAndBiomeGetter, blockState, blockPos, poseStack, vertexConsumer, list, fs, bitSet, ambientOcclusionFace);
				bl2 = true;
			}
		}

		random.setSeed(l);
		List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
		if (!list2.isEmpty()) {
			this.renderModelFaceAO(blockAndBiomeGetter, blockState, blockPos, poseStack, vertexConsumer, list2, fs, bitSet, ambientOcclusionFace);
			bl2 = true;
		}

		return bl2;
	}

	public boolean tesselateWithoutAO(
		BlockAndBiomeGetter blockAndBiomeGetter,
		BakedModel bakedModel,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		boolean bl,
		Random random,
		long l
	) {
		boolean bl2 = false;
		BitSet bitSet = new BitSet(3);

		for (Direction direction : Direction.values()) {
			random.setSeed(l);
			List<BakedQuad> list = bakedModel.getQuads(blockState, direction, random);
			if (!list.isEmpty() && (!bl || Block.shouldRenderFace(blockState, blockAndBiomeGetter, blockPos, direction))) {
				int i = blockAndBiomeGetter.getLightColor(blockState, blockPos.relative(direction));
				this.renderModelFaceFlat(blockAndBiomeGetter, blockState, blockPos, i, false, poseStack, vertexConsumer, list, bitSet);
				bl2 = true;
			}
		}

		random.setSeed(l);
		List<BakedQuad> list2 = bakedModel.getQuads(blockState, null, random);
		if (!list2.isEmpty()) {
			this.renderModelFaceFlat(blockAndBiomeGetter, blockState, blockPos, -1, true, poseStack, vertexConsumer, list2, bitSet);
			bl2 = true;
		}

		return bl2;
	}

	private void renderModelFaceAO(
		BlockAndBiomeGetter blockAndBiomeGetter,
		BlockState blockState,
		BlockPos blockPos,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		List<BakedQuad> list,
		float[] fs,
		BitSet bitSet,
		ModelBlockRenderer.AmbientOcclusionFace ambientOcclusionFace
	) {
		for (BakedQuad bakedQuad : list) {
			this.calculateShape(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), fs, bitSet);
			ambientOcclusionFace.calculate(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getDirection(), fs, bitSet);
			this.putQuadData(
				blockAndBiomeGetter,
				blockState,
				blockPos,
				vertexConsumer,
				poseStack.getPose(),
				bakedQuad,
				ambientOcclusionFace.brightness[0],
				ambientOcclusionFace.brightness[1],
				ambientOcclusionFace.brightness[2],
				ambientOcclusionFace.brightness[3],
				ambientOcclusionFace.lightmap[0],
				ambientOcclusionFace.lightmap[1],
				ambientOcclusionFace.lightmap[2],
				ambientOcclusionFace.lightmap[3]
			);
		}
	}

	private void putQuadData(
		BlockAndBiomeGetter blockAndBiomeGetter,
		BlockState blockState,
		BlockPos blockPos,
		VertexConsumer vertexConsumer,
		Matrix4f matrix4f,
		BakedQuad bakedQuad,
		float f,
		float g,
		float h,
		float i,
		int j,
		int k,
		int l,
		int m
	) {
		float o;
		float p;
		float q;
		if (bakedQuad.isTinted()) {
			int n = this.blockColors.getColor(blockState, blockAndBiomeGetter, blockPos, bakedQuad.getTintIndex());
			o = (float)(n >> 16 & 0xFF) / 255.0F;
			p = (float)(n >> 8 & 0xFF) / 255.0F;
			q = (float)(n & 0xFF) / 255.0F;
		} else {
			o = 1.0F;
			p = 1.0F;
			q = 1.0F;
		}

		vertexConsumer.putBulkData(matrix4f, bakedQuad, new float[]{f, g, h, i}, o, p, q, new int[]{j, k, l, m}, true);
	}

	private void calculateShape(
		BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, int[] is, Direction direction, @Nullable float[] fs, BitSet bitSet
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
				bitSet.set(0, g == j && (g < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
				break;
			case UP:
				bitSet.set(1, f >= 1.0E-4F || h >= 1.0E-4F || i <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, g == j && (j > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
				break;
			case NORTH:
				bitSet.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
				bitSet.set(0, h == k && (h < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
				break;
			case SOUTH:
				bitSet.set(1, f >= 1.0E-4F || g >= 1.0E-4F || i <= 0.9999F || j <= 0.9999F);
				bitSet.set(0, h == k && (k > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
				break;
			case WEST:
				bitSet.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, f == i && (f < 1.0E-4F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
				break;
			case EAST:
				bitSet.set(1, g >= 1.0E-4F || h >= 1.0E-4F || j <= 0.9999F || k <= 0.9999F);
				bitSet.set(0, f == i && (i > 0.9999F || blockState.isCollisionShapeFullBlock(blockAndBiomeGetter, blockPos)));
		}
	}

	private void renderModelFaceFlat(
		BlockAndBiomeGetter blockAndBiomeGetter,
		BlockState blockState,
		BlockPos blockPos,
		int i,
		boolean bl,
		PoseStack poseStack,
		VertexConsumer vertexConsumer,
		List<BakedQuad> list,
		BitSet bitSet
	) {
		for (BakedQuad bakedQuad : list) {
			if (bl) {
				this.calculateShape(blockAndBiomeGetter, blockState, blockPos, bakedQuad.getVertices(), bakedQuad.getDirection(), null, bitSet);
				BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(bakedQuad.getDirection()) : blockPos;
				i = blockAndBiomeGetter.getLightColor(blockState, blockPos2);
			}

			this.putQuadData(blockAndBiomeGetter, blockState, blockPos, vertexConsumer, poseStack.getPose(), bakedQuad, 1.0F, 1.0F, 1.0F, 1.0F, i, i, i, i);
		}
	}

	public void renderModel(
		Matrix4f matrix4f, VertexConsumer vertexConsumer, @Nullable BlockState blockState, BakedModel bakedModel, float f, float g, float h, int i
	) {
		Random random = new Random();
		long l = 42L;

		for (Direction direction : Direction.values()) {
			random.setSeed(42L);
			renderQuadList(matrix4f, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, direction, random), i);
		}

		random.setSeed(42L);
		renderQuadList(matrix4f, vertexConsumer, f, g, h, bakedModel.getQuads(blockState, null, random), i);
	}

	private static void renderQuadList(Matrix4f matrix4f, VertexConsumer vertexConsumer, float f, float g, float h, List<BakedQuad> list, int i) {
		for (BakedQuad bakedQuad : list) {
			float j;
			float k;
			float l;
			if (bakedQuad.isTinted()) {
				j = Mth.clamp(f, 0.0F, 1.0F);
				k = Mth.clamp(g, 0.0F, 1.0F);
				l = Mth.clamp(h, 0.0F, 1.0F);
			} else {
				j = 1.0F;
				k = 1.0F;
				l = 1.0F;
			}

			vertexConsumer.putBulkData(matrix4f, bakedQuad, j, k, l, i);
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

		public void calculate(BlockAndBiomeGetter blockAndBiomeGetter, BlockState blockState, BlockPos blockPos, Direction direction, float[] fs, BitSet bitSet) {
			BlockPos blockPos2 = bitSet.get(0) ? blockPos.relative(direction) : blockPos;
			ModelBlockRenderer.AdjacencyInfo adjacencyInfo = ModelBlockRenderer.AdjacencyInfo.fromFacing(direction);
			BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
			ModelBlockRenderer.Cache cache = (ModelBlockRenderer.Cache)ModelBlockRenderer.CACHE.get();
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]);
			BlockState blockState2 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
			int i = cache.getLightColor(blockState2, blockAndBiomeGetter, mutableBlockPos);
			float f = cache.getShadeBrightness(blockState2, blockAndBiomeGetter, mutableBlockPos);
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]);
			BlockState blockState3 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
			int j = cache.getLightColor(blockState3, blockAndBiomeGetter, mutableBlockPos);
			float g = cache.getShadeBrightness(blockState3, blockAndBiomeGetter, mutableBlockPos);
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[2]);
			BlockState blockState4 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
			int k = cache.getLightColor(blockState4, blockAndBiomeGetter, mutableBlockPos);
			float h = cache.getShadeBrightness(blockState4, blockAndBiomeGetter, mutableBlockPos);
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[3]);
			BlockState blockState5 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
			int l = cache.getLightColor(blockState5, blockAndBiomeGetter, mutableBlockPos);
			float m = cache.getShadeBrightness(blockState5, blockAndBiomeGetter, mutableBlockPos);
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(direction);
			boolean bl = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(direction);
			boolean bl2 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[2]).move(direction);
			boolean bl3 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
			mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[3]).move(direction);
			boolean bl4 = blockAndBiomeGetter.getBlockState(mutableBlockPos).getLightBlock(blockAndBiomeGetter, mutableBlockPos) == 0;
			float n;
			int o;
			if (!bl3 && !bl) {
				n = f;
				o = i;
			} else {
				mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(adjacencyInfo.corners[2]);
				BlockState blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
				n = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
				o = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
			}

			float p;
			int q;
			if (!bl4 && !bl) {
				p = f;
				q = i;
			} else {
				mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[0]).move(adjacencyInfo.corners[3]);
				BlockState blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
				p = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
				q = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
			}

			float r;
			int s;
			if (!bl3 && !bl2) {
				r = f;
				s = i;
			} else {
				mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(adjacencyInfo.corners[2]);
				BlockState blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
				r = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
				s = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
			}

			float t;
			int u;
			if (!bl4 && !bl2) {
				t = f;
				u = i;
			} else {
				mutableBlockPos.set(blockPos2).move(adjacencyInfo.corners[1]).move(adjacencyInfo.corners[3]);
				BlockState blockState6 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
				t = cache.getShadeBrightness(blockState6, blockAndBiomeGetter, mutableBlockPos);
				u = cache.getLightColor(blockState6, blockAndBiomeGetter, mutableBlockPos);
			}

			int v = cache.getLightColor(blockState, blockAndBiomeGetter, blockPos);
			mutableBlockPos.set(blockPos).move(direction);
			BlockState blockState7 = blockAndBiomeGetter.getBlockState(mutableBlockPos);
			if (bitSet.get(0) || !blockState7.isSolidRender(blockAndBiomeGetter, mutableBlockPos)) {
				v = cache.getLightColor(blockState7, blockAndBiomeGetter, mutableBlockPos);
			}

			float w = bitSet.get(0)
				? cache.getShadeBrightness(blockAndBiomeGetter.getBlockState(blockPos2), blockAndBiomeGetter, blockPos2)
				: cache.getShadeBrightness(blockAndBiomeGetter.getBlockState(blockPos), blockAndBiomeGetter, blockPos);
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

		public int getLightColor(BlockState blockState, BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
			long l = blockPos.asLong();
			if (this.enabled) {
				int i = this.colorCache.get(l);
				if (i != Integer.MAX_VALUE) {
					return i;
				}
			}

			int i = blockAndBiomeGetter.getLightColor(blockState, blockPos);
			if (this.enabled) {
				if (this.colorCache.size() == 100) {
					this.colorCache.removeFirstInt();
				}

				this.colorCache.put(l, i);
			}

			return i;
		}

		public float getShadeBrightness(BlockState blockState, BlockAndBiomeGetter blockAndBiomeGetter, BlockPos blockPos) {
			long l = blockPos.asLong();
			if (this.enabled) {
				float f = this.brightnessCache.get(l);
				if (!Float.isNaN(f)) {
					return f;
				}
			}

			float f = blockState.getShadeBrightness(blockAndBiomeGetter, blockPos);
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

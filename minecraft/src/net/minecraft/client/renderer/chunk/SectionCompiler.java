package net.minecraft.client.renderer.chunk;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexSorting;
import it.unimi.dsi.fastutil.objects.Reference2ObjectArrayMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.block.ModelBlockRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

@Environment(EnvType.CLIENT)
public class SectionCompiler {
	private final BlockRenderDispatcher blockRenderer;
	private final BlockEntityRenderDispatcher blockEntityRenderer;

	public SectionCompiler(BlockRenderDispatcher blockRenderDispatcher, BlockEntityRenderDispatcher blockEntityRenderDispatcher) {
		this.blockRenderer = blockRenderDispatcher;
		this.blockEntityRenderer = blockEntityRenderDispatcher;
	}

	public SectionCompiler.Results compile(
		SectionPos sectionPos, RenderChunkRegion renderChunkRegion, VertexSorting vertexSorting, SectionBufferBuilderPack sectionBufferBuilderPack
	) {
		SectionCompiler.Results results = new SectionCompiler.Results();
		BlockPos blockPos = sectionPos.origin();
		BlockPos blockPos2 = blockPos.offset(15, 15, 15);
		VisGraph visGraph = new VisGraph();
		PoseStack poseStack = new PoseStack();
		ModelBlockRenderer.enableCaching();
		Map<RenderType, BufferBuilder> map = new Reference2ObjectArrayMap<>(RenderType.chunkBufferLayers().size());
		RandomSource randomSource = RandomSource.create();

		for (BlockPos blockPos3 : BlockPos.betweenClosed(blockPos, blockPos2)) {
			BlockState blockState = renderChunkRegion.getBlockState(blockPos3);
			if (blockState.isSolidRender()) {
				visGraph.setOpaque(blockPos3);
			}

			if (blockState.hasBlockEntity()) {
				BlockEntity blockEntity = renderChunkRegion.getBlockEntity(blockPos3);
				if (blockEntity != null) {
					this.handleBlockEntity(results, blockEntity);
				}
			}

			FluidState fluidState = blockState.getFluidState();
			if (!fluidState.isEmpty()) {
				RenderType renderType = ItemBlockRenderTypes.getRenderLayer(fluidState);
				BufferBuilder bufferBuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, renderType);
				this.blockRenderer.renderLiquid(blockPos3, renderChunkRegion, bufferBuilder, blockState, fluidState);
			}

			if (blockState.getRenderShape() == RenderShape.MODEL) {
				RenderType renderType = ItemBlockRenderTypes.getChunkRenderType(blockState);
				BufferBuilder bufferBuilder = this.getOrBeginLayer(map, sectionBufferBuilderPack, renderType);
				poseStack.pushPose();
				poseStack.translate(
					(float)SectionPos.sectionRelative(blockPos3.getX()),
					(float)SectionPos.sectionRelative(blockPos3.getY()),
					(float)SectionPos.sectionRelative(blockPos3.getZ())
				);
				this.blockRenderer.renderBatched(blockState, blockPos3, renderChunkRegion, poseStack, bufferBuilder, true, randomSource);
				poseStack.popPose();
			}
		}

		for (Entry<RenderType, BufferBuilder> entry : map.entrySet()) {
			RenderType renderType2 = (RenderType)entry.getKey();
			MeshData meshData = ((BufferBuilder)entry.getValue()).build();
			if (meshData != null) {
				if (renderType2 == RenderType.translucent()) {
					results.transparencyState = meshData.sortQuads(sectionBufferBuilderPack.buffer(RenderType.translucent()), vertexSorting);
				}

				results.renderedLayers.put(renderType2, meshData);
			}
		}

		ModelBlockRenderer.clearCache();
		results.visibilitySet = visGraph.resolve();
		return results;
	}

	private BufferBuilder getOrBeginLayer(Map<RenderType, BufferBuilder> map, SectionBufferBuilderPack sectionBufferBuilderPack, RenderType renderType) {
		BufferBuilder bufferBuilder = (BufferBuilder)map.get(renderType);
		if (bufferBuilder == null) {
			ByteBufferBuilder byteBufferBuilder = sectionBufferBuilderPack.buffer(renderType);
			bufferBuilder = new BufferBuilder(byteBufferBuilder, VertexFormat.Mode.QUADS, DefaultVertexFormat.BLOCK);
			map.put(renderType, bufferBuilder);
		}

		return bufferBuilder;
	}

	private <E extends BlockEntity> void handleBlockEntity(SectionCompiler.Results results, E blockEntity) {
		BlockEntityRenderer<E> blockEntityRenderer = this.blockEntityRenderer.getRenderer(blockEntity);
		if (blockEntityRenderer != null) {
			results.blockEntities.add(blockEntity);
			if (blockEntityRenderer.shouldRenderOffScreen(blockEntity)) {
				results.globalBlockEntities.add(blockEntity);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	public static final class Results {
		public final List<BlockEntity> globalBlockEntities = new ArrayList();
		public final List<BlockEntity> blockEntities = new ArrayList();
		public final Map<RenderType, MeshData> renderedLayers = new Reference2ObjectArrayMap<>();
		public VisibilitySet visibilitySet = new VisibilitySet();
		@Nullable
		public MeshData.SortState transparencyState;

		public void release() {
			this.renderedLayers.values().forEach(MeshData::close);
		}
	}
}

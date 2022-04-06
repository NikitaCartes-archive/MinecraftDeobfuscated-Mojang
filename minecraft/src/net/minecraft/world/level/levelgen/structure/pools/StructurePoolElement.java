package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public abstract class StructurePoolElement {
	public static final Codec<StructurePoolElement> CODEC = Registry.STRUCTURE_POOL_ELEMENT
		.byNameCodec()
		.dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
	@Nullable
	private volatile StructureTemplatePool.Projection projection;

	protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
		return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
	}

	protected StructurePoolElement(StructureTemplatePool.Projection projection) {
		this.projection = projection;
	}

	public abstract Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation);

	public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, RandomSource randomSource
	);

	public abstract BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation);

	public abstract boolean place(
		StructureTemplateManager structureTemplateManager,
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		RandomSource randomSource,
		boolean bl
	);

	public abstract StructurePoolElementType<?> getType();

	public void handleDataMarker(
		LevelAccessor levelAccessor,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		BlockPos blockPos,
		Rotation rotation,
		RandomSource randomSource,
		BoundingBox boundingBox
	) {
	}

	public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
		this.projection = projection;
		return this;
	}

	public StructureTemplatePool.Projection getProjection() {
		StructureTemplatePool.Projection projection = this.projection;
		if (projection == null) {
			throw new IllegalStateException();
		} else {
			return projection;
		}
	}

	public int getGroundLevelDelta() {
		return 1;
	}

	public static Function<StructureTemplatePool.Projection, EmptyPoolElement> empty() {
		return projection -> EmptyPoolElement.INSTANCE;
	}

	public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String string) {
		return projection -> new LegacySinglePoolElement(Either.left(new ResourceLocation(string)), ProcessorLists.EMPTY, projection);
	}

	public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String string, Holder<StructureProcessorList> holder) {
		return projection -> new LegacySinglePoolElement(Either.left(new ResourceLocation(string)), holder, projection);
	}

	public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String string) {
		return projection -> new SinglePoolElement(Either.left(new ResourceLocation(string)), ProcessorLists.EMPTY, projection);
	}

	public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String string, Holder<StructureProcessorList> holder) {
		return projection -> new SinglePoolElement(Either.left(new ResourceLocation(string)), holder, projection);
	}

	public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(Holder<PlacedFeature> holder) {
		return projection -> new FeaturePoolElement(holder, projection);
	}

	public static Function<StructureTemplatePool.Projection, ListPoolElement> list(
		List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> list
	) {
		return projection -> new ListPoolElement(
				(List<StructurePoolElement>)list.stream().map(function -> (StructurePoolElement)function.apply(projection)).collect(Collectors.toList()), projection
			);
	}
}

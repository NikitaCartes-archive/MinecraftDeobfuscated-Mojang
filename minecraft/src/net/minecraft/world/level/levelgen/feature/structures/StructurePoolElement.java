package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public abstract class StructurePoolElement {
	public static final Codec<StructurePoolElement> CODEC = Registry.STRUCTURE_POOL_ELEMENT
		.dispatch("element_type", StructurePoolElement::getType, StructurePoolElementType::codec);
	@Nullable
	private volatile StructureTemplatePool.Projection projection;

	protected static <E extends StructurePoolElement> RecordCodecBuilder<E, StructureTemplatePool.Projection> projectionCodec() {
		return StructureTemplatePool.Projection.CODEC.fieldOf("projection").forGetter(StructurePoolElement::getProjection);
	}

	protected StructurePoolElement(StructureTemplatePool.Projection projection) {
		this.projection = projection;
	}

	public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random
	);

	public abstract BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation);

	public abstract boolean place(
		StructureManager structureManager,
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		Random random,
		boolean bl
	);

	public abstract StructurePoolElementType<?> getType();

	public void handleDataMarker(
		LevelAccessor levelAccessor,
		StructureTemplate.StructureBlockInfo structureBlockInfo,
		BlockPos blockPos,
		Rotation rotation,
		Random random,
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
		return projection -> new LegacySinglePoolElement(Either.left(new ResourceLocation(string)), ImmutableList::of, projection);
	}

	public static Function<StructureTemplatePool.Projection, LegacySinglePoolElement> legacy(String string, ImmutableList<StructureProcessor> immutableList) {
		return projection -> new LegacySinglePoolElement(Either.left(new ResourceLocation(string)), () -> immutableList, projection);
	}

	public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String string) {
		return projection -> new SinglePoolElement(Either.left(new ResourceLocation(string)), ImmutableList::of, projection);
	}

	public static Function<StructureTemplatePool.Projection, SinglePoolElement> single(String string, ImmutableList<StructureProcessor> immutableList) {
		return projection -> new SinglePoolElement(Either.left(new ResourceLocation(string)), () -> immutableList, projection);
	}

	public static Function<StructureTemplatePool.Projection, FeaturePoolElement> feature(ConfiguredFeature<?, ?> configuredFeature) {
		return projection -> new FeaturePoolElement(() -> configuredFeature, projection);
	}

	public static Function<StructureTemplatePool.Projection, ListPoolElement> list(
		List<Function<StructureTemplatePool.Projection, ? extends StructurePoolElement>> list
	) {
		return projection -> new ListPoolElement(
				(List<StructurePoolElement>)list.stream().map(function -> (StructurePoolElement)function.apply(projection)).collect(Collectors.toList()), projection
			);
	}
}

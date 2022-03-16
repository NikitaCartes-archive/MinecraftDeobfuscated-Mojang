package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.Codec;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class EmptyPoolElement extends StructurePoolElement {
	public static final Codec<EmptyPoolElement> CODEC = Codec.unit((Supplier<EmptyPoolElement>)(() -> EmptyPoolElement.INSTANCE));
	public static final EmptyPoolElement INSTANCE = new EmptyPoolElement();

	private EmptyPoolElement() {
		super(StructureTemplatePool.Projection.TERRAIN_MATCHING);
	}

	@Override
	public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
		return Vec3i.ZERO;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, Random random
	) {
		return Collections.emptyList();
	}

	@Override
	public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation) {
		throw new IllegalStateException("Invalid call to EmtyPoolElement.getBoundingBox, filter me!");
	}

	@Override
	public boolean place(
		StructureTemplateManager structureTemplateManager,
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		Random random,
		boolean bl
	) {
		return true;
	}

	@Override
	public StructurePoolElementType<?> getType() {
		return StructurePoolElementType.EMPTY;
	}

	public String toString() {
		return "Empty";
	}
}

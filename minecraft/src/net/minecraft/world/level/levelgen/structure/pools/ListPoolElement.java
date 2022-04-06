package net.minecraft.world.level.levelgen.structure.pools;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class ListPoolElement extends StructurePoolElement {
	public static final Codec<ListPoolElement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(StructurePoolElement.CODEC.listOf().fieldOf("elements").forGetter(listPoolElement -> listPoolElement.elements), projectionCodec())
				.apply(instance, ListPoolElement::new)
	);
	private final List<StructurePoolElement> elements;

	public ListPoolElement(List<StructurePoolElement> list, StructureTemplatePool.Projection projection) {
		super(projection);
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Elements are empty");
		} else {
			this.elements = list;
			this.setProjectionOnEachElement(projection);
		}
	}

	@Override
	public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
		int i = 0;
		int j = 0;
		int k = 0;

		for (StructurePoolElement structurePoolElement : this.elements) {
			Vec3i vec3i = structurePoolElement.getSize(structureTemplateManager, rotation);
			i = Math.max(i, vec3i.getX());
			j = Math.max(j, vec3i.getY());
			k = Math.max(k, vec3i.getZ());
		}

		return new Vec3i(i, j, k);
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, RandomSource randomSource
	) {
		return ((StructurePoolElement)this.elements.get(0)).getShuffledJigsawBlocks(structureTemplateManager, blockPos, rotation, randomSource);
	}

	@Override
	public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation) {
		Stream<BoundingBox> stream = this.elements
			.stream()
			.filter(structurePoolElement -> structurePoolElement != EmptyPoolElement.INSTANCE)
			.map(structurePoolElement -> structurePoolElement.getBoundingBox(structureTemplateManager, blockPos, rotation));
		return (BoundingBox)BoundingBox.encapsulatingBoxes(stream::iterator)
			.orElseThrow(() -> new IllegalStateException("Unable to calculate boundingbox for ListPoolElement"));
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
		RandomSource randomSource,
		boolean bl
	) {
		for (StructurePoolElement structurePoolElement : this.elements) {
			if (!structurePoolElement.place(
				structureTemplateManager, worldGenLevel, structureManager, chunkGenerator, blockPos, blockPos2, rotation, boundingBox, randomSource, bl
			)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public StructurePoolElementType<?> getType() {
		return StructurePoolElementType.LIST;
	}

	@Override
	public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
		super.setProjection(projection);
		this.setProjectionOnEachElement(projection);
		return this;
	}

	public String toString() {
		return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
	}

	private void setProjectionOnEachElement(StructureTemplatePool.Projection projection) {
		this.elements.forEach(structurePoolElement -> structurePoolElement.setProjection(projection));
	}
}

package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class ListPoolElement extends StructurePoolElement {
	private final List<StructurePoolElement> elements;

	@Deprecated
	public ListPoolElement(List<StructurePoolElement> list) {
		this(list, StructureTemplatePool.Projection.RIGID);
	}

	public ListPoolElement(List<StructurePoolElement> list, StructureTemplatePool.Projection projection) {
		super(projection);
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Elements are empty");
		} else {
			this.elements = list;
			this.setProjectionOnEachElement(projection);
		}
	}

	public ListPoolElement(Dynamic<?> dynamic) {
		super(dynamic);
		List<StructurePoolElement> list = dynamic.get("elements")
			.asList(dynamicx -> Deserializer.deserialize(dynamicx, Registry.STRUCTURE_POOL_ELEMENT, "element_type", EmptyPoolElement.INSTANCE));
		if (list.isEmpty()) {
			throw new IllegalArgumentException("Elements are empty");
		} else {
			this.elements = list;
		}
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random
	) {
		return ((StructurePoolElement)this.elements.get(0)).getShuffledJigsawBlocks(structureManager, blockPos, rotation, random);
	}

	@Override
	public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
		BoundingBox boundingBox = BoundingBox.getUnknownBox();

		for (StructurePoolElement structurePoolElement : this.elements) {
			BoundingBox boundingBox2 = structurePoolElement.getBoundingBox(structureManager, blockPos, rotation);
			boundingBox.expand(boundingBox2);
		}

		return boundingBox;
	}

	@Override
	public boolean place(
		StructureManager structureManager,
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<?> chunkGenerator,
		BlockPos blockPos,
		BlockPos blockPos2,
		Rotation rotation,
		BoundingBox boundingBox,
		Random random
	) {
		for (StructurePoolElement structurePoolElement : this.elements) {
			if (!structurePoolElement.place(structureManager, levelAccessor, structureFeatureManager, chunkGenerator, blockPos, blockPos2, rotation, boundingBox, random)
				)
			 {
				return false;
			}
		}

		return true;
	}

	@Override
	public StructurePoolElementType getType() {
		return StructurePoolElementType.LIST;
	}

	@Override
	public StructurePoolElement setProjection(StructureTemplatePool.Projection projection) {
		super.setProjection(projection);
		this.setProjectionOnEachElement(projection);
		return this;
	}

	@Override
	public <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(this.elements.stream().map(structurePoolElement -> structurePoolElement.serialize(dynamicOps).getValue()));
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("elements"), object)));
	}

	public String toString() {
		return "List[" + (String)this.elements.stream().map(Object::toString).collect(Collectors.joining(", ")) + "]";
	}

	private void setProjectionOnEachElement(StructureTemplatePool.Projection projection) {
		this.elements.forEach(structurePoolElement -> structurePoolElement.setProjection(projection));
	}
}

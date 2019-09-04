package net.minecraft.world.level.levelgen.feature.structures;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public abstract class StructurePoolElement {
	@Nullable
	private volatile StructureTemplatePool.Projection projection;

	protected StructurePoolElement(StructureTemplatePool.Projection projection) {
		this.projection = projection;
	}

	protected StructurePoolElement(Dynamic<?> dynamic) {
		this.projection = StructureTemplatePool.Projection.byName(dynamic.get("projection").asString(StructureTemplatePool.Projection.RIGID.getName()));
	}

	public abstract List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random
	);

	public abstract BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation);

	public abstract boolean place(
		StructureManager structureManager,
		LevelAccessor levelAccessor,
		ChunkGenerator<?> chunkGenerator,
		BlockPos blockPos,
		Rotation rotation,
		BoundingBox boundingBox,
		Random random
	);

	public abstract StructurePoolElementType getType();

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

	protected abstract <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps);

	public <T> Dynamic<T> serialize(DynamicOps<T> dynamicOps) {
		T object = this.getDynamic(dynamicOps).getValue();
		T object2 = dynamicOps.mergeInto(
			object, dynamicOps.createString("element_type"), dynamicOps.createString(Registry.STRUCTURE_POOL_ELEMENT.getKey(this.getType()).toString())
		);
		return new Dynamic<>(dynamicOps, dynamicOps.mergeInto(object2, dynamicOps.createString("projection"), dynamicOps.createString(this.projection.getName())));
	}

	public int getGroundLevelDelta() {
		return 1;
	}
}

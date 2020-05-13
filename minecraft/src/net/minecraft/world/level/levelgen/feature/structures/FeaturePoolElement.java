package net.minecraft.world.level.levelgen.feature.structures;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public class FeaturePoolElement extends StructurePoolElement {
	private final ConfiguredFeature<?, ?> feature;
	private final CompoundTag defaultJigsawNBT;

	@Deprecated
	public FeaturePoolElement(ConfiguredFeature<?, ?> configuredFeature) {
		this(configuredFeature, StructureTemplatePool.Projection.RIGID);
	}

	private FeaturePoolElement(ConfiguredFeature<?, ?> configuredFeature, StructureTemplatePool.Projection projection) {
		super(projection);
		this.feature = configuredFeature;
		this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
	}

	public <T> FeaturePoolElement(Dynamic<T> dynamic) {
		super(dynamic);
		this.feature = ConfiguredFeature.deserialize(dynamic.get("feature").orElseEmptyMap());
		this.defaultJigsawNBT = this.fillDefaultJigsawNBT();
	}

	private CompoundTag fillDefaultJigsawNBT() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("name", "minecraft:bottom");
		compoundTag.putString("final_state", "minecraft:air");
		compoundTag.putString("pool", "minecraft:empty");
		compoundTag.putString("target", "minecraft:empty");
		compoundTag.putString("joint", JigsawBlockEntity.JointType.ROLLABLE.getSerializedName());
		return compoundTag;
	}

	public BlockPos getSize(StructureManager structureManager, Rotation rotation) {
		return BlockPos.ZERO;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureManager structureManager, BlockPos blockPos, Rotation rotation, Random random
	) {
		List<StructureTemplate.StructureBlockInfo> list = Lists.<StructureTemplate.StructureBlockInfo>newArrayList();
		list.add(
			new StructureTemplate.StructureBlockInfo(
				blockPos,
				Blocks.JIGSAW.defaultBlockState().setValue(JigsawBlock.ORIENTATION, FrontAndTop.fromFrontAndTop(Direction.DOWN, Direction.SOUTH)),
				this.defaultJigsawNBT
			)
		);
		return list;
	}

	@Override
	public BoundingBox getBoundingBox(StructureManager structureManager, BlockPos blockPos, Rotation rotation) {
		BlockPos blockPos2 = this.getSize(structureManager, rotation);
		return new BoundingBox(
			blockPos.getX(),
			blockPos.getY(),
			blockPos.getZ(),
			blockPos.getX() + blockPos2.getX(),
			blockPos.getY() + blockPos2.getY(),
			blockPos.getZ() + blockPos2.getZ()
		);
	}

	@Override
	public boolean place(
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
	) {
		return this.feature.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, blockPos);
	}

	@Override
	public <T> Dynamic<T> getDynamic(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("feature"), this.feature.serialize(dynamicOps).getValue())));
	}

	@Override
	public StructurePoolElementType getType() {
		return StructurePoolElementType.FEATURE;
	}

	public String toString() {
		return "Feature[" + Registry.FEATURE.getKey(this.feature.feature) + "]";
	}
}

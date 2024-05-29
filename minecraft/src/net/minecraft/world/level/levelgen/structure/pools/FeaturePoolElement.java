package net.minecraft.world.level.levelgen.structure.pools;

import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.FrontAndTop;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.JigsawBlockEntity;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class FeaturePoolElement extends StructurePoolElement {
	public static final MapCodec<FeaturePoolElement> CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(PlacedFeature.CODEC.fieldOf("feature").forGetter(featurePoolElement -> featurePoolElement.feature), projectionCodec())
				.apply(instance, FeaturePoolElement::new)
	);
	private final Holder<PlacedFeature> feature;
	private final CompoundTag defaultJigsawNBT;

	protected FeaturePoolElement(Holder<PlacedFeature> holder, StructureTemplatePool.Projection projection) {
		super(projection);
		this.feature = holder;
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

	@Override
	public Vec3i getSize(StructureTemplateManager structureTemplateManager, Rotation rotation) {
		return Vec3i.ZERO;
	}

	@Override
	public List<StructureTemplate.StructureBlockInfo> getShuffledJigsawBlocks(
		StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation, RandomSource randomSource
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
	public BoundingBox getBoundingBox(StructureTemplateManager structureTemplateManager, BlockPos blockPos, Rotation rotation) {
		Vec3i vec3i = this.getSize(structureTemplateManager, rotation);
		return new BoundingBox(
			blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockPos.getX() + vec3i.getX(), blockPos.getY() + vec3i.getY(), blockPos.getZ() + vec3i.getZ()
		);
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
		LiquidSettings liquidSettings,
		boolean bl
	) {
		return this.feature.value().place(worldGenLevel, chunkGenerator, randomSource, blockPos);
	}

	@Override
	public StructurePoolElementType<?> getType() {
		return StructurePoolElementType.FEATURE;
	}

	public String toString() {
		return "Feature[" + this.feature + "]";
	}
}

package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.datafixers.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.util.Deserializer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.EmptyPoolElement;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public abstract class PoolElementStructurePiece extends StructurePiece {
	protected final StructurePoolElement element;
	protected BlockPos position;
	private final int groundLevelDelta;
	protected final Rotation rotation;
	private final List<JigsawJunction> junctions = Lists.<JigsawJunction>newArrayList();
	private final StructureManager structureManager;

	public PoolElementStructurePiece(
		StructurePieceType structurePieceType,
		StructureManager structureManager,
		StructurePoolElement structurePoolElement,
		BlockPos blockPos,
		int i,
		Rotation rotation,
		BoundingBox boundingBox
	) {
		super(structurePieceType, 0);
		this.structureManager = structureManager;
		this.element = structurePoolElement;
		this.position = blockPos;
		this.groundLevelDelta = i;
		this.rotation = rotation;
		this.boundingBox = boundingBox;
	}

	public PoolElementStructurePiece(StructureManager structureManager, CompoundTag compoundTag, StructurePieceType structurePieceType) {
		super(structurePieceType, compoundTag);
		this.structureManager = structureManager;
		this.position = new BlockPos(compoundTag.getInt("PosX"), compoundTag.getInt("PosY"), compoundTag.getInt("PosZ"));
		this.groundLevelDelta = compoundTag.getInt("ground_level_delta");
		this.element = Deserializer.deserialize(
			new Dynamic<>(NbtOps.INSTANCE, compoundTag.getCompound("pool_element")), Registry.STRUCTURE_POOL_ELEMENT, "element_type", EmptyPoolElement.INSTANCE
		);
		this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
		this.boundingBox = this.element.getBoundingBox(structureManager, this.position, this.rotation);
		ListTag listTag = compoundTag.getList("junctions", 10);
		this.junctions.clear();
		listTag.forEach(tag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(NbtOps.INSTANCE, tag))));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag compoundTag) {
		compoundTag.putInt("PosX", this.position.getX());
		compoundTag.putInt("PosY", this.position.getY());
		compoundTag.putInt("PosZ", this.position.getZ());
		compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
		compoundTag.put("pool_element", this.element.serialize(NbtOps.INSTANCE).getValue());
		compoundTag.putString("rotation", this.rotation.name());
		ListTag listTag = new ListTag();

		for (JigsawJunction jigsawJunction : this.junctions) {
			listTag.add(jigsawJunction.serialize(NbtOps.INSTANCE).getValue());
		}

		compoundTag.put("junctions", listTag);
	}

	@Override
	public boolean postProcess(
		LevelAccessor levelAccessor,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator<?> chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		return this.element
			.place(this.structureManager, levelAccessor, structureFeatureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, random);
	}

	@Override
	public void move(int i, int j, int k) {
		super.move(i, j, k);
		this.position = this.position.offset(i, j, k);
	}

	@Override
	public Rotation getRotation() {
		return this.rotation;
	}

	public String toString() {
		return String.format("<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
	}

	public StructurePoolElement getElement() {
		return this.element;
	}

	public BlockPos getPosition() {
		return this.position;
	}

	public int getGroundLevelDelta() {
		return this.groundLevelDelta;
	}

	public void addJunction(JigsawJunction jigsawJunction) {
		this.junctions.add(jigsawJunction);
	}

	public List<JigsawJunction> getJunctions() {
		return this.junctions;
	}
}

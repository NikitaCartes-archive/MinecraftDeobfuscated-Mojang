package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.serialization.Dynamic;
import java.util.List;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.StructurePieceType;
import net.minecraft.world.level.levelgen.feature.structures.JigsawJunction;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
	private static final Logger LOGGER = LogManager.getLogger();
	protected final StructurePoolElement element;
	protected BlockPos position;
	private final int groundLevelDelta;
	protected final Rotation rotation;
	private final List<JigsawJunction> junctions = Lists.<JigsawJunction>newArrayList();
	private final StructureManager structureManager;

	public PoolElementStructurePiece(
		StructureManager structureManager, StructurePoolElement structurePoolElement, BlockPos blockPos, int i, Rotation rotation, BoundingBox boundingBox
	) {
		super(StructurePieceType.JIGSAW, 0, boundingBox);
		this.structureManager = structureManager;
		this.element = structurePoolElement;
		this.position = blockPos;
		this.groundLevelDelta = i;
		this.rotation = rotation;
	}

	public PoolElementStructurePiece(ServerLevel serverLevel, CompoundTag compoundTag) {
		super(StructurePieceType.JIGSAW, compoundTag);
		this.structureManager = serverLevel.getStructureManager();
		this.position = new BlockPos(compoundTag.getInt("PosX"), compoundTag.getInt("PosY"), compoundTag.getInt("PosZ"));
		this.groundLevelDelta = compoundTag.getInt("ground_level_delta");
		RegistryReadOps<Tag> registryReadOps = RegistryReadOps.create(
			NbtOps.INSTANCE, serverLevel.getServer().getResourceManager(), serverLevel.getServer().registryAccess()
		);
		this.element = (StructurePoolElement)StructurePoolElement.CODEC
			.parse(registryReadOps, compoundTag.getCompound("pool_element"))
			.resultOrPartial(LOGGER::error)
			.orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
		this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
		this.boundingBox = this.element.getBoundingBox(this.structureManager, this.position, this.rotation);
		ListTag listTag = compoundTag.getList("junctions", 10);
		this.junctions.clear();
		listTag.forEach(tag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(registryReadOps, tag))));
	}

	@Override
	protected void addAdditionalSaveData(ServerLevel serverLevel, CompoundTag compoundTag) {
		compoundTag.putInt("PosX", this.position.getX());
		compoundTag.putInt("PosY", this.position.getY());
		compoundTag.putInt("PosZ", this.position.getZ());
		compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
		RegistryWriteOps<Tag> registryWriteOps = RegistryWriteOps.create(NbtOps.INSTANCE, serverLevel.getServer().registryAccess());
		StructurePoolElement.CODEC.encodeStart(registryWriteOps, this.element).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("pool_element", tag));
		compoundTag.putString("rotation", this.rotation.name());
		ListTag listTag = new ListTag();

		for (JigsawJunction jigsawJunction : this.junctions) {
			listTag.add(jigsawJunction.serialize(registryWriteOps).getValue());
		}

		compoundTag.put("junctions", listTag);
	}

	@Override
	public boolean postProcess(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		return this.place(worldGenLevel, structureFeatureManager, chunkGenerator, random, boundingBox, blockPos, false);
	}

	public boolean place(
		WorldGenLevel worldGenLevel,
		StructureFeatureManager structureFeatureManager,
		ChunkGenerator chunkGenerator,
		Random random,
		BoundingBox boundingBox,
		BlockPos blockPos,
		boolean bl
	) {
		return this.element
			.place(this.structureManager, worldGenLevel, structureFeatureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, random, bl);
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

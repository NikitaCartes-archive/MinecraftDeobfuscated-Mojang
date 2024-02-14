package net.minecraft.world.level.levelgen.structure;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.pools.JigsawJunction;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import org.slf4j.Logger;

public class PoolElementStructurePiece extends StructurePiece {
	private static final Logger LOGGER = LogUtils.getLogger();
	protected final StructurePoolElement element;
	protected BlockPos position;
	private final int groundLevelDelta;
	protected final Rotation rotation;
	private final List<JigsawJunction> junctions = Lists.<JigsawJunction>newArrayList();
	private final StructureTemplateManager structureTemplateManager;

	public PoolElementStructurePiece(
		StructureTemplateManager structureTemplateManager,
		StructurePoolElement structurePoolElement,
		BlockPos blockPos,
		int i,
		Rotation rotation,
		BoundingBox boundingBox
	) {
		super(StructurePieceType.JIGSAW, 0, boundingBox);
		this.structureTemplateManager = structureTemplateManager;
		this.element = structurePoolElement;
		this.position = blockPos;
		this.groundLevelDelta = i;
		this.rotation = rotation;
	}

	public PoolElementStructurePiece(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		super(StructurePieceType.JIGSAW, compoundTag);
		this.structureTemplateManager = structurePieceSerializationContext.structureTemplateManager();
		this.position = new BlockPos(compoundTag.getInt("PosX"), compoundTag.getInt("PosY"), compoundTag.getInt("PosZ"));
		this.groundLevelDelta = compoundTag.getInt("ground_level_delta");
		DynamicOps<Tag> dynamicOps = structurePieceSerializationContext.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		this.element = (StructurePoolElement)StructurePoolElement.CODEC
			.parse(dynamicOps, compoundTag.getCompound("pool_element"))
			.resultOrPartial(LOGGER::error)
			.orElseThrow(() -> new IllegalStateException("Invalid pool element found"));
		this.rotation = Rotation.valueOf(compoundTag.getString("rotation"));
		this.boundingBox = this.element.getBoundingBox(this.structureTemplateManager, this.position, this.rotation);
		ListTag listTag = compoundTag.getList("junctions", 10);
		this.junctions.clear();
		listTag.forEach(tag -> this.junctions.add(JigsawJunction.deserialize(new Dynamic<>(dynamicOps, tag))));
	}

	@Override
	protected void addAdditionalSaveData(StructurePieceSerializationContext structurePieceSerializationContext, CompoundTag compoundTag) {
		compoundTag.putInt("PosX", this.position.getX());
		compoundTag.putInt("PosY", this.position.getY());
		compoundTag.putInt("PosZ", this.position.getZ());
		compoundTag.putInt("ground_level_delta", this.groundLevelDelta);
		DynamicOps<Tag> dynamicOps = structurePieceSerializationContext.registryAccess().createSerializationContext(NbtOps.INSTANCE);
		StructurePoolElement.CODEC.encodeStart(dynamicOps, this.element).resultOrPartial(LOGGER::error).ifPresent(tag -> compoundTag.put("pool_element", tag));
		compoundTag.putString("rotation", this.rotation.name());
		ListTag listTag = new ListTag();

		for (JigsawJunction jigsawJunction : this.junctions) {
			listTag.add(jigsawJunction.serialize(dynamicOps).getValue());
		}

		compoundTag.put("junctions", listTag);
	}

	@Override
	public void postProcess(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		ChunkPos chunkPos,
		BlockPos blockPos
	) {
		this.place(worldGenLevel, structureManager, chunkGenerator, randomSource, boundingBox, blockPos, false);
	}

	public void place(
		WorldGenLevel worldGenLevel,
		StructureManager structureManager,
		ChunkGenerator chunkGenerator,
		RandomSource randomSource,
		BoundingBox boundingBox,
		BlockPos blockPos,
		boolean bl
	) {
		this.element
			.place(this.structureTemplateManager, worldGenLevel, structureManager, chunkGenerator, this.position, blockPos, this.rotation, boundingBox, randomSource, bl);
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
		return String.format(Locale.ROOT, "<%s | %s | %s | %s>", this.getClass().getSimpleName(), this.position, this.rotation, this.element);
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

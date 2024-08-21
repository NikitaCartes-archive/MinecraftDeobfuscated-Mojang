package net.minecraft.world.level;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.placement.StructurePlacement;

public class StructureManager {
	private final LevelAccessor level;
	private final WorldOptions worldOptions;
	private final StructureCheck structureCheck;

	public StructureManager(LevelAccessor levelAccessor, WorldOptions worldOptions, StructureCheck structureCheck) {
		this.level = levelAccessor;
		this.worldOptions = worldOptions;
		this.structureCheck = structureCheck;
	}

	public StructureManager forWorldGenRegion(WorldGenRegion worldGenRegion) {
		if (worldGenRegion.getLevel() != this.level) {
			throw new IllegalStateException("Using invalid structure manager (source level: " + worldGenRegion.getLevel() + ", region: " + worldGenRegion);
		} else {
			return new StructureManager(worldGenRegion, this.worldOptions, this.structureCheck);
		}
	}

	public List<StructureStart> startsForStructure(ChunkPos chunkPos, Predicate<Structure> predicate) {
		Map<Structure, LongSet> map = this.level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
		Builder<StructureStart> builder = ImmutableList.builder();

		for (Entry<Structure, LongSet> entry : map.entrySet()) {
			Structure structure = (Structure)entry.getKey();
			if (predicate.test(structure)) {
				this.fillStartsForStructure(structure, (LongSet)entry.getValue(), builder::add);
			}
		}

		return builder.build();
	}

	public List<StructureStart> startsForStructure(SectionPos sectionPos, Structure structure) {
		LongSet longSet = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getReferencesForStructure(structure);
		Builder<StructureStart> builder = ImmutableList.builder();
		this.fillStartsForStructure(structure, longSet, builder::add);
		return builder.build();
	}

	public void fillStartsForStructure(Structure structure, LongSet longSet, Consumer<StructureStart> consumer) {
		LongIterator var4 = longSet.iterator();

		while (var4.hasNext()) {
			long l = (Long)var4.next();
			SectionPos sectionPos = SectionPos.of(new ChunkPos(l), this.level.getMinSectionY());
			StructureStart structureStart = this.getStartForStructure(
				sectionPos, structure, this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_STARTS)
			);
			if (structureStart != null && structureStart.isValid()) {
				consumer.accept(structureStart);
			}
		}
	}

	@Nullable
	public StructureStart getStartForStructure(SectionPos sectionPos, Structure structure, StructureAccess structureAccess) {
		return structureAccess.getStartForStructure(structure);
	}

	public void setStartForStructure(SectionPos sectionPos, Structure structure, StructureStart structureStart, StructureAccess structureAccess) {
		structureAccess.setStartForStructure(structure, structureStart);
	}

	public void addReferenceForStructure(SectionPos sectionPos, Structure structure, long l, StructureAccess structureAccess) {
		structureAccess.addReferenceForStructure(structure, l);
	}

	public boolean shouldGenerateStructures() {
		return this.worldOptions.generateStructures();
	}

	public StructureStart getStructureAt(BlockPos blockPos, Structure structure) {
		for (StructureStart structureStart : this.startsForStructure(SectionPos.of(blockPos), structure)) {
			if (structureStart.getBoundingBox().isInside(blockPos)) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, TagKey<Structure> tagKey) {
		return this.getStructureWithPieceAt(blockPos, holder -> holder.is(tagKey));
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, HolderSet<Structure> holderSet) {
		return this.getStructureWithPieceAt(blockPos, holderSet::contains);
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, Predicate<Holder<Structure>> predicate) {
		Registry<Structure> registry = this.registryAccess().registryOrThrow(Registries.STRUCTURE);

		for (StructureStart structureStart : this.startsForStructure(
			new ChunkPos(blockPos), structure -> (Boolean)registry.getHolder(registry.getId(structure)).map(predicate::test).orElse(false)
		)) {
			if (this.structureHasPieceAt(blockPos, structureStart)) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, Structure structure) {
		for (StructureStart structureStart : this.startsForStructure(SectionPos.of(blockPos), structure)) {
			if (this.structureHasPieceAt(blockPos, structureStart)) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public boolean structureHasPieceAt(BlockPos blockPos, StructureStart structureStart) {
		for (StructurePiece structurePiece : structureStart.getPieces()) {
			if (structurePiece.getBoundingBox().isInside(blockPos)) {
				return true;
			}
		}

		return false;
	}

	public boolean hasAnyStructureAt(BlockPos blockPos) {
		SectionPos sectionPos = SectionPos.of(blockPos);
		return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).hasAnyStructureReferences();
	}

	public Map<Structure, LongSet> getAllStructuresAt(BlockPos blockPos) {
		SectionPos sectionPos = SectionPos.of(blockPos);
		return this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
	}

	public StructureCheckResult checkStructurePresence(ChunkPos chunkPos, Structure structure, StructurePlacement structurePlacement, boolean bl) {
		return this.structureCheck.checkStart(chunkPos, structure, structurePlacement, bl);
	}

	public void addReference(StructureStart structureStart) {
		structureStart.addReference();
		this.structureCheck.incrementReference(structureStart.getChunkPos(), structureStart.getStructure());
	}

	public RegistryAccess registryAccess() {
		return this.level.registryAccess();
	}
}

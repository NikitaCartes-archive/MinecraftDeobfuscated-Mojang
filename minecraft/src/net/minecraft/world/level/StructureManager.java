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
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.StructureAccess;
import net.minecraft.world.level.levelgen.WorldGenSettings;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureCheck;
import net.minecraft.world.level.levelgen.structure.StructureCheckResult;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

public class StructureManager {
	private final LevelAccessor level;
	private final WorldGenSettings worldGenSettings;
	private final StructureCheck structureCheck;

	public StructureManager(LevelAccessor levelAccessor, WorldGenSettings worldGenSettings, StructureCheck structureCheck) {
		this.level = levelAccessor;
		this.worldGenSettings = worldGenSettings;
		this.structureCheck = structureCheck;
	}

	public StructureManager forWorldGenRegion(WorldGenRegion worldGenRegion) {
		if (worldGenRegion.getLevel() != this.level) {
			throw new IllegalStateException("Using invalid structure manager (source level: " + worldGenRegion.getLevel() + ", region: " + worldGenRegion);
		} else {
			return new StructureManager(worldGenRegion, this.worldGenSettings, this.structureCheck);
		}
	}

	public List<StructureStart> startsForStructure(SectionPos sectionPos, Predicate<Structure> predicate) {
		Map<Structure, LongSet> map = this.level.getChunk(sectionPos.x(), sectionPos.z(), ChunkStatus.STRUCTURE_REFERENCES).getAllReferences();
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
			SectionPos sectionPos = SectionPos.of(new ChunkPos(l), this.level.getMinSection());
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
		return this.worldGenSettings.generateStructures();
	}

	public StructureStart getStructureAt(BlockPos blockPos, Structure structure) {
		for (StructureStart structureStart : this.startsForStructure(SectionPos.of(blockPos), structure)) {
			if (structureStart.getBoundingBox().isInside(blockPos)) {
				return structureStart;
			}
		}

		return StructureStart.INVALID_START;
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, ResourceKey<Structure> resourceKey) {
		Structure structure = this.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY).get(resourceKey);
		return structure == null ? StructureStart.INVALID_START : this.getStructureWithPieceAt(blockPos, structure);
	}

	public StructureStart getStructureWithPieceAt(BlockPos blockPos, TagKey<Structure> tagKey) {
		Registry<Structure> registry = this.registryAccess().registryOrThrow(Registry.STRUCTURE_REGISTRY);

		for (StructureStart structureStart : this.startsForStructure(
			SectionPos.of(blockPos), structure -> (Boolean)registry.getHolder(registry.getId(structure)).map(holder -> holder.is(tagKey)).orElse(false)
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

	public StructureCheckResult checkStructurePresence(ChunkPos chunkPos, Structure structure, boolean bl) {
		return this.structureCheck.checkStart(chunkPos, structure, bl);
	}

	public void addReference(StructureStart structureStart) {
		structureStart.addReference();
		this.structureCheck.incrementReference(structureStart.getChunkPos(), structureStart.getStructure());
	}

	public RegistryAccess registryAccess() {
		return this.level.registryAccess();
	}
}

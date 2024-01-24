package net.minecraft.world.level.levelgen.structure.placement;

import com.mojang.datafixers.Products.P5;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.codecs.RecordCodecBuilder.Instance;
import com.mojang.serialization.codecs.RecordCodecBuilder.Mu;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkGeneratorStructureState;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.StructureSet;

public abstract class StructurePlacement {
	public static final Codec<StructurePlacement> CODEC = BuiltInRegistries.STRUCTURE_PLACEMENT
		.byNameCodec()
		.dispatch(StructurePlacement::type, StructurePlacementType::codec);
	private static final int HIGHLY_ARBITRARY_RANDOM_SALT = 10387320;
	private final Vec3i locateOffset;
	private final StructurePlacement.FrequencyReductionMethod frequencyReductionMethod;
	private final float frequency;
	private final int salt;
	private final Optional<StructurePlacement.ExclusionZone> exclusionZone;

	protected static <S extends StructurePlacement> P5<Mu<S>, Vec3i, StructurePlacement.FrequencyReductionMethod, Float, Integer, Optional<StructurePlacement.ExclusionZone>> placementCodec(
		Instance<S> instance
	) {
		return instance.group(
			Vec3i.offsetCodec(16).optionalFieldOf("locate_offset", Vec3i.ZERO).forGetter(StructurePlacement::locateOffset),
			StructurePlacement.FrequencyReductionMethod.CODEC
				.optionalFieldOf("frequency_reduction_method", StructurePlacement.FrequencyReductionMethod.DEFAULT)
				.forGetter(StructurePlacement::frequencyReductionMethod),
			Codec.floatRange(0.0F, 1.0F).optionalFieldOf("frequency", 1.0F).forGetter(StructurePlacement::frequency),
			ExtraCodecs.NON_NEGATIVE_INT.fieldOf("salt").forGetter(StructurePlacement::salt),
			StructurePlacement.ExclusionZone.CODEC.optionalFieldOf("exclusion_zone").forGetter(StructurePlacement::exclusionZone)
		);
	}

	protected StructurePlacement(
		Vec3i vec3i, StructurePlacement.FrequencyReductionMethod frequencyReductionMethod, float f, int i, Optional<StructurePlacement.ExclusionZone> optional
	) {
		this.locateOffset = vec3i;
		this.frequencyReductionMethod = frequencyReductionMethod;
		this.frequency = f;
		this.salt = i;
		this.exclusionZone = optional;
	}

	protected Vec3i locateOffset() {
		return this.locateOffset;
	}

	protected StructurePlacement.FrequencyReductionMethod frequencyReductionMethod() {
		return this.frequencyReductionMethod;
	}

	protected float frequency() {
		return this.frequency;
	}

	protected int salt() {
		return this.salt;
	}

	protected Optional<StructurePlacement.ExclusionZone> exclusionZone() {
		return this.exclusionZone;
	}

	public boolean isStructureChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
		return this.isPlacementChunk(chunkGeneratorStructureState, i, j)
			&& this.applyAdditionalChunkRestrictions(i, j, chunkGeneratorStructureState.getLevelSeed())
			&& this.applyInteractionsWithOtherStructures(chunkGeneratorStructureState, i, j);
	}

	public boolean applyAdditionalChunkRestrictions(int i, int j, long l) {
		return !(this.frequency < 1.0F) || this.frequencyReductionMethod.shouldGenerate(l, this.salt, i, j, this.frequency);
	}

	public boolean applyInteractionsWithOtherStructures(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
		return !this.exclusionZone.isPresent()
			|| !((StructurePlacement.ExclusionZone)this.exclusionZone.get()).isPlacementForbidden(chunkGeneratorStructureState, i, j);
	}

	protected abstract boolean isPlacementChunk(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j);

	public BlockPos getLocatePos(ChunkPos chunkPos) {
		return new BlockPos(chunkPos.getMinBlockX(), 0, chunkPos.getMinBlockZ()).offset(this.locateOffset());
	}

	public abstract StructurePlacementType<?> type();

	private static boolean probabilityReducer(long l, int i, int j, int k, float f) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureWithSalt(l, i, j, k);
		return worldgenRandom.nextFloat() < f;
	}

	private static boolean legacyProbabilityReducerWithDouble(long l, int i, int j, int k, float f) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureSeed(l, j, k);
		return worldgenRandom.nextDouble() < (double)f;
	}

	private static boolean legacyArbitrarySaltProbabilityReducer(long l, int i, int j, int k, float f) {
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setLargeFeatureWithSalt(l, j, k, 10387320);
		return worldgenRandom.nextFloat() < f;
	}

	private static boolean legacyPillagerOutpostReducer(long l, int i, int j, int k, float f) {
		int m = j >> 4;
		int n = k >> 4;
		WorldgenRandom worldgenRandom = new WorldgenRandom(new LegacyRandomSource(0L));
		worldgenRandom.setSeed((long)(m ^ n << 4) ^ l);
		worldgenRandom.nextInt();
		return worldgenRandom.nextInt((int)(1.0F / f)) == 0;
	}

	@Deprecated
	public static record ExclusionZone(Holder<StructureSet> otherSet, int chunkCount) {
		public static final Codec<StructurePlacement.ExclusionZone> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						RegistryFileCodec.create(Registries.STRUCTURE_SET, StructureSet.DIRECT_CODEC, false)
							.fieldOf("other_set")
							.forGetter(StructurePlacement.ExclusionZone::otherSet),
						Codec.intRange(1, 16).fieldOf("chunk_count").forGetter(StructurePlacement.ExclusionZone::chunkCount)
					)
					.apply(instance, StructurePlacement.ExclusionZone::new)
		);

		boolean isPlacementForbidden(ChunkGeneratorStructureState chunkGeneratorStructureState, int i, int j) {
			return chunkGeneratorStructureState.hasStructureChunkInRange(this.otherSet, i, j, this.chunkCount);
		}
	}

	@FunctionalInterface
	public interface FrequencyReducer {
		boolean shouldGenerate(long l, int i, int j, int k, float f);
	}

	public static enum FrequencyReductionMethod implements StringRepresentable {
		DEFAULT("default", StructurePlacement::probabilityReducer),
		LEGACY_TYPE_1("legacy_type_1", StructurePlacement::legacyPillagerOutpostReducer),
		LEGACY_TYPE_2("legacy_type_2", StructurePlacement::legacyArbitrarySaltProbabilityReducer),
		LEGACY_TYPE_3("legacy_type_3", StructurePlacement::legacyProbabilityReducerWithDouble);

		public static final Codec<StructurePlacement.FrequencyReductionMethod> CODEC = StringRepresentable.fromEnum(
			StructurePlacement.FrequencyReductionMethod::values
		);
		private final String name;
		private final StructurePlacement.FrequencyReducer reducer;

		private FrequencyReductionMethod(String string2, StructurePlacement.FrequencyReducer frequencyReducer) {
			this.name = string2;
			this.reducer = frequencyReducer;
		}

		public boolean shouldGenerate(long l, int i, int j, int k, float f) {
			return this.reducer.shouldGenerate(l, i, j, k, f);
		}

		@Override
		public String getSerializedName() {
			return this.name;
		}
	}
}

package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final long worldSeed;
	private int salt;
	private boolean includeWorldSeed = true;
	private boolean includeSequenceId = true;
	private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

	public static SavedData.Factory<RandomSequences> factory(long l) {
		return new SavedData.Factory<>(() -> new RandomSequences(l), compoundTag -> load(l, compoundTag), DataFixTypes.SAVED_DATA_RANDOM_SEQUENCES);
	}

	public RandomSequences(long l) {
		this.worldSeed = l;
	}

	public RandomSource get(ResourceLocation resourceLocation) {
		RandomSource randomSource = ((RandomSequence)this.sequences.computeIfAbsent(resourceLocation, this::createSequence)).random();
		return new RandomSequences.DirtyMarkingRandomSource(randomSource);
	}

	private RandomSequence createSequence(ResourceLocation resourceLocation) {
		return this.createSequence(resourceLocation, this.salt, this.includeWorldSeed, this.includeSequenceId);
	}

	private RandomSequence createSequence(ResourceLocation resourceLocation, int i, boolean bl, boolean bl2) {
		long l = (bl ? this.worldSeed : 0L) ^ (long)i;
		return new RandomSequence(l, bl2 ? Optional.of(resourceLocation) : Optional.empty());
	}

	public void forAllSequences(BiConsumer<ResourceLocation, RandomSequence> biConsumer) {
		this.sequences.forEach(biConsumer);
	}

	public void setSeedDefaults(int i, boolean bl, boolean bl2) {
		this.salt = i;
		this.includeWorldSeed = bl;
		this.includeSequenceId = bl2;
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		compoundTag.putInt("salt", this.salt);
		compoundTag.putBoolean("include_world_seed", this.includeWorldSeed);
		compoundTag.putBoolean("include_sequence_id", this.includeSequenceId);
		CompoundTag compoundTag2 = new CompoundTag();
		this.sequences
			.forEach(
				(resourceLocation, randomSequence) -> compoundTag2.put(
						resourceLocation.toString(), (Tag)RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, randomSequence).result().orElseThrow()
					)
			);
		compoundTag.put("sequences", compoundTag2);
		return compoundTag;
	}

	private static boolean getBooleanWithDefault(CompoundTag compoundTag, String string, boolean bl) {
		return compoundTag.contains(string, 1) ? compoundTag.getBoolean(string) : bl;
	}

	public static RandomSequences load(long l, CompoundTag compoundTag) {
		RandomSequences randomSequences = new RandomSequences(l);
		randomSequences.setSeedDefaults(
			compoundTag.getInt("salt"), getBooleanWithDefault(compoundTag, "include_world_seed", true), getBooleanWithDefault(compoundTag, "include_sequence_id", true)
		);
		CompoundTag compoundTag2 = compoundTag.getCompound("sequences");

		for (String string : compoundTag2.getAllKeys()) {
			try {
				RandomSequence randomSequence = (RandomSequence)((Pair)RandomSequence.CODEC.decode(NbtOps.INSTANCE, compoundTag2.get(string)).result().get()).getFirst();
				randomSequences.sequences.put(new ResourceLocation(string), randomSequence);
			} catch (Exception var9) {
				LOGGER.error("Failed to load random sequence {}", string, var9);
			}
		}

		return randomSequences;
	}

	public int clear() {
		int i = this.sequences.size();
		this.sequences.clear();
		return i;
	}

	public void reset(ResourceLocation resourceLocation) {
		this.sequences.put(resourceLocation, this.createSequence(resourceLocation));
	}

	public void reset(ResourceLocation resourceLocation, int i, boolean bl, boolean bl2) {
		this.sequences.put(resourceLocation, this.createSequence(resourceLocation, i, bl, bl2));
	}

	class DirtyMarkingRandomSource implements RandomSource {
		private final RandomSource random;

		DirtyMarkingRandomSource(RandomSource randomSource) {
			this.random = randomSource;
		}

		@Override
		public RandomSource fork() {
			RandomSequences.this.setDirty();
			return this.random.fork();
		}

		@Override
		public PositionalRandomFactory forkPositional() {
			RandomSequences.this.setDirty();
			return this.random.forkPositional();
		}

		@Override
		public void setSeed(long l) {
			RandomSequences.this.setDirty();
			this.random.setSeed(l);
		}

		@Override
		public int nextInt() {
			RandomSequences.this.setDirty();
			return this.random.nextInt();
		}

		@Override
		public int nextInt(int i) {
			RandomSequences.this.setDirty();
			return this.random.nextInt(i);
		}

		@Override
		public long nextLong() {
			RandomSequences.this.setDirty();
			return this.random.nextLong();
		}

		@Override
		public boolean nextBoolean() {
			RandomSequences.this.setDirty();
			return this.random.nextBoolean();
		}

		@Override
		public float nextFloat() {
			RandomSequences.this.setDirty();
			return this.random.nextFloat();
		}

		@Override
		public double nextDouble() {
			RandomSequences.this.setDirty();
			return this.random.nextDouble();
		}

		@Override
		public double nextGaussian() {
			RandomSequences.this.setDirty();
			return this.random.nextGaussian();
		}

		public boolean equals(Object object) {
			if (this == object) {
				return true;
			} else {
				return object instanceof RandomSequences.DirtyMarkingRandomSource dirtyMarkingRandomSource ? this.random.equals(dirtyMarkingRandomSource.random) : false;
			}
		}
	}
}

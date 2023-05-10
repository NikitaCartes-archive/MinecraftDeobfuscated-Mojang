package net.minecraft.world;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import java.util.Map;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;
import net.minecraft.world.level.saveddata.SavedData;
import org.slf4j.Logger;

public class RandomSequences extends SavedData {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final long seed;
	private final Map<ResourceLocation, RandomSequence> sequences = new Object2ObjectOpenHashMap<>();

	public RandomSequences(long l) {
		this.seed = l;
	}

	public RandomSource get(ResourceLocation resourceLocation) {
		final RandomSource randomSource = ((RandomSequence)this.sequences
				.computeIfAbsent(resourceLocation, resourceLocationx -> new RandomSequence(this.seed, resourceLocationx)))
			.random();
		return new RandomSource() {
			@Override
			public RandomSource fork() {
				RandomSequences.this.setDirty();
				return randomSource.fork();
			}

			@Override
			public PositionalRandomFactory forkPositional() {
				RandomSequences.this.setDirty();
				return randomSource.forkPositional();
			}

			@Override
			public void setSeed(long l) {
				RandomSequences.this.setDirty();
				randomSource.setSeed(l);
			}

			@Override
			public int nextInt() {
				RandomSequences.this.setDirty();
				return randomSource.nextInt();
			}

			@Override
			public int nextInt(int i) {
				RandomSequences.this.setDirty();
				return randomSource.nextInt(i);
			}

			@Override
			public long nextLong() {
				RandomSequences.this.setDirty();
				return randomSource.nextLong();
			}

			@Override
			public boolean nextBoolean() {
				RandomSequences.this.setDirty();
				return randomSource.nextBoolean();
			}

			@Override
			public float nextFloat() {
				RandomSequences.this.setDirty();
				return randomSource.nextFloat();
			}

			@Override
			public double nextDouble() {
				RandomSequences.this.setDirty();
				return randomSource.nextDouble();
			}

			@Override
			public double nextGaussian() {
				RandomSequences.this.setDirty();
				return randomSource.nextGaussian();
			}
		};
	}

	@Override
	public CompoundTag save(CompoundTag compoundTag) {
		this.sequences
			.forEach(
				(resourceLocation, randomSequence) -> compoundTag.put(
						resourceLocation.toString(), (Tag)RandomSequence.CODEC.encodeStart(NbtOps.INSTANCE, randomSequence).result().orElseThrow()
					)
			);
		return compoundTag;
	}

	public static RandomSequences load(long l, CompoundTag compoundTag) {
		RandomSequences randomSequences = new RandomSequences(l);

		for (String string : compoundTag.getAllKeys()) {
			try {
				RandomSequence randomSequence = (RandomSequence)((Pair)RandomSequence.CODEC.decode(NbtOps.INSTANCE, compoundTag.get(string)).result().get()).getFirst();
				randomSequences.sequences.put(new ResourceLocation(string), randomSequence);
			} catch (Exception var8) {
				LOGGER.error("Failed to load random sequence {}", string, var8);
			}
		}

		return randomSequences;
	}
}

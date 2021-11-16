package net.minecraft.world.level.levelgen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.BitSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.ProtoChunk;

public final class BelowZeroRetrogen {
	private static final BitSet EMPTY = new BitSet(0);
	private static final Codec<BitSet> BITSET_CODEC = Codec.LONG_STREAM
		.xmap(longStream -> BitSet.valueOf(longStream.toArray()), bitSet -> LongStream.of(bitSet.toLongArray()));
	private static final Codec<ChunkStatus> NON_EMPTY_CHUNK_STATUS = Registry.CHUNK_STATUS
		.byNameCodec()
		.comapFlatMap(
			chunkStatus -> chunkStatus == ChunkStatus.EMPTY ? DataResult.error("target_status cannot be empty") : DataResult.success(chunkStatus), Function.identity()
		);
	public static final Codec<BelowZeroRetrogen> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					NON_EMPTY_CHUNK_STATUS.fieldOf("target_status").forGetter(BelowZeroRetrogen::targetStatus),
					BITSET_CODEC.optionalFieldOf("missing_bedrock")
						.forGetter(belowZeroRetrogen -> belowZeroRetrogen.missingBedrock.isEmpty() ? Optional.empty() : Optional.of(belowZeroRetrogen.missingBedrock))
				)
				.apply(instance, BelowZeroRetrogen::new)
	);
	private static final Set<ResourceKey<Biome>> RETAINED_RETROGEN_BIOMES = Set.of(Biomes.LUSH_CAVES, Biomes.DRIPSTONE_CAVES);
	public static final LevelHeightAccessor UPGRADE_HEIGHT_ACCESSOR = new LevelHeightAccessor() {
		@Override
		public int getHeight() {
			return 64;
		}

		@Override
		public int getMinBuildHeight() {
			return -64;
		}
	};
	private final ChunkStatus targetStatus;
	private final BitSet missingBedrock;

	private BelowZeroRetrogen(ChunkStatus chunkStatus, Optional<BitSet> optional) {
		this.targetStatus = chunkStatus;
		this.missingBedrock = (BitSet)optional.orElse(EMPTY);
	}

	@Nullable
	public static BelowZeroRetrogen read(CompoundTag compoundTag) {
		ChunkStatus chunkStatus = ChunkStatus.byName(compoundTag.getString("target_status"));
		return chunkStatus == ChunkStatus.EMPTY ? null : new BelowZeroRetrogen(chunkStatus, Optional.of(BitSet.valueOf(compoundTag.getLongArray("missing_bedrock"))));
	}

	public static void replaceOldBedrock(ProtoChunk protoChunk) {
		int i = 4;
		BlockPos.betweenClosed(0, 0, 0, 15, 4, 15).forEach(blockPos -> {
			if (protoChunk.getBlockState(blockPos).is(Blocks.BEDROCK)) {
				protoChunk.setBlockState(blockPos, Blocks.DEEPSLATE.defaultBlockState(), false);
			}
		});
	}

	public static void removeBedrock(ProtoChunk protoChunk) {
		LevelHeightAccessor levelHeightAccessor = protoChunk.getHeightAccessorForGeneration();
		int i = levelHeightAccessor.getMinBuildHeight();
		int j = levelHeightAccessor.getMaxBuildHeight() - 1;
		BlockPos.betweenClosed(0, i, 0, 15, j, 15).forEach(blockPos -> protoChunk.setBlockState(blockPos, Blocks.AIR.defaultBlockState(), false));
	}

	public ChunkStatus targetStatus() {
		return this.targetStatus;
	}

	public boolean hasAllBedrockMissing() {
		int i = this.missingBedrock.size();
		return i == 256 && i == this.missingBedrock.cardinality();
	}

	public static BiomeResolver getBiomeResolver(BiomeResolver biomeResolver, Registry<Biome> registry, ChunkAccess chunkAccess) {
		if (!chunkAccess.isUpgrading()) {
			return biomeResolver;
		} else {
			Set<Biome> set = (Set<Biome>)RETAINED_RETROGEN_BIOMES.stream().map(registry::get).collect(Collectors.toSet());
			return (i, j, k, sampler) -> {
				Biome biome = biomeResolver.getNoiseBiome(i, j, k, sampler);
				return set.contains(biome) ? biome : chunkAccess.getNoiseBiome(i, 0, k);
			};
		}
	}
}
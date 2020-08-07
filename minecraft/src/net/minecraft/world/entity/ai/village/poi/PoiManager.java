package net.minecraft.world.entity.ai.village.poi;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ByteOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.io.File;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.SectionTracker;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.storage.SectionStorage;

public class PoiManager extends SectionStorage<PoiSection> {
	private final PoiManager.DistanceTracker distanceTracker;
	private final LongSet loadedChunks = new LongOpenHashSet();

	public PoiManager(File file, DataFixer dataFixer, boolean bl) {
		super(file, PoiSection::codec, PoiSection::new, dataFixer, DataFixTypes.POI_CHUNK, bl);
		this.distanceTracker = new PoiManager.DistanceTracker();
	}

	public void add(BlockPos blockPos, PoiType poiType) {
		this.getOrCreate(SectionPos.of(blockPos).asLong()).add(blockPos, poiType);
	}

	public void remove(BlockPos blockPos) {
		this.getOrCreate(SectionPos.of(blockPos).asLong()).remove(blockPos);
	}

	public long getCountInRange(Predicate<PoiType> predicate, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		return this.getInRange(predicate, blockPos, i, occupancy).count();
	}

	public boolean existsAtPosition(PoiType poiType, BlockPos blockPos) {
		Optional<PoiType> optional = this.getOrCreate(SectionPos.of(blockPos).asLong()).getType(blockPos);
		return optional.isPresent() && ((PoiType)optional.get()).equals(poiType);
	}

	public Stream<PoiRecord> getInSquare(Predicate<PoiType> predicate, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		int j = Math.floorDiv(i, 16) + 1;
		return ChunkPos.rangeClosed(new ChunkPos(blockPos), j).flatMap(chunkPos -> this.getInChunk(predicate, chunkPos, occupancy)).filter(poiRecord -> {
			BlockPos blockPos2 = poiRecord.getPos();
			return Math.abs(blockPos2.getX() - blockPos.getX()) <= i && Math.abs(blockPos2.getZ() - blockPos.getZ()) <= i;
		});
	}

	public Stream<PoiRecord> getInRange(Predicate<PoiType> predicate, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		int j = i * i;
		return this.getInSquare(predicate, blockPos, i, occupancy).filter(poiRecord -> poiRecord.getPos().distSqr(blockPos) <= (double)j);
	}

	public Stream<PoiRecord> getInChunk(Predicate<PoiType> predicate, ChunkPos chunkPos, PoiManager.Occupancy occupancy) {
		return IntStream.range(0, 16)
			.boxed()
			.map(integer -> this.getOrLoad(SectionPos.of(chunkPos, integer).asLong()))
			.filter(Optional::isPresent)
			.flatMap(optional -> ((PoiSection)optional.get()).getRecords(predicate, occupancy));
	}

	public Stream<BlockPos> findAll(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		return this.getInRange(predicate, blockPos, i, occupancy).map(PoiRecord::getPos).filter(predicate2);
	}

	public Stream<BlockPos> findAllClosestFirst(
		Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, PoiManager.Occupancy occupancy
	) {
		return this.findAll(predicate, predicate2, blockPos, i, occupancy).sorted(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)));
	}

	public Optional<BlockPos> find(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		return this.findAll(predicate, predicate2, blockPos, i, occupancy).findFirst();
	}

	public Optional<BlockPos> findClosest(Predicate<PoiType> predicate, BlockPos blockPos, int i, PoiManager.Occupancy occupancy) {
		return this.getInRange(predicate, blockPos, i, occupancy).map(PoiRecord::getPos).min(Comparator.comparingDouble(blockPos2 -> blockPos2.distSqr(blockPos)));
	}

	public Optional<BlockPos> take(Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, BlockPos blockPos, int i) {
		return this.getInRange(predicate, blockPos, i, PoiManager.Occupancy.HAS_SPACE)
			.filter(poiRecord -> predicate2.test(poiRecord.getPos()))
			.findFirst()
			.map(poiRecord -> {
				poiRecord.acquireTicket();
				return poiRecord.getPos();
			});
	}

	public Optional<BlockPos> getRandom(
		Predicate<PoiType> predicate, Predicate<BlockPos> predicate2, PoiManager.Occupancy occupancy, BlockPos blockPos, int i, Random random
	) {
		List<PoiRecord> list = (List<PoiRecord>)this.getInRange(predicate, blockPos, i, occupancy).collect(Collectors.toList());
		Collections.shuffle(list, random);
		return list.stream().filter(poiRecord -> predicate2.test(poiRecord.getPos())).findFirst().map(PoiRecord::getPos);
	}

	public boolean release(BlockPos blockPos) {
		return this.getOrCreate(SectionPos.of(blockPos).asLong()).release(blockPos);
	}

	public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
		return (Boolean)this.getOrLoad(SectionPos.of(blockPos).asLong()).map(poiSection -> poiSection.exists(blockPos, predicate)).orElse(false);
	}

	public Optional<PoiType> getType(BlockPos blockPos) {
		PoiSection poiSection = this.getOrCreate(SectionPos.of(blockPos).asLong());
		return poiSection.getType(blockPos);
	}

	public int sectionsToVillage(SectionPos sectionPos) {
		this.distanceTracker.runAllUpdates();
		return this.distanceTracker.getLevel(sectionPos.asLong());
	}

	private boolean isVillageCenter(long l) {
		Optional<PoiSection> optional = this.get(l);
		return optional == null
			? false
			: (Boolean)optional.map(poiSection -> poiSection.getRecords(PoiType.ALL, PoiManager.Occupancy.IS_OCCUPIED).count() > 0L).orElse(false);
	}

	@Override
	public void tick(BooleanSupplier booleanSupplier) {
		super.tick(booleanSupplier);
		this.distanceTracker.runAllUpdates();
	}

	@Override
	protected void setDirty(long l) {
		super.setDirty(l);
		this.distanceTracker.update(l, this.distanceTracker.getLevelFromSource(l), false);
	}

	@Override
	protected void onSectionLoad(long l) {
		this.distanceTracker.update(l, this.distanceTracker.getLevelFromSource(l), false);
	}

	public void checkConsistencyWithBlocks(ChunkPos chunkPos, LevelChunkSection levelChunkSection) {
		SectionPos sectionPos = SectionPos.of(chunkPos, levelChunkSection.bottomBlockY() >> 4);
		Util.ifElse(this.getOrLoad(sectionPos.asLong()), poiSection -> poiSection.refresh(biConsumer -> {
				if (mayHavePoi(levelChunkSection)) {
					this.updateFromSection(levelChunkSection, sectionPos, biConsumer);
				}
			}), () -> {
			if (mayHavePoi(levelChunkSection)) {
				PoiSection poiSection = this.getOrCreate(sectionPos.asLong());
				this.updateFromSection(levelChunkSection, sectionPos, poiSection::add);
			}
		});
	}

	private static boolean mayHavePoi(LevelChunkSection levelChunkSection) {
		return levelChunkSection.maybeHas(PoiType.ALL_STATES::contains);
	}

	private void updateFromSection(LevelChunkSection levelChunkSection, SectionPos sectionPos, BiConsumer<BlockPos, PoiType> biConsumer) {
		sectionPos.blocksInside()
			.forEach(
				blockPos -> {
					BlockState blockState = levelChunkSection.getBlockState(
						SectionPos.sectionRelative(blockPos.getX()), SectionPos.sectionRelative(blockPos.getY()), SectionPos.sectionRelative(blockPos.getZ())
					);
					PoiType.forState(blockState).ifPresent(poiType -> biConsumer.accept(blockPos, poiType));
				}
			);
	}

	public void ensureLoadedAndValid(LevelReader levelReader, BlockPos blockPos, int i) {
		SectionPos.aroundChunk(new ChunkPos(blockPos), Math.floorDiv(i, 16))
			.map(sectionPos -> Pair.of(sectionPos, this.getOrLoad(sectionPos.asLong())))
			.filter(pair -> !(Boolean)((Optional)pair.getSecond()).map(PoiSection::isValid).orElse(false))
			.map(pair -> ((SectionPos)pair.getFirst()).chunk())
			.filter(chunkPos -> this.loadedChunks.add(chunkPos.toLong()))
			.forEach(chunkPos -> levelReader.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.EMPTY));
	}

	final class DistanceTracker extends SectionTracker {
		private final Long2ByteMap levels = new Long2ByteOpenHashMap();

		protected DistanceTracker() {
			super(7, 16, 256);
			this.levels.defaultReturnValue((byte)7);
		}

		@Override
		protected int getLevelFromSource(long l) {
			return PoiManager.this.isVillageCenter(l) ? 0 : 7;
		}

		@Override
		protected int getLevel(long l) {
			return this.levels.get(l);
		}

		@Override
		protected void setLevel(long l, int i) {
			if (i > 6) {
				this.levels.remove(l);
			} else {
				this.levels.put(l, (byte)i);
			}
		}

		public void runAllUpdates() {
			super.runUpdates(Integer.MAX_VALUE);
		}
	}

	public static enum Occupancy {
		HAS_SPACE(PoiRecord::hasSpace),
		IS_OCCUPIED(PoiRecord::isOccupied),
		ANY(poiRecord -> true);

		private final Predicate<? super PoiRecord> test;

		private Occupancy(Predicate<? super PoiRecord> predicate) {
			this.test = predicate;
		}

		public Predicate<? super PoiRecord> getTest() {
			return this.test;
		}
	}
}

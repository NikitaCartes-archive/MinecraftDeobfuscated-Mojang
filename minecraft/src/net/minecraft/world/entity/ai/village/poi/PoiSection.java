package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoiSection {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
	private final Map<PoiType, Set<PoiRecord>> byType = Maps.<PoiType, Set<PoiRecord>>newHashMap();
	private final Runnable setDirty;
	private boolean isValid;

	public static Codec<PoiSection> codec(Runnable runnable) {
		return RecordCodecBuilder.<PoiSection>create(
				instance -> instance.group(
							RecordCodecBuilder.point(runnable),
							Codec.BOOL.optionalFieldOf("Valid", Boolean.valueOf(false)).forGetter(poiSection -> poiSection.isValid),
							PoiRecord.codec(runnable).listOf().fieldOf("Records").forGetter(poiSection -> ImmutableList.copyOf(poiSection.records.values()))
						)
						.apply(instance, PoiSection::new)
			)
			.orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> new PoiSection(runnable, false, ImmutableList.of()));
	}

	public PoiSection(Runnable runnable) {
		this(runnable, true, ImmutableList.of());
	}

	private PoiSection(Runnable runnable, boolean bl, List<PoiRecord> list) {
		this.setDirty = runnable;
		this.isValid = bl;
		list.forEach(this::add);
	}

	public Stream<PoiRecord> getRecords(Predicate<PoiType> predicate, PoiManager.Occupancy occupancy) {
		return this.byType
			.entrySet()
			.stream()
			.filter(entry -> predicate.test((PoiType)entry.getKey()))
			.flatMap(entry -> ((Set)entry.getValue()).stream())
			.filter(occupancy.getTest());
	}

	public void add(BlockPos blockPos, PoiType poiType) {
		if (this.add(new PoiRecord(blockPos, poiType, this.setDirty))) {
			LOGGER.debug("Added POI of type {} @ {}", () -> poiType, () -> blockPos);
			this.setDirty.run();
		}
	}

	private boolean add(PoiRecord poiRecord) {
		BlockPos blockPos = poiRecord.getPos();
		PoiType poiType = poiRecord.getPoiType();
		short s = SectionPos.sectionRelativePos(blockPos);
		PoiRecord poiRecord2 = this.records.get(s);
		if (poiRecord2 != null) {
			if (poiType.equals(poiRecord2.getPoiType())) {
				return false;
			}

			Util.logAndPauseIfInIde("POI data mismatch: already registered at " + blockPos);
		}

		this.records.put(s, poiRecord);
		((Set)this.byType.computeIfAbsent(poiType, poiTypex -> Sets.newHashSet())).add(poiRecord);
		return true;
	}

	public void remove(BlockPos blockPos) {
		PoiRecord poiRecord = this.records.remove(SectionPos.sectionRelativePos(blockPos));
		if (poiRecord == null) {
			LOGGER.error("POI data mismatch: never registered at {}", blockPos);
		} else {
			((Set)this.byType.get(poiRecord.getPoiType())).remove(poiRecord);
			LOGGER.debug("Removed POI of type {} @ {}", poiRecord::getPoiType, poiRecord::getPos);
			this.setDirty.run();
		}
	}

	@Deprecated
	@VisibleForDebug
	public int getFreeTickets(BlockPos blockPos) {
		return (Integer)this.getPoiRecord(blockPos).map(PoiRecord::getFreeTickets).orElse(0);
	}

	public boolean release(BlockPos blockPos) {
		PoiRecord poiRecord = this.records.get(SectionPos.sectionRelativePos(blockPos));
		if (poiRecord == null) {
			throw (IllegalStateException)Util.pauseInIde(new IllegalStateException("POI never registered at " + blockPos));
		} else {
			boolean bl = poiRecord.releaseTicket();
			this.setDirty.run();
			return bl;
		}
	}

	public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
		return this.getType(blockPos).filter(predicate).isPresent();
	}

	public Optional<PoiType> getType(BlockPos blockPos) {
		return this.getPoiRecord(blockPos).map(PoiRecord::getPoiType);
	}

	private Optional<PoiRecord> getPoiRecord(BlockPos blockPos) {
		return Optional.ofNullable(this.records.get(SectionPos.sectionRelativePos(blockPos)));
	}

	public void refresh(Consumer<BiConsumer<BlockPos, PoiType>> consumer) {
		if (!this.isValid) {
			Short2ObjectMap<PoiRecord> short2ObjectMap = new Short2ObjectOpenHashMap<>(this.records);
			this.clear();
			consumer.accept((BiConsumer)(blockPos, poiType) -> {
				short s = SectionPos.sectionRelativePos(blockPos);
				PoiRecord poiRecord = short2ObjectMap.computeIfAbsent(s, i -> new PoiRecord(blockPos, poiType, this.setDirty));
				this.add(poiRecord);
			});
			this.isValid = true;
			this.setDirty.run();
		}
	}

	private void clear() {
		this.records.clear();
		this.byType.clear();
	}

	boolean isValid() {
		return this.isValid;
	}
}

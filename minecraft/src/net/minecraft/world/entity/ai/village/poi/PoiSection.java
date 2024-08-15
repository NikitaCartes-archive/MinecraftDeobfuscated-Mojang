package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.shorts.Short2ObjectFunction;
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
import net.minecraft.core.Holder;
import net.minecraft.core.SectionPos;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class PoiSection {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
	private final Map<Holder<PoiType>, Set<PoiRecord>> byType = Maps.<Holder<PoiType>, Set<PoiRecord>>newHashMap();
	private final Runnable setDirty;
	private boolean isValid;

	public PoiSection(Runnable runnable) {
		this(runnable, true, ImmutableList.of());
	}

	PoiSection(Runnable runnable, boolean bl, List<PoiRecord> list) {
		this.setDirty = runnable;
		this.isValid = bl;
		list.forEach(this::add);
	}

	public PoiSection.Packed pack() {
		return new PoiSection.Packed(this.isValid, this.records.values().stream().map(PoiRecord::pack).toList());
	}

	public Stream<PoiRecord> getRecords(Predicate<Holder<PoiType>> predicate, PoiManager.Occupancy occupancy) {
		return this.byType
			.entrySet()
			.stream()
			.filter(entry -> predicate.test((Holder)entry.getKey()))
			.flatMap(entry -> ((Set)entry.getValue()).stream())
			.filter(occupancy.getTest());
	}

	public void add(BlockPos blockPos, Holder<PoiType> holder) {
		if (this.add(new PoiRecord(blockPos, holder, this.setDirty))) {
			LOGGER.debug("Added POI of type {} @ {}", holder.getRegisteredName(), blockPos);
			this.setDirty.run();
		}
	}

	private boolean add(PoiRecord poiRecord) {
		BlockPos blockPos = poiRecord.getPos();
		Holder<PoiType> holder = poiRecord.getPoiType();
		short s = SectionPos.sectionRelativePos(blockPos);
		PoiRecord poiRecord2 = this.records.get(s);
		if (poiRecord2 != null) {
			if (holder.equals(poiRecord2.getPoiType())) {
				return false;
			}

			Util.logAndPauseIfInIde("POI data mismatch: already registered at " + blockPos);
		}

		this.records.put(s, poiRecord);
		((Set)this.byType.computeIfAbsent(holder, holderx -> Sets.newHashSet())).add(poiRecord);
		return true;
	}

	public void remove(BlockPos blockPos) {
		PoiRecord poiRecord = this.records.remove(SectionPos.sectionRelativePos(blockPos));
		if (poiRecord == null) {
			LOGGER.error("POI data mismatch: never registered at {}", blockPos);
		} else {
			((Set)this.byType.get(poiRecord.getPoiType())).remove(poiRecord);
			LOGGER.debug("Removed POI of type {} @ {}", LogUtils.defer(poiRecord::getPoiType), LogUtils.defer(poiRecord::getPos));
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

	public boolean exists(BlockPos blockPos, Predicate<Holder<PoiType>> predicate) {
		return this.getType(blockPos).filter(predicate).isPresent();
	}

	public Optional<Holder<PoiType>> getType(BlockPos blockPos) {
		return this.getPoiRecord(blockPos).map(PoiRecord::getPoiType);
	}

	private Optional<PoiRecord> getPoiRecord(BlockPos blockPos) {
		return Optional.ofNullable(this.records.get(SectionPos.sectionRelativePos(blockPos)));
	}

	public void refresh(Consumer<BiConsumer<BlockPos, Holder<PoiType>>> consumer) {
		if (!this.isValid) {
			Short2ObjectMap<PoiRecord> short2ObjectMap = new Short2ObjectOpenHashMap<>(this.records);
			this.clear();
			consumer.accept((BiConsumer)(blockPos, holder) -> {
				short s = SectionPos.sectionRelativePos(blockPos);
				PoiRecord poiRecord = short2ObjectMap.computeIfAbsent(s, (Short2ObjectFunction<? extends PoiRecord>)(sx -> new PoiRecord(blockPos, holder, this.setDirty)));
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

	public static record Packed(boolean isValid, List<PoiRecord.Packed> records) {
		public static final Codec<PoiSection.Packed> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.BOOL.lenientOptionalFieldOf("Valid", Boolean.valueOf(false)).forGetter(PoiSection.Packed::isValid),
						PoiRecord.Packed.CODEC.listOf().fieldOf("Records").forGetter(PoiSection.Packed::records)
					)
					.apply(instance, PoiSection.Packed::new)
		);

		public PoiSection unpack(Runnable runnable) {
			return new PoiSection(runnable, this.isValid, this.records.stream().map(packed -> packed.unpack(runnable)).toList());
		}
	}
}

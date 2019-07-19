package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import it.unimi.dsi.fastutil.shorts.Short2ObjectMap;
import it.unimi.dsi.fastutil.shorts.Short2ObjectOpenHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.Serializable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PoiSection implements Serializable {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<>();
	private final Map<PoiType, Set<PoiRecord>> byType = Maps.<PoiType, Set<PoiRecord>>newHashMap();
	private final Runnable setDirty;
	private boolean isValid;

	public PoiSection(Runnable runnable) {
		this.setDirty = runnable;
		this.isValid = true;
	}

	public <T> PoiSection(Runnable runnable, Dynamic<T> dynamic) {
		this.setDirty = runnable;

		try {
			this.isValid = dynamic.get("Valid").asBoolean(false);
			dynamic.get("Records").asStream().forEach(dynamicx -> this.add(new PoiRecord(dynamicx, runnable)));
		} catch (Exception var4) {
			LOGGER.error("Failed to load POI chunk", (Throwable)var4);
			this.clear();
			this.isValid = false;
		}
	}

	public Stream<PoiRecord> getRecords(Predicate<PoiType> predicate, PoiManager.Occupancy occupancy) {
		return this.byType
			.entrySet()
			.stream()
			.filter(entry -> predicate.test(entry.getKey()))
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
			} else {
				throw new IllegalStateException("POI data mismatch: already registered at " + blockPos);
			}
		} else {
			this.records.put(s, poiRecord);
			((Set)this.byType.computeIfAbsent(poiType, poiTypex -> Sets.newHashSet())).add(poiRecord);
			return true;
		}
	}

	public void remove(BlockPos blockPos) {
		PoiRecord poiRecord = this.records.remove(SectionPos.sectionRelativePos(blockPos));
		if (poiRecord == null) {
			LOGGER.error("POI data mismatch: never registered at " + blockPos);
		} else {
			((Set)this.byType.get(poiRecord.getPoiType())).remove(poiRecord);
			LOGGER.debug("Removed POI of type {} @ {}", poiRecord::getPoiType, poiRecord::getPos);
			this.setDirty.run();
		}
	}

	public boolean release(BlockPos blockPos) {
		PoiRecord poiRecord = this.records.get(SectionPos.sectionRelativePos(blockPos));
		if (poiRecord == null) {
			throw new IllegalStateException("POI never registered at " + blockPos);
		} else {
			boolean bl = poiRecord.releaseTicket();
			this.setDirty.run();
			return bl;
		}
	}

	public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
		short s = SectionPos.sectionRelativePos(blockPos);
		PoiRecord poiRecord = this.records.get(s);
		return poiRecord != null && predicate.test(poiRecord.getPoiType());
	}

	public Optional<PoiType> getType(BlockPos blockPos) {
		short s = SectionPos.sectionRelativePos(blockPos);
		PoiRecord poiRecord = this.records.get(s);
		return poiRecord != null ? Optional.of(poiRecord.getPoiType()) : Optional.empty();
	}

	@Override
	public <T> T serialize(DynamicOps<T> dynamicOps) {
		T object = dynamicOps.createList(this.records.values().stream().map(poiRecord -> poiRecord.serialize(dynamicOps)));
		return dynamicOps.createMap(
			ImmutableMap.of(dynamicOps.createString("Records"), object, dynamicOps.createString("Valid"), dynamicOps.createBoolean(this.isValid))
		);
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
}

/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.village.poi;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
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
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Supplier;

public class PoiSection {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Short2ObjectMap<PoiRecord> records = new Short2ObjectOpenHashMap<PoiRecord>();
    private final Map<PoiType, Set<PoiRecord>> byType = Maps.newHashMap();
    private final Runnable setDirty;
    private boolean isValid;

    public static Codec<PoiSection> codec(Runnable runnable) {
        return RecordCodecBuilder.create(instance -> instance.group(RecordCodecBuilder.point(runnable), Codec.BOOL.optionalFieldOf("Valid", false).forGetter(poiSection -> poiSection.isValid), ((MapCodec)PoiRecord.codec(runnable).listOf().fieldOf("Records")).forGetter(poiSection -> ImmutableList.copyOf(poiSection.records.values()))).apply((Applicative<PoiSection, ?>)instance, PoiSection::new)).orElseGet(Util.prefix("Failed to read POI section: ", LOGGER::error), () -> new PoiSection(runnable, false, ImmutableList.of()));
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
        return this.byType.entrySet().stream().filter(entry -> predicate.test((PoiType)entry.getKey())).flatMap(entry -> ((Set)entry.getValue()).stream()).filter(occupancy.getTest());
    }

    public void add(BlockPos blockPos, PoiType poiType) {
        if (this.add(new PoiRecord(blockPos, poiType, this.setDirty))) {
            LOGGER.debug("Added POI of type {} @ {}", () -> poiType, () -> blockPos);
            this.setDirty.run();
        }
    }

    private boolean add(PoiRecord poiRecord) {
        BlockPos blockPos = poiRecord.getPos();
        PoiType poiType2 = poiRecord.getPoiType();
        short s = SectionPos.sectionRelativePos(blockPos);
        PoiRecord poiRecord2 = (PoiRecord)this.records.get(s);
        if (poiRecord2 != null) {
            if (poiType2.equals(poiRecord2.getPoiType())) {
                return false;
            }
            Util.logAndPauseIfInIde("POI data mismatch: already registered at " + blockPos);
        }
        this.records.put(s, poiRecord);
        this.byType.computeIfAbsent(poiType2, poiType -> Sets.newHashSet()).add(poiRecord);
        return true;
    }

    public void remove(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord)this.records.remove(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            LOGGER.error("POI data mismatch: never registered at {}", (Object)blockPos);
            return;
        }
        this.byType.get(poiRecord.getPoiType()).remove(poiRecord);
        Supplier[] supplierArray = new Supplier[2];
        supplierArray[0] = poiRecord::getPoiType;
        supplierArray[1] = poiRecord::getPos;
        LOGGER.debug("Removed POI of type {} @ {}", supplierArray);
        this.setDirty.run();
    }

    public boolean release(BlockPos blockPos) {
        PoiRecord poiRecord = (PoiRecord)this.records.get(SectionPos.sectionRelativePos(blockPos));
        if (poiRecord == null) {
            throw Util.pauseInIde(new IllegalStateException("POI never registered at " + blockPos));
        }
        boolean bl = poiRecord.releaseTicket();
        this.setDirty.run();
        return bl;
    }

    public boolean exists(BlockPos blockPos, Predicate<PoiType> predicate) {
        short s = SectionPos.sectionRelativePos(blockPos);
        PoiRecord poiRecord = (PoiRecord)this.records.get(s);
        return poiRecord != null && predicate.test(poiRecord.getPoiType());
    }

    public Optional<PoiType> getType(BlockPos blockPos) {
        short s = SectionPos.sectionRelativePos(blockPos);
        PoiRecord poiRecord = (PoiRecord)this.records.get(s);
        return poiRecord != null ? Optional.of(poiRecord.getPoiType()) : Optional.empty();
    }

    public void refresh(Consumer<BiConsumer<BlockPos, PoiType>> consumer) {
        if (!this.isValid) {
            Short2ObjectOpenHashMap<PoiRecord> short2ObjectMap = new Short2ObjectOpenHashMap<PoiRecord>(this.records);
            this.clear();
            consumer.accept((blockPos, poiType) -> {
                short s = SectionPos.sectionRelativePos(blockPos);
                PoiRecord poiRecord = short2ObjectMap.computeIfAbsent(s, i -> new PoiRecord((BlockPos)blockPos, (PoiType)poiType, this.setDirty));
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


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.kinds.Applicative;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.entity.ai.gossip.GossipType;
import org.slf4j.Logger;

public class GossipContainer {
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final int DISCARD_THRESHOLD = 2;
    private final Map<UUID, EntityGossips> gossips = Maps.newHashMap();

    @VisibleForDebug
    public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
        HashMap<UUID, Object2IntMap<GossipType>> map = Maps.newHashMap();
        this.gossips.keySet().forEach(uUID -> {
            EntityGossips entityGossips = this.gossips.get(uUID);
            map.put((UUID)uUID, entityGossips.entries);
        });
        return map;
    }

    public void decay() {
        Iterator<EntityGossips> iterator = this.gossips.values().iterator();
        while (iterator.hasNext()) {
            EntityGossips entityGossips = iterator.next();
            entityGossips.decay();
            if (!entityGossips.isEmpty()) continue;
            iterator.remove();
        }
    }

    private Stream<GossipEntry> unpack() {
        return this.gossips.entrySet().stream().flatMap(entry -> ((EntityGossips)entry.getValue()).unpack((UUID)entry.getKey()));
    }

    private Collection<GossipEntry> selectGossipsForTransfer(RandomSource randomSource, int i) {
        List<GossipEntry> list = this.unpack().toList();
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] is = new int[list.size()];
        int j = 0;
        for (int k = 0; k < list.size(); ++k) {
            GossipEntry gossipEntry = list.get(k);
            is[k] = (j += Math.abs(gossipEntry.weightedValue())) - 1;
        }
        Set<GossipEntry> set = Sets.newIdentityHashSet();
        for (int l = 0; l < i; ++l) {
            int m = randomSource.nextInt(j);
            int n = Arrays.binarySearch(is, m);
            set.add(list.get(n < 0 ? -n - 1 : n));
        }
        return set;
    }

    private EntityGossips getOrCreate(UUID uUID2) {
        return this.gossips.computeIfAbsent(uUID2, uUID -> new EntityGossips());
    }

    public void transferFrom(GossipContainer gossipContainer, RandomSource randomSource, int i) {
        Collection<GossipEntry> collection = gossipContainer.selectGossipsForTransfer(randomSource, i);
        collection.forEach(gossipEntry -> {
            int i = gossipEntry.value - gossipEntry.type.decayPerTransfer;
            if (i >= 2) {
                this.getOrCreate((UUID)gossipEntry.target).entries.mergeInt(gossipEntry.type, i, GossipContainer::mergeValuesForTransfer);
            }
        });
    }

    public int getReputation(UUID uUID, Predicate<GossipType> predicate) {
        EntityGossips entityGossips = this.gossips.get(uUID);
        return entityGossips != null ? entityGossips.weightedValue(predicate) : 0;
    }

    public long getCountForType(GossipType gossipType, DoublePredicate doublePredicate) {
        return this.gossips.values().stream().filter(entityGossips -> doublePredicate.test(entityGossips.entries.getOrDefault((Object)gossipType, 0) * gossipType.weight)).count();
    }

    public void add(UUID uUID, GossipType gossipType, int i2) {
        EntityGossips entityGossips = this.getOrCreate(uUID);
        entityGossips.entries.mergeInt(gossipType, i2, (i, j) -> this.mergeValuesForAddition(gossipType, i, j));
        entityGossips.makeSureValueIsntTooLowOrTooHigh(gossipType);
        if (entityGossips.isEmpty()) {
            this.gossips.remove(uUID);
        }
    }

    public void remove(UUID uUID, GossipType gossipType, int i) {
        this.add(uUID, gossipType, -i);
    }

    public void remove(UUID uUID, GossipType gossipType) {
        EntityGossips entityGossips = this.gossips.get(uUID);
        if (entityGossips != null) {
            entityGossips.remove(gossipType);
            if (entityGossips.isEmpty()) {
                this.gossips.remove(uUID);
            }
        }
    }

    public void remove(GossipType gossipType) {
        Iterator<EntityGossips> iterator = this.gossips.values().iterator();
        while (iterator.hasNext()) {
            EntityGossips entityGossips = iterator.next();
            entityGossips.remove(gossipType);
            if (!entityGossips.isEmpty()) continue;
            iterator.remove();
        }
    }

    public <T> T store(DynamicOps<T> dynamicOps) {
        return (T)GossipEntry.LIST_CODEC.encodeStart(dynamicOps, this.unpack().toList()).resultOrPartial(string -> LOGGER.warn("Failed to serialize gossips: {}", string)).orElseGet(dynamicOps::emptyList);
    }

    public void update(Dynamic<?> dynamic) {
        GossipEntry.LIST_CODEC.decode(dynamic).resultOrPartial(string -> LOGGER.warn("Failed to deserialize gossips: {}", string)).stream().flatMap(pair -> ((List)pair.getFirst()).stream()).forEach(gossipEntry -> this.getOrCreate((UUID)gossipEntry.target).entries.put(gossipEntry.type, gossipEntry.value));
    }

    private static int mergeValuesForTransfer(int i, int j) {
        return Math.max(i, j);
    }

    private int mergeValuesForAddition(GossipType gossipType, int i, int j) {
        int k = i + j;
        return k > gossipType.max ? Math.max(gossipType.max, i) : k;
    }

    static class EntityGossips {
        final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<GossipType>();

        EntityGossips() {
        }

        public int weightedValue(Predicate<GossipType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter(entry -> predicate.test((GossipType)entry.getKey())).mapToInt(entry -> entry.getIntValue() * ((GossipType)entry.getKey()).weight).sum();
        }

        public Stream<GossipEntry> unpack(UUID uUID) {
            return this.entries.object2IntEntrySet().stream().map(entry -> new GossipEntry(uUID, (GossipType)entry.getKey(), entry.getIntValue()));
        }

        public void decay() {
            Iterator objectIterator = this.entries.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int i = entry.getIntValue() - ((GossipType)entry.getKey()).decayPerDay;
                if (i < 2) {
                    objectIterator.remove();
                    continue;
                }
                entry.setValue(i);
            }
        }

        public boolean isEmpty() {
            return this.entries.isEmpty();
        }

        public void makeSureValueIsntTooLowOrTooHigh(GossipType gossipType) {
            int i = this.entries.getInt(gossipType);
            if (i > gossipType.max) {
                this.entries.put(gossipType, gossipType.max);
            }
            if (i < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(GossipType gossipType) {
            this.entries.removeInt(gossipType);
        }
    }

    record GossipEntry(UUID target, GossipType type, int value) {
        public static final Codec<GossipEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(((MapCodec)UUIDUtil.CODEC.fieldOf("Target")).forGetter(GossipEntry::target), ((MapCodec)GossipType.CODEC.fieldOf("Type")).forGetter(GossipEntry::type), ((MapCodec)ExtraCodecs.POSITIVE_INT.fieldOf("Value")).forGetter(GossipEntry::value)).apply((Applicative<GossipEntry, ?>)instance, GossipEntry::new));
        public static final Codec<List<GossipEntry>> LIST_CODEC = CODEC.listOf();

        public int weightedValue() {
            return this.value * this.type.weight;
        }
    }
}


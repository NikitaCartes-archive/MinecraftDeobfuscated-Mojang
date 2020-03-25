/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.SerializableUUID;
import net.minecraft.world.entity.ai.gossip.GossipType;

public class GossipContainer {
    private final Map<UUID, EntityGossips> gossips = Maps.newHashMap();

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

    private Collection<GossipEntry> selectGossipsForTransfer(Random random, int i) {
        List list = this.unpack().collect(Collectors.toList());
        if (list.isEmpty()) {
            return Collections.emptyList();
        }
        int[] is = new int[list.size()];
        int j = 0;
        for (int k = 0; k < list.size(); ++k) {
            GossipEntry gossipEntry = (GossipEntry)list.get(k);
            is[k] = (j += Math.abs(gossipEntry.weightedValue())) - 1;
        }
        Set<GossipEntry> set = Sets.newIdentityHashSet();
        for (int l = 0; l < i; ++l) {
            int m = random.nextInt(j);
            int n = Arrays.binarySearch(is, m);
            set.add((GossipEntry)list.get(n < 0 ? -n - 1 : n));
        }
        return set;
    }

    private EntityGossips getOrCreate(UUID uUID2) {
        return this.gossips.computeIfAbsent(uUID2, uUID -> new EntityGossips());
    }

    public void transferFrom(GossipContainer gossipContainer, Random random, int i) {
        Collection<GossipEntry> collection = gossipContainer.selectGossipsForTransfer(random, i);
        collection.forEach(gossipEntry -> {
            int i = gossipEntry.value - gossipEntry.type.decayPerTransfer;
            if (i >= 2) {
                this.getOrCreate(gossipEntry.target).entries.mergeInt(gossipEntry.type, i, GossipContainer::mergeValuesForTransfer);
            }
        });
    }

    public int getReputation(UUID uUID, Predicate<GossipType> predicate) {
        EntityGossips entityGossips = this.gossips.get(uUID);
        return entityGossips != null ? entityGossips.weightedValue(predicate) : 0;
    }

    public void add(UUID uUID, GossipType gossipType, int i) {
        EntityGossips entityGossips = this.getOrCreate(uUID);
        entityGossips.entries.mergeInt(gossipType, i, (integer, integer2) -> this.mergeValuesForAddition(gossipType, (int)integer, (int)integer2));
        entityGossips.makeSureValueIsntTooLowOrTooHigh(gossipType);
        if (entityGossips.isEmpty()) {
            this.gossips.remove(uUID);
        }
    }

    public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
        return new Dynamic<Object>(dynamicOps, dynamicOps.createList(this.unpack().map(gossipEntry -> gossipEntry.store(dynamicOps)).map(Dynamic::getValue)));
    }

    public void update(Dynamic<?> dynamic) {
        dynamic.asStream().map(GossipEntry::load).flatMap(Util::toStream).forEach(gossipEntry -> this.getOrCreate(gossipEntry.target).entries.put(gossipEntry.type, gossipEntry.value));
    }

    private static int mergeValuesForTransfer(int i, int j) {
        return Math.max(i, j);
    }

    private int mergeValuesForAddition(GossipType gossipType, int i, int j) {
        int k = i + j;
        return k > gossipType.max ? Math.max(gossipType.max, i) : k;
    }

    static class EntityGossips {
        private final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<GossipType>();

        private EntityGossips() {
        }

        public int weightedValue(Predicate<GossipType> predicate) {
            return this.entries.object2IntEntrySet().stream().filter(entry -> predicate.test((GossipType)((Object)entry.getKey()))).mapToInt(entry -> entry.getIntValue() * ((GossipType)((Object)((Object)entry.getKey()))).weight).sum();
        }

        public Stream<GossipEntry> unpack(UUID uUID) {
            return this.entries.object2IntEntrySet().stream().map(entry -> new GossipEntry(uUID, (GossipType)((Object)((Object)entry.getKey())), entry.getIntValue()));
        }

        public void decay() {
            Iterator objectIterator = this.entries.object2IntEntrySet().iterator();
            while (objectIterator.hasNext()) {
                Object2IntMap.Entry entry = (Object2IntMap.Entry)objectIterator.next();
                int i = entry.getIntValue() - ((GossipType)((Object)entry.getKey())).decayPerDay;
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
            int i = this.entries.getInt((Object)gossipType);
            if (i > gossipType.max) {
                this.entries.put(gossipType, gossipType.max);
            }
            if (i < 2) {
                this.remove(gossipType);
            }
        }

        public void remove(GossipType gossipType) {
            this.entries.removeInt((Object)gossipType);
        }
    }

    static class GossipEntry {
        public final UUID target;
        public final GossipType type;
        public final int value;

        public GossipEntry(UUID uUID, GossipType gossipType, int i) {
            this.target = uUID;
            this.type = gossipType;
            this.value = i;
        }

        public int weightedValue() {
            return this.value * this.type.weight;
        }

        public String toString() {
            return "GossipEntry{target=" + this.target + ", type=" + (Object)((Object)this.type) + ", value=" + this.value + '}';
        }

        public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
            return new Dynamic<T>(dynamicOps, dynamicOps.createMap(ImmutableMap.of(dynamicOps.createString("Target"), SerializableUUID.serialize(dynamicOps, this.target), dynamicOps.createString("Type"), dynamicOps.createString(this.type.id), dynamicOps.createString("Value"), dynamicOps.createInt(this.value))));
        }

        public static Optional<GossipEntry> load(Dynamic<?> dynamic) {
            return dynamic.get("Type").asString().map(GossipType::byId).flatMap(gossipType -> dynamic.get("Target").map(SerializableUUID::readUUID).flatMap(uUID -> dynamic.get("Value").asNumber().map(number -> new GossipEntry((UUID)uUID, (GossipType)((Object)gossipType), number.intValue()))));
        }
    }
}


package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.SerializableUUID;

public class GossipContainer {
	private final Map<UUID, GossipContainer.EntityGossips> gossips = Maps.<UUID, GossipContainer.EntityGossips>newHashMap();

	public void decay() {
		Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

		while (iterator.hasNext()) {
			GossipContainer.EntityGossips entityGossips = (GossipContainer.EntityGossips)iterator.next();
			entityGossips.decay();
			if (entityGossips.isEmpty()) {
				iterator.remove();
			}
		}
	}

	private Stream<GossipContainer.GossipEntry> unpack() {
		return this.gossips.entrySet().stream().flatMap(entry -> ((GossipContainer.EntityGossips)entry.getValue()).unpack((UUID)entry.getKey()));
	}

	private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(Random random, int i) {
		List<GossipContainer.GossipEntry> list = (List<GossipContainer.GossipEntry>)this.unpack().collect(Collectors.toList());
		if (list.isEmpty()) {
			return Collections.emptyList();
		} else {
			int[] is = new int[list.size()];
			int j = 0;

			for (int k = 0; k < list.size(); k++) {
				GossipContainer.GossipEntry gossipEntry = (GossipContainer.GossipEntry)list.get(k);
				j += Math.abs(gossipEntry.weightedValue());
				is[k] = j - 1;
			}

			Set<GossipContainer.GossipEntry> set = Sets.newIdentityHashSet();

			for (int l = 0; l < i; l++) {
				int m = random.nextInt(j);
				int n = Arrays.binarySearch(is, m);
				set.add(list.get(n < 0 ? -n - 1 : n));
			}

			return set;
		}
	}

	private GossipContainer.EntityGossips getOrCreate(UUID uUID) {
		return (GossipContainer.EntityGossips)this.gossips.computeIfAbsent(uUID, uUIDx -> new GossipContainer.EntityGossips());
	}

	public void transferFrom(GossipContainer gossipContainer, Random random, int i) {
		Collection<GossipContainer.GossipEntry> collection = gossipContainer.selectGossipsForTransfer(random, i);
		collection.forEach(gossipEntry -> {
			int ix = gossipEntry.value - gossipEntry.type.decayPerTransfer;
			if (ix >= 2) {
				this.getOrCreate(gossipEntry.target).entries.mergeInt(gossipEntry.type, ix, GossipContainer::mergeValuesForTransfer);
			}
		});
	}

	public int getReputation(UUID uUID, Predicate<GossipType> predicate) {
		GossipContainer.EntityGossips entityGossips = (GossipContainer.EntityGossips)this.gossips.get(uUID);
		return entityGossips != null ? entityGossips.weightedValue(predicate) : 0;
	}

	public void add(UUID uUID, GossipType gossipType, int i) {
		GossipContainer.EntityGossips entityGossips = this.getOrCreate(uUID);
		entityGossips.entries.mergeInt(gossipType, i, (integer, integer2) -> this.mergeValuesForAddition(gossipType, integer, integer2));
		entityGossips.makeSureValueIsntTooLowOrTooHigh(gossipType);
		if (entityGossips.isEmpty()) {
			this.gossips.remove(uUID);
		}
	}

	public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
		return new Dynamic<>(dynamicOps, dynamicOps.createList(this.unpack().map(gossipEntry -> gossipEntry.store(dynamicOps)).map(Dynamic::getValue)));
	}

	public void update(Dynamic<?> dynamic) {
		dynamic.asStream()
			.map(GossipContainer.GossipEntry::load)
			.flatMap(dataResult -> Util.toStream(dataResult.result()))
			.forEach(gossipEntry -> this.getOrCreate(gossipEntry.target).entries.put(gossipEntry.type, gossipEntry.value));
	}

	private static int mergeValuesForTransfer(int i, int j) {
		return Math.max(i, j);
	}

	private int mergeValuesForAddition(GossipType gossipType, int i, int j) {
		int k = i + j;
		return k > gossipType.max ? Math.max(gossipType.max, i) : k;
	}

	static class EntityGossips {
		private final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

		private EntityGossips() {
		}

		public int weightedValue(Predicate<GossipType> predicate) {
			return this.entries
				.object2IntEntrySet()
				.stream()
				.filter(entry -> predicate.test(entry.getKey()))
				.mapToInt(entry -> entry.getIntValue() * ((GossipType)entry.getKey()).weight)
				.sum();
		}

		public Stream<GossipContainer.GossipEntry> unpack(UUID uUID) {
			return this.entries.object2IntEntrySet().stream().map(entry -> new GossipContainer.GossipEntry(uUID, (GossipType)entry.getKey(), entry.getIntValue()));
		}

		public void decay() {
			ObjectIterator<Entry<GossipType>> objectIterator = this.entries.object2IntEntrySet().iterator();

			while (objectIterator.hasNext()) {
				Entry<GossipType> entry = (Entry<GossipType>)objectIterator.next();
				int i = entry.getIntValue() - ((GossipType)entry.getKey()).decayPerDay;
				if (i < 2) {
					objectIterator.remove();
				} else {
					entry.setValue(i);
				}
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
			return "GossipEntry{target=" + this.target + ", type=" + this.type + ", value=" + this.value + '}';
		}

		public <T> Dynamic<T> store(DynamicOps<T> dynamicOps) {
			return new Dynamic<>(
				dynamicOps,
				dynamicOps.createMap(
					ImmutableMap.of(
						dynamicOps.createString("Target"),
						(T)SerializableUUID.CODEC.encodeStart(dynamicOps, this.target).result().orElseThrow(RuntimeException::new),
						dynamicOps.createString("Type"),
						dynamicOps.createString(this.type.id),
						dynamicOps.createString("Value"),
						dynamicOps.createInt(this.value)
					)
				)
			);
		}

		public static DataResult<GossipContainer.GossipEntry> load(Dynamic<?> dynamic) {
			return DataResult.unbox(
				DataResult.instance()
					.group(
						dynamic.get("Target").read(SerializableUUID.CODEC),
						dynamic.get("Type").asString().map(GossipType::byId),
						dynamic.get("Value").asNumber().map(Number::intValue)
					)
					.apply(DataResult.instance(), GossipContainer.GossipEntry::new)
			);
		}
	}
}

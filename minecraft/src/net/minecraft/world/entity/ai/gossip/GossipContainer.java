package net.minecraft.world.entity.ai.gossip;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.IntBinaryOperator;
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
import java.util.Set;
import java.util.UUID;
import java.util.function.DoublePredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;
import net.minecraft.core.UUIDUtil;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.util.VisibleForDebug;
import org.slf4j.Logger;

public class GossipContainer {
	private static final Logger LOGGER = LogUtils.getLogger();
	public static final int DISCARD_THRESHOLD = 2;
	private final Map<UUID, GossipContainer.EntityGossips> gossips = Maps.<UUID, GossipContainer.EntityGossips>newHashMap();

	@VisibleForDebug
	public Map<UUID, Object2IntMap<GossipType>> getGossipEntries() {
		Map<UUID, Object2IntMap<GossipType>> map = Maps.<UUID, Object2IntMap<GossipType>>newHashMap();
		this.gossips.keySet().forEach(uUID -> {
			GossipContainer.EntityGossips entityGossips = (GossipContainer.EntityGossips)this.gossips.get(uUID);
			map.put(uUID, entityGossips.entries);
		});
		return map;
	}

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

	private Collection<GossipContainer.GossipEntry> selectGossipsForTransfer(RandomSource randomSource, int i) {
		List<GossipContainer.GossipEntry> list = this.unpack().toList();
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
				int m = randomSource.nextInt(j);
				int n = Arrays.binarySearch(is, m);
				set.add((GossipContainer.GossipEntry)list.get(n < 0 ? -n - 1 : n));
			}

			return set;
		}
	}

	private GossipContainer.EntityGossips getOrCreate(UUID uUID) {
		return (GossipContainer.EntityGossips)this.gossips.computeIfAbsent(uUID, uUIDx -> new GossipContainer.EntityGossips());
	}

	public void transferFrom(GossipContainer gossipContainer, RandomSource randomSource, int i) {
		Collection<GossipContainer.GossipEntry> collection = gossipContainer.selectGossipsForTransfer(randomSource, i);
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

	public long getCountForType(GossipType gossipType, DoublePredicate doublePredicate) {
		return this.gossips
			.values()
			.stream()
			.filter(entityGossips -> doublePredicate.test((double)(entityGossips.entries.getOrDefault(gossipType, 0) * gossipType.weight)))
			.count();
	}

	public void add(UUID uUID, GossipType gossipType, int i) {
		GossipContainer.EntityGossips entityGossips = this.getOrCreate(uUID);
		entityGossips.entries.mergeInt(gossipType, i, (IntBinaryOperator)((ix, j) -> this.mergeValuesForAddition(gossipType, ix, j)));
		entityGossips.makeSureValueIsntTooLowOrTooHigh(gossipType);
		if (entityGossips.isEmpty()) {
			this.gossips.remove(uUID);
		}
	}

	public void remove(UUID uUID, GossipType gossipType, int i) {
		this.add(uUID, gossipType, -i);
	}

	public void remove(UUID uUID, GossipType gossipType) {
		GossipContainer.EntityGossips entityGossips = (GossipContainer.EntityGossips)this.gossips.get(uUID);
		if (entityGossips != null) {
			entityGossips.remove(gossipType);
			if (entityGossips.isEmpty()) {
				this.gossips.remove(uUID);
			}
		}
	}

	public void remove(GossipType gossipType) {
		Iterator<GossipContainer.EntityGossips> iterator = this.gossips.values().iterator();

		while (iterator.hasNext()) {
			GossipContainer.EntityGossips entityGossips = (GossipContainer.EntityGossips)iterator.next();
			entityGossips.remove(gossipType);
			if (entityGossips.isEmpty()) {
				iterator.remove();
			}
		}
	}

	public <T> T store(DynamicOps<T> dynamicOps) {
		return (T)GossipContainer.GossipEntry.LIST_CODEC
			.encodeStart(dynamicOps, this.unpack().toList())
			.resultOrPartial(string -> LOGGER.warn("Failed to serialize gossips: {}", string))
			.orElseGet(dynamicOps::emptyList);
	}

	public void update(Dynamic<?> dynamic) {
		GossipContainer.GossipEntry.LIST_CODEC
			.decode(dynamic)
			.resultOrPartial(string -> LOGGER.warn("Failed to deserialize gossips: {}", string))
			.stream()
			.flatMap(pair -> ((List)pair.getFirst()).stream())
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
		final Object2IntMap<GossipType> entries = new Object2IntOpenHashMap<>();

		public int weightedValue(Predicate<GossipType> predicate) {
			return this.entries
				.object2IntEntrySet()
				.stream()
				.filter(entry -> predicate.test((GossipType)entry.getKey()))
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

	static record GossipEntry(UUID target, GossipType type, int value) {
		public static final Codec<GossipContainer.GossipEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						UUIDUtil.CODEC.fieldOf("Target").forGetter(GossipContainer.GossipEntry::target),
						GossipType.CODEC.fieldOf("Type").forGetter(GossipContainer.GossipEntry::type),
						ExtraCodecs.POSITIVE_INT.fieldOf("Value").forGetter(GossipContainer.GossipEntry::value)
					)
					.apply(instance, GossipContainer.GossipEntry::new)
		);
		public static final Codec<List<GossipContainer.GossipEntry>> LIST_CODEC = CODEC.listOf();

		public int weightedValue() {
			return this.value * this.type.weight;
		}
	}
}

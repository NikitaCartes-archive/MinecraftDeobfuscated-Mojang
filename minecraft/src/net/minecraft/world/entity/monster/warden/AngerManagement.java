package net.minecraft.world.entity.monster.warden;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Streams;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public class AngerManagement {
	@VisibleForTesting
	protected static final int CONVERSION_DELAY = 2;
	@VisibleForTesting
	protected static final int MAX_ANGER = 150;
	private static final int DEFAULT_ANGER_DECREASE = 1;
	private int conversionDelay = Mth.randomBetweenInclusive(RandomSource.create(), 0, 2);
	int highestAnger;
	private static final Codec<Pair<UUID, Integer>> SUSPECT_ANGER_PAIR = RecordCodecBuilder.create(
		instance -> instance.group(
					ExtraCodecs.UUID.fieldOf("uuid").forGetter(Pair::getFirst), ExtraCodecs.NON_NEGATIVE_INT.fieldOf("anger").forGetter(Pair::getSecond)
				)
				.apply(instance, Pair::of)
	);
	private final Predicate<Entity> filter;
	@VisibleForTesting
	protected final ArrayList<Entity> suspects;
	private final AngerManagement.Sorter suspectSorter;
	@VisibleForTesting
	protected final Object2IntMap<Entity> angerBySuspect;
	@VisibleForTesting
	protected final Object2IntMap<UUID> angerByUuid;

	public static Codec<AngerManagement> codec(Predicate<Entity> predicate) {
		return RecordCodecBuilder.create(
			instance -> instance.group(SUSPECT_ANGER_PAIR.listOf().fieldOf("suspects").orElse(Collections.emptyList()).forGetter(AngerManagement::createUuidAngerPairs))
					.apply(instance, list -> new AngerManagement(predicate, list))
		);
	}

	public AngerManagement(Predicate<Entity> predicate, List<Pair<UUID, Integer>> list) {
		this.filter = predicate;
		this.suspects = new ArrayList();
		this.suspectSorter = new AngerManagement.Sorter(this);
		this.angerBySuspect = new Object2IntOpenHashMap<>();
		this.angerByUuid = new Object2IntOpenHashMap<>(list.size());
		list.forEach(pair -> this.angerByUuid.put((UUID)pair.getFirst(), (Integer)pair.getSecond()));
	}

	private List<Pair<UUID, Integer>> createUuidAngerPairs() {
		return (List<Pair<UUID, Integer>>)Streams.concat(
				this.suspects.stream().map(entity -> Pair.of(entity.getUUID(), this.angerBySuspect.getInt(entity))),
				this.angerByUuid.object2IntEntrySet().stream().map(entry -> Pair.of((UUID)entry.getKey(), entry.getIntValue()))
			)
			.collect(Collectors.toList());
	}

	public void tick(ServerLevel serverLevel, Predicate<Entity> predicate) {
		this.conversionDelay--;
		if (this.conversionDelay <= 0) {
			this.convertFromUuids(serverLevel);
			this.conversionDelay = 2;
		}

		ObjectIterator<Entry<UUID>> objectIterator = this.angerByUuid.object2IntEntrySet().iterator();

		while (objectIterator.hasNext()) {
			Entry<UUID> entry = (Entry<UUID>)objectIterator.next();
			int i = entry.getIntValue();
			if (i <= 1) {
				objectIterator.remove();
			} else {
				entry.setValue(i - 1);
			}
		}

		ObjectIterator<Entry<Entity>> objectIterator2 = this.angerBySuspect.object2IntEntrySet().iterator();

		while (objectIterator2.hasNext()) {
			Entry<Entity> entry2 = (Entry<Entity>)objectIterator2.next();
			int j = entry2.getIntValue();
			Entity entity = (Entity)entry2.getKey();
			Entity.RemovalReason removalReason = entity.getRemovalReason();
			if (j > 1 && predicate.test(entity) && removalReason == null) {
				entry2.setValue(j - 1);
			} else {
				this.suspects.remove(entity);
				objectIterator2.remove();
				if (j > 1 && removalReason != null) {
					switch (removalReason) {
						case CHANGED_DIMENSION:
						case UNLOADED_TO_CHUNK:
						case UNLOADED_WITH_PLAYER:
							this.angerByUuid.put(entity.getUUID(), j - 1);
					}
				}
			}
		}

		this.sortAndUpdateHighestAnger();
	}

	private void sortAndUpdateHighestAnger() {
		this.highestAnger = 0;
		this.suspects.sort(this.suspectSorter);
		if (this.suspects.size() == 1) {
			this.highestAnger = this.angerBySuspect.getInt(this.suspects.get(0));
		}
	}

	private void convertFromUuids(ServerLevel serverLevel) {
		ObjectIterator<Entry<UUID>> objectIterator = this.angerByUuid.object2IntEntrySet().iterator();

		while (objectIterator.hasNext()) {
			Entry<UUID> entry = (Entry<UUID>)objectIterator.next();
			int i = entry.getIntValue();
			Entity entity = serverLevel.getEntity((UUID)entry.getKey());
			if (entity != null) {
				this.angerBySuspect.put(entity, i);
				this.suspects.add(entity);
				objectIterator.remove();
			}
		}
	}

	public int increaseAnger(Entity entity, int i) {
		boolean bl = !this.angerBySuspect.containsKey(entity);
		int j = this.angerBySuspect.computeInt(entity, (entityx, integer) -> Math.min(150, (integer == null ? 0 : integer) + i));
		if (bl) {
			int k = this.angerByUuid.removeInt(entity.getUUID());
			j += k;
			this.angerBySuspect.put(entity, j);
			this.suspects.add(entity);
		}

		this.sortAndUpdateHighestAnger();
		return j;
	}

	public void clearAnger(Entity entity) {
		this.angerBySuspect.removeInt(entity);
		this.suspects.remove(entity);
		this.sortAndUpdateHighestAnger();
	}

	@Nullable
	private Entity getTopSuspect() {
		return (Entity)this.suspects.stream().filter(this.filter).findFirst().orElse(null);
	}

	public int getActiveAnger(@Nullable Entity entity) {
		return entity == null ? this.highestAnger : this.angerBySuspect.getInt(entity);
	}

	public Optional<LivingEntity> getActiveEntity() {
		return Optional.ofNullable(this.getTopSuspect()).filter(entity -> entity instanceof LivingEntity).map(entity -> (LivingEntity)entity);
	}

	@VisibleForTesting
	protected static record Sorter(AngerManagement angerManagement) implements Comparator<Entity> {
		public int compare(Entity entity, Entity entity2) {
			if (entity.equals(entity2)) {
				return 0;
			} else {
				int i = this.angerManagement.angerBySuspect.getOrDefault(entity, 0);
				int j = this.angerManagement.angerBySuspect.getOrDefault(entity2, 0);
				this.angerManagement.highestAnger = Math.max(this.angerManagement.highestAnger, Math.max(i, j));
				boolean bl = AngerLevel.byAnger(i).isAngry();
				boolean bl2 = AngerLevel.byAnger(j).isAngry();
				if (bl != bl2) {
					return bl ? -1 : 1;
				} else {
					boolean bl3 = entity instanceof Player;
					boolean bl4 = entity2 instanceof Player;
					if (bl3 != bl4) {
						return bl3 ? -1 : 1;
					} else {
						return i > j ? -1 : 1;
					}
				}
			}
		}
	}
}

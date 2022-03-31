package net.minecraft.world.entity.monster.warden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class AngerManagement {
	private static final int MAX_ANGER = 150;
	private static final int DEFAULT_ANGER_DECREASE = 1;
	public static final Codec<AngerManagement> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					Codec.unboundedMap(ExtraCodecs.UUID, ExtraCodecs.NON_NEGATIVE_INT).fieldOf("suspects").forGetter(angerManagement -> angerManagement.angerBySuspect)
				)
				.apply(instance, AngerManagement::new)
	);
	private final Object2IntMap<UUID> angerBySuspect;

	public AngerManagement(Map<UUID, Integer> map) {
		this.angerBySuspect = new Object2IntOpenHashMap<>(map);
	}

	public void tick() {
		ObjectIterator<Entry<UUID>> objectIterator = this.angerBySuspect.object2IntEntrySet().iterator();

		while (objectIterator.hasNext()) {
			Entry<UUID> entry = (Entry<UUID>)objectIterator.next();
			int i = entry.getIntValue();
			if (i <= 1) {
				objectIterator.remove();
			} else {
				entry.setValue(Math.max(0, i - 1));
			}
		}
	}

	public int addAnger(Entity entity, int i) {
		return this.angerBySuspect.computeInt(entity.getUUID(), (uUID, integer) -> Math.min(150, (integer == null ? 0 : integer) + i));
	}

	public void clearAnger(Entity entity) {
		this.angerBySuspect.removeInt(entity.getUUID());
	}

	private Optional<Entry<UUID>> getTopEntry() {
		return this.angerBySuspect.object2IntEntrySet().stream().max(java.util.Map.Entry.comparingByValue());
	}

	public int getActiveAnger() {
		return (Integer)this.getTopEntry().map(java.util.Map.Entry::getValue).orElse(0);
	}

	public Optional<LivingEntity> getActiveEntity(Level level) {
		return level instanceof ServerLevel serverLevel
			? this.getTopEntry()
				.map(java.util.Map.Entry::getKey)
				.map(serverLevel::getEntity)
				.filter(entity -> entity instanceof LivingEntity)
				.map(entity -> (LivingEntity)entity)
			: Optional.empty();
	}
}

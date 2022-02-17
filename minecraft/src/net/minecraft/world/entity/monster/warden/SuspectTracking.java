package net.minecraft.world.entity.monster.warden;

import com.google.common.collect.Maps;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;

public class SuspectTracking {
	private final Map<UUID, Integer> suspectIdToAnger = Maps.<UUID, Integer>newHashMap();
	private static final String SUSPECT_TAG_NAME = "Suspect";
	private static final String UUID_TAG_NAME = "UUID";
	private static final String ANGER_TAG_NAME = "Anger";
	private final int maxAnger;
	private final int decrementAngerBy;
	private final Supplier<Boolean> shouldDecrement;

	public SuspectTracking(int i, int j, Supplier<Boolean> supplier) {
		this.decrementAngerBy = i;
		this.maxAnger = j;
		this.shouldDecrement = supplier;
	}

	public void update() {
		if ((Boolean)this.shouldDecrement.get()) {
			this.suspectIdToAnger.replaceAll((uUID, integer) -> Math.max(0, integer - this.decrementAngerBy));
			this.suspectIdToAnger.entrySet().removeIf(entry -> (Integer)entry.getValue() <= 0);
		}
	}

	private Optional<Entry<UUID, Integer>> getTopEntry() {
		return this.suspectIdToAnger.entrySet().stream().max(Entry.comparingByValue());
	}

	public void addSuspicion(UUID uUID, int i) {
		Integer integer = (Integer)this.suspectIdToAnger.putIfAbsent(uUID, i);
		this.suspectIdToAnger.replace(uUID, Math.min(this.maxAnger, (integer != null ? integer : 0) + i));
	}

	public void clearSuspicion(UUID uUID) {
		this.suspectIdToAnger.remove(uUID);
	}

	public int getAngerFor(UUID uUID) {
		return !this.suspectIdToAnger.containsKey(uUID) ? 0 : (Integer)this.suspectIdToAnger.get(uUID);
	}

	public int getActiveAnger() {
		return (Integer)this.getTopEntry().map(Entry::getValue).orElse(0);
	}

	public Optional<UUID> getActiveSuspect() {
		return this.getTopEntry().map(Entry::getKey);
	}

	public void write(CompoundTag compoundTag) {
		ListTag listTag = new ListTag();

		for (Entry<UUID, Integer> entry : this.suspectIdToAnger.entrySet()) {
			UUID uUID = (UUID)entry.getKey();
			int i = (Integer)entry.getValue();
			CompoundTag compoundTag2 = new CompoundTag();
			compoundTag2.putUUID("UUID", uUID);
			compoundTag2.putInt("Anger", i);
			listTag.add(compoundTag2);
		}

		compoundTag.put("Suspect", listTag);
	}

	public void read(CompoundTag compoundTag) {
		if (compoundTag.contains("Suspect", 9)) {
			ListTag listTag = compoundTag.getList("Suspect", 10);

			for (int i = 0; i < listTag.size(); i++) {
				CompoundTag compoundTag2 = listTag.getCompound(i);
				UUID uUID = compoundTag2.getUUID("UUID");
				int j = compoundTag2.getInt("Anger");
				this.suspectIdToAnger.put(uUID, j);
			}
		}
	}
}

package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.util.Mth;

public class ItemCooldowns {
	private final Map<Item, ItemCooldowns.CooldownInstance> cooldowns = Maps.<Item, ItemCooldowns.CooldownInstance>newHashMap();
	private int tickCount;

	public boolean isOnCooldown(Item item) {
		return this.getCooldownPercent(item, 0.0F) > 0.0F;
	}

	public float getCooldownPercent(Item item, float f) {
		ItemCooldowns.CooldownInstance cooldownInstance = (ItemCooldowns.CooldownInstance)this.cooldowns.get(item);
		if (cooldownInstance != null) {
			float g = (float)(cooldownInstance.endTime - cooldownInstance.startTime);
			float h = (float)cooldownInstance.endTime - ((float)this.tickCount + f);
			return Mth.clamp(h / g, 0.0F, 1.0F);
		} else {
			return 0.0F;
		}
	}

	public void tick() {
		this.tickCount++;
		if (!this.cooldowns.isEmpty()) {
			Iterator<Entry<Item, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<Item, ItemCooldowns.CooldownInstance> entry = (Entry<Item, ItemCooldowns.CooldownInstance>)iterator.next();
				if (((ItemCooldowns.CooldownInstance)entry.getValue()).endTime <= this.tickCount) {
					iterator.remove();
					this.onCooldownEnded((Item)entry.getKey());
				}
			}
		}
	}

	public void addCooldown(Item item, int i) {
		this.cooldowns.put(item, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + i));
		this.onCooldownStarted(item, i);
	}

	public void removeCooldown(Item item) {
		this.cooldowns.remove(item);
		this.onCooldownEnded(item);
	}

	protected void onCooldownStarted(Item item, int i) {
	}

	protected void onCooldownEnded(Item item) {
	}

	class CooldownInstance {
		final int startTime;
		final int endTime;

		CooldownInstance(int i, int j) {
			this.startTime = i;
			this.endTime = j;
		}
	}
}

package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.component.UseCooldown;

public class ItemCooldowns {
	private final Map<ResourceLocation, ItemCooldowns.CooldownInstance> cooldowns = Maps.<ResourceLocation, ItemCooldowns.CooldownInstance>newHashMap();
	private int tickCount;

	public boolean isOnCooldown(ItemStack itemStack) {
		return this.getCooldownPercent(itemStack, 0.0F) > 0.0F;
	}

	public float getCooldownPercent(ItemStack itemStack, float f) {
		ResourceLocation resourceLocation = this.getCooldownGroup(itemStack);
		ItemCooldowns.CooldownInstance cooldownInstance = (ItemCooldowns.CooldownInstance)this.cooldowns.get(resourceLocation);
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
			Iterator<Entry<ResourceLocation, ItemCooldowns.CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();

			while (iterator.hasNext()) {
				Entry<ResourceLocation, ItemCooldowns.CooldownInstance> entry = (Entry<ResourceLocation, ItemCooldowns.CooldownInstance>)iterator.next();
				if (((ItemCooldowns.CooldownInstance)entry.getValue()).endTime <= this.tickCount) {
					iterator.remove();
					this.onCooldownEnded((ResourceLocation)entry.getKey());
				}
			}
		}
	}

	public ResourceLocation getCooldownGroup(ItemStack itemStack) {
		UseCooldown useCooldown = itemStack.get(DataComponents.USE_COOLDOWN);
		ResourceLocation resourceLocation = BuiltInRegistries.ITEM.getKey(itemStack.getItem());
		return useCooldown == null ? resourceLocation : (ResourceLocation)useCooldown.cooldownGroup().orElse(resourceLocation);
	}

	public void addCooldown(ItemStack itemStack, int i) {
		this.addCooldown(this.getCooldownGroup(itemStack), i);
	}

	public void addCooldown(ResourceLocation resourceLocation, int i) {
		this.cooldowns.put(resourceLocation, new ItemCooldowns.CooldownInstance(this.tickCount, this.tickCount + i));
		this.onCooldownStarted(resourceLocation, i);
	}

	public void removeCooldown(ResourceLocation resourceLocation) {
		this.cooldowns.remove(resourceLocation);
		this.onCooldownEnded(resourceLocation);
	}

	protected void onCooldownStarted(ResourceLocation resourceLocation, int i) {
	}

	protected void onCooldownEnded(ResourceLocation resourceLocation) {
	}

	static record CooldownInstance(int startTime, int endTime) {
	}
}

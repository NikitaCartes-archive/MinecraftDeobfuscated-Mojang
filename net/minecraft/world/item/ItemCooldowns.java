/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item;

import com.google.common.collect.Maps;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;

public class ItemCooldowns {
    private final Map<Item, CooldownInstance> cooldowns = Maps.newHashMap();
    private int tickCount;

    public boolean isOnCooldown(Item item) {
        return this.getCooldownPercent(item, 0.0f) > 0.0f;
    }

    public float getCooldownPercent(Item item, float f) {
        CooldownInstance cooldownInstance = this.cooldowns.get(item);
        if (cooldownInstance != null) {
            float g = cooldownInstance.endTime - cooldownInstance.startTime;
            float h = (float)cooldownInstance.endTime - ((float)this.tickCount + f);
            return Mth.clamp(h / g, 0.0f, 1.0f);
        }
        return 0.0f;
    }

    public void tick() {
        ++this.tickCount;
        if (!this.cooldowns.isEmpty()) {
            Iterator<Map.Entry<Item, CooldownInstance>> iterator = this.cooldowns.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<Item, CooldownInstance> entry = iterator.next();
                if (entry.getValue().endTime > this.tickCount) continue;
                iterator.remove();
                this.onCooldownEnded(entry.getKey());
            }
        }
    }

    public void addCooldown(Item item, int i) {
        this.cooldowns.put(item, new CooldownInstance(this.tickCount, this.tickCount + i));
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
        private final int startTime;
        private final int endTime;

        private CooldownInstance(int i, int j) {
            this.startTime = i;
            this.endTime = j;
        }
    }
}


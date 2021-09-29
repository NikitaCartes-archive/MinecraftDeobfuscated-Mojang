/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.item.alchemy;

import com.google.common.collect.ImmutableList;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public class Potion {
    @Nullable
    private final String name;
    private final ImmutableList<MobEffectInstance> effects;

    public static Potion byName(String string) {
        return Registry.POTION.get(ResourceLocation.tryParse(string));
    }

    public Potion(MobEffectInstance ... mobEffectInstances) {
        this((String)null, mobEffectInstances);
    }

    public Potion(@Nullable String string, MobEffectInstance ... mobEffectInstances) {
        this.name = string;
        this.effects = ImmutableList.copyOf(mobEffectInstances);
    }

    public String getName(String string) {
        return string + (this.name == null ? Registry.POTION.getKey(this).getPath() : this.name);
    }

    public List<MobEffectInstance> getEffects() {
        return this.effects;
    }

    public boolean hasInstantEffects() {
        if (!this.effects.isEmpty()) {
            for (MobEffectInstance mobEffectInstance : this.effects) {
                if (!mobEffectInstance.getEffect().isInstantenous()) continue;
                return true;
            }
        }
        return false;
    }
}


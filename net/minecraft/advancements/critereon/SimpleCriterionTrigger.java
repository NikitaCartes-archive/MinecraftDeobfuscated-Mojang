/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.advancements.critereon;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;

public abstract class SimpleCriterionTrigger<T extends CriterionTriggerInstance>
implements CriterionTrigger<T> {
    private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.newIdentityHashMap();

    @Override
    public final void addPlayerListener(PlayerAdvancements playerAdvancements2, CriterionTrigger.Listener<T> listener) {
        this.players.computeIfAbsent(playerAdvancements2, playerAdvancements -> Sets.newHashSet()).add(listener);
    }

    @Override
    public final void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener) {
        Set<CriterionTrigger.Listener<T>> set = this.players.get(playerAdvancements);
        if (set != null) {
            set.remove(listener);
            if (set.isEmpty()) {
                this.players.remove(playerAdvancements);
            }
        }
    }

    @Override
    public final void removePlayerListeners(PlayerAdvancements playerAdvancements) {
        this.players.remove(playerAdvancements);
    }

    protected void trigger(PlayerAdvancements playerAdvancements, Predicate<T> predicate) {
        Set<CriterionTrigger.Listener<T>> set = this.players.get(playerAdvancements);
        if (set == null) {
            return;
        }
        ArrayList<CriterionTrigger.Listener<T>> list = null;
        for (CriterionTrigger.Listener<T> listener : set) {
            if (!predicate.test(listener.getTriggerInstance())) continue;
            if (list == null) {
                list = Lists.newArrayList();
            }
            list.add(listener);
        }
        if (list != null) {
            for (CriterionTrigger.Listener<Object> listener : list) {
                listener.run(playerAdvancements);
            }
        }
    }

    protected void trigger(PlayerAdvancements playerAdvancements) {
        Set<CriterionTrigger.Listener<T>> set = this.players.get(playerAdvancements);
        if (set != null && !set.isEmpty()) {
            for (CriterionTrigger.Listener listener : ImmutableSet.copyOf(set)) {
                listener.run(playerAdvancements);
            }
        }
    }
}


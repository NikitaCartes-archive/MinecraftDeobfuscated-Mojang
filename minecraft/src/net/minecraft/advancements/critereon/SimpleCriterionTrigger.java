package net.minecraft.advancements.critereon;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.CriterionTriggerInstance;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.loot.LootContext;

public abstract class SimpleCriterionTrigger<T extends SimpleCriterionTrigger.SimpleInstance> implements CriterionTrigger<T> {
	private final Map<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>> players = Maps.<PlayerAdvancements, Set<CriterionTrigger.Listener<T>>>newIdentityHashMap();

	@Override
	public final void addPlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener) {
		((Set)this.players.computeIfAbsent(playerAdvancements, playerAdvancementsx -> Sets.newHashSet())).add(listener);
	}

	@Override
	public final void removePlayerListener(PlayerAdvancements playerAdvancements, CriterionTrigger.Listener<T> listener) {
		Set<CriterionTrigger.Listener<T>> set = (Set<CriterionTrigger.Listener<T>>)this.players.get(playerAdvancements);
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

	protected abstract T createInstance(JsonObject jsonObject, Optional<ContextAwarePredicate> optional, DeserializationContext deserializationContext);

	public final T createInstance(JsonObject jsonObject, DeserializationContext deserializationContext) {
		Optional<ContextAwarePredicate> optional = EntityPredicate.fromJson(jsonObject, "player", deserializationContext);
		return this.createInstance(jsonObject, optional, deserializationContext);
	}

	protected void trigger(ServerPlayer serverPlayer, Predicate<T> predicate) {
		PlayerAdvancements playerAdvancements = serverPlayer.getAdvancements();
		Set<CriterionTrigger.Listener<T>> set = (Set<CriterionTrigger.Listener<T>>)this.players.get(playerAdvancements);
		if (set != null && !set.isEmpty()) {
			LootContext lootContext = EntityPredicate.createContext(serverPlayer, serverPlayer);
			List<CriterionTrigger.Listener<T>> list = null;

			for (CriterionTrigger.Listener<T> listener : set) {
				T simpleInstance = listener.trigger();
				if (predicate.test(simpleInstance)) {
					Optional<ContextAwarePredicate> optional = simpleInstance.playerPredicate();
					if (optional.isEmpty() || ((ContextAwarePredicate)optional.get()).matches(lootContext)) {
						if (list == null) {
							list = Lists.<CriterionTrigger.Listener<T>>newArrayList();
						}

						list.add(listener);
					}
				}
			}

			if (list != null) {
				for (CriterionTrigger.Listener<T> listenerx : list) {
					listenerx.run(playerAdvancements);
				}
			}
		}
	}

	public interface SimpleInstance extends CriterionTriggerInstance {
		Optional<ContextAwarePredicate> playerPredicate();
	}
}

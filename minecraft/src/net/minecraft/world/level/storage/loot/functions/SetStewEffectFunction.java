package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.SuspiciousStewEffects;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetStewEffectFunction extends LootItemConditionalFunction {
	private static final Codec<List<SetStewEffectFunction.EffectEntry>> EFFECTS_LIST = ExtraCodecs.validate(
		SetStewEffectFunction.EffectEntry.CODEC.listOf(), list -> {
			Set<Holder<MobEffect>> set = new ObjectOpenHashSet<>();

			for (SetStewEffectFunction.EffectEntry effectEntry : list) {
				if (!set.add(effectEntry.effect())) {
					return DataResult.error(() -> "Encountered duplicate mob effect: '" + effectEntry.effect() + "'");
				}
			}

			return DataResult.success(list);
		}
	);
	public static final Codec<SetStewEffectFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(ExtraCodecs.strictOptionalField(EFFECTS_LIST, "effects", List.of()).forGetter(setStewEffectFunction -> setStewEffectFunction.effects))
				.apply(instance, SetStewEffectFunction::new)
	);
	private final List<SetStewEffectFunction.EffectEntry> effects;

	SetStewEffectFunction(List<LootItemCondition> list, List<SetStewEffectFunction.EffectEntry> list2) {
		super(list);
		this.effects = list2;
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_STEW_EFFECT;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.effects
			.stream()
			.flatMap(effectEntry -> effectEntry.duration().getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.is(Items.SUSPICIOUS_STEW) && !this.effects.isEmpty()) {
			SetStewEffectFunction.EffectEntry effectEntry = Util.getRandom(this.effects, lootContext.getRandom());
			Holder<MobEffect> holder = effectEntry.effect();
			int i = effectEntry.duration().getInt(lootContext);
			if (!holder.value().isInstantenous()) {
				i *= 20;
			}

			SuspiciousStewEffects.Entry entry = new SuspiciousStewEffects.Entry(holder, i);
			itemStack.update(DataComponents.SUSPICIOUS_STEW_EFFECTS, SuspiciousStewEffects.EMPTY, entry, SuspiciousStewEffects::withEffectAdded);
			return itemStack;
		} else {
			return itemStack;
		}
	}

	public static SetStewEffectFunction.Builder stewEffect() {
		return new SetStewEffectFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetStewEffectFunction.Builder> {
		private final ImmutableList.Builder<SetStewEffectFunction.EffectEntry> effects = ImmutableList.builder();

		protected SetStewEffectFunction.Builder getThis() {
			return this;
		}

		public SetStewEffectFunction.Builder withEffect(Holder<MobEffect> holder, NumberProvider numberProvider) {
			this.effects.add(new SetStewEffectFunction.EffectEntry(holder, numberProvider));
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetStewEffectFunction(this.getConditions(), this.effects.build());
		}
	}

	static record EffectEntry(Holder<MobEffect> effect, NumberProvider duration) {
		public static final Codec<SetStewEffectFunction.EffectEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						BuiltInRegistries.MOB_EFFECT.holderByNameCodec().fieldOf("type").forGetter(SetStewEffectFunction.EffectEntry::effect),
						NumberProviders.CODEC.fieldOf("duration").forGetter(SetStewEffectFunction.EffectEntry::duration)
					)
					.apply(instance, SetStewEffectFunction.EffectEntry::new)
		);
	}
}

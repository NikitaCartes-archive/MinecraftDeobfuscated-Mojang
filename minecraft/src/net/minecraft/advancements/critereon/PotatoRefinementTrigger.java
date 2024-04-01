package net.minecraft.advancements.critereon;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.Criterion;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.LubricationComponent;

public class PotatoRefinementTrigger extends SimpleCriterionTrigger<PotatoRefinementTrigger.TriggerInstance> {
	static final Codec<PotatoRefinementTrigger.ItemStackPredicate> ITEM_STACK_PREDICATE_CODEC = StringRepresentable.fromEnum(PotatoRefinementTrigger.Type::values)
		.dispatch(PotatoRefinementTrigger.ItemStackPredicate::type, PotatoRefinementTrigger.Type::codec);

	@Override
	public Codec<PotatoRefinementTrigger.TriggerInstance> codec() {
		return PotatoRefinementTrigger.TriggerInstance.CODEC;
	}

	public void trigger(ServerPlayer serverPlayer, ItemStack itemStack) {
		this.trigger(serverPlayer, triggerInstance -> triggerInstance.matches(itemStack));
	}

	public interface ItemStackPredicate extends Predicate<ItemStack> {
		PotatoRefinementTrigger.Type type();
	}

	static record MinLubricationItemStackPredicate(ItemPredicate itemPredicate, int minLubricationLevel) implements PotatoRefinementTrigger.ItemStackPredicate {
		public static final Codec<PotatoRefinementTrigger.MinLubricationItemStackPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(PotatoRefinementTrigger.MinLubricationItemStackPredicate::itemPredicate),
						Codec.INT.fieldOf("min_lubrication").forGetter(PotatoRefinementTrigger.MinLubricationItemStackPredicate::minLubricationLevel)
					)
					.apply(instance, PotatoRefinementTrigger.MinLubricationItemStackPredicate::new)
		);

		@Override
		public PotatoRefinementTrigger.Type type() {
			return PotatoRefinementTrigger.Type.LUBRICATION;
		}

		public boolean test(ItemStack itemStack) {
			if (this.itemPredicate.matches(itemStack)) {
				LubricationComponent lubricationComponent = itemStack.get(DataComponents.LUBRICATION);
				if (lubricationComponent != null) {
					return lubricationComponent.getLevel() >= this.minLubricationLevel;
				}
			}

			return false;
		}
	}

	static record StandardItemStackPredicate(ItemPredicate itemPredicate) implements PotatoRefinementTrigger.ItemStackPredicate {
		public static final Codec<PotatoRefinementTrigger.StandardItemStackPredicate> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(ItemPredicate.CODEC.fieldOf("item_predicate").forGetter(PotatoRefinementTrigger.StandardItemStackPredicate::itemPredicate))
					.apply(instance, PotatoRefinementTrigger.StandardItemStackPredicate::new)
		);

		@Override
		public PotatoRefinementTrigger.Type type() {
			return PotatoRefinementTrigger.Type.STANDARD;
		}

		public boolean test(ItemStack itemStack) {
			return this.itemPredicate.matches(itemStack);
		}
	}

	public static record TriggerInstance(Optional<ContextAwarePredicate> player, PotatoRefinementTrigger.ItemStackPredicate resultPredicate)
		implements SimpleCriterionTrigger.SimpleInstance {
		public static final Codec<PotatoRefinementTrigger.TriggerInstance> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ExtraCodecs.strictOptionalField(EntityPredicate.ADVANCEMENT_CODEC, "player").forGetter(PotatoRefinementTrigger.TriggerInstance::player),
						PotatoRefinementTrigger.ITEM_STACK_PREDICATE_CODEC.fieldOf("result_predicate").forGetter(PotatoRefinementTrigger.TriggerInstance::resultPredicate)
					)
					.apply(instance, PotatoRefinementTrigger.TriggerInstance::new)
		);

		public static Criterion<PotatoRefinementTrigger.TriggerInstance> refined(Item item) {
			return CriteriaTriggers.POTATO_REFINED
				.createCriterion(
					new PotatoRefinementTrigger.TriggerInstance(
						Optional.empty(), new PotatoRefinementTrigger.StandardItemStackPredicate(ItemPredicate.Builder.item().of(item).build())
					)
				);
		}

		public static Criterion<PotatoRefinementTrigger.TriggerInstance> lubricatedAtLeast(int i) {
			return CriteriaTriggers.POTATO_REFINED
				.createCriterion(
					new PotatoRefinementTrigger.TriggerInstance(
						Optional.empty(), new PotatoRefinementTrigger.MinLubricationItemStackPredicate(ItemPredicate.Builder.item().build(), i)
					)
				);
		}

		public static Criterion<PotatoRefinementTrigger.TriggerInstance> lubricatedAtLeast(ItemPredicate itemPredicate, int i) {
			return CriteriaTriggers.POTATO_REFINED
				.createCriterion(
					new PotatoRefinementTrigger.TriggerInstance(Optional.empty(), new PotatoRefinementTrigger.MinLubricationItemStackPredicate(itemPredicate, i))
				);
		}

		public boolean matches(ItemStack itemStack) {
			return this.resultPredicate.test(itemStack);
		}
	}

	static enum Type implements StringRepresentable {
		STANDARD("standard", () -> PotatoRefinementTrigger.StandardItemStackPredicate.CODEC),
		LUBRICATION("lubrication", () -> PotatoRefinementTrigger.MinLubricationItemStackPredicate.CODEC);

		private final String serializedName;
		private final Supplier<Codec<? extends PotatoRefinementTrigger.ItemStackPredicate>> codec;

		private Type(String string2, Supplier<Codec<? extends PotatoRefinementTrigger.ItemStackPredicate>> supplier) {
			this.serializedName = string2;
			this.codec = supplier;
		}

		private Codec<? extends PotatoRefinementTrigger.ItemStackPredicate> codec() {
			return (Codec<? extends PotatoRefinementTrigger.ItemStackPredicate>)this.codec.get();
		}

		@Override
		public String getSerializedName() {
			return this.serializedName;
		}
	}
}

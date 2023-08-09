package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraft.world.level.storage.loot.providers.number.NumberProviders;

public class SetAttributesFunction extends LootItemConditionalFunction {
	public static final Codec<SetAttributesFunction> CODEC = RecordCodecBuilder.create(
		instance -> commonFields(instance)
				.and(
					ExtraCodecs.nonEmptyList(SetAttributesFunction.Modifier.CODEC.listOf())
						.fieldOf("modifiers")
						.forGetter(setAttributesFunction -> setAttributesFunction.modifiers)
				)
				.apply(instance, SetAttributesFunction::new)
	);
	private final List<SetAttributesFunction.Modifier> modifiers;

	SetAttributesFunction(List<LootItemCondition> list, List<SetAttributesFunction.Modifier> list2) {
		super(list);
		this.modifiers = List.copyOf(list2);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.SET_ATTRIBUTES;
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return (Set<LootContextParam<?>>)this.modifiers
			.stream()
			.flatMap(modifier -> modifier.amount.getReferencedContextParams().stream())
			.collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		RandomSource randomSource = lootContext.getRandom();

		for (SetAttributesFunction.Modifier modifier : this.modifiers) {
			UUID uUID = (UUID)modifier.id.orElseGet(UUID::randomUUID);
			EquipmentSlot equipmentSlot = Util.getRandom(modifier.slots, randomSource);
			itemStack.addAttributeModifier(
				modifier.attribute.value(), new AttributeModifier(uUID, modifier.name, (double)modifier.amount.getFloat(lootContext), modifier.operation), equipmentSlot
			);
		}

		return itemStack;
	}

	public static SetAttributesFunction.ModifierBuilder modifier(
		String string, Holder<Attribute> holder, AttributeModifier.Operation operation, NumberProvider numberProvider
	) {
		return new SetAttributesFunction.ModifierBuilder(string, holder, operation, numberProvider);
	}

	public static SetAttributesFunction.Builder setAttributes() {
		return new SetAttributesFunction.Builder();
	}

	public static class Builder extends LootItemConditionalFunction.Builder<SetAttributesFunction.Builder> {
		private final List<SetAttributesFunction.Modifier> modifiers = Lists.<SetAttributesFunction.Modifier>newArrayList();

		protected SetAttributesFunction.Builder getThis() {
			return this;
		}

		public SetAttributesFunction.Builder withModifier(SetAttributesFunction.ModifierBuilder modifierBuilder) {
			this.modifiers.add(modifierBuilder.build());
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new SetAttributesFunction(this.getConditions(), this.modifiers);
		}
	}

	static record Modifier(
		String name, Holder<Attribute> attribute, AttributeModifier.Operation operation, NumberProvider amount, List<EquipmentSlot> slots, Optional<UUID> id
	) {
		private static final Codec<List<EquipmentSlot>> SLOTS_CODEC = ExtraCodecs.nonEmptyList(
			Codec.either(EquipmentSlot.CODEC, EquipmentSlot.CODEC.listOf())
				.xmap(either -> either.map(List::of, Function.identity()), list -> list.size() == 1 ? Either.left((EquipmentSlot)list.get(0)) : Either.right(list))
		);
		public static final Codec<SetAttributesFunction.Modifier> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						Codec.STRING.fieldOf("name").forGetter(SetAttributesFunction.Modifier::name),
						BuiltInRegistries.ATTRIBUTE.holderByNameCodec().fieldOf("attribute").forGetter(SetAttributesFunction.Modifier::attribute),
						AttributeModifier.Operation.CODEC.fieldOf("operation").forGetter(SetAttributesFunction.Modifier::operation),
						NumberProviders.CODEC.fieldOf("amount").forGetter(SetAttributesFunction.Modifier::amount),
						SLOTS_CODEC.fieldOf("slot").forGetter(SetAttributesFunction.Modifier::slots),
						ExtraCodecs.strictOptionalField(UUIDUtil.STRING_CODEC, "id").forGetter(SetAttributesFunction.Modifier::id)
					)
					.apply(instance, SetAttributesFunction.Modifier::new)
		);
	}

	public static class ModifierBuilder {
		private final String name;
		private final Holder<Attribute> attribute;
		private final AttributeModifier.Operation operation;
		private final NumberProvider amount;
		private Optional<UUID> id = Optional.empty();
		private final Set<EquipmentSlot> slots = EnumSet.noneOf(EquipmentSlot.class);

		public ModifierBuilder(String string, Holder<Attribute> holder, AttributeModifier.Operation operation, NumberProvider numberProvider) {
			this.name = string;
			this.attribute = holder;
			this.operation = operation;
			this.amount = numberProvider;
		}

		public SetAttributesFunction.ModifierBuilder forSlot(EquipmentSlot equipmentSlot) {
			this.slots.add(equipmentSlot);
			return this;
		}

		public SetAttributesFunction.ModifierBuilder withUuid(UUID uUID) {
			this.id = Optional.of(uUID);
			return this;
		}

		public SetAttributesFunction.Modifier build() {
			return new SetAttributesFunction.Modifier(this.name, this.attribute, this.operation, this.amount, List.copyOf(this.slots), this.id);
		}
	}
}

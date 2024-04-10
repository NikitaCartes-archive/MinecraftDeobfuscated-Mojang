package net.minecraft.world.level.storage.loot.functions;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulator;
import net.minecraft.world.level.storage.loot.ContainerComponentManipulators;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class ModifyContainerContents extends LootItemConditionalFunction {
	public static final MapCodec<ModifyContainerContents> CODEC = RecordCodecBuilder.mapCodec(
		instance -> commonFields(instance)
				.<ContainerComponentManipulator<?>, LootItemFunction>and(
					instance.group(
						ContainerComponentManipulators.CODEC.fieldOf("component").forGetter(modifyContainerContents -> modifyContainerContents.component),
						LootItemFunctions.ROOT_CODEC.fieldOf("modifier").forGetter(modifyContainerContents -> modifyContainerContents.modifier)
					)
				)
				.apply(instance, ModifyContainerContents::new)
	);
	private final ContainerComponentManipulator<?> component;
	private final LootItemFunction modifier;

	private ModifyContainerContents(
		List<LootItemCondition> list, ContainerComponentManipulator<?> containerComponentManipulator, LootItemFunction lootItemFunction
	) {
		super(list);
		this.component = containerComponentManipulator;
		this.modifier = lootItemFunction;
	}

	@Override
	public LootItemFunctionType<ModifyContainerContents> getType() {
		return LootItemFunctions.MODIFY_CONTENTS;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			this.component.modifyItems(itemStack, itemStackx -> (ItemStack)this.modifier.apply(itemStackx, lootContext));
			return itemStack;
		}
	}

	@Override
	public void validate(ValidationContext validationContext) {
		super.validate(validationContext);
		this.modifier.validate(validationContext.forChild(".modifier"));
	}
}

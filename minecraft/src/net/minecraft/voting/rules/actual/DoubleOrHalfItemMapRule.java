package net.minecraft.voting.rules.actual;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.DoubleOrHalfMapRule;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DoubleOrHalfItemMapRule extends DoubleOrHalfMapRule<ResourceKey<Item>> {
	private final String descriptionId;

	public DoubleOrHalfItemMapRule(String string, int i, int j) {
		super(ResourceKey.codec(Registries.ITEM), i, j);
		this.descriptionId = string;
	}

	@Override
	protected Stream<ResourceKey<Item>> randomDomainValues(MinecraftServer minecraftServer, RandomSource randomSource) {
		Registry<Item> registry = minecraftServer.registryAccess().registryOrThrow(Registries.ITEM);
		return Stream.generate(() -> registry.getRandom(randomSource).map(Holder.Reference::key)).flatMap(Optional::stream);
	}

	public ItemStack adjustItemStack(ItemStack itemStack) {
		if (itemStack.isEmpty()) {
			return itemStack;
		} else {
			Item item = itemStack.getItem();
			float f = this.getFloat(item.builtInRegistryHolder().key());
			if ((double)f == 1.0) {
				return itemStack;
			} else {
				int i = Math.round((float)itemStack.getCount() * f);
				return itemStack.copyWithCount(Mth.clamp(i, 1, item.getMaxStackSize()));
			}
		}
	}

	protected Component description(ResourceKey<Item> resourceKey, Integer integer) {
		Component component = BuiltInRegistries.ITEM.get(resourceKey.location()).getDescription();
		return Component.translatable(this.descriptionId, component, Component.literal(powerOfTwoText(integer)));
	}
}

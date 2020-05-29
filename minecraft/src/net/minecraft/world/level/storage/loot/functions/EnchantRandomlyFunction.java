package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnchantRandomlyFunction extends LootItemConditionalFunction {
	private static final Logger LOGGER = LogManager.getLogger();
	private final List<Enchantment> enchantments;

	private EnchantRandomlyFunction(LootItemCondition[] lootItemConditions, Collection<Enchantment> collection) {
		super(lootItemConditions);
		this.enchantments = ImmutableList.copyOf(collection);
	}

	@Override
	public LootItemFunctionType getType() {
		return LootItemFunctions.ENCHANT_RANDOMLY;
	}

	@Override
	public ItemStack run(ItemStack itemStack, LootContext lootContext) {
		Random random = lootContext.getRandom();
		Enchantment enchantment;
		if (this.enchantments.isEmpty()) {
			boolean bl = itemStack.getItem() == Items.BOOK;
			List<Enchantment> list = (List<Enchantment>)Registry.ENCHANTMENT
				.stream()
				.filter(Enchantment::isDiscoverable)
				.filter(enchantmentx -> bl || enchantmentx.canEnchant(itemStack))
				.collect(Collectors.toList());
			if (list.isEmpty()) {
				LOGGER.warn("Couldn't find a compatible enchantment for {}", itemStack);
				return itemStack;
			}

			enchantment = (Enchantment)list.get(random.nextInt(list.size()));
		} else {
			enchantment = (Enchantment)this.enchantments.get(random.nextInt(this.enchantments.size()));
		}

		return enchantItem(itemStack, enchantment, random);
	}

	private static ItemStack enchantItem(ItemStack itemStack, Enchantment enchantment, Random random) {
		int i = Mth.nextInt(random, enchantment.getMinLevel(), enchantment.getMaxLevel());
		if (itemStack.getItem() == Items.BOOK) {
			itemStack = new ItemStack(Items.ENCHANTED_BOOK);
			EnchantedBookItem.addEnchantment(itemStack, new EnchantmentInstance(enchantment, i));
		} else {
			itemStack.enchant(enchantment, i);
		}

		return itemStack;
	}

	public static LootItemConditionalFunction.Builder<?> randomApplicableEnchantment() {
		return simpleBuilder(lootItemConditions -> new EnchantRandomlyFunction(lootItemConditions, ImmutableList.<Enchantment>of()));
	}

	public static class Builder extends LootItemConditionalFunction.Builder<EnchantRandomlyFunction.Builder> {
		private final Set<Enchantment> enchantments = Sets.<Enchantment>newHashSet();

		protected EnchantRandomlyFunction.Builder getThis() {
			return this;
		}

		public EnchantRandomlyFunction.Builder withEnchantment(Enchantment enchantment) {
			this.enchantments.add(enchantment);
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new EnchantRandomlyFunction(this.getConditions(), this.enchantments);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<EnchantRandomlyFunction> {
		public void serialize(JsonObject jsonObject, EnchantRandomlyFunction enchantRandomlyFunction, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, enchantRandomlyFunction, jsonSerializationContext);
			if (!enchantRandomlyFunction.enchantments.isEmpty()) {
				JsonArray jsonArray = new JsonArray();

				for (Enchantment enchantment : enchantRandomlyFunction.enchantments) {
					ResourceLocation resourceLocation = Registry.ENCHANTMENT.getKey(enchantment);
					if (resourceLocation == null) {
						throw new IllegalArgumentException("Don't know how to serialize enchantment " + enchantment);
					}

					jsonArray.add(new JsonPrimitive(resourceLocation.toString()));
				}

				jsonObject.add("enchantments", jsonArray);
			}
		}

		public EnchantRandomlyFunction deserialize(
			JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions
		) {
			List<Enchantment> list = Lists.<Enchantment>newArrayList();
			if (jsonObject.has("enchantments")) {
				for (JsonElement jsonElement : GsonHelper.getAsJsonArray(jsonObject, "enchantments")) {
					String string = GsonHelper.convertToString(jsonElement, "enchantment");
					Enchantment enchantment = (Enchantment)Registry.ENCHANTMENT
						.getOptional(new ResourceLocation(string))
						.orElseThrow(() -> new JsonSyntaxException("Unknown enchantment '" + string + "'"));
					list.add(enchantment);
				}
			}

			return new EnchantRandomlyFunction(lootItemConditions, list);
		}
	}
}

package net.minecraft.world.level.storage.loot.functions;

import com.google.common.collect.ImmutableSet;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import java.util.Random;
import java.util.Set;
import java.util.function.BiFunction;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class AddBookContents extends LootItemConditionalFunction {
	private final AddBookContents.ContentProvider provider;

	protected AddBookContents(LootItemCondition[] lootItemConditions, AddBookContents.ContentProvider contentProvider) {
		super(lootItemConditions);
		this.provider = contentProvider;
	}

	public static LootItemConditionalFunction.Builder<?> addContents(AddBookContents.ContentProvider contentProvider) {
		return simpleBuilder(lootItemConditions -> new AddBookContents(lootItemConditions, contentProvider));
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return ImmutableSet.of(LootContextParams.BLOCK_POS);
	}

	@Override
	protected ItemStack run(ItemStack itemStack, LootContext lootContext) {
		String string = (String)this.provider.apply(lootContext.getRandom(), lootContext.getParam(LootContextParams.BLOCK_POS));
		CompoundTag compoundTag = itemStack.getOrCreateTag();
		ListTag listTag = new ListTag();
		listTag.add(StringTag.valueOf(Component.Serializer.toJson(new TextComponent(string))));
		compoundTag.put("pages", listTag);
		compoundTag.putString("author", ChatFormatting.OBFUSCATED + "Deepest Lore");
		compoundTag.putString("title", "Orders");
		return itemStack;
	}

	public static enum ContentProvider implements BiFunction<Random, BlockPos, String> {
		ORDERS("orders") {
			private final String[] verb = new String[]{
				"capture", "destroy", "cut", "find", "obliterate", "discover", "observe", "reinforce", "build", "deploy", "restore", "deliver"
			};
			private final String[] object = new String[]{
				"cheese",
				"footprints",
				"bananas",
				"toeshoes",
				"mah brewskis",
				"bicycle build for two",
				"my canoe",
				"Minecraft 3D: Lost Floppies",
				"content",
				"those pesky modders",
				"license-free mappings",
				"those VHS",
				"pre-mixed coctails",
				"quasi-connectivity"
			};

			public String apply(Random random, BlockPos blockPos) {
				return this.verb[random.nextInt(this.verb.length)] + " " + ChatFormatting.OBFUSCATED + this.object[random.nextInt(this.object.length)];
			}
		};

		private final String name;

		private ContentProvider(String string2) {
			this.name = string2;
		}

		public static AddBookContents.ContentProvider getByName(String string) {
			for (AddBookContents.ContentProvider contentProvider : values()) {
				if (contentProvider.name.equals(string)) {
					return contentProvider;
				}
			}

			throw new IllegalArgumentException("Invalid content source " + string);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<AddBookContents> {
		public Serializer() {
			super(new ResourceLocation("add_book_contents"), AddBookContents.class);
		}

		public void serialize(JsonObject jsonObject, AddBookContents addBookContents, JsonSerializationContext jsonSerializationContext) {
			super.serialize(jsonObject, addBookContents, jsonSerializationContext);
			jsonObject.addProperty("provider", addBookContents.provider.name);
		}

		public AddBookContents deserialize(JsonObject jsonObject, JsonDeserializationContext jsonDeserializationContext, LootItemCondition[] lootItemConditions) {
			AddBookContents.ContentProvider contentProvider = AddBookContents.ContentProvider.getByName(GsonHelper.getAsString(jsonObject, "provider"));
			return new AddBookContents(lootItemConditions, contentProvider);
		}
	}
}

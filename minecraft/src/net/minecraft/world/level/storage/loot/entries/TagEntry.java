package net.minecraft.world.level.storage.loot.entries;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

public class TagEntry extends LootPoolSingletonContainer {
	public static final Codec<TagEntry> CODEC = RecordCodecBuilder.create(
		instance -> instance.group(
					TagKey.codec(Registries.ITEM).fieldOf("name").forGetter(tagEntry -> tagEntry.tag), Codec.BOOL.fieldOf("expand").forGetter(tagEntry -> tagEntry.expand)
				)
				.<int, int, List<LootItemCondition>, List<LootItemFunction>>and(singletonFields(instance))
				.apply(instance, TagEntry::new)
	);
	private final TagKey<Item> tag;
	private final boolean expand;

	private TagEntry(TagKey<Item> tagKey, boolean bl, int i, int j, List<LootItemCondition> list, List<LootItemFunction> list2) {
		super(i, j, list, list2);
		this.tag = tagKey;
		this.expand = bl;
	}

	@Override
	public LootPoolEntryType getType() {
		return LootPoolEntries.TAG;
	}

	@Override
	public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
		BuiltInRegistries.ITEM.getTagOrEmpty(this.tag).forEach(holder -> consumer.accept(new ItemStack(holder)));
	}

	private boolean expandTag(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		if (!this.canRun(lootContext)) {
			return false;
		} else {
			for (final Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(this.tag)) {
				consumer.accept(new LootPoolSingletonContainer.EntryBase() {
					@Override
					public void createItemStack(Consumer<ItemStack> consumer, LootContext lootContext) {
						consumer.accept(new ItemStack(holder));
					}
				});
			}

			return true;
		}
	}

	@Override
	public boolean expand(LootContext lootContext, Consumer<LootPoolEntry> consumer) {
		return this.expand ? this.expandTag(lootContext, consumer) : super.expand(lootContext, consumer);
	}

	public static LootPoolSingletonContainer.Builder<?> tagContents(TagKey<Item> tagKey) {
		return simpleBuilder((i, j, list, list2) -> new TagEntry(tagKey, false, i, j, list, list2));
	}

	public static LootPoolSingletonContainer.Builder<?> expandTag(TagKey<Item> tagKey) {
		return simpleBuilder((i, j, list, list2) -> new TagEntry(tagKey, true, i, j, list, list2));
	}
}

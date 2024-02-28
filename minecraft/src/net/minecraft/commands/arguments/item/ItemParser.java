package net.minecraft.commands.arguments.item;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import org.apache.commons.lang3.mutable.MutableObject;

public class ItemParser {
	private final ItemSyntaxParser syntax;

	public ItemParser(HolderLookup.Provider provider) {
		this.syntax = new ItemSyntaxParser(provider, false);
	}

	public ItemParser.ItemResult parse(StringReader stringReader) throws CommandSyntaxException {
		final MutableObject<Holder<Item>> mutableObject = new MutableObject<>();
		final DataComponentMap.Builder builder = DataComponentMap.builder();
		this.syntax.parse(stringReader, new ItemSyntaxParser.Visitor() {
			@Override
			public void visitItem(Holder<Item> holder) {
				mutableObject.setValue(holder);
			}

			@Override
			public <T> void visitComponent(DataComponentType<T> dataComponentType, T object) {
				builder.set(dataComponentType, object);
			}

			@Override
			public void visitCustomData(CompoundTag compoundTag) {
				builder.set(DataComponents.CUSTOM_DATA, CustomData.of(compoundTag));
			}
		});
		return new ItemParser.ItemResult((Holder<Item>)Objects.requireNonNull(mutableObject.getValue(), "Parser gave no item"), builder.build());
	}

	public CompletableFuture<Suggestions> fillSuggestions(SuggestionsBuilder suggestionsBuilder) {
		return this.syntax.fillSuggestions(suggestionsBuilder);
	}

	public static record ItemResult(Holder<Item> item, DataComponentMap components) {
	}
}

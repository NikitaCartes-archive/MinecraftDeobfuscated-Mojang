package net.minecraft.commands.arguments;

import com.google.common.collect.Maps;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.EquipmentSlot;

public class SlotArgument implements ArgumentType<Integer> {
	private static final Collection<String> EXAMPLES = Arrays.asList("container.5", "12", "weapon");
	private static final DynamicCommandExceptionType ERROR_UNKNOWN_SLOT = new DynamicCommandExceptionType(
		object -> new TranslatableComponent("slot.unknown", object)
	);
	private static final Map<String, Integer> SLOTS = Util.make(Maps.<String, Integer>newHashMap(), hashMap -> {
		for (int i = 0; i < 54; i++) {
			hashMap.put("container." + i, i);
		}

		for (int i = 0; i < 9; i++) {
			hashMap.put("hotbar." + i, i);
		}

		for (int i = 0; i < 27; i++) {
			hashMap.put("inventory." + i, 9 + i);
		}

		for (int i = 0; i < 27; i++) {
			hashMap.put("enderchest." + i, 200 + i);
		}

		for (int i = 0; i < 8; i++) {
			hashMap.put("villager." + i, 300 + i);
		}

		for (int i = 0; i < 15; i++) {
			hashMap.put("horse." + i, 500 + i);
		}

		hashMap.put("weapon", 98);
		hashMap.put("weapon.mainhand", 98);
		hashMap.put("weapon.offhand", 99);
		hashMap.put("armor.head", 100 + EquipmentSlot.HEAD.getIndex());
		hashMap.put("armor.chest", 100 + EquipmentSlot.CHEST.getIndex());
		hashMap.put("armor.legs", 100 + EquipmentSlot.LEGS.getIndex());
		hashMap.put("armor.feet", 100 + EquipmentSlot.FEET.getIndex());
		hashMap.put("horse.saddle", 400);
		hashMap.put("horse.armor", 401);
		hashMap.put("horse.chest", 499);
	});

	public static SlotArgument slot() {
		return new SlotArgument();
	}

	public static int getSlot(CommandContext<CommandSourceStack> commandContext, String string) {
		return commandContext.<Integer>getArgument(string, Integer.class);
	}

	public Integer parse(StringReader stringReader) throws CommandSyntaxException {
		String string = stringReader.readUnquotedString();
		if (!SLOTS.containsKey(string)) {
			throw ERROR_UNKNOWN_SLOT.create(string);
		} else {
			return (Integer)SLOTS.get(string);
		}
	}

	@Override
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		return SharedSuggestionProvider.suggest(SLOTS.keySet(), suggestionsBuilder);
	}

	@Override
	public Collection<String> getExamples() {
		return EXAMPLES;
	}
}

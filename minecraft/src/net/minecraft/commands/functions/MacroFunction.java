package net.minecraft.commands.functions;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.FunctionInstantiationException;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class MacroFunction<T extends ExecutionCommandSource<T>> implements CommandFunction<T> {
	private static final DecimalFormat DECIMAL_FORMAT = Util.make(new DecimalFormat("#"), decimalFormat -> {
		decimalFormat.setMaximumFractionDigits(15);
		decimalFormat.setDecimalFormatSymbols(DecimalFormatSymbols.getInstance(Locale.US));
	});
	private static final int MAX_CACHE_ENTRIES = 8;
	private final List<String> parameters;
	private final Object2ObjectLinkedOpenHashMap<List<String>, InstantiatedFunction<T>> cache = new Object2ObjectLinkedOpenHashMap<>(8, 0.25F);
	private final ResourceLocation id;
	private final List<MacroFunction.Entry<T>> entries;

	public MacroFunction(ResourceLocation resourceLocation, List<MacroFunction.Entry<T>> list, List<String> list2) {
		this.id = resourceLocation;
		this.entries = list;
		this.parameters = list2;
	}

	@Override
	public ResourceLocation id() {
		return this.id;
	}

	public InstantiatedFunction<T> instantiate(@Nullable CompoundTag compoundTag, CommandDispatcher<T> commandDispatcher, T executionCommandSource) throws FunctionInstantiationException {
		if (compoundTag == null) {
			throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_arguments", Component.translationArg(this.id())));
		} else {
			List<String> list = new ArrayList(this.parameters.size());

			for (String string : this.parameters) {
				Tag tag = compoundTag.get(string);
				if (tag == null) {
					throw new FunctionInstantiationException(Component.translatable("commands.function.error.missing_argument", Component.translationArg(this.id()), string));
				}

				list.add(stringify(tag));
			}

			InstantiatedFunction<T> instantiatedFunction = this.cache.getAndMoveToLast(list);
			if (instantiatedFunction != null) {
				return instantiatedFunction;
			} else {
				if (this.cache.size() >= 8) {
					this.cache.removeFirst();
				}

				InstantiatedFunction<T> instantiatedFunction2 = this.substituteAndParse(this.parameters, list, commandDispatcher, executionCommandSource);
				this.cache.put(list, instantiatedFunction2);
				return instantiatedFunction2;
			}
		}
	}

	private static String stringify(Tag tag) {
		if (tag instanceof FloatTag floatTag) {
			return DECIMAL_FORMAT.format((double)floatTag.getAsFloat());
		} else if (tag instanceof DoubleTag doubleTag) {
			return DECIMAL_FORMAT.format(doubleTag.getAsDouble());
		} else if (tag instanceof ByteTag byteTag) {
			return String.valueOf(byteTag.getAsByte());
		} else if (tag instanceof ShortTag shortTag) {
			return String.valueOf(shortTag.getAsShort());
		} else {
			return tag instanceof LongTag longTag ? String.valueOf(longTag.getAsLong()) : tag.getAsString();
		}
	}

	private static void lookupValues(List<String> list, IntList intList, List<String> list2) {
		list2.clear();
		intList.forEach(i -> list2.add((String)list.get(i)));
	}

	private InstantiatedFunction<T> substituteAndParse(List<String> list, List<String> list2, CommandDispatcher<T> commandDispatcher, T executionCommandSource) throws FunctionInstantiationException {
		List<UnboundEntryAction<T>> list3 = new ArrayList(this.entries.size());
		List<String> list4 = new ArrayList(list2.size());

		for (MacroFunction.Entry<T> entry : this.entries) {
			lookupValues(list2, entry.parameters(), list4);
			list3.add(entry.instantiate(list4, commandDispatcher, executionCommandSource, this.id));
		}

		return new PlainTextFunction<>(this.id().withPath((UnaryOperator<String>)(string -> string + "/" + list.hashCode())), list3);
	}

	interface Entry<T> {
		IntList parameters();

		UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, T object, ResourceLocation resourceLocation) throws FunctionInstantiationException;
	}

	static class MacroEntry<T extends ExecutionCommandSource<T>> implements MacroFunction.Entry<T> {
		private final StringTemplate template;
		private final IntList parameters;

		public MacroEntry(StringTemplate stringTemplate, IntList intList) {
			this.template = stringTemplate;
			this.parameters = intList;
		}

		@Override
		public IntList parameters() {
			return this.parameters;
		}

		public UnboundEntryAction<T> instantiate(
			List<String> list, CommandDispatcher<T> commandDispatcher, T executionCommandSource, ResourceLocation resourceLocation
		) throws FunctionInstantiationException {
			String string = this.template.substitute(list);

			try {
				return CommandFunction.parseCommand(commandDispatcher, executionCommandSource, new StringReader(string));
			} catch (CommandSyntaxException var7) {
				throw new FunctionInstantiationException(
					Component.translatable("commands.function.error.parse", Component.translationArg(resourceLocation), string, var7.getMessage())
				);
			}
		}
	}

	static class PlainTextEntry<T> implements MacroFunction.Entry<T> {
		private final UnboundEntryAction<T> compiledAction;

		public PlainTextEntry(UnboundEntryAction<T> unboundEntryAction) {
			this.compiledAction = unboundEntryAction;
		}

		@Override
		public IntList parameters() {
			return IntLists.emptyList();
		}

		@Override
		public UnboundEntryAction<T> instantiate(List<String> list, CommandDispatcher<T> commandDispatcher, T object, ResourceLocation resourceLocation) {
			return this.compiledAction;
		}
	}
}

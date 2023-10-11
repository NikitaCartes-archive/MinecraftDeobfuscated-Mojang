package net.minecraft.commands.functions;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.ExecutionCommandSource;
import net.minecraft.commands.execution.UnboundEntryAction;
import net.minecraft.resources.ResourceLocation;

class FunctionBuilder<T extends ExecutionCommandSource<T>> {
	@Nullable
	private List<UnboundEntryAction<T>> plainEntries = new ArrayList();
	@Nullable
	private List<MacroFunction.Entry<T>> macroEntries;
	private final List<String> macroArguments = new ArrayList();

	public void addCommand(UnboundEntryAction<T> unboundEntryAction) {
		if (this.macroEntries != null) {
			this.macroEntries.add(new MacroFunction.PlainTextEntry<>(unboundEntryAction));
		} else {
			this.plainEntries.add(unboundEntryAction);
		}
	}

	private int getArgumentIndex(String string) {
		int i = this.macroArguments.indexOf(string);
		if (i == -1) {
			i = this.macroArguments.size();
			this.macroArguments.add(string);
		}

		return i;
	}

	private IntList convertToIndices(List<String> list) {
		IntArrayList intArrayList = new IntArrayList(list.size());

		for (String string : list) {
			intArrayList.add(this.getArgumentIndex(string));
		}

		return intArrayList;
	}

	public void addMacro(String string, int i) {
		StringTemplate stringTemplate = StringTemplate.fromString(string, i);
		if (this.plainEntries != null) {
			this.macroEntries = new ArrayList(this.plainEntries.size() + 1);

			for (UnboundEntryAction<T> unboundEntryAction : this.plainEntries) {
				this.macroEntries.add(new MacroFunction.PlainTextEntry<>(unboundEntryAction));
			}

			this.plainEntries = null;
		}

		this.macroEntries.add(new MacroFunction.MacroEntry(stringTemplate, this.convertToIndices(stringTemplate.variables())));
	}

	public CommandFunction<T> build(ResourceLocation resourceLocation) {
		return (CommandFunction<T>)(this.macroEntries != null
			? new MacroFunction<>(resourceLocation, this.macroEntries, this.macroArguments)
			: new PlainTextFunction<>(resourceLocation, this.plainEntries));
	}
}

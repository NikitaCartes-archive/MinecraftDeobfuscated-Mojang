package net.minecraft.commands.arguments.selector;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

public record SelectorPattern(String pattern, EntitySelector resolved) {
	public static final Codec<SelectorPattern> CODEC = Codec.STRING.comapFlatMap(SelectorPattern::parse, SelectorPattern::pattern);

	public static DataResult<SelectorPattern> parse(String string) {
		try {
			EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string), true);
			return DataResult.success(new SelectorPattern(string, entitySelectorParser.parse()));
		} catch (CommandSyntaxException var2) {
			return DataResult.error(() -> "Invalid selector component: " + string + ": " + var2.getMessage());
		}
	}

	public boolean equals(Object object) {
		if (object instanceof SelectorPattern selectorPattern && this.pattern.equals(selectorPattern.pattern)) {
			return true;
		}

		return false;
	}

	public int hashCode() {
		return this.pattern.hashCode();
	}

	public String toString() {
		return this.pattern;
	}
}

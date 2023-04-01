package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.RuleChange;

public class CodepointStyleRule extends MapRule<Integer, CodepointStyleRule.CodepointChange> {
	private static final String ALT_SUPPORTED = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private static final String ILLAGER_SUPPORTED = "!,-.0123456789?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
	private final Int2ObjectMap<CodepointStyleRule.CodepointChange> entries = new Int2ObjectOpenHashMap<>();

	public CodepointStyleRule() {
		super(Codec.INT, CodepointStyleRule.CodepointChange.CODEC);
	}

	@Nullable
	public CodepointStyleRule.CodepointChange getChange(int i) {
		return this.entries.get(i);
	}

	protected Component description(Integer integer, CodepointStyleRule.CodepointChange codepointChange) {
		return Component.translatable(codepointChange.descriptionId, Character.toString(integer));
	}

	protected void set(Integer integer, CodepointStyleRule.CodepointChange codepointChange) {
		this.entries.put(integer.intValue(), codepointChange);
	}

	protected void remove(Integer integer) {
		this.entries.remove(integer.intValue());
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries
			.entrySet()
			.stream()
			.map(entry -> new MapRule.MapRuleChange((Integer)entry.getKey(), (CodepointStyleRule.CodepointChange)entry.getValue()));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		int j = randomSource.nextIntBetweenInclusive(32, 126);
		ObjectArrayList<CodepointStyleRule.CodepointChange> objectArrayList = new ObjectArrayList<>(CodepointStyleRule.CodepointChange.values());
		Util.shuffle(objectArrayList, randomSource);
		if ("!,-.0123456789?ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(j) == -1) {
			objectArrayList.remove(CodepointStyleRule.CodepointChange.ILLAGER);
		}

		if ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".indexOf(j) == -1) {
			objectArrayList.remove(CodepointStyleRule.CodepointChange.SGA);
		}

		CodepointStyleRule.CodepointChange codepointChange = this.entries.get(j);
		if (codepointChange != null) {
			objectArrayList.remove(codepointChange);
		}

		return objectArrayList.stream().limit((long)i).map(codepointChangex -> new MapRule.MapRuleChange(j, codepointChangex));
	}

	public static enum CodepointChange implements StringRepresentable {
		HIDE("hide"),
		BLANK("blank"),
		BLACK("black"),
		DARK_BLUE("dark_blue"),
		DARK_GREEN("dark_green"),
		DARK_AQUA("dark_aqua"),
		DARK_RED("dark_red"),
		DARK_PURPLE("dark_purple"),
		GOLD("gold"),
		GRAY("gray"),
		DARK_GRAY("dark_gray"),
		BLUE("blue"),
		GREEN("green"),
		AQUA("aqua"),
		RED("red"),
		LIGHT_PURPLE("light_purple"),
		YELLOW("yellow"),
		WHITE("white"),
		OBFUSCATED("obfuscated"),
		BOLD("bold"),
		STRIKETHROUGH("strikethrough"),
		UNDERLINE("underline"),
		ITALIC("italic"),
		THIN("thin"),
		SGA("sga"),
		ILLAGER("illager");

		public static final Codec<CodepointStyleRule.CodepointChange> CODEC = StringRepresentable.fromEnum(CodepointStyleRule.CodepointChange::values);
		private final String id;
		final String descriptionId;

		private CodepointChange(String string2) {
			this.id = string2;
			this.descriptionId = "rule.text_style." + string2;
		}

		@Override
		public String getSerializedName() {
			return this.id;
		}
	}
}

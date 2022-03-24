package net.minecraft.client.gui.components;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Optional;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.ComponentCollector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public class ComponentRenderUtils {
	private static final FormattedCharSequence INDENT = FormattedCharSequence.codepoint(32, Style.EMPTY);

	private static String stripColor(String string) {
		return Minecraft.getInstance().options.chatColors().get() ? string : ChatFormatting.stripFormatting(string);
	}

	public static List<FormattedCharSequence> wrapComponents(FormattedText formattedText, int i, Font font) {
		ComponentCollector componentCollector = new ComponentCollector();
		formattedText.visit((style, string) -> {
			componentCollector.append(FormattedText.of(stripColor(string), style));
			return Optional.empty();
		}, Style.EMPTY);
		List<FormattedCharSequence> list = Lists.<FormattedCharSequence>newArrayList();
		font.getSplitter().splitLines(componentCollector.getResultOrEmpty(), i, Style.EMPTY, (formattedTextx, boolean_) -> {
			FormattedCharSequence formattedCharSequence = Language.getInstance().getVisualOrder(formattedTextx);
			list.add(boolean_ ? FormattedCharSequence.composite(INDENT, formattedCharSequence) : formattedCharSequence);
		});
		return (List<FormattedCharSequence>)(list.isEmpty() ? Lists.<FormattedCharSequence>newArrayList(FormattedCharSequence.EMPTY) : list);
	}
}

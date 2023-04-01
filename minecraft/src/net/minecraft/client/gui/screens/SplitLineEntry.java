package net.minecraft.client.gui.screens;

import com.google.common.collect.Streams;
import java.util.stream.Stream;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

@Environment(EnvType.CLIENT)
public record SplitLineEntry(FormattedCharSequence contents, long index, Component original) {
	public static Stream<SplitLineEntry> splitToWidth(Font font, Stream<Component> stream, int i) {
		return stream.flatMap(
			component -> Streams.mapWithIndex(font.split(component, i).stream(), (formattedCharSequence, l) -> new SplitLineEntry(formattedCharSequence, l, component))
		);
	}
}

package net.minecraft.client.gui.narration;

import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

@Environment(EnvType.CLIENT)
public interface NarrationElementOutput {
	default void add(NarratedElementType narratedElementType, Component component) {
		this.add(narratedElementType, NarrationThunk.from(component.getString()));
	}

	default void add(NarratedElementType narratedElementType, String string) {
		this.add(narratedElementType, NarrationThunk.from(string));
	}

	default void add(NarratedElementType narratedElementType, Component... components) {
		this.add(narratedElementType, NarrationThunk.from(ImmutableList.copyOf(components)));
	}

	void add(NarratedElementType narratedElementType, NarrationThunk<?> narrationThunk);

	NarrationElementOutput nest();
}

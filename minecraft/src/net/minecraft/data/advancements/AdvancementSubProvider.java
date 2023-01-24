package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;

public interface AdvancementSubProvider {
	static Advancement createPlaceholder(String string) {
		return Advancement.Builder.advancement().build(new ResourceLocation(string));
	}

	void generate(HolderLookup.Provider provider, Consumer<Advancement> consumer);
}

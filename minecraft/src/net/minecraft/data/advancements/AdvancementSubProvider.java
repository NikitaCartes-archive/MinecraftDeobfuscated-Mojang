package net.minecraft.data.advancements;

import java.util.function.Consumer;
import net.minecraft.advancements.Advancement;

public interface AdvancementSubProvider {
	void generate(Consumer<Advancement> consumer);
}

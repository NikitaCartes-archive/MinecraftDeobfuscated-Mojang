package net.minecraft.data.advancements.packs;

import java.util.List;
import net.minecraft.data.PackOutput;
import net.minecraft.data.advancements.AdvancementProvider;

public class VanillaAdvancementProvider {
	public static AdvancementProvider create(PackOutput packOutput) {
		return new AdvancementProvider(
			packOutput,
			List.of(
				new VanillaTheEndAdvancements(),
				new VanillaHusbandryAdvancements(),
				new VanillaAdventureAdvancements(),
				new VanillaNetherAdvancements(),
				new VanillaStoryAdvancements()
			)
		);
	}
}

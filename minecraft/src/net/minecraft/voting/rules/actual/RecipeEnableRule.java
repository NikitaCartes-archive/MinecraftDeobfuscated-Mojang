package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.Set;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.voting.rules.SetRule;

public class RecipeEnableRule extends SetRule<ResourceLocation> {
	private static final Set<ResourceLocation> SPECIAL_RECIPES = Set.of(
		new ResourceLocation("wob"), new ResourceLocation("m_banner_pattern"), new ResourceLocation("string_concatenation"), new ResourceLocation("diamond_drows")
	);

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return SPECIAL_RECIPES.stream().filter(resourceLocation -> !this.contains(resourceLocation)).limit((long)i).map(object -> new SetRule.SetRuleChange(object));
	}

	@Override
	protected Codec<ResourceLocation> elementCodec() {
		return ResourceLocation.CODEC;
	}

	public boolean isEnabled(ResourceLocation resourceLocation) {
		return this.contains(resourceLocation) ? true : !SPECIAL_RECIPES.contains(resourceLocation);
	}

	protected Component description(ResourceLocation resourceLocation) {
		return Component.translatable(resourceLocation.toLanguageKey("rule.recipe"));
	}
}

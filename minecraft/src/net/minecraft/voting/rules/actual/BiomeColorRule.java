package net.minecraft.voting.rules.actual;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.RgbTxt;
import net.minecraft.voting.rules.MapRule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.level.biome.Biome;

public class BiomeColorRule extends MapRule<ResourceKey<Biome>, RgbTxt.Entry> {
	private final Map<ResourceKey<Biome>, RgbTxt.Entry> entries = new HashMap();
	private final String descriptionId;
	private boolean isDirty;

	public BiomeColorRule(String string) {
		super(ResourceKey.codec(Registries.BIOME), RgbTxt.CODEC);
		this.descriptionId = string;
	}

	public int getColor(Holder<Biome> holder, int i) {
		Optional<ResourceKey<Biome>> optional = holder.unwrapKey();
		if (optional.isEmpty()) {
			return i;
		} else {
			RgbTxt.Entry entry = (RgbTxt.Entry)this.entries.get(optional.get());
			return entry != null ? entry.rgb() : i;
		}
	}

	public Optional<Integer> getColor(Holder<Biome> holder) {
		return holder.unwrapKey().map(this.entries::get).map(RgbTxt.Entry::rgb);
	}

	protected Component description(ResourceKey<Biome> resourceKey, RgbTxt.Entry entry) {
		return Component.translatable(this.descriptionId, Component.translatable(Util.makeDescriptionId("biome", resourceKey.location())), entry.name());
	}

	protected void set(ResourceKey<Biome> resourceKey, RgbTxt.Entry entry) {
		boolean bl = !Objects.equals(entry, this.entries.put(resourceKey, entry));
		this.isDirty |= bl;
	}

	protected void remove(ResourceKey<Biome> resourceKey) {
		boolean bl = this.entries.remove(resourceKey) != null;
		this.isDirty |= bl;
	}

	public boolean getAndClearDirtyStatus() {
		boolean bl = this.isDirty;
		this.isDirty = false;
		return bl;
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries.entrySet().stream().map(entry -> new MapRule.MapRuleChange((ResourceKey)entry.getKey(), (RgbTxt.Entry)entry.getValue()));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<Biome> registry = minecraftServer.registryAccess().registryOrThrow(Registries.BIOME);
		return Stream.generate(() -> Util.getRandomSafe(RgbTxt.COLORS, randomSource))
			.flatMap(Optional::stream)
			.map(entry -> registry.getRandom(randomSource).map(reference -> new MapRule.MapRuleChange(reference.key(), entry)))
			.flatMap(Optional::stream)
			.limit((long)i)
			.map(mapRuleChange -> mapRuleChange);
	}
}

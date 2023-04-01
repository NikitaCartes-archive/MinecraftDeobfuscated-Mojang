package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Unit;

public abstract class ResourceKeySetRule<T> extends SetRule<ResourceKey<T>> {
	private final String desciptionId;
	private final ResourceKey<? extends Registry<T>> registryKey;

	protected ResourceKeySetRule(String string, ResourceKey<? extends Registry<T>> resourceKey) {
		this.desciptionId = string;
		this.registryKey = resourceKey;
	}

	@Override
	protected Codec<ResourceKey<T>> elementCodec() {
		return ResourceKey.codec(this.registryKey);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Stream.generate(() -> Unit.INSTANCE)
			.flatMap(unit -> minecraftServer.registryAccess().registryOrThrow(this.registryKey).getRandom(randomSource).stream())
			.map(Holder.Reference::key)
			.distinct()
			.filter(resourceKey -> !this.contains(resourceKey))
			.limit((long)i)
			.map(object -> new SetRule.SetRuleChange(object));
	}

	protected Component description(ResourceKey<T> resourceKey) {
		return this.description((Component)Component.translatable(Util.makeDescriptionId(this.desciptionId, resourceKey.location())));
	}

	protected abstract Component description(Component component);
}

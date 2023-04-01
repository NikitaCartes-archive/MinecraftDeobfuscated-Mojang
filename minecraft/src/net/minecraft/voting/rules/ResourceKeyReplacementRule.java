package net.minecraft.voting.rules;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public abstract class ResourceKeyReplacementRule<T> extends MapRule<ResourceKey<T>, ResourceKey<T>> {
	protected final ResourceKey<? extends Registry<T>> registryName;
	protected final Map<ResourceKey<T>, ResourceKey<T>> entries = new HashMap();

	public ResourceKeyReplacementRule(ResourceKey<? extends Registry<T>> resourceKey) {
		super(ResourceKey.codec(resourceKey), ResourceKey.codec(resourceKey));
		this.registryName = resourceKey;
	}

	protected void set(ResourceKey<T> resourceKey, ResourceKey<T> resourceKey2) {
		this.entries.put(resourceKey, resourceKey2);
	}

	protected void remove(ResourceKey<T> resourceKey) {
		this.entries.remove(resourceKey);
	}

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.entries.entrySet().stream().map(entry -> new MapRule.MapRuleChange((ResourceKey)entry.getKey(), (ResourceKey)entry.getValue()));
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		Registry<T> registry = minecraftServer.registryAccess().registryOrThrow(this.registryName);
		return registry.getRandom(randomSource)
			.stream()
			.flatMap(
				reference -> {
					ResourceKey<T> resourceKey = reference.key();
					return Stream.generate(() -> registry.getRandom(randomSource))
						.flatMap(Optional::stream)
						.filter(referencex -> !referencex.is(resourceKey))
						.limit((long)i)
						.map(referencex -> new MapRule.MapRuleChange(resourceKey, referencex.key()));
				}
			);
	}
}

package net.minecraft.commands;

import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;

public final class CommandBuildContext {
	private final RegistryAccess registryAccess;
	CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy = CommandBuildContext.MissingTagAccessPolicy.FAIL;

	public CommandBuildContext(RegistryAccess registryAccess) {
		this.registryAccess = registryAccess;
	}

	public void missingTagAccessPolicy(CommandBuildContext.MissingTagAccessPolicy missingTagAccessPolicy) {
		this.missingTagAccessPolicy = missingTagAccessPolicy;
	}

	public <T> HolderLookup<T> holderLookup(ResourceKey<? extends Registry<T>> resourceKey) {
		return new HolderLookup.RegistryLookup<T>(this.registryAccess.registryOrThrow(resourceKey)) {
			@Override
			public Optional<? extends HolderSet<T>> get(TagKey<T> tagKey) {
				return switch (CommandBuildContext.this.missingTagAccessPolicy) {
					case FAIL -> this.registry.getTag(tagKey);
					case CREATE_NEW -> Optional.of(this.registry.getOrCreateTag(tagKey));
					case RETURN_EMPTY -> {
						Optional<? extends HolderSet<T>> optional = this.registry.getTag(tagKey);
						yield Optional.of(optional.isPresent() ? (HolderSet)optional.get() : HolderSet.direct());
					}
				};
			}
		};
	}

	public static enum MissingTagAccessPolicy {
		CREATE_NEW,
		RETURN_EMPTY,
		FAIL;
	}
}

package net.minecraft.voting.rules.actual;

import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.DoubleOrHalfMapRule;
import net.minecraft.world.damagesource.DamageType;

public class DoubleOrHalfDamageTypeMapRule extends DoubleOrHalfMapRule<ResourceKey<DamageType>> {
	private final String descriptionId;

	public DoubleOrHalfDamageTypeMapRule(String string, int i, int j) {
		super(ResourceKey.codec(Registries.DAMAGE_TYPE), i, j);
		this.descriptionId = string;
	}

	@Override
	protected Stream<ResourceKey<DamageType>> randomDomainValues(MinecraftServer minecraftServer, RandomSource randomSource) {
		Registry<DamageType> registry = minecraftServer.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
		return Stream.generate(() -> registry.getRandom(randomSource).map(Holder.Reference::key)).flatMap(Optional::stream);
	}

	public float get(Holder<DamageType> holder) {
		Optional<ResourceKey<DamageType>> optional = holder.unwrapKey();
		return optional.isPresent() ? this.getFloat((ResourceKey<DamageType>)optional.get()) : 1.0F;
	}

	protected Component description(ResourceKey<DamageType> resourceKey, Integer integer) {
		return Component.translatable(this.descriptionId, resourceKey.location().toShortLanguageKey(), Component.literal(powerOfTwoText(integer)));
	}
}

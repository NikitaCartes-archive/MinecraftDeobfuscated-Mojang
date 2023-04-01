package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.voting.rules.SetRule;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

public class GiveEffectRule extends SetRule<GiveEffectRule.MobEffectEntry> {
	public void applyToNewPlayer(ServerPlayer serverPlayer) {
		Registry<MobEffect> registry = serverPlayer.level.registryAccess().registryOrThrow(Registries.MOB_EFFECT);

		for (GiveEffectRule.MobEffectEntry mobEffectEntry : this.values()) {
			MobEffect mobEffect = registry.get(mobEffectEntry.effect);
			if (mobEffect != null) {
				MobEffectInstance mobEffectInstance = new MobEffectInstance(mobEffect, -1, mobEffectEntry.level, false, true);
				serverPlayer.addEffect(mobEffectInstance);
			}
		}
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return Stream.generate(() -> minecraftServer.registryAccess().registryOrThrow(Registries.MOB_EFFECT).getRandom(randomSource))
			.limit(100L)
			.flatMap(Optional::stream)
			.filter(reference -> !((MobEffect)reference.value()).isInstantenous())
			.map(reference -> {
				int ix = randomSource.nextIntBetweenInclusive(1, 5);
				return new GiveEffectRule.MobEffectEntry(reference.key(), ix);
			})
			.limit((long)i)
			.map(object -> new SetRule.SetRuleChange(object));
	}

	protected boolean add(GiveEffectRule.MobEffectEntry mobEffectEntry) {
		return super.add(mobEffectEntry);
	}

	@Override
	protected Codec<GiveEffectRule.MobEffectEntry> elementCodec() {
		return GiveEffectRule.MobEffectEntry.CODEC;
	}

	protected Component description(GiveEffectRule.MobEffectEntry mobEffectEntry) {
		ResourceKey<MobEffect> resourceKey = mobEffectEntry.effect;
		MobEffect mobEffect = BuiltInRegistries.MOB_EFFECT.get(resourceKey);
		MutableComponent mutableComponent;
		if (mobEffect == null) {
			mutableComponent = Component.literal(resourceKey.location().toString());
		} else {
			mutableComponent = Component.translatable(mobEffect.getDescriptionId());
		}

		if (mobEffectEntry.level > 0) {
			mutableComponent = Component.translatable("potion.withAmplifier", mutableComponent, Component.translatable("potion.potency." + mobEffectEntry.level));
		}

		return Component.translatable("rule.give_effect", mutableComponent);
	}

	@Override
	protected void applyEffect(RuleAction ruleAction, MinecraftServer minecraftServer) {
		if (ruleAction == RuleAction.APPROVE) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				this.applyToNewPlayer(serverPlayer);
			}
		}
	}

	public static record MobEffectEntry(ResourceKey<MobEffect> effect, int level) {
		public static final Codec<GiveEffectRule.MobEffectEntry> CODEC = RecordCodecBuilder.create(
			instance -> instance.group(
						ResourceKey.codec(Registries.MOB_EFFECT).fieldOf("effect").forGetter(GiveEffectRule.MobEffectEntry::effect),
						Codec.INT.fieldOf("level").forGetter(GiveEffectRule.MobEffectEntry::level)
					)
					.apply(instance, GiveEffectRule.MobEffectEntry::new)
		);
	}
}

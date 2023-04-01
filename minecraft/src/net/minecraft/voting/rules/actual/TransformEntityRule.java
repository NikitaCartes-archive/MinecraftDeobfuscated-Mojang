package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.Util;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class TransformEntityRule extends OneShotRule.Resettable {
	private final Codec<TransformEntityRule.TransformRuleChange> codec = RecordCodecBuilder.create(
		instance -> instance.group(BuiltInRegistries.ENTITY_TYPE.byNameCodec().optionalFieldOf("entity").forGetter(transformRuleChange -> transformRuleChange.type))
				.apply(instance, optional -> new TransformEntityRule.TransformRuleChange(optional))
	);

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	protected Optional<RuleChange> resetChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		boolean bl = minecraftServer.getPlayerList().getPlayers().stream().anyMatch(serverPlayer -> serverPlayer.getTransform().entity() != null);
		return bl ? Optional.of(new TransformEntityRule.TransformRuleChange(Optional.empty())) : Optional.empty();
	}

	@Override
	protected Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		List<EntityType<?>> list = minecraftServer.registryAccess()
			.registryOrThrow(Registries.ENTITY_TYPE)
			.stream()
			.filter(entityType -> entityType.getCategory() != MobCategory.MISC)
			.toList();
		return Util.getRandomSafe(list, randomSource).map(entityType -> new TransformEntityRule.TransformRuleChange(Optional.of(entityType)));
	}

	protected class TransformRuleChange extends OneShotRule.OneShotRuleChange {
		final Optional<EntityType<?>> type;
		private final Component description;

		protected TransformRuleChange(Optional<EntityType<?>> optional) {
			this.type = optional;
			this.description = (Component)optional.map(entityType -> Component.translatable("rule.transform_entity", entityType.getDescription()))
				.orElse(Component.translatable("rule.reset_entity_transform"));
		}

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				if (this.type.isPresent()) {
					serverPlayer.updateTransform(entityTransformType -> entityTransformType.withEntity((EntityType<?>)this.type.get(), Optional.empty()));
				} else {
					serverPlayer.updateTransform(entityTransformType -> entityTransformType.withEntity(Optional.empty()));
				}
			}
		}
	}
}

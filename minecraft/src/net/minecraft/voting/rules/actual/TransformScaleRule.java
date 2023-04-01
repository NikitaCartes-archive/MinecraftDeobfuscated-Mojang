package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleChange;

public class TransformScaleRule extends OneShotRule.Resettable {
	private static final float CHANGE_AMOUNT = 0.5F;
	private final Codec<TransformScaleRule.ScaleRuleChange> codec = Codec.FLOAT
		.xmap(f -> new TransformScaleRule.ScaleRuleChange(f), scaleRuleChange -> scaleRuleChange.delta);

	@Override
	public Codec<RuleChange> codec() {
		return Rule.puntCodec(this.codec);
	}

	@Override
	protected Optional<RuleChange> randomApprovableChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		return Optional.of(new TransformScaleRule.ScaleRuleChange(randomSource.nextBoolean() ? 0.5F : -0.5F));
	}

	@Override
	protected Optional<RuleChange> resetChange(MinecraftServer minecraftServer, RandomSource randomSource) {
		boolean bl = minecraftServer.getPlayerList().getPlayers().stream().anyMatch(serverPlayer -> serverPlayer.getTransform().scale() != 1.0F);
		return bl ? Optional.of(new TransformScaleRule.ScaleRuleChange(0.0F)) : Optional.empty();
	}

	protected class ScaleRuleChange extends OneShotRule.OneShotRuleChange {
		final float delta;
		private final Component description;

		protected ScaleRuleChange(float f) {
			this.delta = f;
			if (f == 0.0F) {
				this.description = Component.translatable("rule.reset_scale");
			} else {
				this.description = Component.translatable(f > 0.0F ? "rule.increase_scale" : "rule.decrease_scale", String.format("%.1f", Math.abs(f)));
			}
		}

		@Override
		protected Component description() {
			return this.description;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
				if (this.delta == 0.0F) {
					serverPlayer.updateTransform(entityTransformType -> entityTransformType.withScale(1.0F));
				} else {
					serverPlayer.updateTransform(entityTransformType -> entityTransformType.withScale(entityTransformType.scale() + this.delta));
				}
			}
		}
	}
}

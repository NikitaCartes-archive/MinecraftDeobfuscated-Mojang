package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.OneShotRule;
import net.minecraft.voting.rules.RuleChange;

public class TheJokeRule extends OneShotRule {
	static final MutableComponent LABEL = Component.translatable("rule.the_joke");
	private final OneShotRule.OneShotRuleChange change = new OneShotRule.OneShotRuleChange() {
		@Override
		protected Component description() {
			return TheJokeRule.LABEL;
		}

		@Override
		public void run(MinecraftServer minecraftServer) {
			minecraftServer.getPlayerList().broadcastAll(new ClientboundLevelEventPacket(1506, BlockPos.ZERO, 0, true));
		}
	};

	@Override
	public Codec<RuleChange> codec() {
		return Codec.unit(this.change);
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return i > 0 ? Stream.of(this.change) : Stream.empty();
	}
}

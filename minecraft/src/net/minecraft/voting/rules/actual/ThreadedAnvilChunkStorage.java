package net.minecraft.voting.rules.actual;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.Stream;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.Rule;
import net.minecraft.voting.rules.RuleAction;
import net.minecraft.voting.rules.RuleChange;
import org.slf4j.Logger;

public class ThreadedAnvilChunkStorage implements Rule {
	static final Logger LOGGER = LogUtils.getLogger();
	boolean somebodyTried;
	private final ThreadedAnvilChunkStorage.Change change = new ThreadedAnvilChunkStorage.Change();
	private final Codec<RuleChange> codec = RecordCodecBuilder.create(instance -> instance.point(this.change));
	static final float[] FACTORS_A = new float[]{2.32618E-39F, 1.7332302E25F, 7.578229E31F, 7.007856E22F, 4.730713E22F, 4.7414995E30F, 1.8012582E25F};
	static final float[] FACTORS_B = new float[]{1.498926E-39F, 4.631878E27F, 1.6974224E-19F, 7.0081926E28F, 1.7718017E28F};

	@Override
	public Stream<RuleChange> approvedChanges() {
		return this.somebodyTried ? Stream.of(this.change) : Stream.empty();
	}

	@Override
	public Stream<RuleChange> repealableChanges() {
		return Stream.empty();
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		return !this.somebodyTried && i > 0 ? Stream.of(this.change) : Stream.empty();
	}

	@Override
	public Codec<RuleChange> codec() {
		return this.codec;
	}

	static String dump(float[] fs) {
		ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();

		for (float f : fs) {
			byteArrayDataOutput.writeFloat(f);
		}

		byte[] bs = byteArrayDataOutput.toByteArray();
		ByteArrayDataInput byteArrayDataInput = ByteStreams.newDataInput(bs);
		return byteArrayDataInput.readUTF();
	}

	class Change implements RuleChange {
		@Override
		public Rule rule() {
			return ThreadedAnvilChunkStorage.this;
		}

		@Override
		public void update(RuleAction ruleAction) {
			ThreadedAnvilChunkStorage.this.somebodyTried = ruleAction == RuleAction.APPROVE;
		}

		@Override
		public void apply(RuleAction ruleAction, MinecraftServer minecraftServer) {
			if (!ThreadedAnvilChunkStorage.this.somebodyTried && ruleAction == RuleAction.APPROVE) {
				ThreadedAnvilChunkStorage.this.somebodyTried = true;
				ThreadedAnvilChunkStorage.LOGGER.error("LOOK AT YOU HACKER", (Throwable)(new UnsupportedOperationException()));
				Component component = Component.literal("Nice try");

				for (ServerPlayer serverPlayer : minecraftServer.getPlayerList().getPlayers()) {
					serverPlayer.connection.disconnect(component);
				}
			}
		}

		@Override
		public Component description(RuleAction ruleAction) {
			return switch (ruleAction) {
				case APPROVE -> ThreadedAnvilChunkStorage.this.somebodyTried
				? Component.literal(ThreadedAnvilChunkStorage.dump(ThreadedAnvilChunkStorage.FACTORS_A))
				: Component.literal(ThreadedAnvilChunkStorage.dump(ThreadedAnvilChunkStorage.FACTORS_B));
				case REPEAL -> Component.translatable("You Can Not Redo.");
			};
		}
	}
}

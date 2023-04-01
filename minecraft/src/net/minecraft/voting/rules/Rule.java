package net.minecraft.voting.rules;

import com.mojang.serialization.Codec;
import java.util.List;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.RandomSource;

public interface Rule {
	Codec<RuleChange> codec();

	Stream<RuleChange> approvedChanges();

	default Stream<RuleChange> repealableChanges() {
		return this.approvedChanges();
	}

	Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i);

	default int repealAll(boolean bl) {
		List<RuleChange> list = (bl ? this.approvedChanges() : this.repealableChanges()).toList();
		list.forEach(ruleChange -> ruleChange.update(RuleAction.REPEAL));
		return list.size();
	}

	static <T extends RuleChange> Codec<RuleChange> puntCodec(Codec<T> codec) {
		return codec;
	}
}

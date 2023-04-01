package net.minecraft.voting.rules.actual;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.Util;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.voting.rules.RuleChange;
import net.minecraft.voting.rules.SetRule;

public abstract class PlayerSetRule extends SetRule<PlayerEntry> {
	private final List<UUID> uuidList = new ArrayList();
	private final Set<UUID> uuidCheck = new HashSet();

	protected boolean add(PlayerEntry playerEntry) {
		boolean bl = super.add(playerEntry);
		if (bl) {
			this.uuidCheck.add(playerEntry.id());
			this.uuidList.add(playerEntry.id());
		}

		return bl;
	}

	@Override
	protected Codec<PlayerEntry> elementCodec() {
		return PlayerEntry.CODEC;
	}

	protected boolean remove(PlayerEntry playerEntry) {
		boolean bl = super.remove(playerEntry);
		if (bl) {
			this.uuidCheck.remove(playerEntry.id());
			this.uuidList.remove(playerEntry.id());
		}

		return bl;
	}

	public boolean contains(UUID uUID) {
		return this.uuidCheck.contains(uUID);
	}

	public List<UUID> players() {
		return this.uuidList;
	}

	@Override
	public Stream<RuleChange> randomApprovableChanges(MinecraftServer minecraftServer, RandomSource randomSource, int i) {
		ObjectArrayList<ServerPlayer> objectArrayList = new ObjectArrayList<>(minecraftServer.getPlayerList().getPlayers());
		Util.shuffle(objectArrayList, randomSource);
		return objectArrayList.stream().limit((long)i).map(serverPlayer -> new SetRule.SetRuleChange(PlayerEntry.from(serverPlayer)));
	}
}

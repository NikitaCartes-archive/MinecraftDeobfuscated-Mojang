package net.minecraft.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundResetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.DisplaySlot;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerScoreEntry;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.ScoreboardSaveData;

public class ServerScoreboard extends Scoreboard {
	private final MinecraftServer server;
	private final Set<Objective> trackedObjectives = Sets.<Objective>newHashSet();
	private final List<Runnable> dirtyListeners = Lists.<Runnable>newArrayList();

	public ServerScoreboard(MinecraftServer minecraftServer) {
		this.server = minecraftServer;
	}

	@Override
	protected void onScoreChanged(ScoreHolder scoreHolder, Objective objective, Score score) {
		super.onScoreChanged(scoreHolder, objective, score);
		if (this.trackedObjectives.contains(objective)) {
			this.server
				.getPlayerList()
				.broadcastAll(new ClientboundSetScorePacket(scoreHolder.getScoreboardName(), objective.getName(), score.value(), score.display(), score.numberFormat()));
		}

		this.setDirty();
	}

	@Override
	protected void onScoreLockChanged(ScoreHolder scoreHolder, Objective objective) {
		super.onScoreLockChanged(scoreHolder, objective);
		this.setDirty();
	}

	@Override
	public void onPlayerRemoved(ScoreHolder scoreHolder) {
		super.onPlayerRemoved(scoreHolder);
		this.server.getPlayerList().broadcastAll(new ClientboundResetScorePacket(scoreHolder.getScoreboardName(), null));
		this.setDirty();
	}

	@Override
	public void onPlayerScoreRemoved(ScoreHolder scoreHolder, Objective objective) {
		super.onPlayerScoreRemoved(scoreHolder, objective);
		if (this.trackedObjectives.contains(objective)) {
			this.server.getPlayerList().broadcastAll(new ClientboundResetScorePacket(scoreHolder.getScoreboardName(), objective.getName()));
		}

		this.setDirty();
	}

	@Override
	public void setDisplayObjective(DisplaySlot displaySlot, @Nullable Objective objective) {
		Objective objective2 = this.getDisplayObjective(displaySlot);
		super.setDisplayObjective(displaySlot, objective);
		if (objective2 != objective && objective2 != null) {
			if (this.getObjectiveDisplaySlotCount(objective2) > 0) {
				this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(displaySlot, objective));
			} else {
				this.stopTrackingObjective(objective2);
			}
		}

		if (objective != null) {
			if (this.trackedObjectives.contains(objective)) {
				this.server.getPlayerList().broadcastAll(new ClientboundSetDisplayObjectivePacket(displaySlot, objective));
			} else {
				this.startTrackingObjective(objective);
			}
		}

		this.setDirty();
	}

	@Override
	public boolean addPlayerToTeam(String string, PlayerTeam playerTeam) {
		if (super.addPlayerToTeam(string, playerTeam)) {
			this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, string, ClientboundSetPlayerTeamPacket.Action.ADD));
			this.setDirty();
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void removePlayerFromTeam(String string, PlayerTeam playerTeam) {
		super.removePlayerFromTeam(string, playerTeam);
		this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createPlayerPacket(playerTeam, string, ClientboundSetPlayerTeamPacket.Action.REMOVE));
		this.setDirty();
	}

	@Override
	public void onObjectiveAdded(Objective objective) {
		super.onObjectiveAdded(objective);
		this.setDirty();
	}

	@Override
	public void onObjectiveChanged(Objective objective) {
		super.onObjectiveChanged(objective);
		if (this.trackedObjectives.contains(objective)) {
			this.server.getPlayerList().broadcastAll(new ClientboundSetObjectivePacket(objective, 2));
		}

		this.setDirty();
	}

	@Override
	public void onObjectiveRemoved(Objective objective) {
		super.onObjectiveRemoved(objective);
		if (this.trackedObjectives.contains(objective)) {
			this.stopTrackingObjective(objective);
		}

		this.setDirty();
	}

	@Override
	public void onTeamAdded(PlayerTeam playerTeam) {
		super.onTeamAdded(playerTeam);
		this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, true));
		this.setDirty();
	}

	@Override
	public void onTeamChanged(PlayerTeam playerTeam) {
		super.onTeamChanged(playerTeam);
		this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createAddOrModifyPacket(playerTeam, false));
		this.setDirty();
	}

	@Override
	public void onTeamRemoved(PlayerTeam playerTeam) {
		super.onTeamRemoved(playerTeam);
		this.server.getPlayerList().broadcastAll(ClientboundSetPlayerTeamPacket.createRemovePacket(playerTeam));
		this.setDirty();
	}

	public void addDirtyListener(Runnable runnable) {
		this.dirtyListeners.add(runnable);
	}

	protected void setDirty() {
		for (Runnable runnable : this.dirtyListeners) {
			runnable.run();
		}
	}

	public List<Packet<?>> getStartTrackingPackets(Objective objective) {
		List<Packet<?>> list = Lists.<Packet<?>>newArrayList();
		list.add(new ClientboundSetObjectivePacket(objective, 0));

		for (DisplaySlot displaySlot : DisplaySlot.values()) {
			if (this.getDisplayObjective(displaySlot) == objective) {
				list.add(new ClientboundSetDisplayObjectivePacket(displaySlot, objective));
			}
		}

		for (PlayerScoreEntry playerScoreEntry : this.listPlayerScores(objective)) {
			list.add(
				new ClientboundSetScorePacket(
					playerScoreEntry.owner(), objective.getName(), playerScoreEntry.value(), playerScoreEntry.display(), playerScoreEntry.numberFormatOverride()
				)
			);
		}

		return list;
	}

	public void startTrackingObjective(Objective objective) {
		List<Packet<?>> list = this.getStartTrackingPackets(objective);

		for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
			for (Packet<?> packet : list) {
				serverPlayer.connection.send(packet);
			}
		}

		this.trackedObjectives.add(objective);
	}

	public List<Packet<?>> getStopTrackingPackets(Objective objective) {
		List<Packet<?>> list = Lists.<Packet<?>>newArrayList();
		list.add(new ClientboundSetObjectivePacket(objective, 1));

		for (DisplaySlot displaySlot : DisplaySlot.values()) {
			if (this.getDisplayObjective(displaySlot) == objective) {
				list.add(new ClientboundSetDisplayObjectivePacket(displaySlot, objective));
			}
		}

		return list;
	}

	public void stopTrackingObjective(Objective objective) {
		List<Packet<?>> list = this.getStopTrackingPackets(objective);

		for (ServerPlayer serverPlayer : this.server.getPlayerList().getPlayers()) {
			for (Packet<?> packet : list) {
				serverPlayer.connection.send(packet);
			}
		}

		this.trackedObjectives.remove(objective);
	}

	public int getObjectiveDisplaySlotCount(Objective objective) {
		int i = 0;

		for (DisplaySlot displaySlot : DisplaySlot.values()) {
			if (this.getDisplayObjective(displaySlot) == objective) {
				i++;
			}
		}

		return i;
	}

	public SavedData.Factory<ScoreboardSaveData> dataFactory() {
		return new SavedData.Factory<>(this::createData, this::createData, DataFixTypes.SAVED_DATA_SCOREBOARD);
	}

	private ScoreboardSaveData createData() {
		ScoreboardSaveData scoreboardSaveData = new ScoreboardSaveData(this);
		this.addDirtyListener(scoreboardSaveData::setDirty);
		return scoreboardSaveData;
	}

	private ScoreboardSaveData createData(CompoundTag compoundTag) {
		return this.createData().load(compoundTag);
	}

	public static enum Method {
		CHANGE,
		REMOVE;
	}
}

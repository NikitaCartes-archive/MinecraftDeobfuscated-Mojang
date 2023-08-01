package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.logging.LogUtils;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.slf4j.Logger;

public class Scoreboard {
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Map<String, Objective> objectivesByName = Maps.<String, Objective>newHashMap();
	private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.<ObjectiveCriteria, List<Objective>>newHashMap();
	private final Map<String, Map<Objective, Score>> playerScores = Maps.<String, Map<Objective, Score>>newHashMap();
	private final Map<DisplaySlot, Objective> displayObjectives = new EnumMap(DisplaySlot.class);
	private final Map<String, PlayerTeam> teamsByName = Maps.<String, PlayerTeam>newHashMap();
	private final Map<String, PlayerTeam> teamsByPlayer = Maps.<String, PlayerTeam>newHashMap();

	@Nullable
	public Objective getObjective(@Nullable String string) {
		return (Objective)this.objectivesByName.get(string);
	}

	public Objective addObjective(String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType) {
		if (this.objectivesByName.containsKey(string)) {
			throw new IllegalArgumentException("An objective with the name '" + string + "' already exists!");
		} else {
			Objective objective = new Objective(this, string, objectiveCriteria, component, renderType);
			((List)this.objectivesByCriteria.computeIfAbsent(objectiveCriteria, objectiveCriteriax -> Lists.newArrayList())).add(objective);
			this.objectivesByName.put(string, objective);
			this.onObjectiveAdded(objective);
			return objective;
		}
	}

	public final void forAllObjectives(ObjectiveCriteria objectiveCriteria, String string, Consumer<Score> consumer) {
		((List)this.objectivesByCriteria.getOrDefault(objectiveCriteria, Collections.emptyList()))
			.forEach(objective -> consumer.accept(this.getOrCreatePlayerScore(string, objective)));
	}

	public boolean hasPlayerScore(String string, Objective objective) {
		Map<Objective, Score> map = (Map<Objective, Score>)this.playerScores.get(string);
		if (map == null) {
			return false;
		} else {
			Score score = (Score)map.get(objective);
			return score != null;
		}
	}

	public Score getOrCreatePlayerScore(String string, Objective objective) {
		Map<Objective, Score> map = (Map<Objective, Score>)this.playerScores.computeIfAbsent(string, stringx -> Maps.newHashMap());
		return (Score)map.computeIfAbsent(objective, objectivex -> {
			Score score = new Score(this, objectivex, string);
			score.setScore(0);
			return score;
		});
	}

	public Collection<Score> getPlayerScores(Objective objective) {
		List<Score> list = Lists.<Score>newArrayList();

		for (Map<Objective, Score> map : this.playerScores.values()) {
			Score score = (Score)map.get(objective);
			if (score != null) {
				list.add(score);
			}
		}

		list.sort(Score.SCORE_COMPARATOR);
		return list;
	}

	public Collection<Objective> getObjectives() {
		return this.objectivesByName.values();
	}

	public Collection<String> getObjectiveNames() {
		return this.objectivesByName.keySet();
	}

	public Collection<String> getTrackedPlayers() {
		return Lists.<String>newArrayList(this.playerScores.keySet());
	}

	public void resetPlayerScore(String string, @Nullable Objective objective) {
		if (objective == null) {
			Map<Objective, Score> map = (Map<Objective, Score>)this.playerScores.remove(string);
			if (map != null) {
				this.onPlayerRemoved(string);
			}
		} else {
			Map<Objective, Score> map = (Map<Objective, Score>)this.playerScores.get(string);
			if (map != null) {
				Score score = (Score)map.remove(objective);
				if (map.size() < 1) {
					Map<Objective, Score> map2 = (Map<Objective, Score>)this.playerScores.remove(string);
					if (map2 != null) {
						this.onPlayerRemoved(string);
					}
				} else if (score != null) {
					this.onPlayerScoreRemoved(string, objective);
				}
			}
		}
	}

	public Map<Objective, Score> getPlayerScores(String string) {
		Map<Objective, Score> map = (Map<Objective, Score>)this.playerScores.get(string);
		if (map == null) {
			map = Maps.<Objective, Score>newHashMap();
		}

		return map;
	}

	public void removeObjective(Objective objective) {
		this.objectivesByName.remove(objective.getName());

		for (DisplaySlot displaySlot : DisplaySlot.values()) {
			if (this.getDisplayObjective(displaySlot) == objective) {
				this.setDisplayObjective(displaySlot, null);
			}
		}

		List<Objective> list = (List<Objective>)this.objectivesByCriteria.get(objective.getCriteria());
		if (list != null) {
			list.remove(objective);
		}

		for (Map<Objective, Score> map : this.playerScores.values()) {
			map.remove(objective);
		}

		this.onObjectiveRemoved(objective);
	}

	public void setDisplayObjective(DisplaySlot displaySlot, @Nullable Objective objective) {
		this.displayObjectives.put(displaySlot, objective);
	}

	@Nullable
	public Objective getDisplayObjective(DisplaySlot displaySlot) {
		return (Objective)this.displayObjectives.get(displaySlot);
	}

	@Nullable
	public PlayerTeam getPlayerTeam(String string) {
		return (PlayerTeam)this.teamsByName.get(string);
	}

	public PlayerTeam addPlayerTeam(String string) {
		PlayerTeam playerTeam = this.getPlayerTeam(string);
		if (playerTeam != null) {
			LOGGER.warn("Requested creation of existing team '{}'", string);
			return playerTeam;
		} else {
			playerTeam = new PlayerTeam(this, string);
			this.teamsByName.put(string, playerTeam);
			this.onTeamAdded(playerTeam);
			return playerTeam;
		}
	}

	public void removePlayerTeam(PlayerTeam playerTeam) {
		this.teamsByName.remove(playerTeam.getName());

		for (String string : playerTeam.getPlayers()) {
			this.teamsByPlayer.remove(string);
		}

		this.onTeamRemoved(playerTeam);
	}

	public boolean addPlayerToTeam(String string, PlayerTeam playerTeam) {
		if (this.getPlayersTeam(string) != null) {
			this.removePlayerFromTeam(string);
		}

		this.teamsByPlayer.put(string, playerTeam);
		return playerTeam.getPlayers().add(string);
	}

	public boolean removePlayerFromTeam(String string) {
		PlayerTeam playerTeam = this.getPlayersTeam(string);
		if (playerTeam != null) {
			this.removePlayerFromTeam(string, playerTeam);
			return true;
		} else {
			return false;
		}
	}

	public void removePlayerFromTeam(String string, PlayerTeam playerTeam) {
		if (this.getPlayersTeam(string) != playerTeam) {
			throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
		} else {
			this.teamsByPlayer.remove(string);
			playerTeam.getPlayers().remove(string);
		}
	}

	public Collection<String> getTeamNames() {
		return this.teamsByName.keySet();
	}

	public Collection<PlayerTeam> getPlayerTeams() {
		return this.teamsByName.values();
	}

	@Nullable
	public PlayerTeam getPlayersTeam(String string) {
		return (PlayerTeam)this.teamsByPlayer.get(string);
	}

	public void onObjectiveAdded(Objective objective) {
	}

	public void onObjectiveChanged(Objective objective) {
	}

	public void onObjectiveRemoved(Objective objective) {
	}

	public void onScoreChanged(Score score) {
	}

	public void onPlayerRemoved(String string) {
	}

	public void onPlayerScoreRemoved(String string, Objective objective) {
	}

	public void onTeamAdded(PlayerTeam playerTeam) {
	}

	public void onTeamChanged(PlayerTeam playerTeam) {
	}

	public void onTeamRemoved(PlayerTeam playerTeam) {
	}

	public void entityRemoved(Entity entity) {
		if (!(entity instanceof Player) && !entity.isAlive()) {
			String string = entity.getStringUUID();
			this.resetPlayerScore(string, null);
			this.removePlayerFromTeam(string);
		}
	}

	protected ListTag savePlayerScores() {
		ListTag listTag = new ListTag();
		this.playerScores.values().stream().map(Map::values).forEach(collection -> collection.forEach(score -> {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putString("Name", score.getOwner());
				compoundTag.putString("Objective", score.getObjective().getName());
				compoundTag.putInt("Score", score.getScore());
				compoundTag.putBoolean("Locked", score.isLocked());
				listTag.add(compoundTag);
			}));
		return listTag;
	}

	protected void loadPlayerScores(ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			String string = compoundTag.getString("Name");
			String string2 = compoundTag.getString("Objective");
			Objective objective = this.getObjective(string2);
			if (objective == null) {
				LOGGER.error("Unknown objective {} for name {}, ignoring", string2, string);
			} else {
				Score score = this.getOrCreatePlayerScore(string, objective);
				score.setScore(compoundTag.getInt("Score"));
				if (compoundTag.contains("Locked")) {
					score.setLocked(compoundTag.getBoolean("Locked"));
				}
			}
		}
	}
}

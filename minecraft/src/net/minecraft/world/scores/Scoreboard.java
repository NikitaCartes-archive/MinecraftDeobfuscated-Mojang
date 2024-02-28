package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.slf4j.Logger;

public class Scoreboard {
	public static final String HIDDEN_SCORE_PREFIX = "#";
	private static final Logger LOGGER = LogUtils.getLogger();
	private final Object2ObjectMap<String, Objective> objectivesByName = new Object2ObjectOpenHashMap<>(16, 0.5F);
	private final Reference2ObjectMap<ObjectiveCriteria, List<Objective>> objectivesByCriteria = new Reference2ObjectOpenHashMap<>();
	private final Map<String, PlayerScores> playerScores = new Object2ObjectOpenHashMap<>(16, 0.5F);
	private final Map<DisplaySlot, Objective> displayObjectives = new EnumMap(DisplaySlot.class);
	private final Object2ObjectMap<String, PlayerTeam> teamsByName = new Object2ObjectOpenHashMap<>();
	private final Object2ObjectMap<String, PlayerTeam> teamsByPlayer = new Object2ObjectOpenHashMap<>();

	@Nullable
	public Objective getObjective(@Nullable String string) {
		return this.objectivesByName.get(string);
	}

	public Objective addObjective(
		String string,
		ObjectiveCriteria objectiveCriteria,
		Component component,
		ObjectiveCriteria.RenderType renderType,
		boolean bl,
		@Nullable NumberFormat numberFormat
	) {
		if (this.objectivesByName.containsKey(string)) {
			throw new IllegalArgumentException("An objective with the name '" + string + "' already exists!");
		} else {
			Objective objective = new Objective(this, string, objectiveCriteria, component, renderType, bl, numberFormat);
			this.objectivesByCriteria.computeIfAbsent(objectiveCriteria, object -> Lists.<Objective>newArrayList()).add(objective);
			this.objectivesByName.put(string, objective);
			this.onObjectiveAdded(objective);
			return objective;
		}
	}

	public final void forAllObjectives(ObjectiveCriteria objectiveCriteria, ScoreHolder scoreHolder, Consumer<ScoreAccess> consumer) {
		this.objectivesByCriteria
			.getOrDefault(objectiveCriteria, Collections.emptyList())
			.forEach(objective -> consumer.accept(this.getOrCreatePlayerScore(scoreHolder, objective, true)));
	}

	private PlayerScores getOrCreatePlayerInfo(String string) {
		return (PlayerScores)this.playerScores.computeIfAbsent(string, stringx -> new PlayerScores());
	}

	public ScoreAccess getOrCreatePlayerScore(ScoreHolder scoreHolder, Objective objective) {
		return this.getOrCreatePlayerScore(scoreHolder, objective, false);
	}

	public ScoreAccess getOrCreatePlayerScore(ScoreHolder scoreHolder, Objective objective, boolean bl) {
		final boolean bl2 = bl || !objective.getCriteria().isReadOnly();
		PlayerScores playerScores = this.getOrCreatePlayerInfo(scoreHolder.getScoreboardName());
		final MutableBoolean mutableBoolean = new MutableBoolean();
		final Score score = playerScores.getOrCreate(objective, scorex -> mutableBoolean.setTrue());
		return new ScoreAccess() {
			@Override
			public int get() {
				return score.value();
			}

			@Override
			public void set(int i) {
				if (!bl2) {
					throw new IllegalStateException("Cannot modify read-only score");
				} else {
					boolean bl = mutableBoolean.isTrue();
					if (objective.displayAutoUpdate()) {
						Component component = scoreHolder.getDisplayName();
						if (component != null && !component.equals(score.display())) {
							score.display(component);
							bl = true;
						}
					}

					if (i != score.value()) {
						score.value(i);
						bl = true;
					}

					if (bl) {
						this.sendScoreToPlayers();
					}
				}
			}

			@Nullable
			@Override
			public Component display() {
				return score.display();
			}

			@Override
			public void display(@Nullable Component component) {
				if (mutableBoolean.isTrue() || !Objects.equals(component, score.display())) {
					score.display(component);
					this.sendScoreToPlayers();
				}
			}

			@Override
			public void numberFormatOverride(@Nullable NumberFormat numberFormat) {
				score.numberFormat(numberFormat);
				this.sendScoreToPlayers();
			}

			@Override
			public boolean locked() {
				return score.isLocked();
			}

			@Override
			public void unlock() {
				this.setLocked(false);
			}

			@Override
			public void lock() {
				this.setLocked(true);
			}

			private void setLocked(boolean bl) {
				score.setLocked(bl);
				if (mutableBoolean.isTrue()) {
					this.sendScoreToPlayers();
				}

				Scoreboard.this.onScoreLockChanged(scoreHolder, objective);
			}

			private void sendScoreToPlayers() {
				Scoreboard.this.onScoreChanged(scoreHolder, objective, score);
				mutableBoolean.setFalse();
			}
		};
	}

	@Nullable
	public ReadOnlyScoreInfo getPlayerScoreInfo(ScoreHolder scoreHolder, Objective objective) {
		PlayerScores playerScores = (PlayerScores)this.playerScores.get(scoreHolder.getScoreboardName());
		return playerScores != null ? playerScores.get(objective) : null;
	}

	public Collection<PlayerScoreEntry> listPlayerScores(Objective objective) {
		List<PlayerScoreEntry> list = new ArrayList();
		this.playerScores.forEach((string, playerScores) -> {
			Score score = playerScores.get(objective);
			if (score != null) {
				list.add(new PlayerScoreEntry(string, score.value(), score.display(), score.numberFormat()));
			}
		});
		return list;
	}

	public Collection<Objective> getObjectives() {
		return this.objectivesByName.values();
	}

	public Collection<String> getObjectiveNames() {
		return this.objectivesByName.keySet();
	}

	public Collection<ScoreHolder> getTrackedPlayers() {
		return this.playerScores.keySet().stream().map(ScoreHolder::forNameOnly).toList();
	}

	public void resetAllPlayerScores(ScoreHolder scoreHolder) {
		PlayerScores playerScores = (PlayerScores)this.playerScores.remove(scoreHolder.getScoreboardName());
		if (playerScores != null) {
			this.onPlayerRemoved(scoreHolder);
		}
	}

	public void resetSinglePlayerScore(ScoreHolder scoreHolder, Objective objective) {
		PlayerScores playerScores = (PlayerScores)this.playerScores.get(scoreHolder.getScoreboardName());
		if (playerScores != null) {
			boolean bl = playerScores.remove(objective);
			if (!playerScores.hasScores()) {
				PlayerScores playerScores2 = (PlayerScores)this.playerScores.remove(scoreHolder.getScoreboardName());
				if (playerScores2 != null) {
					this.onPlayerRemoved(scoreHolder);
				}
			} else if (bl) {
				this.onPlayerScoreRemoved(scoreHolder, objective);
			}
		}
	}

	public Object2IntMap<Objective> listPlayerScores(ScoreHolder scoreHolder) {
		PlayerScores playerScores = (PlayerScores)this.playerScores.get(scoreHolder.getScoreboardName());
		return playerScores != null ? playerScores.listScores() : Object2IntMaps.emptyMap();
	}

	public void removeObjective(Objective objective) {
		this.objectivesByName.remove(objective.getName());

		for (DisplaySlot displaySlot : DisplaySlot.values()) {
			if (this.getDisplayObjective(displaySlot) == objective) {
				this.setDisplayObjective(displaySlot, null);
			}
		}

		List<Objective> list = this.objectivesByCriteria.get(objective.getCriteria());
		if (list != null) {
			list.remove(objective);
		}

		for (PlayerScores playerScores : this.playerScores.values()) {
			playerScores.remove(objective);
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
		return this.teamsByName.get(string);
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
		return this.teamsByPlayer.get(string);
	}

	public void onObjectiveAdded(Objective objective) {
	}

	public void onObjectiveChanged(Objective objective) {
	}

	public void onObjectiveRemoved(Objective objective) {
	}

	protected void onScoreChanged(ScoreHolder scoreHolder, Objective objective, Score score) {
	}

	protected void onScoreLockChanged(ScoreHolder scoreHolder, Objective objective) {
	}

	public void onPlayerRemoved(ScoreHolder scoreHolder) {
	}

	public void onPlayerScoreRemoved(ScoreHolder scoreHolder, Objective objective) {
	}

	public void onTeamAdded(PlayerTeam playerTeam) {
	}

	public void onTeamChanged(PlayerTeam playerTeam) {
	}

	public void onTeamRemoved(PlayerTeam playerTeam) {
	}

	public void entityRemoved(Entity entity) {
		if (!(entity instanceof Player) && !entity.isAlive()) {
			this.resetAllPlayerScores(entity);
			this.removePlayerFromTeam(entity.getScoreboardName());
		}
	}

	protected ListTag savePlayerScores(HolderLookup.Provider provider) {
		ListTag listTag = new ListTag();
		this.playerScores.forEach((string, playerScores) -> playerScores.listRawScores().forEach((objective, score) -> {
				CompoundTag compoundTag = score.write(provider);
				compoundTag.putString("Name", string);
				compoundTag.putString("Objective", objective.getName());
				listTag.add(compoundTag);
			}));
		return listTag;
	}

	protected void loadPlayerScores(ListTag listTag, HolderLookup.Provider provider) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			Score score = Score.read(compoundTag, provider);
			String string = compoundTag.getString("Name");
			String string2 = compoundTag.getString("Objective");
			Objective objective = this.getObjective(string2);
			if (objective == null) {
				LOGGER.error("Unknown objective {} for name {}, ignoring", string2, string);
			} else {
				this.getOrCreatePlayerInfo(string).setScore(objective, score);
			}
		}
	}
}

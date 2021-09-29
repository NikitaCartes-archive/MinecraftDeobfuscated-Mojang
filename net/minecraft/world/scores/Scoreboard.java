/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.scores;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;
import org.jetbrains.annotations.Nullable;

public class Scoreboard {
    public static final int DISPLAY_SLOT_LIST = 0;
    public static final int DISPLAY_SLOT_SIDEBAR = 1;
    public static final int DISPLAY_SLOT_BELOW_NAME = 2;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_START = 3;
    public static final int DISPLAY_SLOT_TEAMS_SIDEBAR_END = 18;
    public static final int DISPLAY_SLOTS = 19;
    private final Map<String, Objective> objectivesByName = Maps.newHashMap();
    private final Map<ObjectiveCriteria, List<Objective>> objectivesByCriteria = Maps.newHashMap();
    private final Map<String, Map<Objective, Score>> playerScores = Maps.newHashMap();
    private final Objective[] displayObjectives = new Objective[19];
    private final Map<String, PlayerTeam> teamsByName = Maps.newHashMap();
    private final Map<String, PlayerTeam> teamsByPlayer = Maps.newHashMap();
    @Nullable
    private static String[] displaySlotNames;

    public boolean hasObjective(String string) {
        return this.objectivesByName.containsKey(string);
    }

    public Objective getOrCreateObjective(String string) {
        return this.objectivesByName.get(string);
    }

    @Nullable
    public Objective getObjective(@Nullable String string) {
        return this.objectivesByName.get(string);
    }

    public Objective addObjective(String string, ObjectiveCriteria objectiveCriteria2, Component component, ObjectiveCriteria.RenderType renderType) {
        if (this.objectivesByName.containsKey(string)) {
            throw new IllegalArgumentException("An objective with the name '" + string + "' already exists!");
        }
        Objective objective = new Objective(this, string, objectiveCriteria2, component, renderType);
        this.objectivesByCriteria.computeIfAbsent(objectiveCriteria2, objectiveCriteria -> Lists.newArrayList()).add(objective);
        this.objectivesByName.put(string, objective);
        this.onObjectiveAdded(objective);
        return objective;
    }

    public final void forAllObjectives(ObjectiveCriteria objectiveCriteria, String string, Consumer<Score> consumer) {
        this.objectivesByCriteria.getOrDefault(objectiveCriteria, Collections.emptyList()).forEach(objective -> consumer.accept(this.getOrCreatePlayerScore(string, (Objective)objective)));
    }

    public boolean hasPlayerScore(String string, Objective objective) {
        Map<Objective, Score> map = this.playerScores.get(string);
        if (map == null) {
            return false;
        }
        Score score = map.get(objective);
        return score != null;
    }

    public Score getOrCreatePlayerScore(String string2, Objective objective2) {
        Map map = this.playerScores.computeIfAbsent(string2, string -> Maps.newHashMap());
        return map.computeIfAbsent(objective2, objective -> {
            Score score = new Score(this, (Objective)objective, string2);
            score.setScore(0);
            return score;
        });
    }

    public Collection<Score> getPlayerScores(Objective objective) {
        ArrayList<Score> list = Lists.newArrayList();
        for (Map<Objective, Score> map : this.playerScores.values()) {
            Score score = map.get(objective);
            if (score == null) continue;
            list.add(score);
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
        return Lists.newArrayList(this.playerScores.keySet());
    }

    public void resetPlayerScore(String string, @Nullable Objective objective) {
        if (objective == null) {
            Map<Objective, Score> map = this.playerScores.remove(string);
            if (map != null) {
                this.onPlayerRemoved(string);
            }
        } else {
            Map<Objective, Score> map = this.playerScores.get(string);
            if (map != null) {
                Score score = map.remove(objective);
                if (map.size() < 1) {
                    Map<Objective, Score> map2 = this.playerScores.remove(string);
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
        Map<Objective, Score> map = this.playerScores.get(string);
        if (map == null) {
            map = Maps.newHashMap();
        }
        return map;
    }

    public void removeObjective(Objective objective) {
        this.objectivesByName.remove(objective.getName());
        for (int i = 0; i < 19; ++i) {
            if (this.getDisplayObjective(i) != objective) continue;
            this.setDisplayObjective(i, null);
        }
        List<Objective> list = this.objectivesByCriteria.get(objective.getCriteria());
        if (list != null) {
            list.remove(objective);
        }
        for (Map<Objective, Score> map : this.playerScores.values()) {
            map.remove(objective);
        }
        this.onObjectiveRemoved(objective);
    }

    public void setDisplayObjective(int i, @Nullable Objective objective) {
        this.displayObjectives[i] = objective;
    }

    @Nullable
    public Objective getDisplayObjective(int i) {
        return this.displayObjectives[i];
    }

    @Nullable
    public PlayerTeam getPlayerTeam(String string) {
        return this.teamsByName.get(string);
    }

    public PlayerTeam addPlayerTeam(String string) {
        PlayerTeam playerTeam = this.getPlayerTeam(string);
        if (playerTeam != null) {
            throw new IllegalArgumentException("A team with the name '" + string + "' already exists!");
        }
        playerTeam = new PlayerTeam(this, string);
        this.teamsByName.put(string, playerTeam);
        this.onTeamAdded(playerTeam);
        return playerTeam;
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
        }
        return false;
    }

    public void removePlayerFromTeam(String string, PlayerTeam playerTeam) {
        if (this.getPlayersTeam(string) != playerTeam) {
            throw new IllegalStateException("Player is either on another team or not on any team. Cannot remove from team '" + playerTeam.getName() + "'.");
        }
        this.teamsByPlayer.remove(string);
        playerTeam.getPlayers().remove(string);
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

    public static String getDisplaySlotName(int i) {
        ChatFormatting chatFormatting;
        switch (i) {
            case 0: {
                return "list";
            }
            case 1: {
                return "sidebar";
            }
            case 2: {
                return "belowName";
            }
        }
        if (i >= 3 && i <= 18 && (chatFormatting = ChatFormatting.getById(i - 3)) != null && chatFormatting != ChatFormatting.RESET) {
            return "sidebar.team." + chatFormatting.getName();
        }
        return null;
    }

    public static int getDisplaySlotByName(String string) {
        String string2;
        ChatFormatting chatFormatting;
        if ("list".equalsIgnoreCase(string)) {
            return 0;
        }
        if ("sidebar".equalsIgnoreCase(string)) {
            return 1;
        }
        if ("belowName".equalsIgnoreCase(string)) {
            return 2;
        }
        if (string.startsWith("sidebar.team.") && (chatFormatting = ChatFormatting.getByName(string2 = string.substring("sidebar.team.".length()))) != null && chatFormatting.getId() >= 0) {
            return chatFormatting.getId() + 3;
        }
        return -1;
    }

    public static String[] getDisplaySlotNames() {
        if (displaySlotNames == null) {
            displaySlotNames = new String[19];
            for (int i = 0; i < 19; ++i) {
                Scoreboard.displaySlotNames[i] = Scoreboard.getDisplaySlotName(i);
            }
        }
        return displaySlotNames;
    }

    public void entityRemoved(Entity entity) {
        if (entity == null || entity instanceof Player || entity.isAlive()) {
            return;
        }
        String string = entity.getStringUUID();
        this.resetPlayerScore(string, null);
        this.removePlayerFromTeam(string);
    }

    protected ListTag savePlayerScores() {
        ListTag listTag = new ListTag();
        this.playerScores.values().stream().map(Map::values).forEach(collection -> collection.stream().filter(score -> score.getObjective() != null).forEach(score -> {
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
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            Objective objective = this.getOrCreateObjective(compoundTag.getString("Objective"));
            String string = compoundTag.getString("Name");
            Score score = this.getOrCreatePlayerScore(string, objective);
            score.setScore(compoundTag.getInt("Score"));
            if (!compoundTag.contains("Locked")) continue;
            score.setLocked(compoundTag.getBoolean("Locked"));
        }
    }
}


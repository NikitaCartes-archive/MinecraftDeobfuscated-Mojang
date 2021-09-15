/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.scores;

import java.util.Collection;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.minecraft.world.scores.Team;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardSaveData
extends SavedData {
    public static final String FILE_ID = "scoreboard";
    private final Scoreboard scoreboard;

    public ScoreboardSaveData(Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public ScoreboardSaveData load(CompoundTag compoundTag) {
        this.loadObjectives(compoundTag.getList("Objectives", 10));
        this.scoreboard.loadPlayerScores(compoundTag.getList("PlayerScores", 10));
        if (compoundTag.contains("DisplaySlots", 10)) {
            this.loadDisplaySlots(compoundTag.getCompound("DisplaySlots"));
        }
        if (compoundTag.contains("Teams", 9)) {
            this.loadTeams(compoundTag.getList("Teams", 10));
        }
        return this;
    }

    private void loadTeams(ListTag listTag) {
        for (int i = 0; i < listTag.size(); ++i) {
            Team.CollisionRule collisionRule;
            Team.Visibility visibility;
            MutableComponent component2;
            CompoundTag compoundTag = listTag.getCompound(i);
            String string = compoundTag.getString("Name");
            PlayerTeam playerTeam = this.scoreboard.addPlayerTeam(string);
            MutableComponent component = Component.Serializer.fromJson(compoundTag.getString("DisplayName"));
            if (component != null) {
                playerTeam.setDisplayName(component);
            }
            if (compoundTag.contains("TeamColor", 8)) {
                playerTeam.setColor(ChatFormatting.getByName(compoundTag.getString("TeamColor")));
            }
            if (compoundTag.contains("AllowFriendlyFire", 99)) {
                playerTeam.setAllowFriendlyFire(compoundTag.getBoolean("AllowFriendlyFire"));
            }
            if (compoundTag.contains("SeeFriendlyInvisibles", 99)) {
                playerTeam.setSeeFriendlyInvisibles(compoundTag.getBoolean("SeeFriendlyInvisibles"));
            }
            if (compoundTag.contains("MemberNamePrefix", 8) && (component2 = Component.Serializer.fromJson(compoundTag.getString("MemberNamePrefix"))) != null) {
                playerTeam.setPlayerPrefix(component2);
            }
            if (compoundTag.contains("MemberNameSuffix", 8) && (component2 = Component.Serializer.fromJson(compoundTag.getString("MemberNameSuffix"))) != null) {
                playerTeam.setPlayerSuffix(component2);
            }
            if (compoundTag.contains("NameTagVisibility", 8) && (visibility = Team.Visibility.byName(compoundTag.getString("NameTagVisibility"))) != null) {
                playerTeam.setNameTagVisibility(visibility);
            }
            if (compoundTag.contains("DeathMessageVisibility", 8) && (visibility = Team.Visibility.byName(compoundTag.getString("DeathMessageVisibility"))) != null) {
                playerTeam.setDeathMessageVisibility(visibility);
            }
            if (compoundTag.contains("CollisionRule", 8) && (collisionRule = Team.CollisionRule.byName(compoundTag.getString("CollisionRule"))) != null) {
                playerTeam.setCollisionRule(collisionRule);
            }
            this.loadTeamPlayers(playerTeam, compoundTag.getList("Players", 8));
        }
    }

    private void loadTeamPlayers(PlayerTeam playerTeam, ListTag listTag) {
        for (int i = 0; i < listTag.size(); ++i) {
            this.scoreboard.addPlayerToTeam(listTag.getString(i), playerTeam);
        }
    }

    private void loadDisplaySlots(CompoundTag compoundTag) {
        for (int i = 0; i < 19; ++i) {
            if (!compoundTag.contains("slot_" + i, 8)) continue;
            String string = compoundTag.getString("slot_" + i);
            Objective objective = this.scoreboard.getObjective(string);
            this.scoreboard.setDisplayObjective(i, objective);
        }
    }

    private void loadObjectives(ListTag listTag) {
        for (int i = 0; i < listTag.size(); ++i) {
            CompoundTag compoundTag = listTag.getCompound(i);
            ObjectiveCriteria.byName(compoundTag.getString("CriteriaName")).ifPresent(objectiveCriteria -> {
                String string = compoundTag.getString("Name");
                MutableComponent component = Component.Serializer.fromJson(compoundTag.getString("DisplayName"));
                ObjectiveCriteria.RenderType renderType = ObjectiveCriteria.RenderType.byId(compoundTag.getString("RenderType"));
                this.scoreboard.addObjective(string, (ObjectiveCriteria)objectiveCriteria, component, renderType);
            });
        }
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag) {
        compoundTag.put("Objectives", this.saveObjectives());
        compoundTag.put("PlayerScores", this.scoreboard.savePlayerScores());
        compoundTag.put("Teams", this.saveTeams());
        this.saveDisplaySlots(compoundTag);
        return compoundTag;
    }

    private ListTag saveTeams() {
        ListTag listTag = new ListTag();
        Collection<PlayerTeam> collection = this.scoreboard.getPlayerTeams();
        for (PlayerTeam playerTeam : collection) {
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Name", playerTeam.getName());
            compoundTag.putString("DisplayName", Component.Serializer.toJson(playerTeam.getDisplayName()));
            if (playerTeam.getColor().getId() >= 0) {
                compoundTag.putString("TeamColor", playerTeam.getColor().getName());
            }
            compoundTag.putBoolean("AllowFriendlyFire", playerTeam.isAllowFriendlyFire());
            compoundTag.putBoolean("SeeFriendlyInvisibles", playerTeam.canSeeFriendlyInvisibles());
            compoundTag.putString("MemberNamePrefix", Component.Serializer.toJson(playerTeam.getPlayerPrefix()));
            compoundTag.putString("MemberNameSuffix", Component.Serializer.toJson(playerTeam.getPlayerSuffix()));
            compoundTag.putString("NameTagVisibility", playerTeam.getNameTagVisibility().name);
            compoundTag.putString("DeathMessageVisibility", playerTeam.getDeathMessageVisibility().name);
            compoundTag.putString("CollisionRule", playerTeam.getCollisionRule().name);
            ListTag listTag2 = new ListTag();
            for (String string : playerTeam.getPlayers()) {
                listTag2.add(StringTag.valueOf(string));
            }
            compoundTag.put("Players", listTag2);
            listTag.add(compoundTag);
        }
        return listTag;
    }

    private void saveDisplaySlots(CompoundTag compoundTag) {
        CompoundTag compoundTag2 = new CompoundTag();
        boolean bl = false;
        for (int i = 0; i < 19; ++i) {
            Objective objective = this.scoreboard.getDisplayObjective(i);
            if (objective == null) continue;
            compoundTag2.putString("slot_" + i, objective.getName());
            bl = true;
        }
        if (bl) {
            compoundTag.put("DisplaySlots", compoundTag2);
        }
    }

    private ListTag saveObjectives() {
        ListTag listTag = new ListTag();
        Collection<Objective> collection = this.scoreboard.getObjectives();
        for (Objective objective : collection) {
            if (objective.getCriteria() == null) continue;
            CompoundTag compoundTag = new CompoundTag();
            compoundTag.putString("Name", objective.getName());
            compoundTag.putString("CriteriaName", objective.getCriteria().getName());
            compoundTag.putString("DisplayName", Component.Serializer.toJson(objective.getDisplayName()));
            compoundTag.putString("RenderType", objective.getRenderType().getId());
            listTag.add(compoundTag);
        }
        return listTag;
    }
}


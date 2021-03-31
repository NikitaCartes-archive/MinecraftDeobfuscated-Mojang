package net.minecraft.world.scores;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class ScoreboardSaveData extends SavedData {
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
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			String string = compoundTag.getString("Name");
			if (string.length() > 16) {
				string = string.substring(0, 16);
			}

			PlayerTeam playerTeam = this.scoreboard.addPlayerTeam(string);
			Component component = Component.Serializer.fromJson(compoundTag.getString("DisplayName"));
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

			if (compoundTag.contains("MemberNamePrefix", 8)) {
				Component component2 = Component.Serializer.fromJson(compoundTag.getString("MemberNamePrefix"));
				if (component2 != null) {
					playerTeam.setPlayerPrefix(component2);
				}
			}

			if (compoundTag.contains("MemberNameSuffix", 8)) {
				Component component2 = Component.Serializer.fromJson(compoundTag.getString("MemberNameSuffix"));
				if (component2 != null) {
					playerTeam.setPlayerSuffix(component2);
				}
			}

			if (compoundTag.contains("NameTagVisibility", 8)) {
				Team.Visibility visibility = Team.Visibility.byName(compoundTag.getString("NameTagVisibility"));
				if (visibility != null) {
					playerTeam.setNameTagVisibility(visibility);
				}
			}

			if (compoundTag.contains("DeathMessageVisibility", 8)) {
				Team.Visibility visibility = Team.Visibility.byName(compoundTag.getString("DeathMessageVisibility"));
				if (visibility != null) {
					playerTeam.setDeathMessageVisibility(visibility);
				}
			}

			if (compoundTag.contains("CollisionRule", 8)) {
				Team.CollisionRule collisionRule = Team.CollisionRule.byName(compoundTag.getString("CollisionRule"));
				if (collisionRule != null) {
					playerTeam.setCollisionRule(collisionRule);
				}
			}

			this.loadTeamPlayers(playerTeam, compoundTag.getList("Players", 8));
		}
	}

	private void loadTeamPlayers(PlayerTeam playerTeam, ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			this.scoreboard.addPlayerToTeam(listTag.getString(i), playerTeam);
		}
	}

	private void loadDisplaySlots(CompoundTag compoundTag) {
		for (int i = 0; i < 19; i++) {
			if (compoundTag.contains("slot_" + i, 8)) {
				String string = compoundTag.getString("slot_" + i);
				Objective objective = this.scoreboard.getObjective(string);
				this.scoreboard.setDisplayObjective(i, objective);
			}
		}
	}

	private void loadObjectives(ListTag listTag) {
		for (int i = 0; i < listTag.size(); i++) {
			CompoundTag compoundTag = listTag.getCompound(i);
			ObjectiveCriteria.byName(compoundTag.getString("CriteriaName")).ifPresent(objectiveCriteria -> {
				String string = compoundTag.getString("Name");
				if (string.length() > 16) {
					string = string.substring(0, 16);
				}

				Component component = Component.Serializer.fromJson(compoundTag.getString("DisplayName"));
				ObjectiveCriteria.RenderType renderType = ObjectiveCriteria.RenderType.byId(compoundTag.getString("RenderType"));
				this.scoreboard.addObjective(string, objectiveCriteria, component, renderType);
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

		for (PlayerTeam playerTeam : this.scoreboard.getPlayerTeams()) {
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

		for (int i = 0; i < 19; i++) {
			Objective objective = this.scoreboard.getDisplayObjective(i);
			if (objective != null) {
				compoundTag2.putString("slot_" + i, objective.getName());
				bl = true;
			}
		}

		if (bl) {
			compoundTag.put("DisplaySlots", compoundTag2);
		}
	}

	private ListTag saveObjectives() {
		ListTag listTag = new ListTag();

		for (Objective objective : this.scoreboard.getObjectives()) {
			if (objective.getCriteria() != null) {
				CompoundTag compoundTag = new CompoundTag();
				compoundTag.putString("Name", objective.getName());
				compoundTag.putString("CriteriaName", objective.getCriteria().getName());
				compoundTag.putString("DisplayName", Component.Serializer.toJson(objective.getDisplayName()));
				compoundTag.putString("RenderType", objective.getRenderType().getId());
				listTag.add(compoundTag);
			}
		}

		return listTag;
	}
}

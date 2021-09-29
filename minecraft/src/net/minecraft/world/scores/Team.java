package net.minecraft.world.scores;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class Team {
	public boolean isAlliedTo(@Nullable Team team) {
		return team == null ? false : this == team;
	}

	public abstract String getName();

	public abstract MutableComponent getFormattedName(Component component);

	public abstract boolean canSeeFriendlyInvisibles();

	public abstract boolean isAllowFriendlyFire();

	public abstract Team.Visibility getNameTagVisibility();

	public abstract ChatFormatting getColor();

	public abstract Collection<String> getPlayers();

	public abstract Team.Visibility getDeathMessageVisibility();

	public abstract Team.CollisionRule getCollisionRule();

	public static enum CollisionRule {
		ALWAYS("always", 0),
		NEVER("never", 1),
		PUSH_OTHER_TEAMS("pushOtherTeams", 2),
		PUSH_OWN_TEAM("pushOwnTeam", 3);

		private static final Map<String, Team.CollisionRule> BY_NAME = (Map<String, Team.CollisionRule>)Arrays.stream(values())
			.collect(Collectors.toMap(collisionRule -> collisionRule.name, collisionRule -> collisionRule));
		public final String name;
		public final int id;

		@Nullable
		public static Team.CollisionRule byName(String string) {
			return (Team.CollisionRule)BY_NAME.get(string);
		}

		private CollisionRule(String string2, int j) {
			this.name = string2;
			this.id = j;
		}

		public Component getDisplayName() {
			return new TranslatableComponent("team.collision." + this.name);
		}
	}

	public static enum Visibility {
		ALWAYS("always", 0),
		NEVER("never", 1),
		HIDE_FOR_OTHER_TEAMS("hideForOtherTeams", 2),
		HIDE_FOR_OWN_TEAM("hideForOwnTeam", 3);

		private static final Map<String, Team.Visibility> BY_NAME = (Map<String, Team.Visibility>)Arrays.stream(values())
			.collect(Collectors.toMap(visibility -> visibility.name, visibility -> visibility));
		public final String name;
		public final int id;

		public static String[] getAllNames() {
			return (String[])BY_NAME.keySet().toArray(new String[0]);
		}

		@Nullable
		public static Team.Visibility byName(String string) {
			return (Team.Visibility)BY_NAME.get(string);
		}

		private Visibility(String string2, int j) {
			this.name = string2;
			this.id = j;
		}

		public Component getDisplayName() {
			return new TranslatableComponent("team.visibility." + this.name);
		}
	}
}

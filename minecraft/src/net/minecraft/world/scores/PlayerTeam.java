package net.minecraft.world.scores;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import javax.annotation.Nullable;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;

public class PlayerTeam extends Team {
	private final Scoreboard scoreboard;
	private final String name;
	private final Set<String> players = Sets.<String>newHashSet();
	private Component displayName;
	private Component playerPrefix = new TextComponent("");
	private Component playerSuffix = new TextComponent("");
	private boolean allowFriendlyFire = true;
	private boolean seeFriendlyInvisibles = true;
	private Team.Visibility nameTagVisibility = Team.Visibility.ALWAYS;
	private Team.Visibility deathMessageVisibility = Team.Visibility.ALWAYS;
	private ChatFormatting color = ChatFormatting.RESET;
	private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

	public PlayerTeam(Scoreboard scoreboard, String string) {
		this.scoreboard = scoreboard;
		this.name = string;
		this.displayName = new TextComponent(string);
	}

	@Override
	public String getName() {
		return this.name;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	public Component getFormattedDisplayName() {
		Component component = ComponentUtils.wrapInSquareBrackets(
			this.displayName
				.deepCopy()
				.withStyle(style -> style.setInsertion(this.name).setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.name))))
		);
		ChatFormatting chatFormatting = this.getColor();
		if (chatFormatting != ChatFormatting.RESET) {
			component.withStyle(chatFormatting);
		}

		return component;
	}

	public void setDisplayName(Component component) {
		if (component == null) {
			throw new IllegalArgumentException("Name cannot be null");
		} else {
			this.displayName = component;
			this.scoreboard.onTeamChanged(this);
		}
	}

	public void setPlayerPrefix(@Nullable Component component) {
		this.playerPrefix = (Component)(component == null ? new TextComponent("") : component.deepCopy());
		this.scoreboard.onTeamChanged(this);
	}

	public Component getPlayerPrefix() {
		return this.playerPrefix;
	}

	public void setPlayerSuffix(@Nullable Component component) {
		this.playerSuffix = (Component)(component == null ? new TextComponent("") : component.deepCopy());
		this.scoreboard.onTeamChanged(this);
	}

	public Component getPlayerSuffix() {
		return this.playerSuffix;
	}

	@Override
	public Collection<String> getPlayers() {
		return this.players;
	}

	@Override
	public Component getFormattedName(Component component) {
		Component component2 = new TextComponent("").append(this.playerPrefix).append(component).append(this.playerSuffix);
		ChatFormatting chatFormatting = this.getColor();
		if (chatFormatting != ChatFormatting.RESET) {
			component2.withStyle(chatFormatting);
		}

		return component2;
	}

	public static Component formatNameForTeam(@Nullable Team team, Component component) {
		return team == null ? component.deepCopy() : team.getFormattedName(component);
	}

	@Override
	public boolean isAllowFriendlyFire() {
		return this.allowFriendlyFire;
	}

	public void setAllowFriendlyFire(boolean bl) {
		this.allowFriendlyFire = bl;
		this.scoreboard.onTeamChanged(this);
	}

	@Override
	public boolean canSeeFriendlyInvisibles() {
		return this.seeFriendlyInvisibles;
	}

	public void setSeeFriendlyInvisibles(boolean bl) {
		this.seeFriendlyInvisibles = bl;
		this.scoreboard.onTeamChanged(this);
	}

	@Override
	public Team.Visibility getNameTagVisibility() {
		return this.nameTagVisibility;
	}

	@Override
	public Team.Visibility getDeathMessageVisibility() {
		return this.deathMessageVisibility;
	}

	public void setNameTagVisibility(Team.Visibility visibility) {
		this.nameTagVisibility = visibility;
		this.scoreboard.onTeamChanged(this);
	}

	public void setDeathMessageVisibility(Team.Visibility visibility) {
		this.deathMessageVisibility = visibility;
		this.scoreboard.onTeamChanged(this);
	}

	@Override
	public Team.CollisionRule getCollisionRule() {
		return this.collisionRule;
	}

	public void setCollisionRule(Team.CollisionRule collisionRule) {
		this.collisionRule = collisionRule;
		this.scoreboard.onTeamChanged(this);
	}

	public int packOptions() {
		int i = 0;
		if (this.isAllowFriendlyFire()) {
			i |= 1;
		}

		if (this.canSeeFriendlyInvisibles()) {
			i |= 2;
		}

		return i;
	}

	@Environment(EnvType.CLIENT)
	public void unpackOptions(int i) {
		this.setAllowFriendlyFire((i & 1) > 0);
		this.setSeeFriendlyInvisibles((i & 2) > 0);
	}

	public void setColor(ChatFormatting chatFormatting) {
		this.color = chatFormatting;
		this.scoreboard.onTeamChanged(this);
	}

	@Override
	public ChatFormatting getColor() {
		return this.color;
	}
}

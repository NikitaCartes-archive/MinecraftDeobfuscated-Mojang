package net.minecraft.world.scores;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
	public static final int MAX_NAME_LENGTH = 16;
	private final Scoreboard scoreboard;
	private final String name;
	private final ObjectiveCriteria criteria;
	private Component displayName;
	private Component formattedDisplayName;
	private ObjectiveCriteria.RenderType renderType;

	public Objective(Scoreboard scoreboard, String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType) {
		this.scoreboard = scoreboard;
		this.name = string;
		this.criteria = objectiveCriteria;
		this.displayName = component;
		this.formattedDisplayName = this.createFormattedDisplayName();
		this.renderType = renderType;
	}

	public Scoreboard getScoreboard() {
		return this.scoreboard;
	}

	public String getName() {
		return this.name;
	}

	public ObjectiveCriteria getCriteria() {
		return this.criteria;
	}

	public Component getDisplayName() {
		return this.displayName;
	}

	private Component createFormattedDisplayName() {
		return ComponentUtils.wrapInSquareBrackets(
			this.displayName.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.name))))
		);
	}

	public Component getFormattedDisplayName() {
		return this.formattedDisplayName;
	}

	public void setDisplayName(Component component) {
		this.displayName = component;
		this.formattedDisplayName = this.createFormattedDisplayName();
		this.scoreboard.onObjectiveChanged(this);
	}

	public ObjectiveCriteria.RenderType getRenderType() {
		return this.renderType;
	}

	public void setRenderType(ObjectiveCriteria.RenderType renderType) {
		this.renderType = renderType;
		this.scoreboard.onObjectiveChanged(this);
	}
}

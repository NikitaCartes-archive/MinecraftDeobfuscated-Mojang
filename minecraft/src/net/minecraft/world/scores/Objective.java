package net.minecraft.world.scores;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
	private final Scoreboard scoreboard;
	private final String name;
	private final ObjectiveCriteria criteria;
	private Component displayName;
	private ObjectiveCriteria.RenderType renderType;

	public Objective(Scoreboard scoreboard, String string, ObjectiveCriteria objectiveCriteria, Component component, ObjectiveCriteria.RenderType renderType) {
		this.scoreboard = scoreboard;
		this.name = string;
		this.criteria = objectiveCriteria;
		this.displayName = component;
		this.renderType = renderType;
	}

	@Environment(EnvType.CLIENT)
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

	public Component getFormattedDisplayName() {
		return ComponentUtils.wrapInSquareBrackets(
			this.displayName.deepCopy().withStyle(style -> style.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getName()))))
		);
	}

	public void setDisplayName(Component component) {
		this.displayName = component;
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

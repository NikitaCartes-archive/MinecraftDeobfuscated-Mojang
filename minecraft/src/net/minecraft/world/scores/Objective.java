package net.minecraft.world.scores;

import java.util.Objects;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.criteria.ObjectiveCriteria;

public class Objective {
	private final Scoreboard scoreboard;
	private final String name;
	private final ObjectiveCriteria criteria;
	private Component displayName;
	private Component formattedDisplayName;
	private ObjectiveCriteria.RenderType renderType;
	private boolean displayAutoUpdate;
	@Nullable
	private NumberFormat numberFormat;

	public Objective(
		Scoreboard scoreboard,
		String string,
		ObjectiveCriteria objectiveCriteria,
		Component component,
		ObjectiveCriteria.RenderType renderType,
		boolean bl,
		@Nullable NumberFormat numberFormat
	) {
		this.scoreboard = scoreboard;
		this.name = string;
		this.criteria = objectiveCriteria;
		this.displayName = component;
		this.formattedDisplayName = this.createFormattedDisplayName();
		this.renderType = renderType;
		this.displayAutoUpdate = bl;
		this.numberFormat = numberFormat;
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

	public boolean displayAutoUpdate() {
		return this.displayAutoUpdate;
	}

	@Nullable
	public NumberFormat numberFormat() {
		return this.numberFormat;
	}

	public NumberFormat numberFormatOrDefault(NumberFormat numberFormat) {
		return (NumberFormat)Objects.requireNonNullElse(this.numberFormat, numberFormat);
	}

	private Component createFormattedDisplayName() {
		return ComponentUtils.wrapInSquareBrackets(
			this.displayName.copy().withStyle(style -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal(this.name))))
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

	public void setDisplayAutoUpdate(boolean bl) {
		this.displayAutoUpdate = bl;
		this.scoreboard.onObjectiveChanged(this);
	}

	public void setNumberFormat(@Nullable NumberFormat numberFormat) {
		this.numberFormat = numberFormat;
		this.scoreboard.onObjectiveChanged(this);
	}
}

package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import net.minecraft.world.scores.Scoreboard;

public class ScoreComponent extends BaseComponent implements ContextAwareComponent {
	private final String name;
	@Nullable
	private final EntitySelector selector;
	private final String objective;

	@Nullable
	private static EntitySelector parseSelector(String string) {
		try {
			return new EntitySelectorParser(new StringReader(string)).parse();
		} catch (CommandSyntaxException var2) {
			return null;
		}
	}

	public ScoreComponent(String string, String string2) {
		this(string, parseSelector(string), string2);
	}

	private ScoreComponent(String string, @Nullable EntitySelector entitySelector, String string2) {
		this.name = string;
		this.selector = entitySelector;
		this.objective = string2;
	}

	public String getName() {
		return this.name;
	}

	public String getObjective() {
		return this.objective;
	}

	private String findTargetName(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		if (this.selector != null) {
			List<? extends Entity> list = this.selector.findEntities(commandSourceStack);
			if (!list.isEmpty()) {
				if (list.size() != 1) {
					throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
				}

				return ((Entity)list.get(0)).getScoreboardName();
			}
		}

		return this.name;
	}

	private String getScore(String string, CommandSourceStack commandSourceStack) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer != null) {
			Scoreboard scoreboard = minecraftServer.getScoreboard();
			Objective objective = scoreboard.getObjective(this.objective);
			if (scoreboard.hasPlayerScore(string, objective)) {
				Score score = scoreboard.getOrCreatePlayerScore(string, objective);
				return Integer.toString(score.getScore());
			}
		}

		return "";
	}

	public ScoreComponent toMutable() {
		return new ScoreComponent(this.name, this.selector, this.objective);
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack == null) {
			return new TextComponent("");
		} else {
			String string = this.findTargetName(commandSourceStack);
			String string2 = entity != null && string.equals("*") ? entity.getScoreboardName() : string;
			return new TextComponent(this.getScore(string2, commandSourceStack));
		}
	}

	@Override
	public boolean equals(Object object) {
		if (this == object) {
			return true;
		} else if (!(object instanceof ScoreComponent)) {
			return false;
		} else {
			ScoreComponent scoreComponent = (ScoreComponent)object;
			return this.name.equals(scoreComponent.name) && this.objective.equals(scoreComponent.objective) && super.equals(object);
		}
	}

	@Override
	public String toString() {
		return "ScoreComponent{name='"
			+ this.name
			+ '\''
			+ "objective='"
			+ this.objective
			+ '\''
			+ ", siblings="
			+ this.siblings
			+ ", style="
			+ this.getStyle()
			+ '}';
	}
}

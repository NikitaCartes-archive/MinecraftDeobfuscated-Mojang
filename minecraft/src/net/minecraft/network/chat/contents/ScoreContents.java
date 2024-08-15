package net.minecraft.network.chat.contents;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.SelectorPattern;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.StyledFormat;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

public record ScoreContents(Either<SelectorPattern, String> name, String objective) implements ComponentContents {
	public static final MapCodec<ScoreContents> INNER_CODEC = RecordCodecBuilder.mapCodec(
		instance -> instance.group(
					Codec.either(SelectorPattern.CODEC, Codec.STRING).fieldOf("name").forGetter(ScoreContents::name),
					Codec.STRING.fieldOf("objective").forGetter(ScoreContents::objective)
				)
				.apply(instance, ScoreContents::new)
	);
	public static final MapCodec<ScoreContents> CODEC = INNER_CODEC.fieldOf("score");
	public static final ComponentContents.Type<ScoreContents> TYPE = new ComponentContents.Type<>(CODEC, "score");

	@Override
	public ComponentContents.Type<?> type() {
		return TYPE;
	}

	private ScoreHolder findTargetName(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
		Optional<SelectorPattern> optional = this.name.left();
		if (optional.isPresent()) {
			List<? extends Entity> list = ((SelectorPattern)optional.get()).resolved().findEntities(commandSourceStack);
			if (!list.isEmpty()) {
				if (list.size() != 1) {
					throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
				}

				return (ScoreHolder)list.getFirst();
			}
		}

		return ScoreHolder.forNameOnly((String)this.name.right().orElseThrow());
	}

	private MutableComponent getScore(ScoreHolder scoreHolder, CommandSourceStack commandSourceStack) {
		MinecraftServer minecraftServer = commandSourceStack.getServer();
		if (minecraftServer != null) {
			Scoreboard scoreboard = minecraftServer.getScoreboard();
			Objective objective = scoreboard.getObjective(this.objective);
			if (objective != null) {
				ReadOnlyScoreInfo readOnlyScoreInfo = scoreboard.getPlayerScoreInfo(scoreHolder, objective);
				if (readOnlyScoreInfo != null) {
					return readOnlyScoreInfo.formatValue(objective.numberFormatOrDefault(StyledFormat.NO_STYLE));
				}
			}
		}

		return Component.empty();
	}

	@Override
	public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
		if (commandSourceStack == null) {
			return Component.empty();
		} else {
			ScoreHolder scoreHolder = this.findTargetName(commandSourceStack);
			ScoreHolder scoreHolder2 = (ScoreHolder)(entity != null && scoreHolder.equals(ScoreHolder.WILDCARD) ? entity : scoreHolder);
			return this.getScore(scoreHolder2, commandSourceStack);
		}
	}

	public String toString() {
		return "score{name='" + this.name + "', objective='" + this.objective + "'}";
	}
}

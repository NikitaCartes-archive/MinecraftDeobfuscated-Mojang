/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import org.jetbrains.annotations.Nullable;

public class ScoreComponent
extends BaseComponent
implements ContextAwareComponent {
    private static final String SCORER_PLACEHOLDER = "*";
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;

    @Nullable
    private static EntitySelector parseSelector(String string) {
        try {
            return new EntitySelectorParser(new StringReader(string)).parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            return null;
        }
    }

    public ScoreComponent(String string, String string2) {
        this(string, ScoreComponent.parseSelector(string), string2);
    }

    private ScoreComponent(String string, @Nullable EntitySelector entitySelector, String string2) {
        this.name = string;
        this.selector = entitySelector;
        this.objective = string2;
    }

    public String getName() {
        return this.name;
    }

    @Nullable
    public EntitySelector getSelector() {
        return this.selector;
    }

    public String getObjective() {
        return this.objective;
    }

    private String findTargetName(CommandSourceStack commandSourceStack) throws CommandSyntaxException {
        List<? extends Entity> list;
        if (this.selector != null && !(list = this.selector.findEntities(commandSourceStack)).isEmpty()) {
            if (list.size() != 1) {
                throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
            }
            return list.get(0).getScoreboardName();
        }
        return this.name;
    }

    private String getScore(String string, CommandSourceStack commandSourceStack) {
        Objective objective;
        ServerScoreboard scoreboard;
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer != null && (scoreboard = minecraftServer.getScoreboard()).hasPlayerScore(string, objective = scoreboard.getObjective(this.objective))) {
            Score score = scoreboard.getOrCreatePlayerScore(string, objective);
            return Integer.toString(score.getScore());
        }
        return "";
    }

    @Override
    public ScoreComponent plainCopy() {
        return new ScoreComponent(this.name, this.selector, this.objective);
    }

    @Override
    public MutableComponent resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        if (commandSourceStack == null) {
            return new TextComponent("");
        }
        String string = this.findTargetName(commandSourceStack);
        String string2 = entity != null && string.equals(SCORER_PLACEHOLDER) ? entity.getScoreboardName() : string;
        return new TextComponent(this.getScore(string2, commandSourceStack));
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object instanceof ScoreComponent) {
            ScoreComponent scoreComponent = (ScoreComponent)object;
            return this.name.equals(scoreComponent.name) && this.objective.equals(scoreComponent.objective) && super.equals(object);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ScoreComponent{name='" + this.name + '\'' + "objective='" + this.objective + '\'' + ", siblings=" + this.siblings + ", style=" + this.getStyle() + '}';
    }

    @Override
    public /* synthetic */ BaseComponent plainCopy() {
        return this.plainCopy();
    }

    @Override
    public /* synthetic */ MutableComponent plainCopy() {
        return this.plainCopy();
    }
}


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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ContextAwareComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.Score;
import org.jetbrains.annotations.Nullable;

public class ScoreComponent
extends BaseComponent
implements ContextAwareComponent {
    private final String name;
    @Nullable
    private final EntitySelector selector;
    private final String objective;
    private String value = "";

    public ScoreComponent(String string, String string2) {
        this.name = string;
        this.objective = string2;
        EntitySelector entitySelector = null;
        try {
            EntitySelectorParser entitySelectorParser = new EntitySelectorParser(new StringReader(string));
            entitySelector = entitySelectorParser.parse();
        } catch (CommandSyntaxException commandSyntaxException) {
            // empty catch block
        }
        this.selector = entitySelector;
    }

    public String getName() {
        return this.name;
    }

    public String getObjective() {
        return this.objective;
    }

    public void setValue(String string) {
        this.value = string;
    }

    @Override
    public String getContents() {
        return this.value;
    }

    private void resolve(CommandSourceStack commandSourceStack) {
        MinecraftServer minecraftServer = commandSourceStack.getServer();
        if (minecraftServer != null && minecraftServer.isInitialized() && StringUtil.isNullOrEmpty(this.value)) {
            Objective objective;
            ServerScoreboard scoreboard = minecraftServer.getScoreboard();
            if (scoreboard.hasPlayerScore(this.name, objective = scoreboard.getObjective(this.objective))) {
                Score score = scoreboard.getOrCreatePlayerScore(this.name, objective);
                this.setValue(String.format("%d", score.getScore()));
            } else {
                this.value = "";
            }
        }
    }

    @Override
    public ScoreComponent copy() {
        ScoreComponent scoreComponent = new ScoreComponent(this.name, this.objective);
        scoreComponent.setValue(this.value);
        return scoreComponent;
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    @Override
    public Component resolve(@Nullable CommandSourceStack commandSourceStack, @Nullable Entity entity, int i) throws CommandSyntaxException {
        String string;
        if (commandSourceStack == null) {
            return this.copy();
        }
        if (this.selector != null) {
            List<? extends Entity> list = this.selector.findEntities(commandSourceStack);
            if (list.isEmpty()) {
                string = this.name;
            } else {
                if (list.size() != 1) throw EntityArgument.ERROR_NOT_SINGLE_ENTITY.create();
                string = list.get(0).getScoreboardName();
            }
        } else {
            string = this.name;
        }
        String string2 = entity != null && string.equals("*") ? entity.getScoreboardName() : string;
        ScoreComponent scoreComponent = new ScoreComponent(string2, this.objective);
        scoreComponent.setValue(this.value);
        scoreComponent.resolve(commandSourceStack);
        return scoreComponent;
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
    public /* synthetic */ Component copy() {
        return this.copy();
    }
}


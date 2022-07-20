/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.chat;

import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.context.CommandContextBuilder;
import com.mojang.brigadier.context.ParsedArgument;
import com.mojang.brigadier.context.ParsedCommandNode;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.commands.arguments.PreviewedArgument;

public record PreviewableCommand<S>(List<Argument<S>> arguments) {
    public static <S> PreviewableCommand<S> of(ParseResults<S> parseResults) {
        CommandContextBuilder<S> commandContextBuilder3;
        CommandContextBuilder<S> commandContextBuilder;
        CommandContextBuilder<S> commandContextBuilder2 = commandContextBuilder = parseResults.getContext();
        List<Argument<S>> list = PreviewableCommand.collectArguments(commandContextBuilder2);
        while ((commandContextBuilder3 = commandContextBuilder2.getChild()) != null) {
            boolean bl;
            boolean bl2 = bl = commandContextBuilder3.getRootNode() != commandContextBuilder.getRootNode();
            if (!bl) break;
            list.addAll(PreviewableCommand.collectArguments(commandContextBuilder3));
            commandContextBuilder2 = commandContextBuilder3;
        }
        return new PreviewableCommand<S>(list);
    }

    private static <S> List<Argument<S>> collectArguments(CommandContextBuilder<S> commandContextBuilder) {
        ArrayList<Argument<S>> list = new ArrayList<Argument<S>>();
        for (ParsedCommandNode<S> parsedCommandNode : commandContextBuilder.getNodes()) {
            ArgumentCommandNode argumentCommandNode;
            CommandNode<S> commandNode = parsedCommandNode.getNode();
            if (!(commandNode instanceof ArgumentCommandNode) || !((commandNode = (argumentCommandNode = (ArgumentCommandNode)commandNode).getType()) instanceof PreviewedArgument)) continue;
            PreviewedArgument previewedArgument = (PreviewedArgument)((Object)commandNode);
            ParsedArgument<S, ?> parsedArgument = commandContextBuilder.getArguments().get(argumentCommandNode.getName());
            if (parsedArgument == null) continue;
            list.add(new Argument<S>(argumentCommandNode, parsedArgument, previewedArgument));
        }
        return list;
    }

    public boolean isPreviewed(CommandNode<?> commandNode) {
        for (Argument<S> argument : this.arguments) {
            if (argument.node() != commandNode) continue;
            return true;
        }
        return false;
    }

    public record Argument<S>(ArgumentCommandNode<S, ?> node, ParsedArgument<S, ?> parsedValue, PreviewedArgument<?> previewType) {
        public String name() {
            return this.node.getName();
        }
    }
}


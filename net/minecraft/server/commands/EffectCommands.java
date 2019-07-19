/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.MobEffectArgument;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class EffectCommands {
    private static final SimpleCommandExceptionType ERROR_GIVE_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.effect.give.failed", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_CLEAR_EVERYTHING_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.effect.clear.everything.failed", new Object[0]));
    private static final SimpleCommandExceptionType ERROR_CLEAR_SPECIFIC_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.effect.clear.specific.failed", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("effect").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.literal("clear").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.entities()).executes(commandContext -> EffectCommands.clearEffects((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets")))).then(Commands.argument("effect", MobEffectArgument.effect()).executes(commandContext -> EffectCommands.clearEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect"))))))).then(Commands.literal("give").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("targets", EntityArgument.entities()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("effect", MobEffectArgument.effect()).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect"), null, 0, true))).then(((RequiredArgumentBuilder)Commands.argument("seconds", IntegerArgumentType.integer(1, 1000000)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), 0, true))).then(((RequiredArgumentBuilder)Commands.argument("amplifier", IntegerArgumentType.integer(0, 255)).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), IntegerArgumentType.getInteger(commandContext, "amplifier"), true))).then(Commands.argument("hideParticles", BoolArgumentType.bool()).executes(commandContext -> EffectCommands.giveEffect((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntities(commandContext, "targets"), MobEffectArgument.getEffect(commandContext, "effect"), IntegerArgumentType.getInteger(commandContext, "seconds"), IntegerArgumentType.getInteger(commandContext, "amplifier"), !BoolArgumentType.getBool(commandContext, "hideParticles"))))))))));
    }

    private static int giveEffect(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, MobEffect mobEffect, @Nullable Integer integer, int i, boolean bl) throws CommandSyntaxException {
        int j = 0;
        int k = integer != null ? (mobEffect.isInstantenous() ? integer : integer * 20) : (mobEffect.isInstantenous() ? 1 : 600);
        for (Entity entity : collection) {
            MobEffectInstance mobEffectInstance;
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).addEffect(mobEffectInstance = new MobEffectInstance(mobEffect, k, i, false, bl))) continue;
            ++j;
        }
        if (j == 0) {
            throw ERROR_GIVE_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.give.success.single", mobEffect.getDisplayName(), collection.iterator().next().getDisplayName(), k / 20), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.give.success.multiple", mobEffect.getDisplayName(), collection.size(), k / 20), true);
        }
        return j;
    }

    private static int clearEffects(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection) throws CommandSyntaxException {
        int i = 0;
        for (Entity entity : collection) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).removeAllEffects()) continue;
            ++i;
        }
        if (i == 0) {
            throw ERROR_CLEAR_EVERYTHING_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.clear.everything.success.single", collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.clear.everything.success.multiple", collection.size()), true);
        }
        return i;
    }

    private static int clearEffect(CommandSourceStack commandSourceStack, Collection<? extends Entity> collection, MobEffect mobEffect) throws CommandSyntaxException {
        int i = 0;
        for (Entity entity : collection) {
            if (!(entity instanceof LivingEntity) || !((LivingEntity)entity).removeEffect(mobEffect)) continue;
            ++i;
        }
        if (i == 0) {
            throw ERROR_CLEAR_SPECIFIC_FAILED.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.clear.specific.success.single", mobEffect.getDisplayName(), collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.effect.clear.specific.success.multiple", mobEffect.getDisplayName(), collection.size()), true);
        }
        return i;
    }
}


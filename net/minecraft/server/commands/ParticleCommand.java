/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
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
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

public class ParticleCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.particle.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("particle").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("name", ParticleArgument.particle()).executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), ((CommandSourceStack)commandContext.getSource()).getPosition(), Vec3.ZERO, 0.0f, 0, false, ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getPlayers()))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3.ZERO, 0.0f, 0, false, ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("delta", Vec3Argument.vec3(false)).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("speed", FloatArgumentType.floatArg(0.0f)).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("count", IntegerArgumentType.integer(0)).executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3Argument.getVec3(commandContext, "delta"), FloatArgumentType.getFloat(commandContext, "speed"), IntegerArgumentType.getInteger(commandContext, "count"), false, ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getPlayers()))).then(((LiteralArgumentBuilder)Commands.literal("force").executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3Argument.getVec3(commandContext, "delta"), FloatArgumentType.getFloat(commandContext, "speed"), IntegerArgumentType.getInteger(commandContext, "count"), true, ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("viewers", EntityArgument.players()).executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3Argument.getVec3(commandContext, "delta"), FloatArgumentType.getFloat(commandContext, "speed"), IntegerArgumentType.getInteger(commandContext, "count"), true, EntityArgument.getPlayers(commandContext, "viewers")))))).then(((LiteralArgumentBuilder)Commands.literal("normal").executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3Argument.getVec3(commandContext, "delta"), FloatArgumentType.getFloat(commandContext, "speed"), IntegerArgumentType.getInteger(commandContext, "count"), false, ((CommandSourceStack)commandContext.getSource()).getServer().getPlayerList().getPlayers()))).then(Commands.argument("viewers", EntityArgument.players()).executes(commandContext -> ParticleCommand.sendParticles((CommandSourceStack)commandContext.getSource(), ParticleArgument.getParticle(commandContext, "name"), Vec3Argument.getVec3(commandContext, "pos"), Vec3Argument.getVec3(commandContext, "delta"), FloatArgumentType.getFloat(commandContext, "speed"), IntegerArgumentType.getInteger(commandContext, "count"), false, EntityArgument.getPlayers(commandContext, "viewers")))))))))));
    }

    private static int sendParticles(CommandSourceStack commandSourceStack, ParticleOptions particleOptions, Vec3 vec3, Vec3 vec32, float f, int i, boolean bl, Collection<ServerPlayer> collection) throws CommandSyntaxException {
        int j = 0;
        for (ServerPlayer serverPlayer : collection) {
            if (!commandSourceStack.getLevel().sendParticles(serverPlayer, particleOptions, bl, vec3.x, vec3.y, vec3.z, i, vec32.x, vec32.y, vec32.z, f)) continue;
            ++j;
        }
        if (j == 0) {
            throw ERROR_FAILED.create();
        }
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.particle.success", Registry.PARTICLE_TYPE.getKey(particleOptions.getType()).toString()), true);
        return j;
    }
}


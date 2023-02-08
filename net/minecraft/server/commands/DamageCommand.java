/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;

public class DamageCommand {
    private static final SimpleCommandExceptionType ERROR_INVULNERABLE = new SimpleCommandExceptionType(Component.translatable("commands.damage.invulnerable"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("damage").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("target", EntityArgument.entity()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("amount", FloatArgumentType.floatArg(0.0f)).executes(commandContext -> DamageCommand.damage((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), FloatArgumentType.getFloat(commandContext, "amount"), ((CommandSourceStack)commandContext.getSource()).getLevel().damageSources().generic()))).then(((RequiredArgumentBuilder)((RequiredArgumentBuilder)Commands.argument("damageType", ResourceArgument.resource(commandBuildContext, Registries.DAMAGE_TYPE)).executes(commandContext -> DamageCommand.damage((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), FloatArgumentType.getFloat(commandContext, "amount"), new DamageSource(ResourceArgument.getResource(commandContext, "damageType", Registries.DAMAGE_TYPE))))).then(Commands.literal("at").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("location", Vec3Argument.vec3()).executes(commandContext -> DamageCommand.damage((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), FloatArgumentType.getFloat(commandContext, "amount"), new DamageSource(ResourceArgument.getResource(commandContext, "damageType", Registries.DAMAGE_TYPE), Vec3Argument.getVec3(commandContext, "location"))))))).then(Commands.literal("by").then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("entity", EntityArgument.entity()).executes(commandContext -> DamageCommand.damage((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), FloatArgumentType.getFloat(commandContext, "amount"), new DamageSource(ResourceArgument.getResource(commandContext, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(commandContext, "entity"))))).then(Commands.literal("from").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("cause", EntityArgument.entity()).executes(commandContext -> DamageCommand.damage((CommandSourceStack)commandContext.getSource(), EntityArgument.getEntity(commandContext, "target"), FloatArgumentType.getFloat(commandContext, "amount"), new DamageSource(ResourceArgument.getResource(commandContext, "damageType", Registries.DAMAGE_TYPE), EntityArgument.getEntity(commandContext, "entity"), EntityArgument.getEntity(commandContext, "cause"))))))))))));
    }

    private static int damage(CommandSourceStack commandSourceStack, Entity entity, float f, DamageSource damageSource) throws CommandSyntaxException {
        if (entity.hurt(damageSource, f)) {
            commandSourceStack.sendSuccess(Component.translatable("commands.damage.success", Float.valueOf(f), entity.getDisplayName()), true);
            return 1;
        }
        throw ERROR_INVULNERABLE.create();
    }
}


/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.CompoundTagArgument;
import net.minecraft.commands.arguments.EntitySummonArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.global.LightningBolt;
import net.minecraft.world.phys.Vec3;

public class SummonCommand {
    private static final SimpleCommandExceptionType ERROR_FAILED = new SimpleCommandExceptionType(new TranslatableComponent("commands.summon.failed", new Object[0]));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("summon").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(((RequiredArgumentBuilder)Commands.argument("entity", EntitySummonArgument.id()).suggests(SuggestionProviders.SUMMONABLE_ENTITIES).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), EntitySummonArgument.getSummonableEntity(commandContext, "entity"), ((CommandSourceStack)commandContext.getSource()).getPosition(), new CompoundTag(), true))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), EntitySummonArgument.getSummonableEntity(commandContext, "entity"), Vec3Argument.getVec3(commandContext, "pos"), new CompoundTag(), true))).then(Commands.argument("nbt", CompoundTagArgument.compoundTag()).executes(commandContext -> SummonCommand.spawnEntity((CommandSourceStack)commandContext.getSource(), EntitySummonArgument.getSummonableEntity(commandContext, "entity"), Vec3Argument.getVec3(commandContext, "pos"), CompoundTagArgument.getCompoundTag(commandContext, "nbt"), false))))));
    }

    private static int spawnEntity(CommandSourceStack commandSourceStack, ResourceLocation resourceLocation, Vec3 vec3, CompoundTag compoundTag, boolean bl) throws CommandSyntaxException {
        CompoundTag compoundTag2 = compoundTag.copy();
        compoundTag2.putString("id", resourceLocation.toString());
        if (EntityType.getKey(EntityType.LIGHTNING_BOLT).equals(resourceLocation)) {
            LightningBolt lightningBolt = new LightningBolt(commandSourceStack.getLevel(), vec3.x, vec3.y, vec3.z, false);
            commandSourceStack.getLevel().addGlobalEntity(lightningBolt);
            commandSourceStack.sendSuccess(new TranslatableComponent("commands.summon.success", lightningBolt.getDisplayName()), true);
            return 1;
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        Entity entity2 = EntityType.loadEntityRecursive(compoundTag2, serverLevel, entity -> {
            entity.moveTo(vec3.x, vec3.y, vec3.z, entity.yRot, entity.xRot);
            if (!serverLevel.addWithUUID((Entity)entity)) {
                return null;
            }
            return entity;
        });
        if (entity2 == null) {
            throw ERROR_FAILED.create();
        }
        if (bl && entity2 instanceof Mob) {
            ((Mob)entity2).finalizeSpawn(commandSourceStack.getLevel(), commandSourceStack.getLevel().getCurrentDifficultyAt(new BlockPos(entity2)), MobSpawnType.COMMAND, null, null);
        }
        commandSourceStack.sendSuccess(new TranslatableComponent("commands.summon.success", entity2.getDisplayName()), true);
        return 1;
    }
}


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
import java.util.Collection;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PlaySoundCommand {
    private static final SimpleCommandExceptionType ERROR_TOO_FAR = new SimpleCommandExceptionType(Component.translatable("commands.playsound.failed"));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        RequiredArgumentBuilder<CommandSourceStack, ResourceLocation> requiredArgumentBuilder = Commands.argument("sound", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_SOUNDS);
        for (SoundSource soundSource : SoundSource.values()) {
            requiredArgumentBuilder.then(PlaySoundCommand.source(soundSource));
        }
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("playsound").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(requiredArgumentBuilder));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> source(SoundSource soundSource) {
        return (LiteralArgumentBuilder)Commands.literal(soundSource.getName()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("targets", EntityArgument.players()).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ResourceLocationArgument.getId(commandContext, "sound"), soundSource, ((CommandSourceStack)commandContext.getSource()).getPosition(), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pos", Vec3Argument.vec3()).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ResourceLocationArgument.getId(commandContext, "sound"), soundSource, Vec3Argument.getVec3(commandContext, "pos"), 1.0f, 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("volume", FloatArgumentType.floatArg(0.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ResourceLocationArgument.getId(commandContext, "sound"), soundSource, Vec3Argument.getVec3(commandContext, "pos"), commandContext.getArgument("volume", Float.class).floatValue(), 1.0f, 0.0f))).then(((RequiredArgumentBuilder)Commands.argument("pitch", FloatArgumentType.floatArg(0.0f, 2.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ResourceLocationArgument.getId(commandContext, "sound"), soundSource, Vec3Argument.getVec3(commandContext, "pos"), commandContext.getArgument("volume", Float.class).floatValue(), commandContext.getArgument("pitch", Float.class).floatValue(), 0.0f))).then(Commands.argument("minVolume", FloatArgumentType.floatArg(0.0f, 1.0f)).executes(commandContext -> PlaySoundCommand.playSound((CommandSourceStack)commandContext.getSource(), EntityArgument.getPlayers(commandContext, "targets"), ResourceLocationArgument.getId(commandContext, "sound"), soundSource, Vec3Argument.getVec3(commandContext, "pos"), commandContext.getArgument("volume", Float.class).floatValue(), commandContext.getArgument("pitch", Float.class).floatValue(), commandContext.getArgument("minVolume", Float.class).floatValue())))))));
    }

    private static int playSound(CommandSourceStack commandSourceStack, Collection<ServerPlayer> collection, ResourceLocation resourceLocation, SoundSource soundSource, Vec3 vec3, float f, float g, float h) throws CommandSyntaxException {
        Holder<SoundEvent> holder = Holder.direct(SoundEvent.createVariableRangeEvent(resourceLocation));
        double d = Mth.square(holder.value().getRange(f));
        int i = 0;
        long l = commandSourceStack.getLevel().getRandom().nextLong();
        for (ServerPlayer serverPlayer : collection) {
            double e = vec3.x - serverPlayer.getX();
            double j = vec3.y - serverPlayer.getY();
            double k = vec3.z - serverPlayer.getZ();
            double m = e * e + j * j + k * k;
            Vec3 vec32 = vec3;
            float n = f;
            if (m > d) {
                if (h <= 0.0f) continue;
                double o = Math.sqrt(m);
                vec32 = new Vec3(serverPlayer.getX() + e / o * 2.0, serverPlayer.getY() + j / o * 2.0, serverPlayer.getZ() + k / o * 2.0);
                n = h;
            }
            serverPlayer.connection.send(new ClientboundSoundPacket(holder, soundSource, vec32.x(), vec32.y(), vec32.z(), n, g, l));
            ++i;
        }
        if (i == 0) {
            throw ERROR_TOO_FAR.create();
        }
        if (collection.size() == 1) {
            commandSourceStack.sendSuccess(Component.translatable("commands.playsound.success.single", resourceLocation, collection.iterator().next().getDisplayName()), true);
        } else {
            commandSourceStack.sendSuccess(Component.translatable("commands.playsound.success.multiple", resourceLocation, collection.size()), true);
        }
        return i;
    }
}


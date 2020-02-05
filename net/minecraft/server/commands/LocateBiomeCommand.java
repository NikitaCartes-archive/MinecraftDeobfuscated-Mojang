/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LocateCommand;
import net.minecraft.world.level.biome.Biome;

public class LocateBiomeCommand {
    public static final DynamicCommandExceptionType ERROR_INVALID_BIOME = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.locatebiome.invalid", object));
    private static final DynamicCommandExceptionType ERROR_BIOME_NOT_FOUND = new DynamicCommandExceptionType(object -> new TranslatableComponent("commands.locatebiome.notFound", object));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("locatebiome").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("biome", ResourceLocationArgument.id()).suggests(SuggestionProviders.AVAILABLE_BIOMES).executes(commandContext -> LocateBiomeCommand.locateBiome((CommandSourceStack)commandContext.getSource(), LocateBiomeCommand.getBiome(commandContext, "biome")))));
    }

    private static int locateBiome(CommandSourceStack commandSourceStack, Biome biome) throws CommandSyntaxException {
        BlockPos blockPos = new BlockPos(commandSourceStack.getPosition());
        BlockPos blockPos2 = commandSourceStack.getLevel().findNearestBiome(biome, blockPos, 6400, 8);
        if (blockPos2 == null) {
            throw ERROR_BIOME_NOT_FOUND.create(biome.getName().getString());
        }
        return LocateCommand.showLocateResult(commandSourceStack, biome.getName().getString(), blockPos, blockPos2, "commands.locatebiome.success");
    }

    private static Biome getBiome(CommandContext<CommandSourceStack> commandContext, String string) throws CommandSyntaxException {
        ResourceLocation resourceLocation = commandContext.getArgument(string, ResourceLocation.class);
        return Registry.BIOME.getOptional(resourceLocation).orElseThrow(() -> ERROR_INVALID_BIOME.create(resourceLocation));
    }
}


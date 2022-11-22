/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.server.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import java.util.ArrayList;
import java.util.function.Predicate;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceOrTagArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.apache.commons.lang3.mutable.MutableInt;

public class FillBiomeCommand {
    private static final int MAX_FILL_AREA = 32768;
    public static final SimpleCommandExceptionType ERROR_NOT_LOADED = new SimpleCommandExceptionType(Component.translatable("argument.pos.unloaded"));
    private static final Dynamic2CommandExceptionType ERROR_VOLUME_TOO_LARGE = new Dynamic2CommandExceptionType((object, object2) -> Component.translatable("commands.fillbiome.toobig", object, object2));

    public static void register(CommandDispatcher<CommandSourceStack> commandDispatcher, CommandBuildContext commandBuildContext) {
        commandDispatcher.register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal("fillbiome").requires(commandSourceStack -> commandSourceStack.hasPermission(2))).then(Commands.argument("from", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("to", BlockPosArgument.blockPos()).then((ArgumentBuilder<CommandSourceStack, ?>)((RequiredArgumentBuilder)Commands.argument("biome", ResourceArgument.resource(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to"), ResourceArgument.getResource(commandContext, "biome", Registries.BIOME), holder -> true))).then(Commands.literal("replace").then((ArgumentBuilder<CommandSourceStack, ?>)Commands.argument("filter", ResourceOrTagArgument.resourceOrTag(commandBuildContext, Registries.BIOME)).executes(commandContext -> FillBiomeCommand.fill((CommandSourceStack)commandContext.getSource(), BlockPosArgument.getLoadedBlockPos(commandContext, "from"), BlockPosArgument.getLoadedBlockPos(commandContext, "to"), ResourceArgument.getResource(commandContext, "biome", Registries.BIOME), ResourceOrTagArgument.getResourceOrTag(commandContext, "filter", Registries.BIOME)::test))))))));
    }

    private static int quantize(int i) {
        return QuartPos.toBlock(QuartPos.fromBlock(i));
    }

    private static BlockPos quantize(BlockPos blockPos) {
        return new BlockPos(FillBiomeCommand.quantize(blockPos.getX()), FillBiomeCommand.quantize(blockPos.getY()), FillBiomeCommand.quantize(blockPos.getZ()));
    }

    private static BiomeResolver makeResolver(MutableInt mutableInt, ChunkAccess chunkAccess, BoundingBox boundingBox, Holder<Biome> holder, Predicate<Holder<Biome>> predicate) {
        return (i, j, k, sampler) -> {
            int l = QuartPos.toBlock(i);
            int m = QuartPos.toBlock(j);
            int n = QuartPos.toBlock(k);
            Holder<Biome> holder2 = chunkAccess.getNoiseBiome(i, j, k);
            if (boundingBox.isInside(l, m, n) && predicate.test(holder2)) {
                mutableInt.increment();
                return holder;
            }
            return holder2;
        };
    }

    private static int fill(CommandSourceStack commandSourceStack, BlockPos blockPos, BlockPos blockPos2, Holder.Reference<Biome> reference, Predicate<Holder<Biome>> predicate) throws CommandSyntaxException {
        BlockPos blockPos4;
        BlockPos blockPos3 = FillBiomeCommand.quantize(blockPos);
        BoundingBox boundingBox = BoundingBox.fromCorners(blockPos3, blockPos4 = FillBiomeCommand.quantize(blockPos2));
        int i = boundingBox.getXSpan() * boundingBox.getYSpan() * boundingBox.getZSpan();
        if (i > 32768) {
            throw ERROR_VOLUME_TOO_LARGE.create(32768, i);
        }
        ServerLevel serverLevel = commandSourceStack.getLevel();
        ArrayList<ChunkAccess> list = new ArrayList<ChunkAccess>();
        for (int j = SectionPos.blockToSectionCoord(boundingBox.minZ()); j <= SectionPos.blockToSectionCoord(boundingBox.maxZ()); ++j) {
            for (int k = SectionPos.blockToSectionCoord(boundingBox.minX()); k <= SectionPos.blockToSectionCoord(boundingBox.maxX()); ++k) {
                ChunkAccess chunkAccess = serverLevel.getChunk(k, j, ChunkStatus.FULL, false);
                if (chunkAccess == null) {
                    throw ERROR_NOT_LOADED.create();
                }
                list.add(chunkAccess);
            }
        }
        MutableInt mutableInt = new MutableInt(0);
        for (ChunkAccess chunkAccess : list) {
            chunkAccess.fillBiomesFromNoise(FillBiomeCommand.makeResolver(mutableInt, chunkAccess, boundingBox, reference, predicate), serverLevel.getChunkSource().randomState().sampler());
            chunkAccess.setUnsaved(true);
            serverLevel.getChunkSource().chunkMap.resendChunk(chunkAccess);
        }
        commandSourceStack.sendSuccess(Component.translatable("commands.fillbiome.success.count", mutableInt.getValue(), boundingBox.minX(), boundingBox.minY(), boundingBox.minZ(), boundingBox.maxX(), boundingBox.maxY(), boundingBox.maxZ()), true);
        return mutableInt.getValue();
    }
}


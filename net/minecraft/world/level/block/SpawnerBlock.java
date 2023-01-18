/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.world.level.block;

import java.util.List;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class SpawnerBlock
extends BaseEntityBlock {
    protected SpawnerBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SpawnerBlockEntity(blockPos, blockState);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState blockState, BlockEntityType<T> blockEntityType) {
        return SpawnerBlock.createTickerHelper(blockEntityType, BlockEntityType.MOB_SPAWNER, level.isClientSide ? SpawnerBlockEntity::clientTick : SpawnerBlockEntity::serverTick);
    }

    @Override
    public void spawnAfterBreak(BlockState blockState, ServerLevel serverLevel, BlockPos blockPos, ItemStack itemStack, boolean bl) {
        super.spawnAfterBreak(blockState, serverLevel, blockPos, itemStack, bl);
        if (bl) {
            int i = 15 + serverLevel.random.nextInt(15) + serverLevel.random.nextInt(15);
            this.popExperience(serverLevel, blockPos, i);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }

    @Override
    public void appendHoverText(ItemStack itemStack, @Nullable BlockGetter blockGetter, List<Component> list, TooltipFlag tooltipFlag) {
        super.appendHoverText(itemStack, blockGetter, list, tooltipFlag);
        Optional<Component> optional = this.getSpawnEntityDisplayName(itemStack);
        if (optional.isPresent()) {
            list.add(optional.get());
        } else {
            list.add(CommonComponents.EMPTY);
            list.add(Component.translatable("block.minecraft.spawner.desc1").withStyle(ChatFormatting.GRAY));
            list.add(CommonComponents.space().append(Component.translatable("block.minecraft.spawner.desc2").withStyle(ChatFormatting.BLUE)));
        }
    }

    private Optional<Component> getSpawnEntityDisplayName(ItemStack itemStack) {
        String string;
        ResourceLocation resourceLocation;
        CompoundTag compoundTag = BlockItem.getBlockEntityData(itemStack);
        if (compoundTag != null && compoundTag.contains("SpawnData", 10) && (resourceLocation = ResourceLocation.tryParse(string = compoundTag.getCompound("SpawnData").getCompound("entity").getString("id"))) != null) {
            return BuiltInRegistries.ENTITY_TYPE.getOptional(resourceLocation).map(entityType -> Component.translatable(entityType.getDescriptionId()).withStyle(ChatFormatting.GRAY));
        }
        return Optional.empty();
    }
}


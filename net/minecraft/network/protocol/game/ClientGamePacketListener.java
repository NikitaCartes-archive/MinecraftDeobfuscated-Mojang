/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundAddExperienceOrbPacket;
import net.minecraft.network.protocol.game.ClientboundAddMobPacket;
import net.minecraft.network.protocol.game.ClientboundAddPaintingPacket;
import net.minecraft.network.protocol.game.ClientboundAddPlayerPacket;
import net.minecraft.network.protocol.game.ClientboundAnimatePacket;
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket;
import net.minecraft.network.protocol.game.ClientboundBlockBreakAckPacket;
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundBlockEventPacket;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundChunkBlocksUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundCommandSuggestionsPacket;
import net.minecraft.network.protocol.game.ClientboundCommandsPacket;
import net.minecraft.network.protocol.game.ClientboundContainerAckPacket;
import net.minecraft.network.protocol.game.ClientboundContainerClosePacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.network.protocol.game.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ClientboundCustomSoundPacket;
import net.minecraft.network.protocol.game.ClientboundDisconnectPacket;
import net.minecraft.network.protocol.game.ClientboundEntityEventPacket;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.network.protocol.game.ClientboundForgetLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.network.protocol.game.ClientboundHorseScreenOpenPacket;
import net.minecraft.network.protocol.game.ClientboundKeepAlivePacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacket;
import net.minecraft.network.protocol.game.ClientboundLevelEventPacket;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.network.protocol.game.ClientboundLightUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundLoginPacket;
import net.minecraft.network.protocol.game.ClientboundMapItemDataPacket;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundMoveVehiclePacket;
import net.minecraft.network.protocol.game.ClientboundOpenBookPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ClientboundPlaceGhostRecipePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerCombatPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerLookAtPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket;
import net.minecraft.network.protocol.game.ClientboundRecipePacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundRotateHeadPacket;
import net.minecraft.network.protocol.game.ClientboundSelectAdvancementsTabPacket;
import net.minecraft.network.protocol.game.ClientboundSetBorderPacket;
import net.minecraft.network.protocol.game.ClientboundSetCameraPacket;
import net.minecraft.network.protocol.game.ClientboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheCenterPacket;
import net.minecraft.network.protocol.game.ClientboundSetChunkCacheRadiusPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityLinkPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetEquipmentPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.network.protocol.game.ClientboundSetScorePacket;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesPacket;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundStopSoundPacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ClientboundTagQueryPacket;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateRecipesPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateTagsPacket;

public interface ClientGamePacketListener
extends PacketListener {
    public void handleAddEntity(ClientboundAddEntityPacket var1);

    public void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket var1);

    public void handleAddMob(ClientboundAddMobPacket var1);

    public void handleAddObjective(ClientboundSetObjectivePacket var1);

    public void handleAddPainting(ClientboundAddPaintingPacket var1);

    public void handleAddPlayer(ClientboundAddPlayerPacket var1);

    public void handleAnimate(ClientboundAnimatePacket var1);

    public void handleAwardStats(ClientboundAwardStatsPacket var1);

    public void handleAddOrRemoveRecipes(ClientboundRecipePacket var1);

    public void handleBlockDestruction(ClientboundBlockDestructionPacket var1);

    public void handleOpenSignEditor(ClientboundOpenSignEditorPacket var1);

    public void handleBlockEntityData(ClientboundBlockEntityDataPacket var1);

    public void handleBlockEvent(ClientboundBlockEventPacket var1);

    public void handleBlockUpdate(ClientboundBlockUpdatePacket var1);

    public void handleChat(ClientboundChatPacket var1);

    public void handleChunkBlocksUpdate(ClientboundChunkBlocksUpdatePacket var1);

    public void handleMapItemData(ClientboundMapItemDataPacket var1);

    public void handleContainerAck(ClientboundContainerAckPacket var1);

    public void handleContainerClose(ClientboundContainerClosePacket var1);

    public void handleContainerContent(ClientboundContainerSetContentPacket var1);

    public void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket var1);

    public void handleContainerSetData(ClientboundContainerSetDataPacket var1);

    public void handleContainerSetSlot(ClientboundContainerSetSlotPacket var1);

    public void handleCustomPayload(ClientboundCustomPayloadPacket var1);

    public void handleDisconnect(ClientboundDisconnectPacket var1);

    public void handleEntityEvent(ClientboundEntityEventPacket var1);

    public void handleEntityLinkPacket(ClientboundSetEntityLinkPacket var1);

    public void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket var1);

    public void handleExplosion(ClientboundExplodePacket var1);

    public void handleGameEvent(ClientboundGameEventPacket var1);

    public void handleKeepAlive(ClientboundKeepAlivePacket var1);

    public void handleLevelChunk(ClientboundLevelChunkPacket var1);

    public void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket var1);

    public void handleLevelEvent(ClientboundLevelEventPacket var1);

    public void handleLogin(ClientboundLoginPacket var1);

    public void handleMoveEntity(ClientboundMoveEntityPacket var1);

    public void handleMovePlayer(ClientboundPlayerPositionPacket var1);

    public void handleParticleEvent(ClientboundLevelParticlesPacket var1);

    public void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket var1);

    public void handlePlayerInfo(ClientboundPlayerInfoPacket var1);

    public void handleRemoveEntity(ClientboundRemoveEntitiesPacket var1);

    public void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket var1);

    public void handleRespawn(ClientboundRespawnPacket var1);

    public void handleRotateMob(ClientboundRotateHeadPacket var1);

    public void handleSetCarriedItem(ClientboundSetCarriedItemPacket var1);

    public void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket var1);

    public void handleSetEntityData(ClientboundSetEntityDataPacket var1);

    public void handleSetEntityMotion(ClientboundSetEntityMotionPacket var1);

    public void handleSetEquipment(ClientboundSetEquipmentPacket var1);

    public void handleSetExperience(ClientboundSetExperiencePacket var1);

    public void handleSetHealth(ClientboundSetHealthPacket var1);

    public void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket var1);

    public void handleSetScore(ClientboundSetScorePacket var1);

    public void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket var1);

    public void handleSetTime(ClientboundSetTimePacket var1);

    public void handleSoundEvent(ClientboundSoundPacket var1);

    public void handleSoundEntityEvent(ClientboundSoundEntityPacket var1);

    public void handleCustomSoundEvent(ClientboundCustomSoundPacket var1);

    public void handleTakeItemEntity(ClientboundTakeItemEntityPacket var1);

    public void handleTeleportEntity(ClientboundTeleportEntityPacket var1);

    public void handleUpdateAttributes(ClientboundUpdateAttributesPacket var1);

    public void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket var1);

    public void handleUpdateTags(ClientboundUpdateTagsPacket var1);

    public void handlePlayerCombat(ClientboundPlayerCombatPacket var1);

    public void handleChangeDifficulty(ClientboundChangeDifficultyPacket var1);

    public void handleSetCamera(ClientboundSetCameraPacket var1);

    public void handleSetBorder(ClientboundSetBorderPacket var1);

    public void handleSetTitles(ClientboundSetTitlesPacket var1);

    public void handleTabListCustomisation(ClientboundTabListPacket var1);

    public void handleResourcePack(ClientboundResourcePackPacket var1);

    public void handleBossUpdate(ClientboundBossEventPacket var1);

    public void handleItemCooldown(ClientboundCooldownPacket var1);

    public void handleMoveVehicle(ClientboundMoveVehiclePacket var1);

    public void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket var1);

    public void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket var1);

    public void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket var1);

    public void handleCommands(ClientboundCommandsPacket var1);

    public void handleStopSoundEvent(ClientboundStopSoundPacket var1);

    public void handleCommandSuggestions(ClientboundCommandSuggestionsPacket var1);

    public void handleUpdateRecipes(ClientboundUpdateRecipesPacket var1);

    public void handleLookAt(ClientboundPlayerLookAtPacket var1);

    public void handleTagQueryPacket(ClientboundTagQueryPacket var1);

    public void handleLightUpdatePacked(ClientboundLightUpdatePacket var1);

    public void handleOpenBook(ClientboundOpenBookPacket var1);

    public void handleOpenScreen(ClientboundOpenScreenPacket var1);

    public void handleMerchantOffers(ClientboundMerchantOffersPacket var1);

    public void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket var1);

    public void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket var1);

    public void handleBlockBreakAck(ClientboundBlockBreakAckPacket var1);
}


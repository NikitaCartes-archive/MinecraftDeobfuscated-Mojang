package net.minecraft.network.protocol.game;

import net.minecraft.network.PacketListener;

public interface ClientGamePacketListener extends PacketListener {
	void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket);

	void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket);

	void handleAddVibrationSignal(ClientboundAddVibrationSignalPacket clientboundAddVibrationSignalPacket);

	void handleAddMob(ClientboundAddMobPacket clientboundAddMobPacket);

	void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket);

	void handleAddPainting(ClientboundAddPaintingPacket clientboundAddPaintingPacket);

	void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket);

	void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket);

	void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket);

	void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket);

	void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket);

	void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket);

	void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket);

	void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket);

	void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket);

	void handleChat(ClientboundChatPacket clientboundChatPacket);

	void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket);

	void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket);

	void handleContainerAck(ClientboundContainerAckPacket clientboundContainerAckPacket);

	void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket);

	void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket);

	void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket);

	void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket);

	void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket);

	void handleCustomPayload(ClientboundCustomPayloadPacket clientboundCustomPayloadPacket);

	void handleDisconnect(ClientboundDisconnectPacket clientboundDisconnectPacket);

	void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket);

	void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket);

	void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket);

	void handleExplosion(ClientboundExplodePacket clientboundExplodePacket);

	void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket);

	void handleKeepAlive(ClientboundKeepAlivePacket clientboundKeepAlivePacket);

	void handleLevelChunk(ClientboundLevelChunkPacket clientboundLevelChunkPacket);

	void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket);

	void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket);

	void handleLogin(ClientboundLoginPacket clientboundLoginPacket);

	void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket);

	void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket);

	void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket);

	void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket);

	void handlePlayerInfo(ClientboundPlayerInfoPacket clientboundPlayerInfoPacket);

	void handleRemoveEntity(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket);

	void handleRemoveMobEffect(ClientboundRemoveMobEffectPacket clientboundRemoveMobEffectPacket);

	void handleRespawn(ClientboundRespawnPacket clientboundRespawnPacket);

	void handleRotateMob(ClientboundRotateHeadPacket clientboundRotateHeadPacket);

	void handleSetCarriedItem(ClientboundSetCarriedItemPacket clientboundSetCarriedItemPacket);

	void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket clientboundSetDisplayObjectivePacket);

	void handleSetEntityData(ClientboundSetEntityDataPacket clientboundSetEntityDataPacket);

	void handleSetEntityMotion(ClientboundSetEntityMotionPacket clientboundSetEntityMotionPacket);

	void handleSetEquipment(ClientboundSetEquipmentPacket clientboundSetEquipmentPacket);

	void handleSetExperience(ClientboundSetExperiencePacket clientboundSetExperiencePacket);

	void handleSetHealth(ClientboundSetHealthPacket clientboundSetHealthPacket);

	void handleSetPlayerTeamPacket(ClientboundSetPlayerTeamPacket clientboundSetPlayerTeamPacket);

	void handleSetScore(ClientboundSetScorePacket clientboundSetScorePacket);

	void handleSetSpawn(ClientboundSetDefaultSpawnPositionPacket clientboundSetDefaultSpawnPositionPacket);

	void handleSetTime(ClientboundSetTimePacket clientboundSetTimePacket);

	void handleSoundEvent(ClientboundSoundPacket clientboundSoundPacket);

	void handleSoundEntityEvent(ClientboundSoundEntityPacket clientboundSoundEntityPacket);

	void handleCustomSoundEvent(ClientboundCustomSoundPacket clientboundCustomSoundPacket);

	void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket);

	void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket);

	void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket);

	void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket);

	void handleUpdateTags(ClientboundUpdateTagsPacket clientboundUpdateTagsPacket);

	void handlePlayerCombat(ClientboundPlayerCombatPacket clientboundPlayerCombatPacket);

	void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket);

	void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket);

	void handleSetBorder(ClientboundSetBorderPacket clientboundSetBorderPacket);

	void handleSetTitles(ClientboundSetTitlesPacket clientboundSetTitlesPacket);

	void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket);

	void handleResourcePack(ClientboundResourcePackPacket clientboundResourcePackPacket);

	void handleBossUpdate(ClientboundBossEventPacket clientboundBossEventPacket);

	void handleItemCooldown(ClientboundCooldownPacket clientboundCooldownPacket);

	void handleMoveVehicle(ClientboundMoveVehiclePacket clientboundMoveVehiclePacket);

	void handleUpdateAdvancementsPacket(ClientboundUpdateAdvancementsPacket clientboundUpdateAdvancementsPacket);

	void handleSelectAdvancementsTab(ClientboundSelectAdvancementsTabPacket clientboundSelectAdvancementsTabPacket);

	void handlePlaceRecipe(ClientboundPlaceGhostRecipePacket clientboundPlaceGhostRecipePacket);

	void handleCommands(ClientboundCommandsPacket clientboundCommandsPacket);

	void handleStopSoundEvent(ClientboundStopSoundPacket clientboundStopSoundPacket);

	void handleCommandSuggestions(ClientboundCommandSuggestionsPacket clientboundCommandSuggestionsPacket);

	void handleUpdateRecipes(ClientboundUpdateRecipesPacket clientboundUpdateRecipesPacket);

	void handleLookAt(ClientboundPlayerLookAtPacket clientboundPlayerLookAtPacket);

	void handleTagQueryPacket(ClientboundTagQueryPacket clientboundTagQueryPacket);

	void handleLightUpdatePacked(ClientboundLightUpdatePacket clientboundLightUpdatePacket);

	void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket);

	void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket);

	void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket);

	void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket);

	void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket);

	void handleBlockBreakAck(ClientboundBlockBreakAckPacket clientboundBlockBreakAckPacket);
}

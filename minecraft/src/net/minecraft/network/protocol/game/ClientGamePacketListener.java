package net.minecraft.network.protocol.game;

import net.minecraft.network.ConnectionProtocol;
import net.minecraft.network.protocol.common.ClientCommonPacketListener;

public interface ClientGamePacketListener extends ClientCommonPacketListener {
	@Override
	default ConnectionProtocol protocol() {
		return ConnectionProtocol.PLAY;
	}

	void handleAddEntity(ClientboundAddEntityPacket clientboundAddEntityPacket);

	void handleAddExperienceOrb(ClientboundAddExperienceOrbPacket clientboundAddExperienceOrbPacket);

	void handleAddObjective(ClientboundSetObjectivePacket clientboundSetObjectivePacket);

	void handleAddPlayer(ClientboundAddPlayerPacket clientboundAddPlayerPacket);

	void handleAnimate(ClientboundAnimatePacket clientboundAnimatePacket);

	void handleHurtAnimation(ClientboundHurtAnimationPacket clientboundHurtAnimationPacket);

	void handleAwardStats(ClientboundAwardStatsPacket clientboundAwardStatsPacket);

	void handleAddOrRemoveRecipes(ClientboundRecipePacket clientboundRecipePacket);

	void handleBlockDestruction(ClientboundBlockDestructionPacket clientboundBlockDestructionPacket);

	void handleOpenSignEditor(ClientboundOpenSignEditorPacket clientboundOpenSignEditorPacket);

	void handleBlockEntityData(ClientboundBlockEntityDataPacket clientboundBlockEntityDataPacket);

	void handleBlockEvent(ClientboundBlockEventPacket clientboundBlockEventPacket);

	void handleBlockUpdate(ClientboundBlockUpdatePacket clientboundBlockUpdatePacket);

	void handleSystemChat(ClientboundSystemChatPacket clientboundSystemChatPacket);

	void handlePlayerChat(ClientboundPlayerChatPacket clientboundPlayerChatPacket);

	void handleDisguisedChat(ClientboundDisguisedChatPacket clientboundDisguisedChatPacket);

	void handleDeleteChat(ClientboundDeleteChatPacket clientboundDeleteChatPacket);

	void handleChunkBlocksUpdate(ClientboundSectionBlocksUpdatePacket clientboundSectionBlocksUpdatePacket);

	void handleMapItemData(ClientboundMapItemDataPacket clientboundMapItemDataPacket);

	void handleContainerClose(ClientboundContainerClosePacket clientboundContainerClosePacket);

	void handleContainerContent(ClientboundContainerSetContentPacket clientboundContainerSetContentPacket);

	void handleHorseScreenOpen(ClientboundHorseScreenOpenPacket clientboundHorseScreenOpenPacket);

	void handleContainerSetData(ClientboundContainerSetDataPacket clientboundContainerSetDataPacket);

	void handleContainerSetSlot(ClientboundContainerSetSlotPacket clientboundContainerSetSlotPacket);

	void handleEntityEvent(ClientboundEntityEventPacket clientboundEntityEventPacket);

	void handleEntityLinkPacket(ClientboundSetEntityLinkPacket clientboundSetEntityLinkPacket);

	void handleSetEntityPassengersPacket(ClientboundSetPassengersPacket clientboundSetPassengersPacket);

	void handleExplosion(ClientboundExplodePacket clientboundExplodePacket);

	void handleGameEvent(ClientboundGameEventPacket clientboundGameEventPacket);

	void handleLevelChunkWithLight(ClientboundLevelChunkWithLightPacket clientboundLevelChunkWithLightPacket);

	void handleChunksBiomes(ClientboundChunksBiomesPacket clientboundChunksBiomesPacket);

	void handleForgetLevelChunk(ClientboundForgetLevelChunkPacket clientboundForgetLevelChunkPacket);

	void handleLevelEvent(ClientboundLevelEventPacket clientboundLevelEventPacket);

	void handleLogin(ClientboundLoginPacket clientboundLoginPacket);

	void handleMoveEntity(ClientboundMoveEntityPacket clientboundMoveEntityPacket);

	void handleMovePlayer(ClientboundPlayerPositionPacket clientboundPlayerPositionPacket);

	void handleParticleEvent(ClientboundLevelParticlesPacket clientboundLevelParticlesPacket);

	void handlePlayerAbilities(ClientboundPlayerAbilitiesPacket clientboundPlayerAbilitiesPacket);

	void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket clientboundPlayerInfoRemovePacket);

	void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket clientboundPlayerInfoUpdatePacket);

	void handleRemoveEntities(ClientboundRemoveEntitiesPacket clientboundRemoveEntitiesPacket);

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

	void handleTakeItemEntity(ClientboundTakeItemEntityPacket clientboundTakeItemEntityPacket);

	void handleTeleportEntity(ClientboundTeleportEntityPacket clientboundTeleportEntityPacket);

	void handleUpdateAttributes(ClientboundUpdateAttributesPacket clientboundUpdateAttributesPacket);

	void handleUpdateMobEffect(ClientboundUpdateMobEffectPacket clientboundUpdateMobEffectPacket);

	void handlePlayerCombatEnd(ClientboundPlayerCombatEndPacket clientboundPlayerCombatEndPacket);

	void handlePlayerCombatEnter(ClientboundPlayerCombatEnterPacket clientboundPlayerCombatEnterPacket);

	void handlePlayerCombatKill(ClientboundPlayerCombatKillPacket clientboundPlayerCombatKillPacket);

	void handleChangeDifficulty(ClientboundChangeDifficultyPacket clientboundChangeDifficultyPacket);

	void handleSetCamera(ClientboundSetCameraPacket clientboundSetCameraPacket);

	void handleInitializeBorder(ClientboundInitializeBorderPacket clientboundInitializeBorderPacket);

	void handleSetBorderLerpSize(ClientboundSetBorderLerpSizePacket clientboundSetBorderLerpSizePacket);

	void handleSetBorderSize(ClientboundSetBorderSizePacket clientboundSetBorderSizePacket);

	void handleSetBorderWarningDelay(ClientboundSetBorderWarningDelayPacket clientboundSetBorderWarningDelayPacket);

	void handleSetBorderWarningDistance(ClientboundSetBorderWarningDistancePacket clientboundSetBorderWarningDistancePacket);

	void handleSetBorderCenter(ClientboundSetBorderCenterPacket clientboundSetBorderCenterPacket);

	void handleTabListCustomisation(ClientboundTabListPacket clientboundTabListPacket);

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

	void handleLightUpdatePacket(ClientboundLightUpdatePacket clientboundLightUpdatePacket);

	void handleOpenBook(ClientboundOpenBookPacket clientboundOpenBookPacket);

	void handleOpenScreen(ClientboundOpenScreenPacket clientboundOpenScreenPacket);

	void handleMerchantOffers(ClientboundMerchantOffersPacket clientboundMerchantOffersPacket);

	void handleSetChunkCacheRadius(ClientboundSetChunkCacheRadiusPacket clientboundSetChunkCacheRadiusPacket);

	void handleSetSimulationDistance(ClientboundSetSimulationDistancePacket clientboundSetSimulationDistancePacket);

	void handleSetChunkCacheCenter(ClientboundSetChunkCacheCenterPacket clientboundSetChunkCacheCenterPacket);

	void handleBlockChangedAck(ClientboundBlockChangedAckPacket clientboundBlockChangedAckPacket);

	void setActionBarText(ClientboundSetActionBarTextPacket clientboundSetActionBarTextPacket);

	void setSubtitleText(ClientboundSetSubtitleTextPacket clientboundSetSubtitleTextPacket);

	void setTitleText(ClientboundSetTitleTextPacket clientboundSetTitleTextPacket);

	void setTitlesAnimation(ClientboundSetTitlesAnimationPacket clientboundSetTitlesAnimationPacket);

	void handleTitlesClear(ClientboundClearTitlesPacket clientboundClearTitlesPacket);

	void handleServerData(ClientboundServerDataPacket clientboundServerDataPacket);

	void handleCustomChatCompletions(ClientboundCustomChatCompletionsPacket clientboundCustomChatCompletionsPacket);

	void handleBundlePacket(ClientboundBundlePacket clientboundBundlePacket);

	void handleDamageEvent(ClientboundDamageEventPacket clientboundDamageEventPacket);

	void handleConfigurationStart(ClientboundStartConfigurationPacket clientboundStartConfigurationPacket);

	void handleChunkBatchStart(ClientboundChunkBatchStartPacket clientboundChunkBatchStartPacket);

	void handleChunkBatchFinished(ClientboundChunkBatchFinishedPacket clientboundChunkBatchFinishedPacket);
}

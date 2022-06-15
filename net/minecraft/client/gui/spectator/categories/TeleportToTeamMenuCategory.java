/*
 * Decompiled with CFR 0.2.0 (FabricMC d28b102d).
 */
package net.minecraft.client.gui.spectator.categories;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.List;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.spectator.SpectatorGui;
import net.minecraft.client.gui.spectator.SpectatorMenu;
import net.minecraft.client.gui.spectator.SpectatorMenuCategory;
import net.minecraft.client.gui.spectator.SpectatorMenuItem;
import net.minecraft.client.gui.spectator.categories.TeleportToPlayerMenuCategory;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.scores.PlayerTeam;

@Environment(value=EnvType.CLIENT)
public class TeleportToTeamMenuCategory
implements SpectatorMenuCategory,
SpectatorMenuItem {
    private static final Component TELEPORT_TEXT = Component.translatable("spectatorMenu.team_teleport");
    private static final Component TELEPORT_PROMPT = Component.translatable("spectatorMenu.team_teleport.prompt");
    private final List<SpectatorMenuItem> items = Lists.newArrayList();

    public TeleportToTeamMenuCategory() {
        Minecraft minecraft = Minecraft.getInstance();
        for (PlayerTeam playerTeam : minecraft.level.getScoreboard().getPlayerTeams()) {
            this.items.add(new TeamSelectionItem(playerTeam));
        }
    }

    @Override
    public List<SpectatorMenuItem> getItems() {
        return this.items;
    }

    @Override
    public Component getPrompt() {
        return TELEPORT_PROMPT;
    }

    @Override
    public void selectItem(SpectatorMenu spectatorMenu) {
        spectatorMenu.selectCategory(this);
    }

    @Override
    public Component getName() {
        return TELEPORT_TEXT;
    }

    @Override
    public void renderIcon(PoseStack poseStack, float f, int i) {
        RenderSystem.setShaderTexture(0, SpectatorGui.SPECTATOR_LOCATION);
        GuiComponent.blit(poseStack, 0, 0, 16.0f, 0.0f, 16, 16, 256, 256);
    }

    @Override
    public boolean isEnabled() {
        for (SpectatorMenuItem spectatorMenuItem : this.items) {
            if (!spectatorMenuItem.isEnabled()) continue;
            return true;
        }
        return false;
    }

    @Environment(value=EnvType.CLIENT)
    static class TeamSelectionItem
    implements SpectatorMenuItem {
        private final PlayerTeam team;
        private final ResourceLocation location;
        private final List<PlayerInfo> players;

        public TeamSelectionItem(PlayerTeam playerTeam) {
            this.team = playerTeam;
            this.players = Lists.newArrayList();
            for (String string : playerTeam.getPlayers()) {
                PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(string);
                if (playerInfo == null) continue;
                this.players.add(playerInfo);
            }
            if (this.players.isEmpty()) {
                this.location = DefaultPlayerSkin.getDefaultSkin();
            } else {
                String string2 = this.players.get(RandomSource.create().nextInt(this.players.size())).getProfile().getName();
                this.location = AbstractClientPlayer.getSkinLocation(string2);
                AbstractClientPlayer.registerSkinTexture(this.location, string2);
            }
        }

        @Override
        public void selectItem(SpectatorMenu spectatorMenu) {
            spectatorMenu.selectCategory(new TeleportToPlayerMenuCategory(this.players));
        }

        @Override
        public Component getName() {
            return this.team.getDisplayName();
        }

        @Override
        public void renderIcon(PoseStack poseStack, float f, int i) {
            Integer integer = this.team.getColor().getColor();
            if (integer != null) {
                float g = (float)(integer >> 16 & 0xFF) / 255.0f;
                float h = (float)(integer >> 8 & 0xFF) / 255.0f;
                float j = (float)(integer & 0xFF) / 255.0f;
                GuiComponent.fill(poseStack, 1, 1, 15, 15, Mth.color(g * f, h * f, j * f) | i << 24);
            }
            RenderSystem.setShaderTexture(0, this.location);
            RenderSystem.setShaderColor(f, f, f, (float)i / 255.0f);
            PlayerFaceRenderer.draw(poseStack, 2, 2, 12);
        }

        @Override
        public boolean isEnabled() {
            return !this.players.isEmpty();
        }
    }
}


package net.minecraft.server.bossevents;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;

public class CustomBossEvent extends ServerBossEvent {
	private final ResourceLocation id;
	private final Set<UUID> players = Sets.<UUID>newHashSet();
	private int value;
	private int max = 100;

	public CustomBossEvent(ResourceLocation resourceLocation, Component component) {
		super(component, BossEvent.BossBarColor.WHITE, BossEvent.BossBarOverlay.PROGRESS);
		this.id = resourceLocation;
		this.setProgress(0.0F);
	}

	public ResourceLocation getTextId() {
		return this.id;
	}

	@Override
	public void addPlayer(ServerPlayer serverPlayer) {
		super.addPlayer(serverPlayer);
		this.players.add(serverPlayer.getUUID());
	}

	public void addOfflinePlayer(UUID uUID) {
		this.players.add(uUID);
	}

	@Override
	public void removePlayer(ServerPlayer serverPlayer) {
		super.removePlayer(serverPlayer);
		this.players.remove(serverPlayer.getUUID());
	}

	@Override
	public void removeAllPlayers() {
		super.removeAllPlayers();
		this.players.clear();
	}

	public int getValue() {
		return this.value;
	}

	public int getMax() {
		return this.max;
	}

	public void setValue(int i) {
		this.value = i;
		this.setProgress(Mth.clamp((float)i / (float)this.max, 0.0F, 1.0F));
	}

	public void setMax(int i) {
		this.max = i;
		this.setProgress(Mth.clamp((float)this.value / (float)i, 0.0F, 1.0F));
	}

	public final Component getDisplayName() {
		return ComponentUtils.wrapInSquareBrackets(this.getName())
			.withStyle(
				style -> style.withColor(this.getColor().getFormatting())
						.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent(this.getTextId().toString())))
						.withInsertion(this.getTextId().toString())
			);
	}

	public boolean setPlayers(Collection<ServerPlayer> collection) {
		Set<UUID> set = Sets.<UUID>newHashSet();
		Set<ServerPlayer> set2 = Sets.<ServerPlayer>newHashSet();

		for (UUID uUID : this.players) {
			boolean bl = false;

			for (ServerPlayer serverPlayer : collection) {
				if (serverPlayer.getUUID().equals(uUID)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				set.add(uUID);
			}
		}

		for (ServerPlayer serverPlayer2 : collection) {
			boolean bl = false;

			for (UUID uUID2 : this.players) {
				if (serverPlayer2.getUUID().equals(uUID2)) {
					bl = true;
					break;
				}
			}

			if (!bl) {
				set2.add(serverPlayer2);
			}
		}

		for (UUID uUID : set) {
			for (ServerPlayer serverPlayer3 : this.getPlayers()) {
				if (serverPlayer3.getUUID().equals(uUID)) {
					this.removePlayer(serverPlayer3);
					break;
				}
			}

			this.players.remove(uUID);
		}

		for (ServerPlayer serverPlayer2 : set2) {
			this.addPlayer(serverPlayer2);
		}

		return !set.isEmpty() || !set2.isEmpty();
	}

	public CompoundTag save() {
		CompoundTag compoundTag = new CompoundTag();
		compoundTag.putString("Name", Component.Serializer.toJson(this.name));
		compoundTag.putBoolean("Visible", this.isVisible());
		compoundTag.putInt("Value", this.value);
		compoundTag.putInt("Max", this.max);
		compoundTag.putString("Color", this.getColor().getName());
		compoundTag.putString("Overlay", this.getOverlay().getName());
		compoundTag.putBoolean("DarkenScreen", this.shouldDarkenScreen());
		compoundTag.putBoolean("PlayBossMusic", this.shouldPlayBossMusic());
		compoundTag.putBoolean("CreateWorldFog", this.shouldCreateWorldFog());
		ListTag listTag = new ListTag();

		for (UUID uUID : this.players) {
			listTag.add(NbtUtils.createUUID(uUID));
		}

		compoundTag.put("Players", listTag);
		return compoundTag;
	}

	public static CustomBossEvent load(CompoundTag compoundTag, ResourceLocation resourceLocation) {
		CustomBossEvent customBossEvent = new CustomBossEvent(resourceLocation, Component.Serializer.fromJson(compoundTag.getString("Name")));
		customBossEvent.setVisible(compoundTag.getBoolean("Visible"));
		customBossEvent.setValue(compoundTag.getInt("Value"));
		customBossEvent.setMax(compoundTag.getInt("Max"));
		customBossEvent.setColor(BossEvent.BossBarColor.byName(compoundTag.getString("Color")));
		customBossEvent.setOverlay(BossEvent.BossBarOverlay.byName(compoundTag.getString("Overlay")));
		customBossEvent.setDarkenScreen(compoundTag.getBoolean("DarkenScreen"));
		customBossEvent.setPlayBossMusic(compoundTag.getBoolean("PlayBossMusic"));
		customBossEvent.setCreateWorldFog(compoundTag.getBoolean("CreateWorldFog"));
		ListTag listTag = compoundTag.getList("Players", 11);

		for (int i = 0; i < listTag.size(); i++) {
			customBossEvent.addOfflinePlayer(NbtUtils.loadUUID(listTag.get(i)));
		}

		return customBossEvent;
	}

	public void onPlayerConnect(ServerPlayer serverPlayer) {
		if (this.players.contains(serverPlayer.getUUID())) {
			this.addPlayer(serverPlayer);
		}
	}

	public void onPlayerDisconnect(ServerPlayer serverPlayer) {
		super.removePlayer(serverPlayer);
	}
}

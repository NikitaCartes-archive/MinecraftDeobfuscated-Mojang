package net.minecraft.server.players;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.io.IOUtils;

public class GameProfileCache {
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
	private static boolean usesAuthentication;
	private final Map<String, GameProfileCache.GameProfileInfo> profilesByName = Maps.<String, GameProfileCache.GameProfileInfo>newHashMap();
	private final Map<UUID, GameProfileCache.GameProfileInfo> profilesByUUID = Maps.<UUID, GameProfileCache.GameProfileInfo>newHashMap();
	private final Deque<GameProfile> profileMRUList = Lists.<GameProfile>newLinkedList();
	private final GameProfileRepository profileRepository;
	protected final Gson gson;
	private final File file;
	private static final ParameterizedType GAMEPROFILE_ENTRY_TYPE = new ParameterizedType() {
		public Type[] getActualTypeArguments() {
			return new Type[]{GameProfileCache.GameProfileInfo.class};
		}

		public Type getRawType() {
			return List.class;
		}

		public Type getOwnerType() {
			return null;
		}
	};

	public GameProfileCache(GameProfileRepository gameProfileRepository, File file) {
		this.profileRepository = gameProfileRepository;
		this.file = file;
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeHierarchyAdapter(GameProfileCache.GameProfileInfo.class, new GameProfileCache.Serializer());
		this.gson = gsonBuilder.create();
		this.load();
	}

	private static GameProfile lookupGameProfile(GameProfileRepository gameProfileRepository, String string) {
		final GameProfile[] gameProfiles = new GameProfile[1];
		ProfileLookupCallback profileLookupCallback = new ProfileLookupCallback() {
			@Override
			public void onProfileLookupSucceeded(GameProfile gameProfile) {
				gameProfiles[0] = gameProfile;
			}

			@Override
			public void onProfileLookupFailed(GameProfile gameProfile, Exception exception) {
				gameProfiles[0] = null;
			}
		};
		gameProfileRepository.findProfilesByNames(new String[]{string}, Agent.MINECRAFT, profileLookupCallback);
		if (!usesAuthentication() && gameProfiles[0] == null) {
			UUID uUID = Player.createPlayerUUID(new GameProfile(null, string));
			GameProfile gameProfile = new GameProfile(uUID, string);
			profileLookupCallback.onProfileLookupSucceeded(gameProfile);
		}

		return gameProfiles[0];
	}

	public static void setUsesAuthentication(boolean bl) {
		usesAuthentication = bl;
	}

	private static boolean usesAuthentication() {
		return usesAuthentication;
	}

	public void add(GameProfile gameProfile) {
		this.add(gameProfile, null);
	}

	private void add(GameProfile gameProfile, Date date) {
		UUID uUID = gameProfile.getId();
		if (date == null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(new Date());
			calendar.add(2, 1);
			date = calendar.getTime();
		}

		GameProfileCache.GameProfileInfo gameProfileInfo = new GameProfileCache.GameProfileInfo(gameProfile, date);
		if (this.profilesByUUID.containsKey(uUID)) {
			GameProfileCache.GameProfileInfo gameProfileInfo2 = (GameProfileCache.GameProfileInfo)this.profilesByUUID.get(uUID);
			this.profilesByName.remove(gameProfileInfo2.getProfile().getName().toLowerCase(Locale.ROOT));
			this.profileMRUList.remove(gameProfile);
		}

		this.profilesByName.put(gameProfile.getName().toLowerCase(Locale.ROOT), gameProfileInfo);
		this.profilesByUUID.put(uUID, gameProfileInfo);
		this.profileMRUList.addFirst(gameProfile);
		this.save();
	}

	@Nullable
	public GameProfile get(String string) {
		String string2 = string.toLowerCase(Locale.ROOT);
		GameProfileCache.GameProfileInfo gameProfileInfo = (GameProfileCache.GameProfileInfo)this.profilesByName.get(string2);
		if (gameProfileInfo != null && new Date().getTime() >= gameProfileInfo.expirationDate.getTime()) {
			this.profilesByUUID.remove(gameProfileInfo.getProfile().getId());
			this.profilesByName.remove(gameProfileInfo.getProfile().getName().toLowerCase(Locale.ROOT));
			this.profileMRUList.remove(gameProfileInfo.getProfile());
			gameProfileInfo = null;
		}

		if (gameProfileInfo != null) {
			GameProfile gameProfile = gameProfileInfo.getProfile();
			this.profileMRUList.remove(gameProfile);
			this.profileMRUList.addFirst(gameProfile);
		} else {
			GameProfile gameProfile = lookupGameProfile(this.profileRepository, string2);
			if (gameProfile != null) {
				this.add(gameProfile);
				gameProfileInfo = (GameProfileCache.GameProfileInfo)this.profilesByName.get(string2);
			}
		}

		this.save();
		return gameProfileInfo == null ? null : gameProfileInfo.getProfile();
	}

	@Nullable
	public GameProfile get(UUID uUID) {
		GameProfileCache.GameProfileInfo gameProfileInfo = (GameProfileCache.GameProfileInfo)this.profilesByUUID.get(uUID);
		return gameProfileInfo == null ? null : gameProfileInfo.getProfile();
	}

	private GameProfileCache.GameProfileInfo getProfileInfo(UUID uUID) {
		GameProfileCache.GameProfileInfo gameProfileInfo = (GameProfileCache.GameProfileInfo)this.profilesByUUID.get(uUID);
		if (gameProfileInfo != null) {
			GameProfile gameProfile = gameProfileInfo.getProfile();
			this.profileMRUList.remove(gameProfile);
			this.profileMRUList.addFirst(gameProfile);
		}

		return gameProfileInfo;
	}

	public void load() {
		BufferedReader bufferedReader = null;

		try {
			bufferedReader = Files.newReader(this.file, StandardCharsets.UTF_8);
			List<GameProfileCache.GameProfileInfo> list = GsonHelper.fromJson(this.gson, bufferedReader, GAMEPROFILE_ENTRY_TYPE);
			this.profilesByName.clear();
			this.profilesByUUID.clear();
			this.profileMRUList.clear();
			if (list != null) {
				for (GameProfileCache.GameProfileInfo gameProfileInfo : Lists.reverse(list)) {
					if (gameProfileInfo != null) {
						this.add(gameProfileInfo.getProfile(), gameProfileInfo.getExpirationDate());
					}
				}
			}
		} catch (FileNotFoundException var9) {
		} catch (JsonParseException var10) {
		} finally {
			IOUtils.closeQuietly(bufferedReader);
		}
	}

	public void save() {
		String string = this.gson.toJson(this.getTopMRUProfiles(1000));
		BufferedWriter bufferedWriter = null;

		try {
			bufferedWriter = Files.newWriter(this.file, StandardCharsets.UTF_8);
			bufferedWriter.write(string);
			return;
		} catch (FileNotFoundException var8) {
			return;
		} catch (IOException var9) {
		} finally {
			IOUtils.closeQuietly(bufferedWriter);
		}
	}

	private List<GameProfileCache.GameProfileInfo> getTopMRUProfiles(int i) {
		List<GameProfileCache.GameProfileInfo> list = Lists.<GameProfileCache.GameProfileInfo>newArrayList();

		for (GameProfile gameProfile : Lists.newArrayList(Iterators.limit(this.profileMRUList.iterator(), i))) {
			GameProfileCache.GameProfileInfo gameProfileInfo = this.getProfileInfo(gameProfile.getId());
			if (gameProfileInfo != null) {
				list.add(gameProfileInfo);
			}
		}

		return list;
	}

	class GameProfileInfo {
		private final GameProfile profile;
		private final Date expirationDate;

		private GameProfileInfo(GameProfile gameProfile, Date date) {
			this.profile = gameProfile;
			this.expirationDate = date;
		}

		public GameProfile getProfile() {
			return this.profile;
		}

		public Date getExpirationDate() {
			return this.expirationDate;
		}
	}

	class Serializer implements JsonDeserializer<GameProfileCache.GameProfileInfo>, JsonSerializer<GameProfileCache.GameProfileInfo> {
		private Serializer() {
		}

		public JsonElement serialize(GameProfileCache.GameProfileInfo gameProfileInfo, Type type, JsonSerializationContext jsonSerializationContext) {
			JsonObject jsonObject = new JsonObject();
			jsonObject.addProperty("name", gameProfileInfo.getProfile().getName());
			UUID uUID = gameProfileInfo.getProfile().getId();
			jsonObject.addProperty("uuid", uUID == null ? "" : uUID.toString());
			jsonObject.addProperty("expiresOn", GameProfileCache.DATE_FORMAT.format(gameProfileInfo.getExpirationDate()));
			return jsonObject;
		}

		public GameProfileCache.GameProfileInfo deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
			if (jsonElement.isJsonObject()) {
				JsonObject jsonObject = jsonElement.getAsJsonObject();
				JsonElement jsonElement2 = jsonObject.get("name");
				JsonElement jsonElement3 = jsonObject.get("uuid");
				JsonElement jsonElement4 = jsonObject.get("expiresOn");
				if (jsonElement2 != null && jsonElement3 != null) {
					String string = jsonElement3.getAsString();
					String string2 = jsonElement2.getAsString();
					Date date = null;
					if (jsonElement4 != null) {
						try {
							date = GameProfileCache.DATE_FORMAT.parse(jsonElement4.getAsString());
						} catch (ParseException var14) {
							date = null;
						}
					}

					if (string2 != null && string != null) {
						UUID uUID;
						try {
							uUID = UUID.fromString(string);
						} catch (Throwable var13) {
							return null;
						}

						return GameProfileCache.this.new GameProfileInfo(new GameProfile(uUID, string2), date);
					} else {
						return null;
					}
				} else {
					return null;
				}
			} else {
				return null;
			}
		}
	}
}

package com.mojang.realmsclient.gui;

import com.mojang.realmsclient.dto.RealmsNews;
import com.mojang.realmsclient.util.RealmsPersistence;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class RealmsNewsManager {
	private final RealmsPersistence newsLocalStorage;
	private boolean hasUnreadNews;
	private String newsLink;

	public RealmsNewsManager(RealmsPersistence realmsPersistence) {
		this.newsLocalStorage = realmsPersistence;
		RealmsPersistence.RealmsPersistenceData realmsPersistenceData = realmsPersistence.read();
		this.hasUnreadNews = realmsPersistenceData.hasUnreadNews;
		this.newsLink = realmsPersistenceData.newsLink;
	}

	public boolean hasUnreadNews() {
		return this.hasUnreadNews;
	}

	public String newsLink() {
		return this.newsLink;
	}

	public void updateUnreadNews(RealmsNews realmsNews) {
		RealmsPersistence.RealmsPersistenceData realmsPersistenceData = this.updateNewsStorage(realmsNews);
		this.hasUnreadNews = realmsPersistenceData.hasUnreadNews;
		this.newsLink = realmsPersistenceData.newsLink;
	}

	private RealmsPersistence.RealmsPersistenceData updateNewsStorage(RealmsNews realmsNews) {
		RealmsPersistence.RealmsPersistenceData realmsPersistenceData = this.newsLocalStorage.read();
		if (realmsNews.newsLink != null && !realmsNews.newsLink.equals(realmsPersistenceData.newsLink)) {
			RealmsPersistence.RealmsPersistenceData realmsPersistenceData2 = new RealmsPersistence.RealmsPersistenceData();
			realmsPersistenceData2.newsLink = realmsNews.newsLink;
			realmsPersistenceData2.hasUnreadNews = true;
			this.newsLocalStorage.save(realmsPersistenceData2);
			return realmsPersistenceData2;
		} else {
			return realmsPersistenceData;
		}
	}
}

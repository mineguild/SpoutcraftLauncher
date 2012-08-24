/*
 * This file is part of Spoutcraft Launcher.
 *
 * Copyright (c) 2011-2012, SpoutDev <http://www.spout.org/>
 * Spoutcraft Launcher is licensed under the SpoutDev License Version 1.
 *
 * Spoutcraft Launcher is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * In addition, 180 days after any changes are published, you can use the
 * software, incorporating those changes, under the terms of the MIT license,
 * as described in the SpoutDev License Version 1.
 *
 * Spoutcraft Launcher is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License,
 * the MIT license and the SpoutDev License Version 1 along with this program.
 * If not, see <http://www.gnu.org/licenses/> for the GNU Lesser General Public
 * License and see <http://www.spout.org/SpoutDevLicenseV1.txt> for the full license,
 * including the MIT license.
 */
package org.spoutcraft.launcher.yml;

import java.util.Map;

import org.spoutcraft.launcher.Settings;
import org.spoutcraft.launcher.api.Build;
import org.spoutcraft.launcher.api.util.MirrorUtils;
import org.spoutcraft.launcher.api.util.YAMLProcessor;
import org.spoutcraft.launcher.exceptions.NoMirrorsAvailableException;

public class SpoutcraftBuild {
	private String minecraftVersion;
	private String latestVersion;
	private int build;
	Map<String, Object> libraries;
	private String hash;

	private SpoutcraftBuild(String minecraft, String latest, int build, Map<String, Object> libraries, String hash) {
		this.minecraftVersion = minecraft;
		this.latestVersion = latest;
		this.build = build;
		this.libraries = libraries;
		this.hash = hash;
	}

	public String getMD5() {
		return hash;
	}

	public int getBuild() {
		return build;
	}

	public String getMinecraftVersion() {
		return minecraftVersion;
	}

	public String getLatestMinecraftVersion() {
		return latestVersion;
	}

	public String getMinecraftURL(String user) {
		return "http://s3.amazonaws.com/MinecraftDownload/minecraft.jar?user=" + user + "&ticket=1";
	}

	public String getSpoutcraftURL() throws NoMirrorsAvailableException {
		return MirrorUtils.getMirrorUrl("Spoutcraft/" + build + "/spoutcraft-dev-SNAPSHOT.jar");
	}

	public void install() {
		YAMLProcessor config = Resources.Spoutcraft.getYAML();
		config.setProperty("current", getBuild());
		config.save();
	}

	public int getInstalledBuild() {
		YAMLProcessor config = Resources.Spoutcraft.getYAML();
		return config.getInt("current", -1);
	}

	public String getPatchURL() {
		String mirrorURL = "patch/minecraft_";
		mirrorURL += getLatestMinecraftVersion();
		mirrorURL += "-" + getMinecraftVersion() + ".patch";
		String fallbackURL = "http://get.spout.org/patch/minecraft_";
		fallbackURL += getLatestMinecraftVersion();
		fallbackURL += "-" + getMinecraftVersion() + ".patch";
		return MirrorUtils.getMirrorUrl(mirrorURL, fallbackURL);
	}

	public Map<String, Object> getLibraries() {
		return libraries;
	}

	@SuppressWarnings("unchecked")
	public static SpoutcraftBuild getSpoutcraftBuild() {
		YAMLProcessor config = Resources.Spoutcraft.getYAML();
		Map<Integer, Object> builds = (Map<Integer, Object>) config.getProperty("builds");
		int latest = config.getInt("latest", -1);
		int recommended = config.getInt("recommended", -1);
		int selected = Settings.getSpoutcraftSelectedBuild();
		if (Settings.getSpoutcraftBuild() == Build.RECOMMENDED) {
			selected = recommended;
		} else if (Settings.getSpoutcraftBuild() == Build.DEV) {
			selected = latest;
		}

		if (selected < 0 || !builds.containsKey(selected)) {
			selected = recommended;
		}

		Map<Object, Object> build = (Map<Object, Object>) builds.get(selected);
		Map<String, Object> libs = (Map<String, Object>) build.get("libraries");
		String hash = String.valueOf(build.get("hash"));
		return new SpoutcraftBuild(String.valueOf(build.get("minecraft")), Resources.getLatestMinecraftVersion(), selected, libs, hash);
	}
}

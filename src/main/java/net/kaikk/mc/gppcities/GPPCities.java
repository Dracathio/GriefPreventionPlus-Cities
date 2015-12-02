/*
    GriefPreventionPlus-Cities
    Copyright (C) 2015 Antonino Kai Pocorobba

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package net.kaikk.mc.gppcities;
import java.io.File;
import java.util.logging.Level;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;


public class GPPCities extends JavaPlugin{
	private static GPPCities instance;
	private EventListener eventListener;
	private DataStore dataStore;
	
	Config config;

	@Override
	public void onEnable() {
		log("Loading...");
		long loadingTime = System.currentTimeMillis();
		instance=this;
		
		this.config = new Config();
		
		Messages.load();
		// Inizialize database
		try {
			// Get data
			FileConfiguration gpConfig = YamlConfiguration.loadConfiguration(new File("plugins"+File.separator+"GriefPreventionData"+File.separator+"config.yml"));
			
			String dbaddress=gpConfig.getString("GriefPrevention.Database.URL", "");
			String dbname=gpConfig.getString("GriefPrevention.Database.UserName", "");
			String dbpassword=gpConfig.getString("GriefPrevention.Database.Password", "");
			
			if (dbname.isEmpty() || dbaddress.isEmpty()) {
				log("Database settings are missing! This plugin needs GriefPreventionPlus.");
				return;
			} else {
				dataStore = new DataStore(instance, dbaddress, dbname, dbpassword);
			}
		} catch (Exception e) {
			log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			log("Plugin disabled");
			return;
		}
		
		this.eventListener=new EventListener(this);
		
		this.getCommand("city").setExecutor(new CommandExec());
		this.getCommand("citychat").setExecutor(new CommandExec());
		this.getCommand("citychatspy").setExecutor(new CommandExec());
		this.getCommand("cityadmin").setExecutor(new CommandExec());

		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(this.eventListener, this);

		// Schedule InactiveCitiesCheck
		if (this.config.InactivityDays>0 && this.config.InactivityCheckMinutes>0) {
			new InactivityCheckTask(instance).runTaskTimer(instance, 500, 4);
		}
		
		log("Loaded ("+((System.currentTimeMillis()-loadingTime)/1000.000)+" seconds)");
	}

	@Override
	public void onDisable() {
		log("Unloading...");
		if (this.dataStore!=null) {
			this.dataStore.citiesMap=null;
			this.dataStore.playerData=null;
			this.dataStore.dbClose();
			this.dataStore=null;
		}
		instance=null;
		System.gc(); // garbage collector
		log("Done.");
	}

	void log (String msg) {
		getLogger().info(msg);
	}
	
	void log (Level level, String msg) {
		getLogger().log(level, msg);
	}

	public static GPPCities getInstance() {
		return instance;
	}

	public DataStore getDataStore() {
		return dataStore;
	}
}


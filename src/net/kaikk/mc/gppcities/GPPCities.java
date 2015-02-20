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
	public static GPPCities gppc;
	public final EventListener eventListener = new EventListener();
	DataStore ds;
	
	protected Config config;
	

	@Override
	public void onEnable() {
		log("Loading...");
		long loadingTime = System.currentTimeMillis();
		gppc=this;
		
		this.config = new Config();
		// Inizialize database
		try {
			// Get data
			FileConfiguration gpConfig = YamlConfiguration.loadConfiguration(new File("plugins"+File.separator+"GriefPreventionData"+File.separator+"config.yml"));
			
			String dbaddress=gpConfig.getString("GriefPrevention.Database.URL", "");
			String dbname=gpConfig.getString("GriefPrevention.Database.UserName", "");
			String dbpassword=gpConfig.getString("GriefPrevention.Database.Password", "");
			
			if (dbname.isEmpty() || dbpassword.isEmpty() || dbaddress.isEmpty()) {
				log("Database settings are missing! This plugin needs GriefPreventionPlus.");
				return;
			} else {
				ds = new DataStore(gppc, dbaddress, dbname, dbpassword);
			}
		} catch (Exception e) {
			log(Level.SEVERE, e.getMessage());
			e.printStackTrace();
			log("Plugin disabled");
			return;
		}

		this.getCommand("city").setExecutor(new CommandExec());
		this.getCommand("citychat").setExecutor(new CommandExec());
		this.getCommand("cityadmin").setExecutor(new CommandExec());

		PluginManager pm = getServer().getPluginManager();
		
		pm.registerEvents(this.eventListener, this);
		
		// redirect some GP command to GPC's Executor
		GPPCities.gppc.getServer().getPluginCommand("abandonclaim").setExecutor(new CommandExec());
		GPPCities.gppc.getServer().getPluginCommand("abandontoplevelclaim").setExecutor(new CommandExec());
		GPPCities.gppc.getServer().getPluginCommand("abandonallclaims").setExecutor(new CommandExec());
		GPPCities.gppc.getServer().getPluginCommand("deleteclaim").setExecutor(new CommandExec());
		GPPCities.gppc.getServer().getPluginCommand("deleteallclaims").setExecutor(new CommandExec());
		GPPCities.gppc.getServer().getPluginCommand("transferclaim").setExecutor(new CommandExec());
		
		Messages.load();
		
		// Schedule InactiveCitiesCheck
		if (this.config.InactivityDays>0 && this.config.InactivityCheckMinutes>0) {
			new InactivityCheckTask(gppc).runTaskTimer(gppc, 200, (this.config.InactivityCheckMinutes*1200));
		}
		
		log("Loaded ("+((System.currentTimeMillis()-loadingTime)/1000.000)+" seconds)");
	}

	@Override
	public void onDisable() {
		log("Unloading...");
		if (this.ds!=null) {
			this.ds.citiesMap=null;
			this.ds.playerData=null;
			this.ds.dbClose();
			this.ds=null;
		}
		gppc=null;
		System.gc(); // garbage collector
		log("Done.");
	}
	
	void log (String msg) {
		getLogger().info(msg);
	}
	
	void log (Level level, String msg) {
		getLogger().log(level, msg);
	}

}


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
import java.io.IOException;
import java.util.logging.Level;

import net.kaikk.mc.gpp.GriefPreventionPlus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Config {
	final static String configFilePath = "plugins" + File.separator + "GriefPreventionPlus-Cities" + File.separator + "config.yml";
	
	int CityMinSize, ClaimBlocksPerCitizen, /*ClaimBlocksPerNationCitizen,*/ InactivityDays, InactivityCheckMinutes;
	boolean AdminClaimMessage;
	
	Config() {
		File configFile = new File(configFilePath);
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		
		this.CityMinSize=config.getInt("CityMinSize", 500);
		config.set("CityMinSize", this.CityMinSize);
		
		this.ClaimBlocksPerCitizen=config.getInt("ClaimableBlocksPerCitizen", 400);
		config.set("ClaimableBlocksPerCitizen", this.ClaimBlocksPerCitizen);
		/*
		this.ClaimBlocksPerNationCitizen=config.getInt("ClaimableBlocksPerNationCitizen", 100);
		config.set("ClaimableBlocksPerNationCitizen", this.ClaimBlocksPerNationCitizen);
		*/
		this.InactivityDays=config.getInt("InactivityDays", (GriefPreventionPlus.getInstance().config.claims_expirationDays!=0 ? GriefPreventionPlus.getInstance().config.claims_expirationDays-1 : 30));
		config.set("InactivityDays", this.InactivityDays);
		
		this.InactivityCheckMinutes=config.getInt("InactivityCheckMinutes", 60);
		config.set("InactivityCheckMinutes", this.InactivityCheckMinutes);
		
		this.AdminClaimMessage=config.getBoolean("AdminClaimMessage", false);
		config.set("AdminClaimMessage", this.AdminClaimMessage);
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			GPPCities.getInstance().log(Level.SEVERE, "Couldn't create or save config file.");
			e.printStackTrace();
		}
	}
	
}

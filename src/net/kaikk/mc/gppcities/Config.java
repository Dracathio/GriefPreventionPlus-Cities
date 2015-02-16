package net.kaikk.mc.gppcities;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

import net.kaikk.mc.gpp.GriefPreventionPlus;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

class Config {
	final static String configFilePath = "plugins" + File.separator + "GriefPreventionPlus-Cities" + File.separator + "config.yml";
	
	int CityMinSize, /*ClaimableBlocksPerCitizen, ClaimableBlocksPerNationCitizen,*/ InactivityDays, InactivityCheckMinutes;
	
	Config() {
		File configFile = new File(configFilePath);
		FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
		
		this.CityMinSize=config.getInt("CityMinSize", 500);
		config.set("CityMinSize", this.CityMinSize);
		/*
		this.ClaimableBlocksPerCitizen=config.getInt("ClaimableBlocksPerCitizen", 100);
		config.set("ClaimableBlocksPerCitizen", this.ClaimableBlocksPerCitizen);
		
		this.ClaimableBlocksPerNationCitizen=config.getInt("ClaimableBlocksPerNationCitizen", 100);
		config.set("ClaimableBlocksPerNationCitizen", this.ClaimableBlocksPerNationCitizen);
		*/
		this.InactivityDays=config.getInt("InactivityDays", (GriefPreventionPlus.instance.config_claims_expirationDays!=0 ? GriefPreventionPlus.instance.config_claims_expirationDays-1 : 30));
		config.set("InactivityDays", this.InactivityDays);
		
		this.InactivityCheckMinutes=config.getInt("InactivityCheckMinutes", 60);
		config.set("InactivityCheckMinutes", this.InactivityCheckMinutes);
	
		
		try {
			config.save(configFile);
		} catch (IOException e) {
			GPPCities.gppc.log(Level.SEVERE, "Couldn't create or save config file.");
			e.printStackTrace();
		}
	}
	
}

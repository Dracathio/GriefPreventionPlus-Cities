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

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.ClaimPermission;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import net.kaikk.mc.gppcities.City.Citizen;

class DataStore {
	GPPCities instance;
	
	private String dbUrl;
	private String username;
	private String password;
	
	private Connection database = null;
	HashMap<Integer, City> citiesMap = new HashMap<Integer, City>();
	HashMap<UUID, PlayerData> playerData = new HashMap<UUID, PlayerData>();

	ArrayList<UUID> cityChat = new ArrayList<UUID>();
	ArrayList<UUID> cityChatSpy = new ArrayList<UUID>();
	
	DataStore(GPPCities instance, String url, String username, String password) throws Exception {
		this.instance=instance;
		this.dbUrl = url;
		this.username = username;
		this.password = password;
		
		try {
			//load the java driver for mySQL
			Class.forName("com.mysql.jdbc.Driver");
		} catch(Exception e) {
			log(Level.SEVERE, "Unable to load Java's mySQL database driver.  Check to make sure you've installed it properly.");
			throw e;
		}
		
		try {
			this.dbCheck();
		} catch(Exception e) {
			log(Level.SEVERE, "Unable to connect to database.  Check your config file settings.");
			throw e;
		}
		
		try {
			Statement statement = database.createStatement();
			
			ResultSet results = statement.executeQuery("SHOW TABLES LIKE \"gpp_claims\";");
			if(results.next()) {
				// Creates GPC tables on the database
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS gppc_cities ("+
									"id int(11) NOT NULL,"+
									"cname varchar(32) NOT NULL,"+
									"creationDate timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
									"motdRes varchar(100) NOT NULL DEFAULT \"\","+
									"motdOut varchar(100) NOT NULL DEFAULT \"\","+
									"isJoinable tinyint(1) NOT NULL DEFAULT \"0\","+
									"spawnX int(11) NOT NULL,"+
									"spawnY int(11) NOT NULL,"+
									"spawnZ int(11) NOT NULL,"+
									"perms int(11) NOT NULL,"+
									"PRIMARY KEY (id)"+
									");");
				
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS gppc_plots ("+
									"id int(11) NOT NULL,"+
									"cid int(11) NOT NULL,"+
									"citizen binary(16) DEFAULT NULL COMMENT \"uuid\","+
									"motd varchar(100) NOT NULL DEFAULT \"\","+
									"assignedOn timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,"+
									"isTakeable tinyint(1) NOT NULL DEFAULT \"0\","+
									"PRIMARY KEY (id)"+
									");");
				
				
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS gppc_citizens ("+
									"id binary(16) NOT NULL COMMENT \"uuid\","+
									"cid int(11) NOT NULL,"+
									"perms tinyint(4) NOT NULL,"+
									"joinedOn timestamp NOT NULL,"+
									"PRIMARY KEY (id)"+
									");");
				
				statement.executeUpdate("CREATE TABLE IF NOT EXISTS gppc_bans ("+
						"id binary(16) NOT NULL COMMENT \"uuid\","+
						"cid int(11) NOT NULL,"+
						"PRIMARY KEY (id)"+
						");");
			} else {
				throw new Exception("GriefPreventionPlus MySQL tables not found.");
			}
		} catch(SQLException e) {
			log(Level.SEVERE, "Unable to create the necessary database tables. Details:");
			throw e;
		} catch(Exception e) {
			throw e;
		}

		try {
			Statement statement = database.createStatement();
			Statement statement2 = database.createStatement();
			// delete all orphan cities and plots from the database
			// load cities and plots data from the database
			
			long count=0;
			
			// Load cities
			this.dbCheck();
			instance.log("Loading cities...");
			ResultSet results = statement.executeQuery("SELECT id, cname, creationDate, motdRes, motdOut, isJoinable, spawnX, spawnY, spawnZ, perms FROM gppc_cities;");

			while (results.next()) {
				Claim claim = GriefPreventionPlus.getInstance().getDataStore().getClaim(results.getInt(1));
				
				if (claim==null) {
					int id = results.getInt(1);
					instance.log(Level.WARNING, "Removing orphan city ID("+id+") and all references.");
					statement2.executeUpdate("DELETE FROM gppc_citizens WHERE cid = "+id);
					statement2.executeUpdate("DELETE FROM gppc_plots WHERE cid = "+id);
					statement2.executeUpdate("DELETE FROM gppc_bans WHERE cid = "+id);
					statement2.executeUpdate("DELETE FROM gppc_cities WHERE id = "+id);
				} else {
					this.citiesMap.put(results.getInt(1), new City(claim, results.getString(2), new Date(results.getTimestamp(3).getTime()), results.getString(4), results.getString(5), results.getBoolean(6), new Location(claim.getWorld(), results.getInt(7), results.getInt(8), results.getInt(9)), results.getInt(10)));
					count++;
				}
			}
			instance.log("Loaded "+count+" cities.");
			count=0;
			
			
			// Load citizens
			this.dbCheck();
			instance.log("Loading citizens...");
			results = statement.executeQuery("SELECT id, cid, perms, joinedOn FROM gppc_citizens;");
			while (results.next()) {
				City city = this.citiesMap.get(results.getInt(2));
				if (city==null) {
					UUID uuid = toUUID(results.getBytes(1));
					instance.log(Level.WARNING, "Removing orphan citizen UUID["+uuid.toString()+"]");
					statement2.executeUpdate("DELETE FROM gppc_citizens WHERE id = "+UUIDtoHexString(uuid));
				} else {
					city.getCitizens().put(toUUID(results.getBytes(1)), city.new Citizen(toUUID(results.getBytes(1)), results.getByte(3), new Date(results.getTimestamp(4).getTime())));
					count++;
				}
			}
			instance.log("Loaded "+count+" citizens.");
			count=0;
			
			// Load plots
			this.dbCheck();
			instance.log("Loading plots...");
			
			Citizen citizen;
			results = statement.executeQuery("SELECT id, cid, citizen, motd, assignedOn, isTakeable FROM gppc_plots;");
			while (results.next()) {
				City city = this.citiesMap.get(results.getInt(2));
				boolean removeFlag=true;
				if (city!=null) {
					Claim claim = GriefPreventionPlus.getInstance().getDataStore().getClaim(results.getInt(2));
					
					if (claim!=null) {
						for (Claim subclaim : claim.getChildren()) {
							if (subclaim.getID()==results.getInt(1)) {
								if (results.getBytes(3)==null) {
									citizen=null;
								} else {
									citizen=city.getCitizen(GriefPreventionPlus.getInstance().getServer().getOfflinePlayer(toUUID(results.getBytes(3))).getUniqueId());
								}
								
								city.getPlots().put(results.getInt(1), city.new Plot(results.getInt(1), subclaim, citizen, results.getString(4), new Date(results.getTimestamp(5).getTime()), results.getBoolean(6)));
								
								count++;
								removeFlag=false;
								break;
							}
						}
					}
				}
				if (removeFlag) {
					instance.log(Level.WARNING, "Removing orphan plot ID("+results.getInt(1)+") city("+results.getInt(2)+")");
					statement2.executeUpdate("DELETE FROM gppc_plots WHERE id = "+results.getInt(1));
				}
			}
			instance.log("Loaded "+count+" plots.");
			
			// Load banned players (from Mayors who don't want this player on their city)
			this.dbCheck();
			results = statement.executeQuery("SELECT * FROM gppc_bans;");
			while (results.next()) {
				City city = this.citiesMap.get(results.getLong(2));
				if (city!=null) {
					city.getBannedPlayers().add(toUUID(results.getBytes(1)));
				} else {
					String uuidString = UUIDtoHexString(toUUID(results.getBytes(1)));
					instance.log(Level.WARNING, "Removing orphan ban ID("+uuidString+") city("+results.getInt(2)+")");
					statement2.executeUpdate("DELETE FROM gppc_bans WHERE id = "+uuidString);
				}
			}
			
			statement.close();
			statement2.close();
			
			ArrayList<City> citiesToRemove = new ArrayList<City>();
			for (Entry<Integer,City> entry : this.citiesMap.entrySet()) {
				if (!entry.getValue().isValid()) {
					citiesToRemove.add(entry.getValue());
				}
			}
			
			for (City city : citiesToRemove) {
				instance.log(Level.WARNING, "Removed invalid city \""+city.getName()+"\"");
				this.deleteCity(city);
			}
			
		} catch(Exception e) {
			log(Level.SEVERE, "Unable to read the database. Details:");
			throw e;
		}
	}
	
	/** Try to insert a new city on the database. It checks if it exists.
	 * Note that this DOES NOT check if a city with the same ID already exists. (if it exists, it will throw an SQLException)
	 * @return an error message, empty otherwise.
	 * */
	synchronized String newCity(Claim claim, String name, UUID mayor, Location loc) {
		if (name == "") {
			return Messages.CityNameInvalid.get();
		}

		if (this.citiesMap.containsKey(claim.getID())) {
			return Messages.ClaimAlreadyACity.get();
		}

		if (this.getCity(mayor)!=null) {
			return Messages.YouAlreadyOnAnotherCity.get();
		}
		
		if (this.getCity(name)!=null) {
			return Messages.CitySameNameExists.get();
		}
		
		if (mayor.equals(GriefPreventionPlus.UUID1)) {
			return Messages.CitiesNotAvailableOnAdminClaims.get();
		}

		try {
			this.dbCheck();
			Statement statement = database.createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_cities (id, cname, spawnX, spawnY, spawnZ, perms) VALUES ("+claim.getID()+", \""+name+"\", "+loc.getBlockX()+", "+loc.getBlockY()+", "+loc.getBlockZ()+", 0);");
			
			// Cities have public permission set by default
			claim.setPermission(GriefPreventionPlus.UUID0, ClaimPermission.ENTRY);
			
			this.citiesMap.put(claim.getID(), new City(claim, name, mayor, loc));
		} catch(SQLException e) {
			e.getStackTrace();
			log(Level.SEVERE, "Unable to create new city for claim id "+claim.getID()+" named '"+name+"' created by "+instance.getServer().getPlayer(mayor).getName());
			log(Level.SEVERE, e.getMessage());
			return "An error occurred with the database.";
		}
		return "";
	}
	
	/** delete a city */
	synchronized String deleteCity(City city) {
		try {
			this.dbCheck();
			Statement statement = database.createStatement();
			Integer id=city.getClaim().getID();
			statement.executeUpdate("DELETE FROM gppc_citizens WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_plots WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_bans WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_cities WHERE id = "+id);
			
			// remove bonus claimable blocks for the mayor
			int blocks=GPPCities.getInstance().config.ClaimBlocksPerCitizen*city.getCitizens().size();
			if (blocks>0) {
				if (city.isValid()) {
					DataStore.adjustClaimableBlocks(city.getMayor().getId(), -blocks);
					GPPCities.getInstance().log(Level.INFO, "Mayor "+city.getMayor().getName()+" lost "+blocks+" claimable blocks.");
				}
			}
			
			city.getClaim().dropPermission("[gpc.c"+id+"]");
			instance.getServer().broadcastMessage(Messages.CityHasBeenDisbanded.get(city.getName()));
			this.citiesMap.remove(id);
		} catch(SQLException e) {
			e.getStackTrace();
			log(Level.SEVERE, "Unable to delete city id "+city.getName());
			log(Level.SEVERE, e.getMessage());
			return "An error occurred with the database. Contact an administrator!";
		}

		return "";
	}

	
	/** get the city for this player
	 * @return the citizen's city, null otherwise*/
	City getCity(UUID id) {
		for (City city : this.citiesMap.values()) {
			if (city.getCitizens().containsKey(id)) {
				return city;
			}
		}
		return null;
	}

	/** Get the city by name (ignores case)
	 * @return the city, null otherwise
	 * */
	City getCity(String name) {
		for (City city : this.citiesMap.values()) {
			if (city.getName().equalsIgnoreCase(name)) {
				if (!city.isValid()) {
					this.instance.log(Level.WARNING, "Removed city "+city.getName()+": city is invalid");
					this.deleteCity(city);
					return null;
				}
				
				return city;
			}
		}
		return null;
	}
	
	City getCity(Claim claim) {
		return this.getCity(claim.getParent()!=null?claim.getParent().getID():claim.getID());
	}
	
	City getCity(int id) {
		return this.citiesMap.get(id);
	}
	

	
	synchronized void dbCheck() throws SQLException {
		if(this.database == null || this.database.isClosed()) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", this.username);
			connectionProps.put("password", this.password);
			
			this.database = DriverManager.getConnection(this.dbUrl, connectionProps); 
		}
	}
	
	synchronized void dbClose()  {
		try {
			if (!this.database.isClosed()) {
				this.database.close();
				this.database=null;
			}
		} catch (SQLException e) {
			
		}
	}
	
	/** turns a location into a string */
	public static String locationToString(Location location) {
		return locationToString(location, false);
	}
	
	public static String locationToString(Location location, boolean yaw) {
		return location.getWorld().getName()+";"+location.getBlockX()+";"+location.getBlockY()+";"+location.getBlockZ()+(yaw?";"+((int) location.getYaw()):"");
	}
	
	/** turns a location string back into a location */
	public static Location locationFromString(String string) throws Exception {
		//split the input string on the space
		String[] elements = string.split(";");
	    
		//expect four elements - world name, X, Y, and Z, respectively
		if(elements.length < 4) {
			throw new Exception("Expected four distinct parts to the location string: \"" + string + "\"");
		}
	    
		//identify world the claim is in
		World world = GPPCities.getInstance().getServer().getWorld(elements[0]);
		if(world == null) {
			throw new Exception("World not found: \"" + elements[0] + "\"");
		}
		
		//convert those numerical strings to integer values
	    int x = Integer.parseInt(elements[1]);
	    int y = Integer.parseInt(elements[2]);
	    int z = Integer.parseInt(elements[3]);
	    
	    float yaw=0f;
	    if (elements.length==5) {
	    	yaw=Float.parseFloat(elements[4]);
	    }
	    
	    return new Location(world, x, y, z, yaw, 0f);
	}
	
	void cityChatSpy(String message, City city) {
		for (UUID uuid : this.cityChatSpy) {
			Player spy = instance.getServer().getPlayer(uuid);
			if (spy!=null && city.getCitizen(uuid)!=null) {
				spy.sendMessage(message);
			}
		}
	}
	
	
	/** This static method will merge an array of strings from a specific index 
	 * @return null if arrayString.length < i*/
	static String mergeStringArrayFromIndex(String[] arrayString, int i) {
		if (i<arrayString.length){
			String string=arrayString[i];
			i++;
			for(;i<arrayString.length;i++){
				string=string+" "+arrayString[i];
			}
			return string;
		}
		return null;
	}
	
	
	/** log into console */
	private void log(Level level, String msg) {
		instance.getLogger().log(level, msg);
	}

	@SuppressWarnings("deprecation")
	static Player getOnlinePlayer(String name) {
		return GPPCities.getInstance().getServer().getPlayer(name);
	}

	public static UUID toUUID(byte[] bytes) {
	    if (bytes.length != 16) {
	        throw new IllegalArgumentException();
	    }
	    int i = 0;
	    long msl = 0;
	    for (; i < 8; i++) {
	        msl = (msl << 8) | (bytes[i] & 0xFF);
	    }
	    long lsl = 0;
	    for (; i < 16; i++) {
	        lsl = (lsl << 8) | (bytes[i] & 0xFF);
	    }
	    return new UUID(msl, lsl);
	}
	
	public static String UUIDtoHexString(UUID uuid) {
		if (uuid==null) return "0x0";
		return "0x"+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getMostSignificantBits()), 16, "0")+org.apache.commons.lang.StringUtils.leftPad(Long.toHexString(uuid.getLeastSignificantBits()), 16, "0");
	}
	
	public static void adjustClaimableBlocks(UUID playerId, int adjustment) {
		net.kaikk.mc.gpp.PlayerData playerData = GriefPreventionPlus.getInstance().getDataStore().getPlayerData(playerId);
		playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + adjustment);
		GriefPreventionPlus.getInstance().getDataStore().savePlayerData(playerId, playerData);
	}

	Connection getDatabase() {
		return database;
	}
}


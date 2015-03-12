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
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.GriefPreventionPlus;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

class DataStore {
	GPPCities gppc;
	
	private String dbUrl;
	private String username;
	private String password;
	
	protected Connection db = null;
	protected ConcurrentHashMap<Integer, City> citiesMap = new ConcurrentHashMap<>();;
	protected ConcurrentHashMap<UUID, PlayerData> playerData = new ConcurrentHashMap<>();

	protected ArrayList<UUID> cityChat = new ArrayList<UUID>();
	protected ArrayList<UUID> cityChatSpy = new ArrayList<UUID>();
	
	DataStore(GPPCities gpp, String url, String username, String password) throws Exception {
		this.gppc=gpp;
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
			Statement statement = db.createStatement();
			
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
			Statement statement = db.createStatement();
			// delete all orphan cities and plots from the database
			// load cities and plots data from the database
			
			long loadCount=0;
			
			// Load cities
			this.dbCheck();
			gpp.log("Loading cities...");
			ResultSet results = statement.executeQuery("SELECT id, cname, creationDate, motdRes, motdOut, isJoinable, spawnX, spawnY, spawnZ, perms FROM gppc_cities;");
			Claim claim;
			City city;
			while (results.next()) {
				claim = GriefPreventionPlus.instance.dataStore.getClaim(results.getInt(1));
				
				if (claim==null) {
					gpp.log(Level.WARNING, "Skipping orphan city ID("+results.getInt(1)+")");
				} else {
					this.citiesMap.put(results.getInt(1), new City(claim, results.getString(2), new Date(results.getTimestamp(3).getTime()), results.getString(4), results.getString(5), results.getBoolean(6), new Location(claim.world, results.getInt(7), results.getInt(8), results.getInt(9)), results.getInt(10)));
					loadCount++;
				}
			}
			gpp.log("Loaded "+loadCount+" cities.");
			loadCount=0;
			
			
			// Load citizens
			this.dbCheck();
			gpp.log("Loading citizens...");
			results = statement.executeQuery("SELECT id, cid, perms, joinedOn FROM gppc_citizens;");
			while (results.next()) {
				city = this.citiesMap.get(results.getInt(2));
				if (city==null) {
					gpp.log(Level.WARNING, "Skipping orphan citizen UUID["+toUUID(results.getBytes(1)).toString()+"]");
				} else {
					city.citizens.put(toUUID(results.getBytes(1)), new Citizen(toUUID(results.getBytes(1)), results.getByte(3), new Date(results.getTimestamp(4).getTime())));
					loadCount++;
				}
			}
			gpp.log("Loaded "+loadCount+" citizens.");
			loadCount=0;
			
			// Load plots
			this.dbCheck();
			gpp.log("Loading plots...");

			Citizen citizen;
			results = statement.executeQuery("SELECT id, cid, citizen, motd, assignedOn, isTakeable FROM gppc_plots;");
			while (results.next()) {
				city = this.citiesMap.get(results.getInt(2));
				
				if (city==null) {
					gpp.log(Level.WARNING, "Skipping orphan plot ID("+results.getInt(1)+") (missing city id("+results.getInt(2)+"))");
				} else {
					claim = GriefPreventionPlus.instance.dataStore.getClaim(results.getInt(2));
					if (claim==null) {
						gpp.log(Level.WARNING, "Skipping orphan plot ID("+results.getInt(1)+") (missing city claim id("+results.getInt(2)+"))");
					} else {
						boolean skip=true;
						for (Claim subclaim : claim.children) {
							if (subclaim.getID()==results.getInt(1)) {
								if (results.getBytes(3)==null) {
									citizen=null;
								} else {
									citizen=city.getCitizen(GriefPreventionPlus.instance.getServer().getOfflinePlayer(toUUID(results.getBytes(3))).getUniqueId());
								}
								
								city.plots.put(results.getInt(1), new Plot(results.getInt(1), subclaim, citizen, results.getString(4), new Date(results.getTimestamp(5).getTime()), results.getBoolean(6)));
								
								loadCount++;
								skip=false;
								break;
							}
						}
						if (skip) {
							gpp.log(Level.WARNING, "Skipping orphan plot ID("+results.getInt(1)+") (missing plot subclaim)");
						}
					}
				}
			}
			gpp.log("Loaded "+loadCount+" plots.");
			
			// Load banned players (from Mayors who don't want this player on their city)
			this.dbCheck();
			results = statement.executeQuery("SELECT * FROM gppc_bans;");
			while (results.next()) {
				city = this.citiesMap.get(results.getLong(2));
				
				if (city!=null) {
					city.bannedPlayers.add(toUUID(results.getBytes(1)));
				}
			}

			statement.close();
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
			Statement statement = db.createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_cities (id, cname, spawnX, spawnY, spawnZ, perms) VALUES ("+claim.getID()+", \""+name+"\", "+loc.getBlockX()+", "+loc.getBlockY()+", "+loc.getBlockZ()+", 0);");
			
			this.citiesMap.put(claim.getID(), new City(claim, name, mayor, loc));
		} catch(SQLException e) {
			e.getStackTrace();
			log(Level.SEVERE, "Unable to create new city for claim id "+claim.getID()+" named '"+name+"' created by "+gppc.getServer().getPlayer(mayor).getName());
			log(Level.SEVERE, e.getMessage());
			return "An error occurred with the database.";
		}
		return "";
	}
	
	/** delete a city */
	synchronized String deleteCity(City city) {
		try {
			this.dbCheck();
			Statement statement = db.createStatement();
			Integer id=city.claim.getID();
			statement.executeUpdate("DELETE FROM gppc_citizens WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_plots WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_bans WHERE cid = "+id);
			statement.executeUpdate("DELETE FROM gppc_cities WHERE id = "+id);
			this.citiesMap.remove(id);
			city.claim.dropPermission("[gpc.c"+id+"]");
			gppc.getServer().broadcastMessage(Messages.CityHasBeenDisbanded.get(city.name));
		} catch(SQLException e) {
			e.getStackTrace();
			log(Level.SEVERE, "Unable to delete city id "+city.name);
			log(Level.SEVERE, e.getMessage());
			return "An error occurred with the database. Contact an administrator!";
		}

		return "";
	}

	
	/** get the city for this player
	 * @return the citizen's city, null otherwise*/
	City getCity(UUID id) {
		for (City city : this.citiesMap.values()) {
			if (city.citizens.containsKey(id)) {
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
			if (city.name.equalsIgnoreCase(name)) {
				return city;
			}
		}
		return null;
	}
	
	synchronized void dbCheck() throws SQLException {
		if(this.db == null || this.db.isClosed()) {
			Properties connectionProps = new Properties();
			connectionProps.put("user", this.username);
			connectionProps.put("password", this.password);
			
			this.db = DriverManager.getConnection(this.dbUrl, connectionProps); 
		}
	}
	
	synchronized void dbClose()  {
		try {
			if (!this.db.isClosed()) {
				this.db.close();
				this.db=null;
			}
		} catch (SQLException e) {
			
		}
	}
	
	/** turns a location into a string */
	public static String locationToString(Location location) {
		return locationToString(location, false);
	}
	
	public static String locationToString(Location location, boolean yaw) {
		StringBuilder stringBuilder = new StringBuilder(location.getWorld().getName());
		stringBuilder.append(";");
		stringBuilder.append(location.getBlockX());
		stringBuilder.append(";");
		stringBuilder.append(location.getBlockY());
		stringBuilder.append(";");
		stringBuilder.append(location.getBlockZ());
		if (yaw) {
			stringBuilder.append(";");
			stringBuilder.append((int) location.getYaw());
		}
		
		return stringBuilder.toString();
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
		World world = GPPCities.gppc.getServer().getWorld(elements[0]);
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
			Player spy = gppc.getServer().getPlayer(uuid);
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
		gppc.getLogger().log(level, msg);
	}

	static Player getOnlinePlayer(String name) {
		return GPPCities.gppc.getServer().getPlayer(name);
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
		net.kaikk.mc.gpp.PlayerData playerData = GriefPreventionPlus.instance.dataStore.getPlayerData(playerId);
		playerData.setBonusClaimBlocks(playerData.getBonusClaimBlocks() + adjustment);
		GriefPreventionPlus.instance.dataStore.savePlayerData(playerId, playerData);
	}
}


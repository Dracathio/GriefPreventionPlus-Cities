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

import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.ClaimPermission;
import net.kaikk.mc.gpp.GriefPreventionPlus;

class City {
	Claim claim;
	String name;
	Date creationDate;
	String motdRes;
	String motdOut;
	boolean isJoinable=false;
	Location spawn;
	int defaultPerms;
	ConcurrentHashMap<Integer, Plot> plots = new ConcurrentHashMap<>(); // plots could be an arrayList
	ConcurrentHashMap<UUID, Citizen> citizens = new ConcurrentHashMap<>();
	ArrayList<UUID> bannedPlayers = new ArrayList<UUID>();
	
	/** City constructor (new city) */
	City(Claim claim, String name, UUID mayor, Location loc) {
		this.claim = claim;
		this.name = new String(name);
		this.creationDate = new Date();
		this.newCitizen(mayor, 1);
		this.spawn = loc;
		this.motdRes="";
		this.motdOut="";
	}
	
	/** City constructor (from database) */
	City(Claim claim, String name, Date creationDate, String motdRes, String motdOut, boolean isJoinable, Location spawn, int defaultPerms) {
		this.claim = claim;
		this.name = name;
		this.creationDate = creationDate;
		this.motdRes = (motdRes=="" ? null : motdRes);
		this.motdOut = (motdOut=="" ? null : motdOut);
		this.isJoinable = isJoinable;
		this.spawn = spawn;
		this.defaultPerms = defaultPerms;
	}
	
	// City methods
	/** set the motd */
	synchronized void setMotd(String motd, boolean resmotd) {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			if (resmotd){
				this.motdRes = motd; 
				this.sendMessageToAllCitizens(Messages.MotdHasBeenSet.get());
				this.sendMessageToAllCitizens(ChatColor.translateAlternateColorCodes('&', "&a"+motd));
			} else {
				this.motdOut = motd;
			}
			
			statement.executeUpdate("UPDATE gppc_cities SET motd"+(resmotd ? "Res" : "Out")+" = \""+motd+"\" WHERE id = "+this.claim.getID()+";");
			
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set motd for "+this.name);
		}
	}
	
	void setSpawn(Location loc) {
		try {
			GPPCities.gppc.ds.dbCheck();
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			
			statement.executeUpdate("UPDATE gppc_cities SET spawnX="+loc.getBlockX()+", spawnY="+loc.getBlockY()+", spawnZ="+loc.getBlockZ()+" WHERE id = "+this.claim.getID()+";");
			this.spawn=loc;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set motd for "+this.name);
		}
	}
	
	void rename(String newName) {
		try {
			GPPCities.gppc.ds.dbCheck();
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			
			statement.executeUpdate("UPDATE gppc_cities SET cname=\""+newName+"\" WHERE id = "+this.claim.getID()+";");
			this.name=newName;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to change city name for "+this.name);
		}
	}
	
	void changeOwner(UUID newMayor) {
		Citizen citizen=this.getCitizen(newMayor);
		if (citizen==null) {
			return;
		}
		
		try {
			GriefPreventionPlus.instance.dataStore.changeClaimOwner(this.claim, newMayor);
			this.claim.setPermission(this.getMayor().id, ClaimPermission.BUILD);
			this.claim.setPermission(this.getMayor().id, ClaimPermission.MANAGE);
			
			// add bonus claimable blocks for the mayor
			int blocks=GPPCities.gppc.config.ClaimBlocksPerCitizen*this.citizens.size();
			DataStore.adjustClaimableBlocks(this.getMayor().id, -blocks);
			DataStore.adjustClaimableBlocks(newMayor, blocks);
			GPPCities.gppc.log(Level.INFO, "Transferred "+blocks+" claim blocks from "+this.getMayor().getName()+" to "+citizen.getName()+".");
			
			this.getMayor().setPerm(CitizenPermission.Assistant); // Make the current mayor an assistant
			citizen.setPerm(CitizenPermission.Mayor);
			this.sendMessageToAllCitizens(Messages.NewMayor.get(citizen.getDisplayName(), this.name));
		} catch (Exception e) {
			e.printStackTrace(); // this should never happen: city claims are always top-claims
		}
	}
	
	String info() {
		String[] citizensList=this.citizensList();
		OfflinePlayer mayor=GPPCities.gppc.getServer().getOfflinePlayer(this.getMayor().id);
		String mayorName;
		if (mayor.isOnline()) {
			mayorName=mayor.getPlayer().getDisplayName();
		} else {
			mayorName=mayor.getName();
		}
		
		return Messages.CityInfo.get(this.name, Integer.toString(this.claim.getID()), Integer.toString(this.claim.getArea()), Integer.toString(this.plots.size()), this.creationDate(), mayorName, Integer.toString(this.citizens.size()), citizensList[0], citizensList[1]);
	}
	
	String creationDate() {
		return DateFormat.getDateTimeInstance().format(this.creationDate);
	}
	
	void addBan(UUID id) {
		if (this.bannedPlayers.contains(id)) {
			return;
		}
		
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_bans VALUES ("+net.kaikk.mc.gpp.DataStore.UUIDtoHexString(id)+", "+this.claim.getID()+")");
			
			Player bannedPlayer = GPPCities.gppc.getServer().getPlayer(id);
			if (bannedPlayer!=null) {
				bannedPlayer.sendMessage(Messages.YouAreBanned.get(this.name));
			}
			this.removeCitizen(id);
			this.bannedPlayers.add(id);
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to ban "+id.toString()+" on city "+this.name);
		}
	}
	
	void unban(UUID id) {
		this.bannedPlayers.remove(id);
		
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			
			statement.executeUpdate("DELETE FROM gppc_bans WHERE id="+DataStore.UUIDtoHexString(id)+" AND cid="+this.claim.getID());
			
			this.bannedPlayers.remove(id);
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to unban "+id.toString()+" on city "+this.name);
		}
	}
	
	
	void setPerm(CitizenPermission cPerm) {
		this.setPerm(cPerm.perm);
	}
	
	void setPerm(int perm) {
		this.defaultPerms=this.defaultPerms|perm;
		this.dbUpdatePerm();
	}
	
	void unsetPerm(CitizenPermission cPerm) {
		this.unsetPerm(cPerm.perm);
	}
	
	void unsetPerm(int perm) {
		this.defaultPerms=this.defaultPerms&(~perm);
		this.dbUpdatePerm();
	}
	
	boolean checkPerm(CitizenPermission cPerm) {
		return (this.defaultPerms&cPerm.perm)!=0;
	}
	
	boolean checkPerm(int perm) {
		return (this.defaultPerms&perm)!=0;
	}
	
	String permsToString() {
		String perms = "";
		if (checkPerm(CitizenPermission.Assistant)) {
			return "A";
		}
		if (checkPerm(CitizenPermission.Invite)) {
			perms+="I";
		}
		if (checkPerm(CitizenPermission.Expel)) {
			perms+="E";
		}
		if (checkPerm(CitizenPermission.Motd)) {
			perms+="M";
		}
		if (checkPerm(CitizenPermission.Plot)) {
			perms+="P";
		}
		if (checkPerm(CitizenPermission.Spawn)) {
			perms+="S";
		}

		return perms;
	}
	
	void dbUpdatePerm() {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			statement.executeUpdate("UPDATE gppc_cities SET perms = "+this.defaultPerms+" WHERE id = "+this.claim.getID()+";");
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to update perms for city id("+this.claim.getID()+")");
		}
	}
	
	// Citizens methods
	synchronized void newCitizen(UUID id) {
		this.newCitizen(id, (byte)0);
	}
	
	synchronized void newCitizen(UUID playerId, int rank) {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_citizens (id, cid, perms, joinedOn) VALUES ("+DataStore.UUIDtoHexString(playerId)+", "+this.claim.getID()+", "+rank+", NOW());");
			this.sendMessageToAllCitizens(Messages.PlayerJoinedYourCity.get(GPPCities.gppc.getServer().getPlayer(playerId).getDisplayName()));

			GPPCities.gppc.ds.playerData.get(playerId).perm.setPermission("gppc.c"+this.claim.getID(), true);
			
			this.citizens.put(playerId, new Citizen(playerId, rank, this.creationDate));
			
			// add bonus claim blocks for the mayor
			int blocks=GPPCities.gppc.config.ClaimBlocksPerCitizen;
			if (blocks>0) {
				DataStore.adjustClaimableBlocks(this.getMayor().id, blocks);
				GPPCities.gppc.log(Level.INFO, "Mayor "+this.getMayor().getName()+" got "+blocks+" claimable blocks.");
			}
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to create a new citizen for "+this.name);
		}
	}
	
	void sendMessageToAllCitizens(String message) {
		Player player;
		for (Citizen citizen : this.citizens.values()) {
			if ((player=GPPCities.gppc.getServer().getPlayer(citizen.id)) != null) {
				player.sendMessage(message);
			}
		}
	}
	
	/** get the citizen 
	 * @return the citizen, null if it doesn't exist*/
	Citizen getCitizen(UUID id) {
		return this.citizens.get(id);
	}
	Citizen getCitizen(String name) {
		for (Citizen citizen : this.citizens.values()) {
			if (GPPCities.gppc.getServer().getOfflinePlayer(citizen.id).getName().equalsIgnoreCase(name)) {
				return citizen;
			}
		}
		return null;
	}
	
	Citizen getMayor() {
		for (Citizen citizen : this.citizens.values()) {
			if ((citizen.perms&1)!=0) {
				return citizen;
			}
		}
		return null;
	}
	
	void removeCitizen(UUID id) {
		for (Plot plot : this.plots.values()) {
			if (plot.citizen.id==id) {
				plot.unassign();
			}
		}
		
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			statement.executeUpdate("DELETE FROM gppc_citizens WHERE id = "+DataStore.UUIDtoHexString(id)+";");
			GPPCities.gppc.ds.playerData.get(id).perm.unsetPermission("gppc.c"+this.claim.getID());
			this.citizens.remove(id);
			
			// remove bonus claimable blocks for the mayor
			int blocks=GPPCities.gppc.config.ClaimBlocksPerCitizen;
			if (blocks>0) {
				DataStore.adjustClaimableBlocks(this.getMayor().id, -blocks);
				GPPCities.gppc.log(Level.INFO, "Mayor "+this.getMayor().getName()+" lost "+blocks+" claimable blocks.");
			}
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to remove citizen UUID["+id+"]");
		}
	}
	
	String[] citizensList() {
		String[] onList = new String[this.citizens.size()];
		String[] offList = new String[this.citizens.size()];

		int i=0;
		int j=0;
		OfflinePlayer player;
		for (Citizen citizen : this.citizens.values()) {
			player=GPPCities.gppc.getServer().getOfflinePlayer(citizen.id);
			if (player.isOnline()) {
				onList[i]=(citizen.checkPerm(CitizenPermission.Assistant.perm|CitizenPermission.Mayor.perm) ? "*" : "")+player.getPlayer().getDisplayName();
				i++;
			} else {
				offList[j]=(citizen.checkPerm(CitizenPermission.Assistant.perm|CitizenPermission.Mayor.perm) ? "*" : "")+player.getName();
				j++;
			}
		}
		
		if (i>1) {
			Arrays.sort(onList, 0, i);
		}
		if (j>1) {
			Arrays.sort(offList, 0, j);
		}
		
		
		String onListString="";
		String offListString="";
		
		for (String s : onList) {
			if (s!=null) {
				onListString=onListString+", "+s;
			}
		}
		
		for (String s : offList) {
			if (s!=null) {
				offListString=offListString+", "+s;
			}
		}
		
		if (!onListString.isEmpty()) {
			onListString=onListString.substring(2);
		}
		
		if (!offListString.isEmpty()) {
			offListString=offListString.substring(2);
		}
		
		return new String[]{onListString, offListString};
	}
	
	boolean citizenHasAssignedPlot(Citizen citizen) {
		for (Plot plot : this.plots.values()) {
			if (plot.citizen == citizen) {
				return true;
			}
		}

		return false;
	}
	
	// Plots methods
	
	/** Create a plot (without any assignment) 
	 * @return the created plot, null otherwise */
	synchronized Plot newPlot(Claim claim) {
		try {
			GPPCities.gppc.ds.dbCheck();
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("INSERT INTO gppc_plots (id, cid) VALUES ("+claim.getID()+", "+claim.parent.getID()+")");
			Plot plot = new Plot(claim.getID(), claim);
			this.plots.put(claim.getID(), plot);

			return plot;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
			return null;
		}
	}
	
	/** Assign a plot (creates a new plot if it doesn't exist)
	 * @return boolean true if the plot was assigned
	 * */
	synchronized boolean assignPlot(Claim claim, Citizen citizen) {
		Plot plot = this.getPlot(claim);
		if (plot == null) {
			plot = this.newPlot(claim);
			if (plot == null) {
				return false;
			}
		}
		
		try {
			GPPCities.gppc.ds.dbCheck();
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET citizen = "+DataStore.UUIDtoHexString(citizen.id)+", motd = \"\", assignedOn = NOW(), isTakeable = 0 WHERE id = "+plot.id+";");
			
			this.plots.get(plot.id).assign(citizen);
			
			return true;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
			return false;
		}
	}
	
	synchronized void deletePlot(Plot plot) {
		try {
			GPPCities.gppc.ds.dbCheck();
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			statement.executeUpdate("DELETE FROM gppc_plots WHERE id="+plot.id+";");
			
			this.plots.remove(plot.id).claim.clearPermissions();
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
		}
	}

	Plot getPlot(Claim claim) {
		for (Plot plot: this.plots.values()) {
			if (plot.claim == claim) {
				return plot; 
			}
		}
		return null;
	}
}

class CitySizeComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.citizens.size()-city2.citizens.size();
	}
}

class CityAreaComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.claim.getArea()-city2.claim.getArea();
	}
}

class CityNameComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.name.compareTo(city2.name);
	}
}
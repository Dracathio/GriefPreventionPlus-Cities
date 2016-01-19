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
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.ClaimPermission;
import net.kaikk.mc.gpp.GriefPreventionPlus;

public class City {
	private Claim claim;
	private String name;
	private Date creationDate;
	private String motdRes;
	private String motdOut;
	private boolean isJoinable=false;
	private Location spawn;
	private int defaultPerms;
	private ConcurrentHashMap<Integer, Plot> plots = new ConcurrentHashMap<Integer, Plot>();
	private ConcurrentHashMap<UUID, Citizen> citizens = new ConcurrentHashMap<UUID, Citizen>();
	private ArrayList<UUID> bannedPlayers = new ArrayList<UUID>();
	
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
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
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
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for "+this.name);
		}
	}
	
	void setSpawn(Location loc) {
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			
			statement.executeUpdate("UPDATE gppc_cities SET spawnX="+loc.getBlockX()+", spawnY="+loc.getBlockY()+", spawnZ="+loc.getBlockZ()+" WHERE id = "+this.claim.getID()+";");
			this.spawn=loc;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for "+this.name);
		}
	}
	
	void rename(String newName) {
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			
			statement.executeUpdate("UPDATE gppc_cities SET cname=\""+newName+"\" WHERE id = "+this.claim.getID()+";");
			this.name=newName;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to change city name for "+this.name);
		}
	}
	
	void changeOwner(UUID newMayor) {
		Citizen citizen=this.getCitizen(newMayor);
		if (citizen==null) {
			return;
		}
		
		try {
			GriefPreventionPlus.getInstance().getDataStore().changeClaimOwner(this.claim, newMayor);
			this.claim.setPermission(this.getMayor().getId(), ClaimPermission.BUILD);
			this.claim.setPermission(this.getMayor().getId(), ClaimPermission.MANAGE);
			
			// add bonus claimable blocks for the mayor
			int blocks=GPPCities.getInstance().config.ClaimBlocksPerCitizen*this.citizens.size();
			DataStore.adjustClaimableBlocks(this.getMayor().getId(), -blocks);
			DataStore.adjustClaimableBlocks(newMayor, blocks);
			GPPCities.getInstance().log(Level.INFO, "Transferred "+blocks+" claim blocks from "+this.getMayor().getName()+" to "+citizen.getName()+".");
			
			this.getMayor().setPerm(CitizenPermission.Assistant); // Make the current mayor an assistant
			citizen.setPerm(CitizenPermission.Mayor);
			this.sendMessageToAllCitizens(Messages.NewMayor.get(citizen.getDisplayName(), this.name));
		} catch (Exception e) {
			e.printStackTrace(); // this should never happen: city claims are always top-claims
		}
	}
	
	String info() {
		String[] citizensList=this.citizensList();
		OfflinePlayer mayor=GPPCities.getInstance().getServer().getOfflinePlayer(this.getMayor().getId());
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
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_bans VALUES ("+net.kaikk.mc.gpp.DataStore.UUIDtoHexString(id)+", "+this.claim.getID()+")");
			
			Player bannedPlayer = GPPCities.getInstance().getServer().getPlayer(id);
			if (bannedPlayer!=null) {
				bannedPlayer.sendMessage(Messages.YouAreBanned.get(this.name));
			}
			this.removeCitizen(id);
			this.bannedPlayers.add(id);
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to ban "+id.toString()+" on city "+this.name);
		}
	}
	
	void unban(UUID id) {
		this.bannedPlayers.remove(id);
		
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			
			statement.executeUpdate("DELETE FROM gppc_bans WHERE id="+DataStore.UUIDtoHexString(id)+" AND cid="+this.claim.getID());
			
			this.bannedPlayers.remove(id);
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to unban "+id.toString()+" on city "+this.name);
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
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			statement.executeUpdate("UPDATE gppc_cities SET perms = "+this.defaultPerms+" WHERE id = "+this.claim.getID()+";");
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to update perms for city id("+this.claim.getID()+")");
		}
	}
	
	boolean isValid() {
		if (this.citizens.size()==0) {
			return false;
		}
		if (this.getMayor()==null) {
			return false;
		}
		return true;
	}
	
	public boolean isSpawnValid() {
		return this.getClaim().contains(this.getSpawn(), false, false);
	}
	
	public void teleport(Player player) {
		this.getSpawn().getBlock().setType(Material.AIR);
		this.getSpawn().getBlock().getRelative(BlockFace.UP).setType(Material.AIR);
		player.teleport(this.getSpawn());
		player.sendMessage(Messages.CitySpawnTeleported.get(this.getName()));
	}
	
	/** set the motd */
	synchronized void setAutojoin(boolean autojoin) {
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

			statement.executeUpdate("UPDATE gppc_cities SET isJoinable = "+(autojoin ? "1" : "0")+" WHERE id = "+this.claim.getID()+";");
			
			this.isJoinable=autojoin;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for "+this.name);
		}
	}
	
	
	// Citizens methods
	synchronized void newCitizen(UUID id) {
		this.newCitizen(id, (byte)0);
	}
	
	synchronized void newCitizen(UUID playerId, int rank) {
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			
			statement.executeUpdate("INSERT INTO gppc_citizens (id, cid, perms, joinedOn) VALUES ("+DataStore.UUIDtoHexString(playerId)+", "+this.claim.getID()+", "+rank+", NOW());");
			this.sendMessageToAllCitizens(Messages.PlayerJoinedYourCity.get(GPPCities.getInstance().getServer().getPlayer(playerId).getDisplayName()));

			GPPCities.getInstance().getDataStore().playerData.get(playerId).getPerm().setPermission("gppc.c"+this.claim.getID(), true);
			
			this.citizens.put(playerId, new Citizen(playerId, rank, this.creationDate));
			
			// add bonus claim blocks for the mayor
			int blocks=GPPCities.getInstance().config.ClaimBlocksPerCitizen;
			if (blocks>0) {
				DataStore.adjustClaimableBlocks(this.getMayor().getId(), blocks);
				GPPCities.getInstance().log(Level.INFO, "Mayor "+this.getMayor().getName()+" got "+blocks+" claimable blocks.");
			}
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to create a new citizen for "+this.name);
		}
	}
	
	void sendMessageToAllCitizens(String message) {
		Player player;
		for (Citizen citizen : this.citizens.values()) {
			if ((player=citizen.getPlayer()) != null) {
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
			if (citizen.getOfflinePlayer().getName().equalsIgnoreCase(name)) {
				return citizen;
			}
		}
		return null;
	}
	
	Citizen getMayor() {
		for (Citizen citizen : this.citizens.values()) {
			if ((citizen.getPerms()&1)!=0) {
				return citizen;
			}
		}
		return null;
	}
	
	void removeCitizen(UUID id) {
		for (Plot plot : this.plots.values()) {
			if (plot.getCitizen()!=null && plot.getCitizen().getId()==id) {
				plot.unassign();
			}
		}
		
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			statement.executeUpdate("DELETE FROM gppc_citizens WHERE id = "+DataStore.UUIDtoHexString(id)+";");
			PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.get(id);
			if (playerData!=null) {
				playerData.getPerm().unsetPermission("gppc.c"+this.claim.getID());
			}
			this.citizens.remove(id);
			
			// remove bonus claimable blocks for the mayor
			int blocks=GPPCities.getInstance().config.ClaimBlocksPerCitizen;
			if (blocks>0) {
				DataStore.adjustClaimableBlocks(this.getMayor().getId(), -blocks);
				GPPCities.getInstance().log(Level.INFO, "Mayor "+this.getMayor().getName()+" lost "+blocks+" claimable blocks.");
			}
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to remove citizen UUID["+id+"]");
		}
	}
	
	String[] citizensList() {
		String[] onList = new String[this.citizens.size()];
		String[] offList = new String[this.citizens.size()];

		int i=0;
		int j=0;
		OfflinePlayer player;
		for (Citizen citizen : this.citizens.values()) {
			player=GPPCities.getInstance().getServer().getOfflinePlayer(citizen.getId());
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
			if (plot.getCitizen() == citizen) {
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
			GPPCities.getInstance().getDataStore().dbCheck();
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

			statement.executeUpdate("INSERT INTO gppc_plots (id, cid) VALUES ("+claim.getID()+", "+claim.getParent().getID()+")");
			Plot plot = new Plot(claim.getID(), claim);
			this.plots.put(claim.getID(), plot);

			return plot;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
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
			GPPCities.getInstance().getDataStore().dbCheck();
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET citizen = "+DataStore.UUIDtoHexString(citizen.getId())+", motd = \"\", assignedOn = NOW(), isTakeable = 0 WHERE id = "+plot.getId()+";");
			
			this.plots.get(plot.getId()).assign(citizen);
			
			return true;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
			return false;
		}
	}
	
	synchronized void deletePlot(Plot plot) {
		try {
			GPPCities.getInstance().getDataStore().dbCheck();
			Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
			statement.executeUpdate("DELETE FROM gppc_plots WHERE id="+plot.getId()+";");
			
			this.plots.remove(plot.getId()).getClaim().clearPermissions();
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
			GPPCities.getInstance().log(Level.SEVERE, "Unable to create new plot at "+DataStore.locationToString(claim.getLesserBoundaryCorner()));
		}
	}

	Plot getPlot(Claim claim) {
		for (Plot plot: this.plots.values()) {
			if (plot.getClaim() == claim) {
				return plot; 
			}
		}
		return null;
	}
	
	void playerEnterMessage(Player player) {
		if (this.getCitizen(player.getUniqueId())!=null) {
			player.sendMessage(Messages.WelcomeBackTo.get(this.name));
			if (this.motdRes!=null && !this.motdRes.isEmpty()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+this.motdRes));
			}
		} else {
			player.sendMessage(Messages.WelcomeTo.get(this.name));
			if (this.motdOut!=null && !this.motdOut.isEmpty()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+this.motdOut));
			}
			
			if (this.isJoinable && GPPCities.getInstance().getDataStore().getCity(player.getUniqueId())==null) {
				player.sendMessage(Messages.YouOnJoinableCity.get(this.name));
			}
		}
		
		Plot plot = this.getPlot(claim);
		if (plot!=null) {
			plot.playerEnterMessage(player);
		}
	}
	
	Citizen getOldestAssistant() {
		Citizen oldest=null;
		for (Citizen citizen : this.citizens.values()) {
			if (citizen.checkPerm(CitizenPermission.Assistant)) {
				if (oldest==null || oldest.getJoinedOn().getTime()>citizen.getJoinedOn().getTime()) {
					oldest=citizen;
				}
			}
		}
		
		return oldest;
	}
	
	Citizen getOldestCitizen() {
		Citizen oldest=null;
		for (Citizen citizen : this.citizens.values()) {
			if (oldest==null || oldest.getJoinedOn().getTime()>citizen.getJoinedOn().getTime()) {
				oldest=citizen;
			}
		}
		
		return oldest;
	}
	
	static int parsePerms(String perms) {
		int perm=0;
		for (char ch : perms.toLowerCase().toCharArray()) {
			switch (ch) {
			case 'a':
				perm=perm|2;
				break;
			case 'i':
				perm=perm|4;
				break;
			case 'e':
				perm=perm|8;
				break;
			case 'm':
				perm=perm|16;
				break;
			case 'p':
				perm=perm|32;
				break;
			case 's':
				perm=perm|64;
				break;
			}
		}
		
		return perm;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public String getMotdRes() {
		return motdRes;
	}

	void setMotdRes(String motdRes) {
		this.motdRes = motdRes;
	}

	public String getMotdOut() {
		return motdOut;
	}

	void setMotdOut(String motdOut) {
		this.motdOut = motdOut;
	}

	public boolean isJoinable() {
		return isJoinable;
	}

	void setJoinable(boolean isJoinable) {
		this.isJoinable = isJoinable;
	}

	public Location getSpawn() {
		return spawn;
	}

	public int getDefaultPerms() {
		return defaultPerms;
	}

	void setDefaultPerms(int defaultPerms) {
		this.defaultPerms = defaultPerms;
	}

	public ConcurrentHashMap<Integer, Plot> getPlots() {
		return plots;
	}

	void setPlots(ConcurrentHashMap<Integer, Plot> plots) {
		this.plots = plots;
	}

	public ConcurrentHashMap<UUID, Citizen> getCitizens() {
		return citizens;
	}

	void setCitizens(ConcurrentHashMap<UUID, Citizen> citizens) {
		this.citizens = citizens;
	}

	public ArrayList<UUID> getBannedPlayers() {
		return bannedPlayers;
	}

	void setBannedPlayers(ArrayList<UUID> bannedPlayers) {
		this.bannedPlayers = bannedPlayers;
	}

	public Claim getClaim() {
		return claim;
	}

	void setClaim(Claim claim) {
		this.claim = claim;
	}
	
	/** Returns true if this city should be removed because this city is empty. */
	boolean handleInactiveCitizens() {
		if (this.getCitizens().size()==0) {
			return true;
		}
		
		Citizen expiredMayor=null;
		
		// Check all citizens for inactivity
		for (Citizen citizen : this.getCitizens().values()) {
			if(citizen.getLastPlayedDays()>GPPCities.getInstance().config.InactivityDays) {
				// citizen expired
				if (citizen.checkPerm(CitizenPermission.Mayor)) {
					// this citizen is the mayor, let's ignore him atm
					expiredMayor=citizen;
				} else {
					GPPCities.getInstance().log("Removing citizen "+citizen.getName()+" from "+this.getName());
					this.removeCitizen(citizen.getId());
				}
			}
		}
		
		if (expiredMayor!=null) {
			// the mayor is inactive... need to replace him with someone else...
			if (this.getCitizens().size()==1) {
				// the mayor is alone... the city must be removed
				return true;
			} else {
				// change the city owner
				Citizen newMayor=null;
				newMayor=this.getOldestAssistant();
				if (newMayor==null) {
					newMayor=this.getOldestCitizen();
				}
				GPPCities.getInstance().log("Removing old mayor "+expiredMayor.getName()+" from "+this.getName()+". New mayor is "+newMayor.getName());
				this.changeOwner(newMayor.getId());
				this.removeCitizen(expiredMayor.getId());
			}
		}
		
		return false;
	}

	public class Plot {
		private int id;
		private Claim claim;
		private Citizen citizen;
		private String motd;
		private Date assignedOn;
		private boolean isTakeable;
		
		Plot(int id, Claim claim) {
			this.id = id;
			this.claim = claim;
			this.motd = "";
		}
		
		Plot(int id, Claim claim, Citizen citizen) {
			this.id = id;
			this.claim = claim;
			this.citizen = citizen;
			this.motd = "";
			this.assignedOn = new Date();
		}

		Plot(int id, Claim claim, Citizen citizen, String motd, Date assignedOn, boolean isTakeable) {
			this.id = id;
			this.claim = claim;
			this.citizen = citizen;
			this.motd = motd;
			this.assignedOn = assignedOn;
			this.isTakeable = isTakeable;
		}
		
		/** assign this plot to a citizen */
		void assign(Citizen citizen) {
			try {
				GPPCities.getInstance().getDataStore().dbCheck();
				
				Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

				statement.executeUpdate("UPDATE gppc_plots SET citizen = "+DataStore.UUIDtoHexString(citizen.getId())+", motd = \"\", assignedOn = NOW(), isTakeable = 0 WHERE id = "+this.id+";");
				
				// unset permission if it was already assigned
				if (citizen.getId()!=null) {
					PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.get(citizen.getId());
					if (playerData!=null) {
						playerData.getPerm().unsetPermission("gpp.c"+this.id+".b");
						playerData.getPerm().unsetPermission("gpp.c"+this.id+".m");
					}
				}
				
				this.claim.clearPermissions();
				
				this.citizen=citizen;
				this.motd="";
				this.assignedOn=new Date();
				this.isTakeable=false;

				// set permission
				PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.get(citizen.getId());
				if (playerData!=null) {
					playerData.getPerm().setPermission("gpp.c"+this.id+".b", true);
					playerData.getPerm().setPermission("gpp.c"+this.id+".m", true);
				}
			} catch (SQLException e) {
				e.getStackTrace();
				GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
				GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for plot "+this.id);
			}
		}
		
		void unassign() {
			try {
				GPPCities.getInstance().getDataStore().dbCheck();
				
				Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

				statement.executeUpdate("UPDATE gppc_plots SET citizen = NULL, motd = \"\" WHERE id = "+this.id+";");
				
				this.claim.clearPermissions();

				// unset permission
				PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.get(citizen.getId());
				playerData.getPerm().unsetPermission("gpp.c"+this.id+".b");
				playerData.getPerm().unsetPermission("gpp.c"+this.id+".m");
				
				this.citizen=null;
				this.motd="";
			} catch (SQLException e) {
				e.getStackTrace();
				GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
				GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for plot "+this.id);
			}
		}
		
		void motd(String motd) {
			try {
				GPPCities.getInstance().getDataStore().dbCheck();
				
				Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

				statement.executeUpdate("UPDATE gppc_plots SET motd = \""+motd+"\" WHERE id = "+this.id+";");
				this.motd=motd;
			} catch (SQLException e) {
				e.getStackTrace();
				GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
				GPPCities.getInstance().log(Level.SEVERE, "Unable to set motd for plot "+this.id);
			}
		}
		
		void takeable(boolean sw) {
			try {
				GPPCities.getInstance().getDataStore().dbCheck();
				
				Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();

				statement.executeUpdate("UPDATE gppc_plots SET isTakeable = "+(sw?1:0)+" WHERE id = "+this.id+";");

				this.isTakeable=sw;
			} catch (SQLException e) {
				e.getStackTrace();
				GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
				GPPCities.getInstance().log(Level.SEVERE, "Unable to set takeable for plot "+this.id);
			}
		}
		
		void playerEnterMessage(Player player) {
			if (this.citizen != null) {
				player.sendMessage(Messages.YouOnPlot.get(this.citizen.getDisplayName()));
			} else {
				player.sendMessage(Messages.YouOnUnassignedPlot.get());
				
				Citizen citizen = City.this.getCitizen(player.getUniqueId());
				if (this.isTakeable && citizen!=null && !City.this.citizenHasAssignedPlot(citizen)) {
					player.sendMessage(Messages.YouOnTakeablePlot.get());
				}
			}
			if (!this.motd.isEmpty()) {
				player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+this.motd));
			}
		}

		public Claim getClaim() {
			return claim;
		}

		void setClaim(Claim claim) {
			this.claim = claim;
		}

		public int getId() {
			return id;
		}

		void setId(int id) {
			this.id = id;
		}

		public Citizen getCitizen() {
			return citizen;
		}

		void setCitizen(Citizen citizen) {
			this.citizen = citizen;
		}

		public String getMotd() {
			return motd;
		}

		void setMotd(String motd) {
			this.motd = motd;
		}

		public Date getAssignedOn() {
			return assignedOn;
		}

		void setAssignedOn(Date assignedOn) {
			this.assignedOn = assignedOn;
		}

		public boolean isTakeable() {
			return isTakeable;
		}

		void setTakeable(boolean isTakeable) {
			this.isTakeable = isTakeable;
		}
	}

	public class Citizen {
		private UUID id;
		private int perms;
		private Date joinedOn;
		
		Citizen(UUID id) {
			this.id=id;
			this.joinedOn=new Date();
		}
		
		Citizen(UUID id, int rank, Date joinedOn) {
			this.id=id;
			this.perms=rank;
			this.joinedOn=joinedOn;
		}
		
		long getLastPlayed() {
			return GPPCities.getInstance().getServer().getOfflinePlayer(this.id).getLastPlayed();
		}
		
		int getLastPlayedDays() {
			return (int) ((System.currentTimeMillis()-this.getLastPlayed())/86400000);
		}
		/** returns citizen's name */
		String getName() {
			return this.getOfflinePlayer().getName();
		}
		/** returns citizen's display name, or the name if he's not online */
		String getDisplayName() {
			OfflinePlayer citizen = this.getOfflinePlayer();
			if (citizen.isOnline()) {
				return citizen.getPlayer().getDisplayName();
			} else {
				return citizen.getName();
			}
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

		void setPerm(CitizenPermission cPerm) {
			this.setPerm(cPerm.perm);
		}
		
		void setPerm(int perm) {
			if (perm < 3) {
				this.perms=perm;
			} else {
				this.perms=this.perms|perm;
			}
			this.dbUpdatePerm();
		}
		
		void unsetPerm(CitizenPermission cPerm) {
			this.unsetPerm(cPerm.perm);
		}
		
		void unsetPerm(int perm) {
			this.perms=this.perms&(~perm);
			this.dbUpdatePerm();
		}
		
		boolean checkPerm(CitizenPermission cPerm) {
			return (this.perms&cPerm.perm)!=0;
		}
		
		boolean checkPerm(int perm) {
			return (this.perms&perm)!=0;
		}
		
		void dbUpdatePerm() {
			try {
				GPPCities.getInstance().getDataStore().dbCheck();
				
				Statement statement = GPPCities.getInstance().getDataStore().getDatabase().createStatement();
				statement.executeUpdate("UPDATE gppc_citizens SET perms = "+this.perms+" WHERE id = "+DataStore.UUIDtoHexString(this.id)+";");
			} catch (SQLException e) {
				e.getStackTrace();
				GPPCities.getInstance().log(Level.SEVERE, e.getMessage());
				GPPCities.getInstance().log(Level.SEVERE, "Unable to update perms for citizen UUID["+DataStore.UUIDtoHexString(this.id)+"]");
			}
		}
		
		OfflinePlayer getOfflinePlayer() {
			return GPPCities.getInstance().getServer().getOfflinePlayer(this.id);
		}
		
		Player getPlayer() {
			return GPPCities.getInstance().getServer().getPlayer(this.id);
		}

		public UUID getId() {
			return id;
		}

		void setId(UUID id) {
			this.id = id;
		}

		public int getPerms() {
			return perms;
		}

		void setPerms(int perms) {
			this.perms = perms;
		}

		public Date getJoinedOn() {
			return joinedOn;
		}

		void setJoinedOn(Date joinedOn) {
			this.joinedOn = joinedOn;
		}
	}


	class CitizenNameComparator implements Comparator<Citizen> {
		@Override
		public int compare(Citizen c1, Citizen c2) {
			return c1.getName().compareTo(c2.getName());
		}
	}

	/** From the most recent played.*/
	class CitizenLastPlayedComparator implements Comparator<Citizen> {
		@Override
		public int compare(Citizen c1, Citizen c2) {
			return (int) ((c2.getLastPlayed()-c1.getLastPlayed())/1000);
		}
	}

	class CitizenJoinedOnComparator implements Comparator<Citizen> {
		@Override
		public int compare(Citizen c1, Citizen c2) {
			return (int) ((c1.getJoinedOn().getTime()-c2.getJoinedOn().getTime())/1000);
		}
	}
}

class CitySizeComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.getCitizens().size()-city2.getCitizens().size();
	}
}

class CityAreaComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.getClaim().getArea()-city2.getClaim().getArea();
	}
}

class CityNameComparator implements Comparator<City> {
	@Override
	public int compare(City city1, City city2) {
		return city1.getName().compareTo(city2.getName());
	}
}
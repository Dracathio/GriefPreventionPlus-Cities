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
import java.util.Date;
import java.util.logging.Level;

import org.bukkit.Location;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.GriefPreventionPlus;

class Plot {
	int id;
	Claim claim;
	Location lcorner;
	Location gcorner;
	Citizen citizen;
	String motd;
	Date assignedOn;
	boolean isTakeable;
	
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
	
	/** This method returns if the plot's claim is still there. Not sure where to use this: GP commands are overridden. */
	boolean claimExists() {
		Claim claim = GriefPreventionPlus.instance.dataStore.getClaimAt(this.claim.getLesserBoundaryCorner(), true, this.claim);
		if (claim==null) {
			return false;
		}
		return true;
	}
	
	/** assign this plot to a citizen */
	void assign(Citizen citizen) {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET citizen = "+DataStore.UUIDtoHexString(citizen.id)+", motd = \"\", assignedOn = NOW(), isTakeable = 0 WHERE id = "+this.id+";");
			
			// unset permission if it was already assigned
			if (citizen.id!=null) {
				PlayerData playerData = GPPCities.gppc.ds.playerData.get(citizen.id);
				playerData.perm.unsetPermission("gpp.c"+this.id+".b");
				playerData.perm.unsetPermission("gpp.c"+this.id+".m");
			}
			
			this.claim.clearPermissions();
			
			this.citizen=citizen;
			this.motd="";
			this.assignedOn=new Date();
			this.isTakeable=false;

			// set permission
			PlayerData playerData = GPPCities.gppc.ds.playerData.get(citizen.id);
			playerData.perm.setPermission("gpp.c"+this.id+".b", true);
			playerData.perm.setPermission("gpp.c"+this.id+".m", true);
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set motd for plot "+this.id);
		}
	}
	
	void unassign() {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET citizen = NULL, motd = \"\" WHERE id = "+this.id+";");
			
			this.claim.clearPermissions();

			// unset permission
			PlayerData playerData = GPPCities.gppc.ds.playerData.get(citizen.id);
			playerData.perm.unsetPermission("gpp.c"+this.id+".b");
			playerData.perm.unsetPermission("gpp.c"+this.id+".m");
			
			this.citizen=null;
			this.motd="";
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set motd for plot "+this.id);
		}
	}
	
	void motd(String motd) {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET motd = \""+motd+"\" WHERE id = "+this.id+";");
			this.motd=motd;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set motd for plot "+this.id);
		}
	}
	
	void takeable(boolean sw) {
		try {
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();

			statement.executeUpdate("UPDATE gppc_plots SET isTakeable = "+(sw?1:0)+" WHERE id = "+this.id+";");

			this.isTakeable=sw;
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to set takeable for plot "+this.id);
		}
	}
}
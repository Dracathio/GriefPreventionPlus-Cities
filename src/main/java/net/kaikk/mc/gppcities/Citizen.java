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
import java.util.Comparator;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;

import org.bukkit.OfflinePlayer;

class Citizen {
	UUID id;
	int perms;
	Date joinedOn;
	
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
		return GPPCities.gppc.getServer().getOfflinePlayer(this.id).getLastPlayed();
	}
	
	int getLastPlayedDays() {
		return (int) ((System.currentTimeMillis()-this.getLastPlayed())/86400000);
	}
	/** returns citizen's name */
	String getName() {
		return this.getPlayer().getName();
	}
	/** returns citizen's display name, or the name if he's not online */
	String getDisplayName() {
		OfflinePlayer citizen = this.getPlayer();
		if (citizen.isOnline()) {
			return citizen.getPlayer().getDisplayName();
		} else {
			return citizen.getName();
		}
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
			GPPCities.gppc.ds.dbCheck();
			
			Statement statement = GPPCities.gppc.ds.db.createStatement();
			statement.executeUpdate("UPDATE gppc_citizens SET perms = "+this.perms+" WHERE id = "+DataStore.UUIDtoHexString(this.id)+";");
		} catch (SQLException e) {
			e.getStackTrace();
			GPPCities.gppc.log(Level.SEVERE, e.getMessage());
			GPPCities.gppc.log(Level.SEVERE, "Unable to update perms for citizen UUID["+DataStore.UUIDtoHexString(this.id)+"]");
		}
	}
	
	OfflinePlayer getPlayer() {
		return GPPCities.gppc.getServer().getOfflinePlayer(this.id);
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
		return (int) ((c1.joinedOn.getTime()-c2.joinedOn.getTime())/1000);
	}
}
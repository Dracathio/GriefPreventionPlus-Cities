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

import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import net.kaikk.mc.gpp.Claim;

class PlayerData {
	long lastAction;
	Claim lastClaim;
	Claim lastPlot;
	City lastInvitedCity;
	UUID lastInvitedFrom;
	long lastInvitedTime;
	Location lastShovel;
	Player player;
	PermissionAttachment perm;
	
	PlayerData(Player player) {
		this.lastAction=System.currentTimeMillis();
		this.lastClaim=null;
		this.lastPlot=null;
		this.player=player;
		
		this.perm = player.addAttachment(GPPCities.gppc);
	}
	
	void action(Claim lastClaim, Claim lastPlot) {
		this.lastClaim=lastClaim;
		this.lastPlot=lastPlot;
		this.lastAction=System.currentTimeMillis();
	}
	
	void invited(City city, UUID player) {
		this.lastInvitedCity=city;
		this.lastInvitedFrom = player;
		this.lastInvitedTime=System.currentTimeMillis();
	}
	
	void removeInvite() {
		lastInvitedCity=null;
		lastInvitedFrom=null;
		lastInvitedTime=0;
	}
	
	void removePermAttachment() {
		if (this.perm != null) {
			this.player.removeAttachment(this.perm);
		}
	}
}
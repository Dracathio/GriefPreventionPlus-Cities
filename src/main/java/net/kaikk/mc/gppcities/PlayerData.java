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

public class PlayerData {
	private City lastInvitedCity;
	private UUID lastInvitedFrom;
	private long lastInvitedTime;
	private Location lastShovel;
	private Player player;
	private PermissionAttachment perm;
	
	PlayerData(Player player) {
		this.player=player;
		this.perm = player.addAttachment(GPPCities.getInstance());
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

	public City getLastInvitedCity() {
		return lastInvitedCity;
	}

	void setLastInvitedCity(City lastInvitedCity) {
		this.lastInvitedCity = lastInvitedCity;
	}

	public UUID getLastInvitedFrom() {
		return lastInvitedFrom;
	}

	void setLastInvitedFrom(UUID lastInvitedFrom) {
		this.lastInvitedFrom = lastInvitedFrom;
	}

	public long getLastInvitedTime() {
		return lastInvitedTime;
	}

	void setLastInvitedTime(long lastInvitedTime) {
		this.lastInvitedTime = lastInvitedTime;
	}

	public Location getLastShovel() {
		return lastShovel;
	}

	void setLastShovel(Location lastShovel) {
		this.lastShovel = lastShovel;
	}

	public Player getPlayer() {
		return player;
	}

	void setPlayer(Player player) {
		this.player = player;
	}

	public PermissionAttachment getPerm() {
		return perm;
	}

	void setPerm(PermissionAttachment perm) {
		this.perm = perm;
	}
}
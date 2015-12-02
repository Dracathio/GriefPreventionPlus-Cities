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

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.events.ClaimDeleteEvent;
import net.kaikk.mc.gpp.events.ClaimDeleteEvent.Reason;
import net.kaikk.mc.gpp.events.ClaimEnterEvent;
import net.kaikk.mc.gpp.events.ClaimExitEvent;
import net.kaikk.mc.gpp.events.ClaimFromToEvent;
import net.kaikk.mc.gpp.events.ClaimOwnerTransfer;
import net.kaikk.mc.gppcities.City.Plot;

@SuppressWarnings("deprecation")
class EventListener implements Listener {
	private GPPCities instance;
	
	EventListener(GPPCities instance) {
		this.instance = instance;
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClaimEnter(ClaimEnterEvent event) {
		Claim claim=event.getClaim();
		if (!claim.isAdminClaim()) {
			City city=this.instance.getDataStore().getCity(claim);
			if (city!=null) {
				city.playerEnterMessage(event.getPlayer());
			} else {
				event.getPlayer().sendMessage(Messages.YouOnClaim.get(claim.getOwnerName()));
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClaimExit(ClaimExitEvent event) {
		if (!event.getClaim().isAdminClaim()) {
			event.getPlayer().sendMessage(Messages.YouOnWilderness.get());
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	public void onClaimFromTo(ClaimFromToEvent event) {
		Claim claim=event.getNewClaim();
		Claim oldClaim=event.getOldClaim();
		City city=this.instance.getDataStore().getCity(claim);
		if (claim.getTopClaimID()!=oldClaim.getTopClaimID()) {
			// two adiacent claims
			if (city!=null) {
				city.playerEnterMessage(event.getPlayer());
			} else {
				if (!claim.isAdminClaim()) {
					event.getPlayer().sendMessage(Messages.YouOnClaim.get(claim.getOwnerName()));
				}
			}
		} else {
			if (city!=null) {
				// same city, different plots
				Plot plot=city.getPlot(claim);
				if (plot!=null) { // player entered a plot
					plot.playerEnterMessage(event.getPlayer());
				}
			}
		}
	}
	
	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	void onPlayerJoin(PlayerJoinEvent event) {
		GPPCities.getInstance().getDataStore().playerData.put(event.getPlayer().getUniqueId(), new PlayerData(event.getPlayer()));
		City city = GPPCities.getInstance().getDataStore().getCity(event.getPlayer().getUniqueId());
		if (city != null) {
			PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.get(event.getPlayer().getUniqueId());
			
			// general city permission
			playerData.getPerm().setPermission("gppc.c"+city.getClaim().getID(), true);
			
			// load plot permissions
			for (Plot plot : city.getPlots().values()) {
				if (plot.getCitizen()!=null) {
					if (event.getPlayer().getUniqueId().equals(plot.getCitizen().getId())) {
						playerData.getPerm().setPermission("gpp.c"+plot.getId()+".b", true);
						playerData.getPerm().setPermission("gpp.c"+plot.getId()+".m", true);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerQuit(PlayerQuitEvent event){
		PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.remove(event.getPlayer().getUniqueId());
		if (playerData != null) {
			playerData.removePermAttachment();
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	void onPlayerKick(PlayerKickEvent event){
		PlayerData playerData = GPPCities.getInstance().getDataStore().playerData.remove(event.getPlayer().getUniqueId());
		if (playerData != null) {
			playerData.removePermAttachment();
		}
	}

	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if (player==null) { // not sure if this check is needed
			return;
		}
		
		if (!player.hasPermission("gppc.citychat")) {
			return;
		}

		if (event.getMessage().startsWith("/")) { // commands are excluded
			return;
		}
		
		if (GPPCities.getInstance().getDataStore().cityChat.contains(player.getUniqueId())) {
			City city = GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
			if (city==null) {
				GPPCities.getInstance().getDataStore().cityChat.remove(player.getUniqueId()); // this is not thread-safe
				return;
			}
			String message=Messages.CityChatFormat.get(city.getName(), player.getDisplayName(), event.getMessage());
			CityChatEvent cityChatEvent = new CityChatEvent(city, player, message);
			Bukkit.getPluginManager().callEvent(cityChatEvent);
			if (cityChatEvent.isCancelled()) {
				return;
			}
			
			city.sendMessageToAllCitizens(message);
			GPPCities.getInstance().getDataStore().cityChatSpy(message, city);
			GPPCities.getInstance().log("CC["+city.getName()+"] <"+player.getName()+"> "+event.getMessage());
			event.setCancelled(true);
		}
	}
	
	@EventHandler(ignoreCancelled=true)
	void onClaimDelete(ClaimDeleteEvent event) {
		City city = GPPCities.getInstance().getDataStore().getCity(event.getClaim());
		if (city!=null) {
			if (event.getDeleteReason()==Reason.EXPIRED) {
				// the city claim expired!
				if (city.handleInactiveCitizens()) {
					this.instance.log("Removing city: "+city.getName());
					this.instance.getDataStore().deleteCity(city);
				} else {
					event.setCancelled(true);
				}
			} else {
				if (event.getPlayer()!=null) {
					event.getPlayer().sendMessage(Messages.CantRunCommandYouHaveCity.get(String.valueOf(city.getClaim().getID()), city.getName()));
				}
				event.setCancelled(true);
			}
		}
	}
	
	@EventHandler(ignoreCancelled=true) 
	void onClaimOwnerTransfer(ClaimOwnerTransfer event) {
		City city = GPPCities.getInstance().getDataStore().getCity(event.getClaim());
		if (city!=null) {
			event.getPlayer().sendMessage(Messages.CantRunCommandYouHaveCity.get(String.valueOf(city.getClaim().getID()), city.getName()));
			event.setCancelled(true);
		}
	}
}

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

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.GriefPreventionPlus;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class EventListener implements Listener {
	@EventHandler(priority=EventPriority.LOWEST)
	public void onPlayerMove(PlayerMoveEvent event) {
		PlayerData playerData=GPPCities.gppc.ds.playerData.get(event.getPlayer().getUniqueId()); 
		
		// Let's skip this event if the last event for this player was less than 1 sec ago (better plugin's performances)
		if (System.currentTimeMillis() - playerData.lastAction < 1000) {
			return;
		}
		
		Claim claim = GriefPreventionPlus.instance.dataStore.getClaimAt(event.getTo(), false, playerData.lastClaim);
		if (claim != null){
			Claim pClaim = claim;
			if (claim.parent != null) {
				pClaim = claim.parent;
			}
			
			if (pClaim.isAdminClaim() && !GPPCities.gppc.config.AdminClaimMessage) {
				playerData.action(null, null);
				return;
			}
			
			if (playerData.lastPlot != claim) {
				City city = GPPCities.gppc.ds.citiesMap.get(pClaim.getID());
				if (playerData.lastClaim != pClaim) {
					if (city != null){
						if (city.getCitizen(event.getPlayer().getUniqueId())!=null) {
							event.getPlayer().sendMessage(Messages.WelcomeBackTo.get(city.name));
							if (city.motdRes!=null && !city.motdRes.isEmpty()) {
								event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+city.motdRes));
							}
						} else {
							event.getPlayer().sendMessage(Messages.WelcomeTo.get(city.name));
							if (city.motdOut!=null && !city.motdOut.isEmpty()) {
								event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+city.motdOut));
							}
							
							if (city.isJoinable && GPPCities.gppc.ds.getCity(event.getPlayer().getUniqueId())==null) {
								event.getPlayer().sendMessage(Messages.YouOnJoinableCity.get(city.name));
							}
						}
					
					} else {
						event.getPlayer().sendMessage(Messages.YouOnClaim.get(claim.getOwnerName()));
					}
				}
				
				playerData.action(pClaim, claim);

				if (city != null && playerData.lastPlot != playerData.lastClaim){
					Plot plot = city.getPlot(claim);
					if (plot!=null) {
						if (plot.citizen != null) {
							event.getPlayer().sendMessage(Messages.YouOnPlot.get(plot.citizen.getDisplayName()));
						} else {
							event.getPlayer().sendMessage(Messages.YouOnUnassignedPlot.get());
							
							Citizen citizen = city.getCitizen(event.getPlayer().getUniqueId());
							if (plot.isTakeable && citizen!=null && !city.citizenHasAssignedPlot(citizen)) {
								event.getPlayer().sendMessage(Messages.YouOnTakeablePlot.get());
							}
						}
						if (!plot.motd.isEmpty()) {
							event.getPlayer().sendMessage(ChatColor.translateAlternateColorCodes('&', "&a"+plot.motd));
						}
					}
				}
			}
		} else {
			if (playerData.lastClaim!=null) {
				event.getPlayer().sendMessage(Messages.YouOnWilderness.get());
				playerData.action(null, null);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerJoin(PlayerJoinEvent event) {
		GPPCities.gppc.ds.playerData.put(event.getPlayer().getUniqueId(), new PlayerData(event.getPlayer()));
		City city = GPPCities.gppc.ds.getCity(event.getPlayer().getUniqueId());
		if (city != null) {
			PlayerData playerData = GPPCities.gppc.ds.playerData.get(event.getPlayer().getUniqueId());
			
			// general city permission
			playerData.perm.setPermission("gppc.c"+city.claim.getID(), true);
			
			// load plot permissions
			for (Plot plot : city.plots.values()) {
				if (plot.citizen!=null) {
					if (event.getPlayer().getUniqueId().equals(plot.citizen.id)) {
						playerData.perm.setPermission("gpp.c"+plot.id+".b", true);
						playerData.perm.setPermission("gpp.c"+plot.id+".m", true);
					}
				}
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerQuit(PlayerQuitEvent event){
		PlayerData playerData = GPPCities.gppc.ds.playerData.remove(event.getPlayer().getUniqueId());
		if (playerData != null) {
			playerData.removePermAttachment();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerKick(PlayerKickEvent event){
		PlayerData playerData = GPPCities.gppc.ds.playerData.remove(event.getPlayer().getUniqueId());
		if (playerData != null) {
			playerData.removePermAttachment();
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	void onPlayerChat(AsyncPlayerChatEvent event) {
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
		
		if (GPPCities.gppc.ds.cityChat.contains(player.getUniqueId())) {
			City city = GPPCities.gppc.ds.getCity(player.getUniqueId());
			if (city==null) {
				GPPCities.gppc.ds.cityChat.remove(player.getUniqueId()); // this is not thread-safe
				return;
			}
			String message=Messages.CityChatFormat.get(city.name, player.getDisplayName(), event.getMessage());
			city.sendMessageToAllCitizens(message);
			GPPCities.gppc.ds.cityChatSpy(message, city);
			GPPCities.gppc.log("CC["+city.name+"] <"+player.getName()+"> "+event.getMessage());
			event.setCancelled(true);
		}
	}
	
}

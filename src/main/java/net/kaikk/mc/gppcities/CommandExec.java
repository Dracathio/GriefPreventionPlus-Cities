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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.kaikk.mc.gpp.Claim;
import net.kaikk.mc.gpp.GriefPreventionPlus;
import net.kaikk.mc.gppcities.City.Citizen;
import net.kaikk.mc.gppcities.City.Plot;

class CommandExec implements CommandExecutor {
	public Map<Integer, City> citiesMap = GPPCities.getInstance().getDataStore().citiesMap; // GPPCities database
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("Console commands are WIP!");
			return true;
		}
		final Player player = (Player) sender;
		final PlayerData playerData=GPPCities.getInstance().getDataStore().playerData.get(player.getUniqueId()); 

		// GPPCities commands
		if (!player.hasPermission("gppc.city")) {
			player.sendMessage(Messages.NoPermission.get());
			return false;
		}
		
		if (cmd.getName().equalsIgnoreCase("city")) {
			if (args.length==0 || args[0].equalsIgnoreCase("help") || args[0].equalsIgnoreCase("info")) {
				City city = GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if ((city==null && (args.length==0 || !args[0].equalsIgnoreCase("info"))) || (args.length!=0 && args[0].equalsIgnoreCase("help"))) {
					player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6===== GriefPreventionPlus-Cities Help ====="));

					player.sendMessage("- help - show this help");
					player.sendMessage("- list - show city list");
					player.sendMessage("- spawn [city name] - teleport to city spawn");
					player.sendMessage("- join - join a city");
					player.sendMessage("- info - info about a city");
					player.sendMessage("- res - info about a player");
					player.sendMessage("- new [city name] - make a new city");
					if (city!=null) {
						Citizen citizen;
						if ((citizen = city.getCitizen(player.getUniqueId())) != null) {
							if (citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Mayor's commands:"));
							} else if (citizen.checkPerm(CitizenPermission.Assistant.perm)) {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Assistant's commands:"));
							} else {
								player.sendMessage(ChatColor.translateAlternateColorCodes('&', "&6Citizen's commands:"));
							}
							
							player.sendMessage("- leave - Leave the city");
							
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Spawn.perm)) {
								player.sendMessage("- setspawn");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage("- plot [info|new|assign|delete|take|takeable|motd]");
							} else {
								player.sendMessage("- plot [info|take]");
							}
							
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Motd.perm)) {
								player.sendMessage("- motd [res|out|current]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm)) {
								player.sendMessage("- invite [player]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Expel.perm)) {
								player.sendMessage("- expel [citizen name]");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
								player.sendMessage("- ban [citizen name] - ban the player from the city");
								player.sendMessage("- unban [citizen name] - unban the player from the city");
								player.sendMessage("- perm [citizen name] (set|unset) (A|E|M|I|P|S) - Manage permissions");
								player.sendMessage("- autojoin [true|false] - Players can auto join this city");
							}
							if (citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage("- rename [city name]");
								player.sendMessage("- mayor [citizen name] - Change mayor");
								player.sendMessage("- delete [city name]");
							}
						}
						
						if (player.hasPermission("gppc.citychat")) {
							player.sendMessage("/citychat - /cc - send a message to your citizens or toggle the default send channel");
						}
						
					}
					player.sendMessage("§4Wiki: http://is.gd/gMH6vI");
					
					return true;
				}
				
				if (args.length==2) {
					city = GPPCities.getInstance().getDataStore().getCity(args[1]);
					if (city==null) {
						player.sendMessage(Messages.WrongCityName.get());
						return false;
					}
				}
				if (city!=null) {
					player.sendMessage(city.info());
				} else {
					player.sendMessage(Messages.YouNotInACity.get());
				}
				return true;
			} else if (args[0].equalsIgnoreCase("list")) {
				if (citiesMap.isEmpty()) {
					player.sendMessage(Messages.NoCities.get());
					return true;
				}
				
				ArrayList<City> cities = new ArrayList<City>(citiesMap.values());
				Collections.sort(cities, Collections.reverseOrder(new CitySizeComparator()));
				
				player.sendMessage(Messages.CitiesList.get());
				
				for (City city : cities) {
					player.sendMessage(Messages.CitiesListFormat.get(city.getName()+(city.isJoinable()?"*":""), Integer.toString(city.getCitizens().size())));
				}
				
				return true;
			} else if (args[0].equalsIgnoreCase("spawn")) {
				if (!player.hasPermission("gppc.city.spawn")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					final City city = GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.YouNotCitizen.get());
						return true;
					}
					
					if (!city.isSpawnValid()) {
						player.sendMessage(Messages.CitySpawnMissing2.get(city.getName()));
						return true;
					}
					
					if (player.hasPermission("gppc.nodelay")) {
						city.teleport(player);
					} else {
						player.sendMessage(Messages.CitySpawnTeleportDelay.get(city.getName()));
						final int x = player.getLocation().getBlockX(), z = player.getLocation().getBlockZ();
						new BukkitRunnable() {
							byte c=0;
							@Override
							public void run() {
								if (x != player.getLocation().getBlockX() || z != player.getLocation().getBlockZ()) {
									player.sendMessage(Messages.CitySpawnTeleportCancelled.get());
									this.cancel();
									return;
								}
								
								if (c>=10) {
									city.teleport(player);
									this.cancel();
								}
								c++;
							}
						}.runTaskTimer(GPPCities.getInstance(),	0L, 10L);
					}
					return true;
				}
				
				if (!player.hasPermission("gppc.city.spawn.any")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
					
				City city = GPPCities.getInstance().getDataStore().getCity(args[1]);

				if (city==null) {
					player.sendMessage(Messages.ThisCityNotExists.get());
					return true;
				}
				
				if (!city.isSpawnValid()) {
					city.setSpawn(null);
					player.sendMessage(Messages.CitySpawnMissing2.get(city.getName()));
					return true;
				}

				city.teleport(player);
				return true;
			} else if (args[0].equalsIgnoreCase("invite")) {
				if (args.length==1) {
					player.sendMessage("Usage: /city invite [playerName]");
					return false;
				}
				
				City city = GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				
				if (city==null) {
					player.sendMessage(Messages.InviteNotAllowed.get());
					return false;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm)) {
					player.sendMessage(Messages.InviteNotAllowed.get());
					return false;
				}
				
				Player player2 = DataStore.getOnlinePlayer(args[1]);
				
				if (player2 == null) {
					player.sendMessage(Messages.PlayerOfflineOrWrongName.get());
					return false;
				}
				
				if (GPPCities.getInstance().getDataStore().getCity(player2.getUniqueId())!=null) {
					player.sendMessage(Messages.PlayerAlreadyOnAnotherCity.get());
					return false;
				}
				
				if (city.getBannedPlayers().contains(player2.getUniqueId())) {
					player.sendMessage(Messages.PlayerIsBanned.get(city.getName()));
					return false;
				}
				
				PlayerData playerData2=GPPCities.getInstance().getDataStore().playerData.get(player2.getPlayer().getUniqueId()); 
				Date now = new Date();
				if (playerData2.getLastInvitedTime() == 0 || playerData2.getLastInvitedTime()+120000 < now.getTime()) {
					playerData2.invited(city, player.getUniqueId());
					player.sendMessage(Messages.CityInvitationSent.get(args[1]));
					player2.sendMessage(Messages.CityInvitationReceived.get(player.getName(), city.getName()));
					player2.sendMessage(Messages.CityInvitationAcceptOrReject.get());
					GPPCities.getInstance().log(player.getName()+" invited "+player2.getName()+" to "+city.getName());
					return true;
				}
				player.sendMessage(Messages.CityInvitationExpiresOn.get(Long.toString((120-((System.currentTimeMillis()-playerData2.getLastInvitedTime())/1000)))));
				return false;
			} else if (args[0].equalsIgnoreCase("accept") || args[0].equalsIgnoreCase("reject")) {
				Date now = new Date();
				if (playerData.getLastInvitedTime()==0 || playerData.getLastInvitedTime()+120000 < now.getTime()) {
					player.sendMessage(Messages.CityInvitationNoPending.get());
					return false;
				}
				
				if (args[0].equalsIgnoreCase("accept")) {
					player.sendMessage(Messages.CityInvitationAccepted.get());
					GPPCities.getInstance().getServer().getPlayer(playerData.getLastInvitedFrom()).sendMessage(Messages.CityInvitationHasBeenAccepted.get(player.getName()));
					playerData.getLastInvitedCity().newCitizen(player.getUniqueId());
					GPPCities.getInstance().log(player.getName()+" accepted invitation to "+playerData.getLastInvitedCity().getName());
				} else {
					player.sendMessage(Messages.CityInvitationRejected.get());
					GPPCities.getInstance().getServer().getPlayer(playerData.getLastInvitedFrom()).sendMessage(Messages.CityInvitationHasBeenRejected.get(player.getName()));
					GPPCities.getInstance().log(player.getName()+" rejected invitation to "+playerData.getLastInvitedCity().getName());
				}
				playerData.removeInvite();
				return true;
			} else if (args[0].equalsIgnoreCase("join")) {
				if (args.length!=2) {
					player.sendMessage("Usage: /city join [CityName]");
					return false;
				}

				City city = GPPCities.getInstance().getDataStore().getCity(args[1]);
				if (city==null) {
					player.sendMessage(Messages.CityNotExists.get(args[1]));
					return false;
				}
				
				if (GPPCities.getInstance().getDataStore().getCity(player.getUniqueId())!=null) {
					player.sendMessage(Messages.YouAlreadyOnAnotherCity.get());
					return false;
				}
				
				if (city.getBannedPlayers().contains(player.getUniqueId())) {
					player.sendMessage(Messages.YouAreBanned.get(city.getName()));
					return false;
				}
				
				if (city.isJoinable()) {
					player.sendMessage(Messages.YouJoinedCity.get(city.getName()));
					city.newCitizen(player.getUniqueId());
					
					GPPCities.getInstance().log(player.getName()+" joined "+city.getName());
				} else {
					player.sendMessage(Messages.CityAutojoinNotPermitted.get(city.getName()));
				}
				return true;
			} else if (args[0].equalsIgnoreCase("delete")) {
				if (args.length!=2) {
					player.sendMessage("Usage: /city delete CityName");
					return false;
				}
				
				City city = GPPCities.getInstance().getDataStore().getCity(args[1]);
				if (city==null) {
					player.sendMessage(Messages.WrongCityNameCS.get());
					return false;
				}

				if (player.getUniqueId().equals(city.getMayor().getId())) { // The mayor can delete a city
					GPPCities.getInstance().log(player.getName()+" deleted "+city.getName());
					GPPCities.getInstance().getDataStore().deleteCity(city);
					return true;
				}
				
				if (player.hasPermission("gppc.cityAdmin")) { // Admins can delete a city
					GPPCities.getInstance().log("Admin "+player.getName()+" deleted "+city.getName());
					GPPCities.getInstance().getDataStore().deleteCity(city);
					return true;
				}
				
				player.sendMessage(Messages.NoPermission.get());
				return false;
			} else if (args[0].equalsIgnoreCase("autojoin")) {
				if (args.length<2) {
					player.sendMessage("Usage: /city autojoin [true|false]");
					return false;
				}
				
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
					player.sendMessage(Messages.NotAuthorized.get());
					return true;
				}
				
				if (args[1].equalsIgnoreCase("true")) {
					city.setAutojoin(true);
					player.sendMessage(Messages.CityAutojoinOn.get());
					GPPCities.getInstance().log(player.getName()+" set "+city.getName()+" autjoin to true");
					return true;
				}
				if (args[1].equalsIgnoreCase("false")) {
					city.setAutojoin(false);
					player.sendMessage(Messages.CityAutojoinOff.get());
					GPPCities.getInstance().log(player.getName()+" set "+city.getName()+" autjoin to false");
					return true;
				}				
				
				player.sendMessage("Usage: /city autojoin [true|false]");
				return false;
			} else if (args[0].equalsIgnoreCase("motd")) {
				if (args.length<2) {
					player.sendMessage("Usage: /city motd [res|out|current] [message]");
					return false;
				}
				
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Motd.perm)) {
					player.sendMessage(Messages.NotAuthorized.get());
					return true;
				}
				
				String motd=DataStore.mergeStringArrayFromIndex(args, 2);
				
				if (args[1].equalsIgnoreCase("res") || args[1].equalsIgnoreCase("out")) {
					city.setMotd(motd, args[1].equalsIgnoreCase("res"));
					player.sendMessage(Messages.MotdSet.get());
					GPPCities.getInstance().log(player.getName()+" set "+city.getName()+" "+args[1]+" motd to "+motd);
					return true;
				} else if (args[1].equalsIgnoreCase("current")) {
					// show both current motd
					player.sendMessage(Messages.MotdRes.get(ChatColor.translateAlternateColorCodes('&', "&a"+city.getMotdRes()))); 
					player.sendMessage(Messages.MotdOut.get(ChatColor.translateAlternateColorCodes('&', "&a"+city.getMotdOut())));
					return true;
				}
				player.sendMessage("Usage: /city motd [res|out|current] [message]");
				return false;
			} else if (args[0].equalsIgnoreCase("leave")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return false;
				}
				if (city.getMayor().getId().equals(player.getUniqueId())) {
					player.sendMessage(Messages.MayorCannotLeave.get());
					return false;
				}
				
				player.sendMessage(Messages.YouLeftCity.get(city.getName()));
				city.removeCitizen(player.getUniqueId());
				city.sendMessageToAllCitizens(Messages.PlayerLeftCity.get(player.getDisplayName()));
				GPPCities.getInstance().log(player.getName()+" left "+city.getName());
				return true;
			} else if (args[0].equalsIgnoreCase("expel")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city expel [CitizenName]");
					return false;
				}
				
				Citizen citizen = city.getCitizen(player.getUniqueId());
				if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Expel.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				citizen = city.getCitizen(args[1]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				
				if (citizen==city.getMayor()) {
					player.sendMessage(Messages.MayorCannotLeave.get());
					return false;
				}
				
				if (citizen.getPlayer()!=null) {
					citizen.getPlayer().sendMessage(Messages.YouveBeenExpelled.get(city.getName()));
				}
				
				GPPCities.getInstance().log(player.getName()+" expelled "+citizen.getName()+" from "+city.getName());
				city.removeCitizen(citizen.getId());
				player.sendMessage(Messages.CitizenExpelled.get());
				return true;
			} else if (args[0].equalsIgnoreCase("res")) {
				if (args.length==1) {
					City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.YouNotInACity.get());
						return false;
					}
					player.sendMessage(Messages.CitizensList.get(city.getName()));
					
					ArrayList<Citizen> citizens = new ArrayList<Citizen>(city.getCitizens().values());
					Collections.sort(citizens, city.new CitizenLastPlayedComparator());
					
					for (Citizen citizen : citizens) {
						player.sendMessage(Messages.CitizensListFormat.get(citizen.getDisplayName(), Long.toString((System.currentTimeMillis()-citizen.getLastPlayed())/86400000)));
					}
					return true;
				} else if (args.length==2) {
					OfflinePlayer targetPlayer=GriefPreventionPlus.getInstance().resolvePlayer(args[1]);
					
					if (targetPlayer==null || !targetPlayer.hasPlayedBefore()) {
						player.sendMessage(Messages.PlayerOfflineOrWrongName.get());
						return false;
					}

					City city = GPPCities.getInstance().getDataStore().getCity(targetPlayer.getUniqueId());
					if (city==null) {
						player.sendMessage(Messages.PlayerIsNotACitizen.get());
						return false;
					}
					
					Citizen citizen = city.getCitizen(targetPlayer.getUniqueId());
					player.sendMessage(Messages.CitizenInfo.get(citizen.getDisplayName(), city.getName(), DateFormat.getDateTimeInstance().format(citizen.getJoinedOn()), DateFormat.getDateTimeInstance().format(citizen.getLastPlayed())));
					return true;
				}
				player.sendMessage(Messages.WrongCommand.get());
				return false;
			} else if (args[0].equalsIgnoreCase("mayor")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city mayor [CitizenName]");
					return false;
				}
				Citizen citizen = city.getCitizen(args[1]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				
				city.changeOwner(citizen.getId());
				GPPCities.getInstance().log(player.getName()+" changed "+city.getName()+" mayor to "+citizen.getName());
				return true;
			} else if (args[0].equalsIgnoreCase("rename")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=2) {
					player.sendMessage("Usage: /city rename [CityName]");
					return false;
				}
				
				String newName=args[1];
				if (newName==null || newName.isEmpty()) {
					player.sendMessage(Messages.WrongCityName.get());
					return false;
				}
				
				City existentCity = GPPCities.getInstance().getDataStore().getCity(newName);
				if (existentCity!=null) {
					player.sendMessage(Messages.CitySameNameExists.get());
					return false;
				}
				
				GPPCities.getInstance().log(player.getName()+" renamed "+city.getName()+" to "+newName);
				city.rename(newName);
				player.sendMessage(Messages.CityRenamed.get());
				
				return true;
			} else if (args[0].equalsIgnoreCase("ban")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city ban [PlayerName]");
					return false;
				}
				
				UUID playerId=GriefPreventionPlus.getInstance().resolvePlayerId(args[1]);
				if (playerId==null) {
					player.sendMessage(GriefPreventionPlus.getInstance().getDataStore().getMessage(net.kaikk.mc.gpp.Messages.PlayerNotFound2));
					return false;
				}

				if (city.getMayor()==city.getCitizen(playerId)) {
					player.sendMessage(Messages.MayorCannotLeave.get());
					return false;
				}
				
				city.addBan(playerId);
				player.sendMessage(Messages.PlayerBannedConfirm.get(args[1], city.getName()));
				GPPCities.getInstance().log(player.getName()+" banned "+args[1]+" from "+city.getName());
				return true;
			} else if (args[0].equalsIgnoreCase("unban")) {
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				if (city==null) {
					player.sendMessage(Messages.YouNotInACity.get());
					return true;
				}
				
				if (!city.getCitizen(player.getUniqueId()).checkPerm(CitizenPermission.Mayor.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length==1) {
					player.sendMessage("Usage: /city ban [PlayerName]");
					return false;
				}
				
				UUID playerId=GriefPreventionPlus.getInstance().resolvePlayerId(args[1]);
				if (playerId==null) {
					player.sendMessage(GriefPreventionPlus.getInstance().getDataStore().getMessage(net.kaikk.mc.gpp.Messages.PlayerNotFound2));
					return false;
				}
				
				city.unban(playerId);
				player.sendMessage(Messages.PlayerUnbannedConfirm.get(player.getName(), city.getName()));
				GPPCities.getInstance().log(player.getName()+" unbanned "+args[1]+" from "+city.getName());
				return true;
			} else if (args[0].equalsIgnoreCase("perm")) {
				if (args.length==1 || args.length==3) {
					player.sendMessage("Usage: /city perm ([citizen name]|default) (set|unset) (A|I|E|M|P|S)");
					player.sendMessage("[A-ssistant, I-nvite, E-xpel, M-otd, P-lot, S-pawn]");
					return false;
				}
				
				City city=GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
				
				Citizen senderCitizen = city.getCitizen(player.getUniqueId());
				if (!senderCitizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args[1].equalsIgnoreCase("default")) {
					if (args.length==4) {
						if (senderCitizen.checkPerm(CitizenPermission.Assistant) && args[3].toLowerCase().contains("a")) {
							player.sendMessage(Messages.NoPermission.get());
							return false;
						}
						
						if (args[2].equalsIgnoreCase("set")) {
							if (!city.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
								city.setPerm(City.parsePerms(args[3]));
								GPPCities.getInstance().log(player.getName()+" set "+city.getName()+" default perms "+args[3]);
							}
						} else if (args[2].equalsIgnoreCase("unset")) {
							city.unsetPerm(City.parsePerms(args[3]));
							GPPCities.getInstance().log(player.getName()+" unset "+city.getName()+" default perms "+args[3]);
						}
					}
					
					player.sendMessage(Messages.CitizenPermissions.get(city.getName(), city.permsToString()));
					
				} else {
					Citizen citizen = city.getCitizen(args[1]);
					if (citizen==null) {
						player.sendMessage(Messages.PlayerIsNotACitizen.get());
						return false;
					}
	
					if (args.length==4) {
						if (senderCitizen.checkPerm(CitizenPermission.Assistant) && args[3].toLowerCase().contains("a")) {
							player.sendMessage(Messages.NoPermission.get());
							return false;
						}
						
						if (args[2].equalsIgnoreCase("set")) {
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm)) {
								citizen.setPerm(City.parsePerms(args[3]));
								GPPCities.getInstance().log(player.getName()+" set "+citizen.getName()+"'s perms "+args[3]);
							}
						} else if (args[2].equalsIgnoreCase("unset")) {
							citizen.unsetPerm(City.parsePerms(args[3]));
							GPPCities.getInstance().log(player.getName()+" unset "+citizen.getName()+"'s perms "+args[3]);
						}
					}
					
					player.sendMessage(Messages.CitizenPermissions.get(args[1], citizen.permsToString()));
				}
				return true;
			} else {
				Claim claim = GriefPreventionPlus.getInstance().getDataStore().getClaimAt(player.getLocation(), false);

				if (claim == null) {
					player.sendMessage(Messages.YouNotOnAClaim.get());
					return true;
				}
				Claim pClaim; // this contains the parent claim or itself if there's no parent
				if (claim.getParent() != null) {
					pClaim = claim.getParent();
				} else {
					pClaim = claim;
				}
				
				City city = GPPCities.getInstance().getDataStore().citiesMap.get(pClaim.getID());

				if (pClaim.isAdminClaim()) {
					player.sendMessage(Messages.CitiesNotAvailableOnAdminClaims.get());
					return false;
				}
				
				if (args[0].equalsIgnoreCase("new")) {
					if (!player.getUniqueId().equals(pClaim.getOwnerID())) {
						player.sendMessage(Messages.NoPermission.get());
						return false;
					}
					
					if (args.length!=2 || args[1].matches("^.*[^a-zA-Z0-9_-].*$")) {
						player.sendMessage("Usage: /city new [cityName]");
						return false;
					}

					if (city != null) {
						player.sendMessage(Messages.ClaimAlreadyACity.get());
						return false;
					}
					
					if (pClaim.getArea()<GPPCities.getInstance().config.CityMinSize) {
						player.sendMessage(Messages.ClaimTooSmall.get(Integer.toString(GPPCities.getInstance().config.CityMinSize)));
						return false;
					}
					
					String result = GPPCities.getInstance().getDataStore().newCity(pClaim, args[1], player.getUniqueId(), player.getLocation());
					if (result=="") {
						GPPCities.getInstance().getServer().broadcastMessage(Messages.NewCity.get(GPPCities.getInstance().getServer().getPlayer(player.getUniqueId()).getDisplayName(), args[1]));
						GPPCities.getInstance().log("New city "+args[1]+" made by "+player.getName()+" at "+pClaim.locationToString());
					} else {
						player.sendMessage(result);
					}
					return true;
				} else if (args[0].equalsIgnoreCase("setspawn")) {
					Citizen citizen = city.getCitizen(player.getUniqueId());
					
					if (citizen==null || !citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Spawn.perm)) {
						player.sendMessage(Messages.NoPermission.get());
						return false;
					}
					if (city.getClaim().contains(player.getLocation(), false, false)) {
						city.setSpawn(player.getLocation());
						player.sendMessage(Messages.CitySpawnSet.get());
						GPPCities.getInstance().log("City id "+city.getClaim().getID()+" spawn set by "+player.getName()+" to "+player.getLocation().toString());
					} else {
						player.sendMessage(Messages.CitySpawnInvalid.get());
					}
					return true;
				} else if (args[0].equalsIgnoreCase("plot")) {
					if (claim.getParent() == null) {
						player.sendMessage(Messages.StandOnPlotOrSubClaim.get());
						return false;
					}
					if (city == null) {
						player.sendMessage(Messages.StandOnPlotOrSubClaim.get());
						return false;
					}
					
					if (city.getCitizen(player.getUniqueId())==null) {
						player.sendMessage(Messages.YouNotACitizenHere.get());
						return false;
					}
					
					Plot plot = city.getPlot(claim);
					if (plot!=null){
						if (args.length==1) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage("Usage: /city plot [info|assign|unassign|take|takeable|motd|delete]");
							} else {
								player.sendMessage("Usage: /city plot [info|take|motd]");
							}
							return false;
						}

						if (args[1].equalsIgnoreCase("assign")) {
							if (args.length!=3) {
								player.sendMessage("Usage: /city plot assign [citizenName]");
								return false;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							citizen = city.getCitizen(args[2]);
							if (citizen == null) {
								player.sendMessage(Messages.PlayerIsNotACitizen.get());
								return false;
							}
							
							if (plot.getCitizen() != null && GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()) != null) {
								GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							city.assignPlot(claim, citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							
							if (GPPCities.getInstance().getServer().getPlayer(citizen.getId()) != null) {
								GPPCities.getInstance().getServer().getPlayer(citizen.getId()).sendMessage(Messages.YouGotAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							GPPCities.getInstance().log("Plot id "+plot.getId()+" assigned by "+player.getName()+" to "+args[2]);
							
							return true;
						}
						
						if (args[1].equalsIgnoreCase("unassign")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							

							if (GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()) != null) {
								GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							plot.unassign();
							player.sendMessage(Messages.PlotUnassigned.get());
							GPPCities.getInstance().log("Plot id "+plot.getId()+" unassigned by "+player.getName());
							return true;
						}
						
						if (args[1].equalsIgnoreCase("delete")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (plot.getCitizen() != null && GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()) != null) {
								GPPCities.getInstance().getServer().getPlayer(plot.getCitizen().getId()).sendMessage(Messages.YouLostAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							city.deletePlot(plot);
							
							player.sendMessage(Messages.PlotDeleted.get());
							GPPCities.getInstance().log("Plot id "+plot.getId()+" deleted by "+player.getName());
							return true;
						}
						
						if (args[1].equalsIgnoreCase("motd")) {
							if (args.length<2) {
								player.sendMessage("Usage: /city plot motd [message]");
								return false;
							}
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Invite.perm) && (plot.getCitizen()==null || !plot.getCitizen().getId().equals(player.getUniqueId()))) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							String motd=DataStore.mergeStringArrayFromIndex(args, 2);
							if (motd==null) {
								motd="";
							}
							
							plot.motd(motd);
							player.sendMessage(Messages.PlotMotdEdited.get());
							GPPCities.getInstance().log("Plot id "+plot.getId()+" motd modified by "+citizen.getName()+" to "+motd);
							return true;
						}
						
						if (args[1].equalsIgnoreCase("take")) {
							if (!plot.isTakeable()) {
								player.sendMessage(Messages.PlotTakeNotAllowed.get());
								return true;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (citizen==null) {
								player.sendMessage(Messages.YouNotACitizenHere.get());
								return true;
							}
							
							if (city.citizenHasAssignedPlot(citizen)) {
								player.sendMessage(Messages.PlotTakeYouHaveAPlotAlready.get());
								return true;
							}
							
							plot.assign(citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							GPPCities.getInstance().log("Plot id "+plot.getId()+" took by "+citizen.getName());
							
							return true;
						}
						
						
						if (args[1].equalsIgnoreCase("takeable")) {
							if (args.length<3) {
								player.sendMessage("Usage: /city plot takeable (true|false)");
								return false;
							}
							
							if (plot.getCitizen()!=null) {
								
								return false;
							}
							
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (args[2].equalsIgnoreCase("true")) {
								plot.takeable(true);
								player.sendMessage(Messages.TakeableOn.get());
								GPPCities.getInstance().log("Plot id "+plot.getId()+" takeable on");
							} else if (args[2].equalsIgnoreCase("false")) {
								plot.takeable(false);
								player.sendMessage(Messages.TakeableOff.get());
								GPPCities.getInstance().log("Plot id "+plot.getId()+" takeable off");
							}
							return true;
						}
						
						if (args[1].equalsIgnoreCase("info")) {
							if (plot.getCitizen()!=null) {
								player.sendMessage(Messages.PlotInfo.get(Integer.toString(plot.getId()), DateFormat.getDateTimeInstance().format(plot.getAssignedOn()), plot.getCitizen().getDisplayName(), (plot.isTakeable()?"true":"false")));
							} else {
								player.sendMessage(Messages.PlotInfo.get(Integer.toString(plot.getId()), "N/A", "N/A", (plot.isTakeable()?"true":"false")));
							}
							return true;
						}
						
						player.sendMessage(Messages.WrongCommand.get());
						return false;
					} else {
						if (args.length==1) {
							player.sendMessage("Usage: /city plot [new|assign]");
							return false;
						}
						
						
						if (args[1].equalsIgnoreCase("new")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							city.newPlot(claim);
							player.sendMessage(Messages.PlotCreated.get());
							return true;
						} 
						
						if (args[1].equalsIgnoreCase("assign")) {
							Citizen citizen = city.getCitizen(player.getUniqueId());
							if (!citizen.checkPerm(CitizenPermission.Mayor.perm|CitizenPermission.Assistant.perm|CitizenPermission.Plot.perm)) {
								player.sendMessage(Messages.NoPermission.get());
								return false;
							}
							
							if (args.length!=3) {
								player.sendMessage("Usage: /city plot assign [citizenName]");
								return false;
							}
							
							citizen = city.getCitizen(args[2]);
							if (citizen == null) {
								player.sendMessage(Messages.PlayerIsNotACitizen.get());
								return false;
							}
							
							city.assignPlot(claim, citizen);
							player.sendMessage(Messages.PlotAssigned.get());
							
							if (GPPCities.getInstance().getServer().getPlayer(citizen.getId()) != null) {
								GPPCities.getInstance().getServer().getPlayer(citizen.getId()).sendMessage(Messages.YouGotAPlot.get(player.getDisplayName(), claim.locationToString()));
							}
							
							return true;
						}
						
						player.sendMessage(Messages.WrongCommand.get());
						return false;
					}
				}

				player.sendMessage(Messages.WrongCommand.get());
				return true;
			}
		}
		
		
		if (cmd.getName().equalsIgnoreCase("citychat")) {
			if (!player.hasPermission("gppc.citychat")) {
				player.sendMessage(Messages.NoPermission.get());
				return false;
			}
			
			City city = GPPCities.getInstance().getDataStore().getCity(player.getUniqueId());
			if (city==null) {
				player.sendMessage(Messages.YouNotCitizen.get());
				return false;
			}
			
			if (args.length==0) {
				if (GPPCities.getInstance().getDataStore().cityChat.contains(player.getUniqueId())) {
					GPPCities.getInstance().getDataStore().cityChat.remove(player.getUniqueId());
					player.sendMessage(Messages.CityChatOff.get());
				} else {
					GPPCities.getInstance().getDataStore().cityChat.add(player.getUniqueId());
					player.sendMessage(Messages.CityChatOn.get());
				}
				return true;
			}
			
			String message=DataStore.mergeStringArrayFromIndex(args, 0);
			String cMessage=Messages.CityChatFormat.get(city.getName(), player.getDisplayName(), message);
			
			CityChatEvent event = new CityChatEvent(city, player, cMessage);
			Bukkit.getPluginManager().callEvent(event);
			if (event.isCancelled()) {
				return true;
			}
			
			city.sendMessageToAllCitizens(cMessage);
			GPPCities.getInstance().getDataStore().cityChatSpy(cMessage, city);
			GPPCities.getInstance().log("CC["+city.getName()+"] <"+player.getName()+"> "+message);
			return true;
		}
		
		if (cmd.getName().equalsIgnoreCase("citychatspy")) {
			if (!player.hasPermission("gppc.cityadmin.spy")) {
				player.sendMessage(Messages.NoPermission.get());
				return false;
			}
			
			if (args.length!=1) {
				player.sendMessage("Usage: /citychatspy [on|off]");
				return false;
			}
			
			if (args[0].equalsIgnoreCase("on")) {
				GPPCities.getInstance().getDataStore().cityChatSpy.add(player.getUniqueId());
				player.sendMessage("City chat spy on");
				return true;
			} else if (args[0].equalsIgnoreCase("off")) {
				GPPCities.getInstance().getDataStore().cityChatSpy.remove(player.getUniqueId());
				player.sendMessage("City chat spy off");
				return true;
			}
		}
		
		if (cmd.getName().equalsIgnoreCase("cityadmin")) {
			if (!player.hasPermission("gppc.cityadmin")) {
				player.sendMessage(Messages.NoPermission.get());
				return false;
			}
			
			if (args.length<2) {
				player.sendMessage("Usage: /cityadmin [CityName] [join|mayor|rename|delete|expel|setspawn]");
				return false;
			}
			
			City city = GPPCities.getInstance().getDataStore().getCity(args[0]);
			if (city==null) {
				player.sendMessage(Messages.WrongCityNameCS.get());
				return false;
			}
			
			if (args[0].equalsIgnoreCase("delete")) {
				if (!player.hasPermission("gppc.cityadmin.delete")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=2) {
					player.sendMessage("Usage: /cityadmin [CityName] delete");
					return false;
				}

				GPPCities.getInstance().log("Admin "+player.getName()+" deleted "+city.getName());
				GPPCities.getInstance().getDataStore().deleteCity(city);
				return true;
			}
			
			if (args[1].equalsIgnoreCase("join")) {
				if (!player.hasPermission("gppc.cityadmin.join")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=2) {
					player.sendMessage("Usage: /cityadmin [CityName] join");
					return false;
				}
				
				if (GPPCities.getInstance().getDataStore().getCity(player.getUniqueId())!=null) {
					player.sendMessage(Messages.YouAlreadyOnAnotherCity.get());
					return false;
				}

				player.sendMessage(Messages.YouJoinedCity.get(city.getName()));
				city.newCitizen(player.getUniqueId());
				
				GPPCities.getInstance().log("Admin "+player.getName()+" joined "+city.getName());
				
				return true;
			}

			if (args[1].equalsIgnoreCase("mayor")) {
				if (!player.hasPermission("gppc.cityadmin.mayor")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=3) {
					player.sendMessage("Usage: /cityadmin [CityName] mayor [CitizenName]");
					return false;
				}
				
				Citizen citizen = city.getCitizen(args[2]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				
				city.changeOwner(citizen.getId());
				GPPCities.getInstance().log("Admin "+player.getName()+" changed "+city.getName()+" mayor to "+citizen.getName());
				return true;
			}
			
			if (args[1].equalsIgnoreCase("rename")) {
				if (!player.hasPermission("gppc.cityadmin.rename")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=3) {
					player.sendMessage("Usage: /cityadmin [CityName] rename [NewName]");
					return false;
				}

				String newName=args[2];
				if (newName==null || newName.isEmpty()) {
					player.sendMessage(Messages.WrongCityName.get());
					return false;
				}
				
				City existentCity = GPPCities.getInstance().getDataStore().getCity(newName);
				if (existentCity!=null) {
					player.sendMessage(Messages.CitySameNameExists.get());
					return false;
				}
				
				GPPCities.getInstance().log("Admin "+player.getName()+" renamed "+city.getName()+" to "+newName);
				city.rename(newName);
				player.sendMessage(Messages.CityRenamed.get());
				
				return true;
			}
			
			if (args[1].equalsIgnoreCase("setspawn")) {
				if (!player.hasPermission("gppc.cityadmin.setspawn")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=2) {
					player.sendMessage("Usage: /cityadmin [CityName] setspawn");
					return false;
				}
				
				if (city.getClaim().contains(player.getLocation(), false, false)) {
					city.setSpawn(player.getLocation());
					player.sendMessage(Messages.CitySpawnSet.get());
					GPPCities.getInstance().log("City id "+city.getClaim().getID()+" spawn set by "+player.getName()+" to "+player.getLocation().toString());
				} else {
					player.sendMessage(Messages.CitySpawnInvalid.get());
				}
				return true;
			}
			
			if (args[1].equalsIgnoreCase("expel")) {
				if (!player.hasPermission("gppc.cityadmin.expel")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				if (args.length!=3) {
					player.sendMessage("Usage: /cityadmin [CityName] expel [CitizenName]");
					return false;
				}
				
				Citizen citizen = city.getCitizen(args[2]);
				if (citizen==null) {
					player.sendMessage(Messages.WrongCitizenName.get());
					return false;
				}
				if (citizen.getPlayer()!=null) {
					citizen.getPlayer().sendMessage(Messages.YouveBeenExpelled.get(city.getName()));
				}
				
				GPPCities.getInstance().log("Admin "+player.getName()+" expelled "+citizen.getName()+" from "+city.getName());
				city.removeCitizen(citizen.getId());
				player.sendMessage(Messages.CitizenExpelled.get());
				return true;
			}
			
			if (args[1].equalsIgnoreCase("ban")) {	
				if (!player.hasPermission("gppc.cityadmin.ban")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				if (args.length!=3) {
					player.sendMessage("Usage: /cityadmin [CityName] ban [PlayerName]");
					return false;
				}
				
				UUID playerId=GriefPreventionPlus.getInstance().resolvePlayerId(args[2]);
				if (playerId==null) {
					player.sendMessage(GriefPreventionPlus.getInstance().getDataStore().getMessage(net.kaikk.mc.gpp.Messages.PlayerNotFound2));
					return false;
				}
				
				city.addBan(playerId);
				player.sendMessage(Messages.PlayerBannedConfirm.get(player.getName(), city.getName()));
				GPPCities.getInstance().log("Admin "+player.getName()+" banned "+args[2]+" from "+city.getName());
				return true;
			}
			
			if (args[1].equalsIgnoreCase("unban")) {
				if (!player.hasPermission("gppc.cityadmin.ban")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				if (args.length!=3) {
					player.sendMessage("Usage: /cityadmin [CityName] unban [PlayerName]");
					return false;
				}
				
				UUID playerId=GriefPreventionPlus.getInstance().resolvePlayerId(args[2]);
				if (playerId==null) {
					player.sendMessage(GriefPreventionPlus.getInstance().getDataStore().getMessage(net.kaikk.mc.gpp.Messages.PlayerNotFound2));
					return false;
				}
				
				city.unban(playerId);
				player.sendMessage(Messages.PlayerUnbannedConfirm.get(player.getName(), city.getName()));
				GPPCities.getInstance().log("Admin "+player.getName()+" unbanned "+args[2]+" from "+city.getName());
				return true;
			}
			
			if (args[0].equalsIgnoreCase("reload")) {
				if (!player.hasPermission("gppc.cityadmin.reload")) {
					player.sendMessage(Messages.NoPermission.get());
					return false;
				}
				
				player.sendMessage("Reloading GPPCities... prepare for unforeseen consequences!");
				GPPCities instance = GPPCities.getInstance();
				instance.onDisable();
				instance.onEnable();
				player.sendMessage("GPPCities reloaded!");
				return true;
			}
		}

		player.sendMessage(Messages.WrongCommand.get());
		return false;
	}
}

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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Messages {
	CantRunCommandYouHaveCity, CitiesList, CitiesListFormat, CitiesNotAvailableOnAdminClaims, CitizenExpelled, CityAutojoinNotPermitted,
	CityAutojoinOff, CityAutojoinOn, CityHasBeenDisbanded, CityInvitationAcceptOrReject, CityInvitationAccepted, CityInvitationExpiresOn,
	CityInvitationHasBeenAccepted, CityInvitationHasBeenRejected, CityInvitationNoPending, CityInvitationReceived, CityInvitationRejected,
	CityInvitationSent, CityNotExists, CitySpawnInvalid, CitySpawnMissing, CitySpawnSet, ClaimAlreadyACity, InviteNotAllowed, JoinableList,
	MotdHasBeenSet, MotdOut, MotdRes, MotdSet, NewCity, NoCities, NoPermission, NotAuthorized, PlayerAlreadyOnAnotherCity, PlayerIsNotACitizen,
	PlayerJoinedYourCity, PlayerLeftCity, PlayerOfflineOrWrongName, PlotAssigned, PlotTakeNotAllowed, PlotTakeYouHaveAPlotAlready,
	PlotCreated, PlotDeleted, PlotMotdEdited, PlotUnassigned, StandOnPlotOrSubClaim, ThisCityNotExists, WelcomeBackTo, WelcomeTo,
	WrongCitizenName, WrongCityName, WrongCityNameCS, WrongCommand, YouAlreadyOnAnotherCity, YouCantRunCommandOnACity, YouLeftCity,
	YouNotACitizenHere, YouNotCitizen, YouNotInACity, YouNotOnAClaim, YouOnClaim, YouOnPlot, YouOnWilderness, YouveBeenExpelled,
	
	CitizensList, CitizensListFormat, CitizenInfo, ClaimTooSmall, YouGotAPlot, YouLostAPlot, CitizenPermissions, CityInfo, YouJoinedCity,
	YouAreBanned, PlayerIsBanned, NewMayor, CityChatFormat, CityNameInvalid, CitySameNameExists, CityRenamed, CityChatOn, CityChatOff,
	TakeableOn, TakeableOff, NoJoinableCities, PlayerBannedConfirm, PlayerUnbannedConfirm, PlotInfo, MayorCannotLeave;

	static private HashMap<Messages, String> messages = new HashMap<Messages, String>();
	final static String messagesFilePath = "plugins" + File.separator + "GriefPreventionPlus-Cities" + File.separator + "messages.yml";
	
	static void defaults() {
		messages.put(Messages.CantRunCommandYouHaveCity, "&cYou can't run this command: claim ID &b{0} &cis a city named &b{3}&c!");
		messages.put(Messages.CitiesList, "&aCities list:");
		messages.put(Messages.CitiesListFormat, "{0} [&e{1}&f]");
		messages.put(Messages.CitiesNotAvailableOnAdminClaims, "&cCities are not available on admin claims.");
		messages.put(Messages.CitizenExpelled, "&aCitizen expelled.");
		messages.put(Messages.CityAutojoinNotPermitted, "&c{0} does not permit auto-join, ask for an invite.");
		messages.put(Messages.CityAutojoinOff, "&aNow players need an invitation to join this city.");
		messages.put(Messages.CityAutojoinOn, "&aNow players can join this city without an invitation.");
		messages.put(Messages.CityHasBeenDisbanded, "&c{0} has been disbanded!");
		messages.put(Messages.CityInvitationAcceptOrReject, "&aType /c accept to accept; otherwise /c reject.");
		messages.put(Messages.CityInvitationAccepted, "&aYou accepted the invitation.");
		messages.put(Messages.CityInvitationExpiresOn, "&aThis player already recieved an invitation, it expires in: {0} seconds.");
		messages.put(Messages.CityInvitationHasBeenAccepted, "&aYour invitation to &b{0} &ahas been &baccepted&a.");
		messages.put(Messages.CityInvitationHasBeenRejected, "&aYour invitation to &b{0} &ahas been &crejected&a.");
		messages.put(Messages.CityInvitationNoPending, "&cYou do not have a pending invitation.");
		messages.put(Messages.CityInvitationReceived, "&aYou received a city invitation from &b{0} &ato join &b{1}&a.");
		messages.put(Messages.CityInvitationRejected, "&aYou rejected the invitation.");
		messages.put(Messages.CityInvitationSent, "&aCity invitation sent to &b{0}.");
		messages.put(Messages.CityNotExists, "&cThe city &b{0} &cdoes not exist.");
		messages.put(Messages.CitySpawnInvalid, "&cCity spawn must be located inside your city.");
		messages.put(Messages.CitySpawnMissing, "&cYour city's spawn is missing.");
		messages.put(Messages.CitySpawnSet, "&aCity spawn set.");
		messages.put(Messages.ClaimAlreadyACity, "&cThis claim is already a city.");
		messages.put(Messages.InviteNotAllowed, "&cYou're not allowed to invite people.");
		messages.put(Messages.JoinableList, "&aJoinable cities list:");
		messages.put(Messages.MotdHasBeenSet, "&aA new motd has been set:");
		messages.put(Messages.MotdOut, "&aOuters motd: &b{0}");
		messages.put(Messages.MotdRes, "&aResidents motd: &b{0}");
		messages.put(Messages.MotdSet, "&aMotd set.");
		messages.put(Messages.NewCity, "&a{0} has just founded a new city called &b{1}&a.");
		messages.put(Messages.NoCities, "&aThere's no city! Be the first! Make a claim, and type /city new CityName.");
		messages.put(Messages.NoJoinableCities, "&aThere's no joinable city! Ask for an invitation, or make a new city!");
		messages.put(Messages.NoPermission, "&cYou don't have permission to use this command.");
		messages.put(Messages.NotAuthorized, "&cYou're not authorized.");
		messages.put(Messages.PlayerAlreadyOnAnotherCity, "&cThis player is already a member of another city.");
		messages.put(Messages.PlayerIsNotACitizen, "&cThis player is not a citizen.");
		messages.put(Messages.PlayerJoinedYourCity, "&b{0} &ajoined your city!");
		messages.put(Messages.PlayerLeftCity, "&b{0} &aleft the city.");
		messages.put(Messages.PlayerOfflineOrWrongName, "&cThis player is offline or the player name is wrong.");
		messages.put(Messages.PlotAssigned, "&aPlot assigned.");
		messages.put(Messages.PlotTakeNotAllowed, "&cYou can take this plot.");
		messages.put(Messages.PlotTakeYouHaveAPlotAlready, "&cYou can't take another plot if you have an assigned plot already.");
		messages.put(Messages.PlotCreated, "&aNew plot created.");
		messages.put(Messages.PlotDeleted, "&aPlot deleted.");
		messages.put(Messages.PlotMotdEdited, "&aPlot motd edited.");
		messages.put(Messages.PlotUnassigned, "&aPlot unassigned.");
		messages.put(Messages.StandOnPlotOrSubClaim, "&cYou have to stand on a subdivision. Use /subdivideclaims.");
		messages.put(Messages.ThisCityNotExists, "&cThis city doesn't exist.");
		messages.put(Messages.WelcomeBackTo, "&2Welcome back to &a{0}.");
		messages.put(Messages.WelcomeTo, "&2Welcome to &a{0}.");
		messages.put(Messages.WrongCitizenName, "&cWrong citizen name or this citizen is not part of your city.");
		messages.put(Messages.WrongCityName, "&cWrong city name.");
		messages.put(Messages.WrongCityNameCS, "&cWrong city name (it's case-sensitive).");
		messages.put(Messages.WrongCommand, "&cWrong command.");
		messages.put(Messages.YouAlreadyOnAnotherCity, "&cYou're a member of another city.");
		messages.put(Messages.YouCantRunCommandOnACity, "&cYou can't run this command on a city.");
		messages.put(Messages.YouLeftCity, "&aYou left &b{0}.");
		messages.put(Messages.YouNotACitizenHere, "&cYou're not a citizen here.");
		messages.put(Messages.YouNotCitizen, "&cYou're not a citizen.");
		messages.put(Messages.YouNotInACity, "&cYou're not in a city.");
		messages.put(Messages.YouNotOnAClaim, "&cYou're not on a claim.");
		messages.put(Messages.YouOnClaim, "&aYou're on &b{0}&a's claim.");
		messages.put(Messages.YouOnPlot, "&aYou entered &b{0}&a's plot.");
		messages.put(Messages.YouOnWilderness, "&aYou entered the wilderness.");
		messages.put(Messages.YouveBeenExpelled, "&eYou've been expelled from &b{0}.");
		
		messages.put(Messages.CitizensList, "&aCitizen's last played list for {0}.");
		messages.put(Messages.CitizensListFormat, "&a{0} - {1} day(s) ago");
		messages.put(Messages.CitizenInfo, "&b=== {0} ===\n&aResident on {1} from {2}\n&aLast played on {3}.");
		messages.put(Messages.ClaimTooSmall, "&cThis claim is too small. Minimum size required is {0} blocks.");
		messages.put(Messages.YouGotAPlot, "&a{0} assigned you a plot at {1}.");
		messages.put(Messages.YouLostAPlot, "&a{0} unassigned your plot at {1}.");
		messages.put(Messages.CitizenPermissions, "&a{0}'s permissions: {1}.");
		messages.put(Messages.CityInfo, "&b===== {0} ===== ID: {1}\n"+
										"&aArea size: {2} m² | Plots: {3}\n"+
										"&aCreated on: {4} | Mayor: {5}\n"+
										"&aCitizens [{6}]:\n"+
										"&a- Online: {7}\n"+
										"&a- Offline: {8}");
		messages.put(Messages.YouJoinedCity, "&aYou joined {0}.");
		messages.put(Messages.YouAreBanned, "&cYou are banned from {0}!");
		messages.put(Messages.PlayerIsBanned, "&cThis player was banned from {0}!");
		messages.put(Messages.NewMayor, "&a{0} is {1}'s new mayor!");
		messages.put(Messages.CityChatFormat, "&e[&f{0}&e] &f<{1}> {2}");
		messages.put(Messages.CityNameInvalid, "&cCity name invalid.");
		messages.put(Messages.ClaimAlreadyACity, "&cThis claim is already a city.");
		messages.put(Messages.CitySameNameExists, "&cA city with the same name already exists.");
		messages.put(Messages.CityRenamed, "&aCity renamed.");
		messages.put(Messages.CityChatOn, "&aFrom now your messages are sent to the city chat.");
		messages.put(Messages.CityChatOff, "&aFrom now your messages are sent to the public chat.");
		messages.put(Messages.TakeableOn, "&aNew citizen can take this plot on their own.");
		messages.put(Messages.TakeableOff, "&aNew citizen can't take this plot on their own.");
		messages.put(Messages.PlayerBannedConfirm, "&a{0} was banned from {1} and can't autojoin or be invited.");
		messages.put(Messages.PlayerUnbannedConfirm, "&a{0} was unbanned from {1}.");
		messages.put(Messages.PlotInfo, "&bPlot ID: {1}\n"+
				"&aAssigned on: {2} | Assigned to: {3}\n"+
				"&aTakeable: {4}");
		messages.put(Messages.MayorCannotLeave, "&aYou can't leave your city. Do /c mayor [CitizenName] to change the mayor.");
	}
	
	static void load() {
		if (messages.isEmpty()) {
			defaults();
		}
		
		FileConfiguration messagesFile = YamlConfiguration.loadConfiguration(new File(Messages.messagesFilePath));
		
		for (Messages message : Messages.values()) {
			String sendMessage = messages.get(message);
			
			if (sendMessage==null) {
				GPPCities.gppc.log(Level.WARNING, "Missing message ID "+message.toString());
			}
			
			String mess=messagesFile.getString(message.toString());
			if (mess==null) {
				messagesFile.set(message.toString(), sendMessage);
				messages.put(message, ChatColor.translateAlternateColorCodes('&', sendMessage));
			} else {
				messages.put(message, ChatColor.translateAlternateColorCodes('&', mess));
			}
		}
		try {
			messagesFile.save(Messages.messagesFilePath);
		} catch(IOException exception) {
			GPPCities.gppc.log("Unable to write messages file at "+Messages.messagesFilePath);
		}
	}
	
	public String get(String... strs) {
		String message = messages.get(this);
		if (message==null) {
			GPPCities.gppc.log(Level.WARNING, "Missing message ID "+this.toString());
			return "Missing message! Please contact a server admin.";
		}
		
		for (int i=0; i<strs.length; i++) {
			message = message.replace("{"+i+"}", strs[i]);
		}

		return message;
	}
}

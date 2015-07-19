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
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public enum Messages {
	CantRunCommandYouHaveCity, CitiesList, CitiesListFormat, CitiesNotAvailableOnAdminClaims, CitizenExpelled, CityAutojoinNotPermitted,
	CityAutojoinOff, CityAutojoinOn, CityHasBeenDisbanded, CityInvitationAcceptOrReject, CityInvitationAccepted, CityInvitationExpiresOn,
	CityInvitationHasBeenAccepted, CityInvitationHasBeenRejected, CityInvitationNoPending, CityInvitationReceived, CityInvitationRejected,
	CityInvitationSent, CityNotExists, CitySpawnInvalid, CitySpawnMissing2, CitySpawnSet, ClaimAlreadyACity, InviteNotAllowed,
	MotdHasBeenSet, MotdOut, MotdRes, MotdSet, NewCity, NoCities, NoPermission, NotAuthorized, PlayerAlreadyOnAnotherCity, PlayerIsNotACitizen,
	PlayerJoinedYourCity, PlayerLeftCity, PlayerOfflineOrWrongName, PlotAssigned, PlotTakeNotAllowed, PlotTakeYouHaveAPlotAlready,
	PlotCreated, PlotDeleted, PlotMotdEdited, PlotUnassigned, StandOnPlotOrSubClaim, ThisCityNotExists, WelcomeBackTo, WelcomeTo,
	WrongCitizenName, WrongCityName, WrongCityNameCS, WrongCommand, YouAlreadyOnAnotherCity, YouCantRunCommandOnACity, YouLeftCity,
	YouNotACitizenHere, YouNotCitizen, YouNotInACity, YouNotOnAClaim, YouOnClaim, YouOnPlot, YouOnWilderness, YouveBeenExpelled,
	
	CitizensList, CitizensListFormat, CitizenInfo, ClaimTooSmall, YouGotAPlot, YouLostAPlot, CitizenPermissions, CityInfo, YouJoinedCity,
	YouAreBanned, PlayerIsBanned, NewMayor, CityChatFormat, CityNameInvalid, CitySameNameExists, CityRenamed, CityChatOn, CityChatOff,
	TakeableOn, TakeableOff, PlayerBannedConfirm, PlayerUnbannedConfirm, PlotInfo, MayorCannotLeave, YouOnUnassignedPlot,
	YouOnTakeablePlot, YouOnJoinableCity, CitySpawnTeleportDelay, CitySpawnTeleportCancelled, CitySpawnTeleported;

	final static String messagesFilePath = "plugins" + File.separator + "GriefPreventionPlus-Cities" + File.separator + "messages.yml";
	private String text;

	static void defaults() {
		Messages.CantRunCommandYouHaveCity.text = "&cYou can't run this command: claim ID &b{0} &cis a city named &b{1}&c!";
		Messages.CitiesList.text = "&aCities list:";
		Messages.CitiesListFormat.text = "{0} [&e{1}&f]";
		Messages.CitiesNotAvailableOnAdminClaims.text = "&cCities are not available on admin claims.";
		Messages.CitizenExpelled.text = "&aCitizen expelled.";
		Messages.CityAutojoinNotPermitted.text = "&c{0} does not permit auto-join, ask for an invite.";
		Messages.CityAutojoinOff.text = "&aNow players need an invitation to join this city.";
		Messages.CityAutojoinOn.text = "&aNow players can join this city without an invitation.";
		Messages.CityHasBeenDisbanded.text = "&c{0} has been disbanded!";
		Messages.CityInvitationAcceptOrReject.text = "&aType /c accept to accept; otherwise /c reject.";
		Messages.CityInvitationAccepted.text = "&aYou accepted the invitation.";
		Messages.CityInvitationExpiresOn.text = "&aThis player already recieved an invitation, it expires in: {0} seconds.";
		Messages.CityInvitationHasBeenAccepted.text = "&aYour invitation to &b{0} &ahas been &baccepted&a.";
		Messages.CityInvitationHasBeenRejected.text = "&aYour invitation to &b{0} &ahas been &crejected&a.";
		Messages.CityInvitationNoPending.text = "&cYou do not have a pending invitation.";
		Messages.CityInvitationReceived.text = "&aYou received a city invitation from &b{0} &ato join &b{1}&a.";
		Messages.CityInvitationRejected.text = "&aYou rejected the invitation.";
		Messages.CityInvitationSent.text = "&aCity invitation sent to &b{0}.";
		Messages.CityNotExists.text = "&cThe city &b{0} &cdoes not exist.";
		Messages.CitySpawnInvalid.text = "&cCity spawn must be located inside your city.";
		Messages.CitySpawnMissing2.text = "&c{0}'s spawn is missing.";
		Messages.CitySpawnSet.text = "&aCity spawn set.";
		Messages.ClaimAlreadyACity.text = "&cThis claim is already a city.";
		Messages.InviteNotAllowed.text = "&cYou're not allowed to invite people.";
		Messages.MotdHasBeenSet.text = "&aA new motd has been set:";
		Messages.MotdOut.text = "&aOuters motd: &b{0}";
		Messages.MotdRes.text = "&aResidents motd: &b{0}";
		Messages.MotdSet.text = "&aMotd set.";
		Messages.NewCity.text = "&a{0} has just founded a new city called &b{1}&a.";
		Messages.NoCities.text = "&aThere's no city! Be the first! Make a claim, and type /city new CityName.";
		Messages.NoPermission.text = "&cYou don't have permission to use this command.";
		Messages.NotAuthorized.text = "&cYou're not authorized.";
		Messages.PlayerAlreadyOnAnotherCity.text = "&cThis player is already a member of another city.";
		Messages.PlayerIsNotACitizen.text = "&cThis player is not a citizen.";
		Messages.PlayerJoinedYourCity.text = "&b{0} &ajoined your city!";
		Messages.PlayerLeftCity.text = "&b{0} &aleft the city.";
		Messages.PlayerOfflineOrWrongName.text = "&cThis player is offline or the player name is wrong.";
		Messages.PlotAssigned.text = "&aPlot assigned.";
		Messages.PlotTakeNotAllowed.text = "&cYou can take this plot.";
		Messages.PlotTakeYouHaveAPlotAlready.text = "&cYou can't take another plot if you have an assigned plot already.";
		Messages.PlotCreated.text = "&aNew plot created.";
		Messages.PlotDeleted.text = "&aPlot deleted.";
		Messages.PlotMotdEdited.text = "&aPlot motd edited.";
		Messages.PlotUnassigned.text = "&aPlot unassigned.";
		Messages.StandOnPlotOrSubClaim.text = "&cYou have to stand on a subdivision. Use /subdivideclaims.";
		Messages.ThisCityNotExists.text = "&cThis city doesn't exist.";
		Messages.WelcomeBackTo.text = "&2Welcome back to &a{0}.";
		Messages.WelcomeTo.text = "&2Welcome to &a{0}.";
		Messages.WrongCitizenName.text = "&cWrong citizen name or this citizen is not part of your city.";
		Messages.WrongCityName.text = "&cWrong city name.";
		Messages.WrongCityNameCS.text = "&cWrong city name (it's case-sensitive).";
		Messages.WrongCommand.text = "&cWrong command.";
		Messages.YouAlreadyOnAnotherCity.text = "&cYou're a member of another city.";
		Messages.YouCantRunCommandOnACity.text = "&cYou can't run this command on a city.";
		Messages.YouLeftCity.text = "&aYou left &b{0}.";
		Messages.YouNotACitizenHere.text = "&cYou're not a citizen here.";
		Messages.YouNotCitizen.text = "&cYou're not a citizen.";
		Messages.YouNotInACity.text = "&cYou're not in a city.";
		Messages.YouNotOnAClaim.text = "&cYou're not on a claim.";
		Messages.YouOnClaim.text = "&aYou're on &b{0}&a's claim.";
		Messages.YouOnPlot.text = "&aYou entered &b{0}&a's plot.";
		Messages.YouOnWilderness.text = "&aYou entered the wilderness.";
		Messages.YouveBeenExpelled.text = "&eYou've been expelled from &b{0}.";
		
		Messages.CitizensList.text = "&aCitizen's last played list for {0}.";
		Messages.CitizensListFormat.text = "&a{0} - {1} day(s) ago";
		Messages.CitizenInfo.text = "&b=== {0} ===\n&aResident on {1} from {2}\n&aLast played on {3}.";
		Messages.ClaimTooSmall.text = "&cThis claim is too small. Minimum size required is {0} blocks.";
		Messages.YouGotAPlot.text = "&a{0} assigned you a plot at {1}.";
		Messages.YouLostAPlot.text = "&a{0} unassigned your plot at {1}.";
		Messages.CitizenPermissions.text = "&a{0}'s permissions: {1}.";
		Messages.CityInfo.text = "&b===== {0} ===== ID: {1}\n"+
										"&aArea size: {2} m² | Plots: {3}\n"+
										"&aCreated on: {4} | Mayor: {5}\n"+
										"&aCitizens [{6}]:\n"+
										"&a- Online: {7}\n"+
										"&a- Offline: {8}";
		Messages.YouJoinedCity.text = "&aYou joined {0}.";
		Messages.YouAreBanned.text = "&cYou are banned from {0}!";
		Messages.PlayerIsBanned.text = "&cThis player was banned from {0}!";
		Messages.NewMayor.text = "&a{0} is {1}'s new mayor!";
		Messages.CityChatFormat.text = "&e[&f{0}&e] &f<{1}> {2}";
		Messages.CityNameInvalid.text = "&cCity name invalid.";
		Messages.ClaimAlreadyACity.text = "&cThis claim is already a city.";
		Messages.CitySameNameExists.text = "&cA city with the same name already exists.";
		Messages.CityRenamed.text = "&aCity renamed.";
		Messages.CityChatOn.text = "&aFrom now your messages are sent to the city chat.";
		Messages.CityChatOff.text = "&aFrom now your messages are sent to the public chat.";
		Messages.TakeableOn.text = "&aNew citizen can take this plot on their own.";
		Messages.TakeableOff.text = "&aNew citizen can't take this plot on their own.";
		Messages.PlayerBannedConfirm.text = "&a{0} was banned from {1} and can't autojoin or be invited.";
		Messages.PlayerUnbannedConfirm.text = "&a{0} was unbanned from {1}.";
		Messages.PlotInfo.text = "&bPlot ID: {0}\n"+
				"&aAssigned on: {1} | Assigned to: {2}\n"+
				"&aTakeable: {3}";
		Messages.MayorCannotLeave.text = "&aYou can't leave your city. Do /c mayor [CitizenName] to change the mayor.";
		Messages.YouOnUnassignedPlot.text = "&bYou're on an unassigned plot.";
		Messages.YouOnTakeablePlot.text = "&bYou can take this plot with /c plot take.";
		Messages.YouOnJoinableCity.text = "&bYou can join this city with /c join {0}.";
		Messages.CitySpawnTeleportDelay.text = "&bYou'll be teleported to {0}'s spawn in 5 seconds.";
		Messages.CitySpawnTeleportCancelled.text = "&bTeleport cancelled.";
		Messages.CitySpawnTeleported.text = "&bTeleported to {0}'s spawn.";
	}
	
	static void load() {
		defaults();
		
		FileConfiguration messagesFile = YamlConfiguration.loadConfiguration(new File(Messages.messagesFilePath));
		
		for (Messages message : Messages.values()) {
			String sendMessage = message.text;
			
			if (sendMessage==null) {
				GPPCities.getInstance().log(Level.WARNING, "Missing text ID "+message.toString());
			}
			
			String mess=messagesFile.getString(message.toString());
			if (mess==null) {
				messagesFile.set(message.toString(), sendMessage);
				message.text = ChatColor.translateAlternateColorCodes('&', sendMessage);
			} else {
				message.text = ChatColor.translateAlternateColorCodes('&', mess);
			}
		}
		try {
			messagesFile.save(Messages.messagesFilePath);
		} catch(IOException exception) {
			GPPCities.getInstance().log("Unable to write messages file at "+Messages.messagesFilePath);
		}
	}
	
	public String get(String... strs) {
		String message = this.text;
		if (message==null) {
			GPPCities.getInstance().log(Level.WARNING, "Missing message ID "+this.toString());
			return "Missing message ["+this.toString()+"]! Please contact a server admin.";
		}
		
		for (int i=0; i<strs.length; i++) {
			message = message.replace("{"+i+"}", strs[i]);
		}

		return message;
	}
}

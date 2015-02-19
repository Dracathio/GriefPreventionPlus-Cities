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

import java.util.ArrayList;
import java.util.Collections;

import org.bukkit.scheduler.BukkitRunnable;

public class InactivityCheckTask  extends BukkitRunnable {
    private final GPPCities gpp;
    
    public InactivityCheckTask(GPPCities gpp) {
        this.gpp = gpp;
    }
    
	@Override
	public void run() {
		gpp.log("Inactivity check task start!");
		long time=System.currentTimeMillis();
		int removedCitizens=0;
		int removedMayors=0;
		int removedCities=0;
		for (City city : gpp.ds.citiesMap.values()) {
			ArrayList<Citizen> citizensLastPlayed = new ArrayList<Citizen>(city.citizens.values());
			Collections.sort(citizensLastPlayed, Collections.reverseOrder(new CitizenLastPlayedComparator()));
			
			ArrayList<Citizen> citizensJoinedOn = new ArrayList<Citizen>(city.citizens.values());
			Collections.sort(citizensJoinedOn, new CitizenJoinedOnComparator());
			
			for (Citizen citizen : citizensLastPlayed) {
				if (citizen.getLastPlayedDays()<=gpp.config.InactivityDays) {
					break;
				}
				boolean found=true;
				if (citizen.checkPerm(CitizenPermission.Mayor)) {
					gpp.log("Removing mayor "+citizen.getName()+" from "+city.name);
					found=false;
					for (Citizen newMayor : citizensJoinedOn) {
						if (newMayor.getLastPlayedDays()<=gpp.config.InactivityDays) {
							if (newMayor.checkPerm(CitizenPermission.Assistant)) {
								found=true;
								gpp.log("Assigning "+city.name+" mayor to "+newMayor.getName());
								city.changeOwner(newMayor.id);
								removedMayors++;
								break;
							}
						}
					}

					if (!found) {
						for (Citizen newMayor : citizensJoinedOn) {
							found=true;
							gpp.log("Assigning "+city.name+" mayor to "+newMayor.getName());
							city.changeOwner(newMayor.id);
							removedMayors++;
							break;
						}
					}
					
					if (!found) {
						gpp.log("Removing city named "+city.name);
						gpp.ds.deleteCity(city.claim.getID());
						removedCities++;
						break;
					}
				} else {
					gpp.log("Removing citizen "+citizen.getName()+" from "+city.name);
				}
				
				if (found) {
					city.removeCitizen(citizen.id);
				}
				removedCitizens++;
			}
		}
		
		gpp.log("Inactivity check done in "+((System.currentTimeMillis()-time)/1000.00)+" seconds");
		if (removedCitizens!=0||removedMayors!=0||removedCities!=0) {
			gpp.log("Removed "+removedCitizens+" citizens, "+removedMayors+" mayors, "+removedCities+" cities.");
		}
	}
}

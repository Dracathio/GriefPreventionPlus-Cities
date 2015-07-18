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

import java.util.ConcurrentModificationException;
import java.util.Iterator;

import net.kaikk.mc.gppcities.City.Citizen;

import org.bukkit.scheduler.BukkitRunnable;

class InactivityCheckTask extends BukkitRunnable {
	private final GPPCities instance;
	private Iterator<City> iterator;
	private int removedCitizens, removedCities, removedMayors;

	InactivityCheckTask(GPPCities instance) {
		this.instance = instance;
	}

	@Override
	public void run() {
		if (this.iterator==null) {
			this.init();
		}

		try {
			if (this.iterator.hasNext()) {
				City city=this.iterator.next();
				if (city.getCitizens().size()==0) {// empty city...
					this.instance.log("Removing empty city: "+city.getName());
					this.instance.getDataStore().deleteCity(city);
					this.removedCities++;
					return;
				}
				
				Citizen expiredMayor=null;
				
				for (Citizen citizen : city.getCitizens().values()) {
					if(citizen.getLastPlayedDays()>this.instance.config.InactivityDays) {
						// citizen expired
						if (citizen.checkPerm(CitizenPermission.Mayor)) {
							// this citizen is the mayor, let's ignore him atm
							expiredMayor=citizen;
						} else {
							instance.log("Removing citizen "+citizen.getName()+" from "+city.getName());
							city.removeCitizen(citizen.getId());
							this.removedCitizens++;
						}
					}
				}
				
				if (expiredMayor!=null) {
					// the mayor is gone... need to replace him with someone else...
					if (city.getCitizens().size()==1) {
						// the mayor is alone...
						this.instance.log("Removing expired city: "+city.getName());
						this.instance.getDataStore().deleteCity(city);
						this.removedCities++;
					} else {
						// change the city owner
						Citizen newMayor=null;
						newMayor=city.getOldestAssistant();
						if (newMayor==null) {
							newMayor=city.getOldestCitizen();
						}
						this.instance.log("Removing old mayor "+expiredMayor.getName()+" from "+city.getName()+". New mayor is "+newMayor.getName());
						city.changeOwner(newMayor.getId());
						city.removeCitizen(expiredMayor.getId());
						this.removedMayors++;
					}
				}
				
			} else {
				// summary
				this.instance.log("Inactivity Check Task Done! "
						+ ((this.removedCitizens!=0||this.removedMayors!=0||this.removedCities!=0) ? "Removed "+this.removedCitizens+" citizens, "+this.removedMayors+" mayors, "+this.removedCities+" cities." : ""));
				
				// reschedule
				this.cancel();
				new InactivityCheckTask(this.instance).runTaskTimer(this.instance, (this.instance.config.InactivityCheckMinutes*1200), 4);
			}
		} catch (ConcurrentModificationException e) {
			this.instance.log("Inactivity Check Task detected a modification and needs a restart...");
			this.cancel();
			new InactivityCheckTask(this.instance).runTaskTimer(this.instance, 100, 4);
		}
	}

	void init() { // init the iterator
		this.instance.log("Inactivity Check Task start!");
		this.iterator=this.instance.getDataStore().citiesMap.values().iterator();
	}
}

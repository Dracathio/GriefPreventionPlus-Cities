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

import org.bukkit.scheduler.BukkitRunnable;

class InactivityCheckTask extends BukkitRunnable {
	private final GPPCities instance;
	private Iterator<City> iterator;
	private int removedCities;

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
				
				if (city.handleInactiveCitizens()) {
					this.instance.log("Removing city: "+city.getName());
					this.instance.getDataStore().deleteCity(city);
					this.removedCities++;
					return;
				}
			} else {
				// summary
				this.instance.log("Inactivity Check Task Done! " + (this.removedCities!=0 ? "Removed "+this.removedCities+" cities." : ""));
				
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

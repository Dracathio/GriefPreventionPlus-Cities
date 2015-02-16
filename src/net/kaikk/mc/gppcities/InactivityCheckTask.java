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
				
				if (citizen.checkPerm(CitizenPermission.Mayor)) {
					boolean found=false;
					for (Citizen newMayor : citizensJoinedOn) {
						if (newMayor.getLastPlayedDays()<=gpp.config.InactivityDays) {
							if (newMayor.checkPerm(CitizenPermission.Assistant)) {
								found=true;
								city.changeOwner(newMayor.id);
								removedMayors++;
								break;
							}
						}
					}

					if (!found) {
						for (Citizen newMayor : citizensJoinedOn) {
							found=true;
							city.changeOwner(newMayor.id);
							removedMayors++;
							break;
						}
					}
					
					if (!found) {
						gpp.ds.deleteCity(city.claim.getID());
						removedCities++;
						break;
					}
				}
				
				city.removeCitizen(citizen.id);
				removedCitizens++;
			}
		}
		
		gpp.log("Inactivity check done in "+((System.currentTimeMillis()-time)/1000.00)+" seconds");
		gpp.log("Removed "+removedCitizens+" citizens, "+removedMayors+" mayors, "+removedCities+" cities.");
	}
}

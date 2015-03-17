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

public enum CitizenPermission {
	Mayor(1),
	Assistant(2),
	Invite(4),
	Expel(8),
	Motd(16),
	Plot(32),
	Spawn(64);

	public final int perm;
	
	CitizenPermission(int perm) {
		this.perm=perm;
	}
}

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

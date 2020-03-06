package net.TheDgtl.Stargate;

import org.bukkit.Axis;
import org.bukkit.Material;

public class BloxPopulator {
	private Blox blox;
	private Material nextMat;
	private Axis nextAxis;
	
	public BloxPopulator(Blox b, Material m) {
		blox = b;
		nextMat = m;
		nextAxis = null;
	}
	
	public BloxPopulator(Blox b, Material m, Axis a) {
		blox = b;
		nextMat = m;
		nextAxis = a;
	}
	
	public void setBlox(Blox b) {
		blox = b;
	}
	
	public void setMat(Material m) {
		nextMat = m;
	}
	
	public void setAxis(Axis a) {
		nextAxis = a;
	}
	
	public Blox getBlox() {
		return blox;
	}
	
	public Material getMat() {
		return nextMat;
	}
	
	public Axis getAxis() {
		return nextAxis;
	}
	
}

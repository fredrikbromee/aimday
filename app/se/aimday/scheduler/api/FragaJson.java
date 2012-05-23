package se.aimday.scheduler.api;

public class FragaJson {
	public FragaJson(String id, String fråga) {
		this.id = id;
		this.fråga = fråga;
	}
	
	public FragaJson() {

	}
	public String id;
	public String fråga;
	public int vikt = 0;
}

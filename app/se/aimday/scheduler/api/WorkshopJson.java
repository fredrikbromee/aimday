package se.aimday.scheduler.api;

import java.util.ArrayList;
import java.util.List;

public class WorkshopJson {
	public WorkshopJson(String frågeId, String... forskarna) {
		this.frågeId = frågeId;
		forskare = new ArrayList<String>();
		for (String forskarId : forskarna) {
			forskare.add(forskarId);
		}
	}

	public String frågeId;
	public List<String> forskare;
	public List<String> företagsrepresentanter;
}

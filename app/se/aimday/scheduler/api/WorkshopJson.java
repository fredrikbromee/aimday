package se.aimday.scheduler.api;

import java.util.ArrayList;
import java.util.List;

public class WorkshopJson {
	public WorkshopJson() {

	}

	public WorkshopJson(String frågeId, String... forskarna) {
		this.frageId = frågeId;
		forskare = new ArrayList<String>();
		for (String forskarId : forskarna) {
			forskare.add(forskarId);
		}
	}

	public String frageId;
	public List<String> forskare;
	public List<String> foretagsrepresentanter;
	public double score;
}

package se.aimday.scheduler.api;

import java.io.Serializable;

public class ScheduleRequest implements Serializable {

	public int tracks;
	public int sessions;
	public int generations;
	public int placeWeight;
	public int wsWeight;
	public int agendaWeight;
	public int maxAntalDeltagare;
	public int minAntalDeltagare;

	public ScheduleRequest(int tracks, int sessions, int generations, int placeWeight, int wsWeight, int agendaWeight,
			int maxAntalDeltagare, int minAntalDeltagare) {
		this.tracks = tracks;
		this.sessions = sessions;
		this.generations = generations;
		this.placeWeight = placeWeight;
		this.wsWeight = wsWeight;
		this.agendaWeight = agendaWeight;
		this.maxAntalDeltagare = maxAntalDeltagare;
		this.minAntalDeltagare = minAntalDeltagare;
	}

}

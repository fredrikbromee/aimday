package controllers;

import java.io.Serializable;

public class ScheduleRequest implements Serializable {

	public final int tracks;
	public final int sessions;
	public final int generations;
	public final int placeWeight;
	public final int wsWeight;
	public final int agendaWeight;
	public final int maxAntalDeltagare;
	public final int minAntalDeltagare;

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

package se.aimday.scheduler;
import java.util.ArrayList;

import models.Participant;
import models.Question;

public class QuestionWithParticipants {

	private final Question fråga;
	private ArrayList<Participant> kandidater;

	public QuestionWithParticipants(Question fråga) {
		this.fråga = fråga;
		kandidater = new ArrayList<Participant>();
	}

	public int antalKandidater() {
		return kandidater.size();
	}

	public void läggTillDeltagare(Participant deltagare) {
		kandidater.add(deltagare);
	}

	public Question getFråga() {
		return fråga;
	}

	public boolean harKandidat() {
		return !kandidater.isEmpty();
	}

	public Participant taKandidat() {
		int randomIndex = (int) Math.floor(Math.random() * kandidater.size());
		return kandidater.remove(randomIndex);
	}

	public QuestionWithParticipants deepClone() {
		QuestionWithParticipants frågaMedDeltagare = new QuestionWithParticipants(fråga);
		frågaMedDeltagare.kandidater.addAll(kandidater);
		return frågaMedDeltagare;
	}

	@Override
	public String toString() {
		return "[" + fråga + ", kandidater=" + kandidater + "]";
	}

}

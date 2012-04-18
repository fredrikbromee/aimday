package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;

import models.ForetagsRepresentant;
import models.Participant;
import models.Question;

public class FrågaMedDeltagare {

	private final Question fråga;
	private ArrayList<Participant> kandidater;
	private ArrayList<ForetagsRepresentant> lyssnare;

	public FrågaMedDeltagare(Question fråga) {
		this.fråga = fråga;
		kandidater = new ArrayList<Participant>();
		lyssnare = new ArrayList<ForetagsRepresentant>();
	}

	public int antalKandidater() {
		return kandidater.size();
	}

	public void läggTillDeltagare(Participant deltagare) {
		kandidater.add(deltagare);
	}

	public void läggTillFöretagare(ForetagsRepresentant frågare) {
		lyssnare.add(frågare);
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

	public FrågaMedDeltagare deepClone() {
		FrågaMedDeltagare frågaMedDeltagare = new FrågaMedDeltagare(fråga);
		frågaMedDeltagare.kandidater.addAll(kandidater);
		frågaMedDeltagare.lyssnare.addAll(lyssnare);
		return frågaMedDeltagare;
	}

	@Override
	public String toString() {
		return "[" + fråga + ", kandidater=" + kandidater + "]";
	}

	public Collection<ForetagsRepresentant> getFrågare() {
		return lyssnare;
	}
}

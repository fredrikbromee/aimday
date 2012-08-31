package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Question;

public class FragaMedDeltagare {

	private final Question fråga;
	private ArrayList<Forskare> kandidater;
	private ArrayList<ForetagsRepresentant> lyssnare;

	public FragaMedDeltagare(Question fråga) {
		this.fråga = fråga;
		kandidater = new ArrayList<Forskare>();
		lyssnare = new ArrayList<ForetagsRepresentant>();
	}

	public int antalKandidater() {
		return kandidater.size();
	}

	public void läggTillDeltagare(Forskare deltagare) {
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

	public Forskare taKandidat() {
		int randomIndex = (int) Math.floor(Math.random() * kandidater.size());
		return kandidater.remove(randomIndex);
	}

	public FragaMedDeltagare deepClone() {
		FragaMedDeltagare frågaMedDeltagare = new FragaMedDeltagare(fråga);
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

	public List<Forskare> getKandidater() {
		return new ArrayList<Forskare>(kandidater);
	}
}

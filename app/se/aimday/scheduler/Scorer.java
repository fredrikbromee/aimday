package se.aimday.scheduler;

import java.util.List;

import models.Question;

/**
 * Scores an individual agenda
 * 
 * @author fredrikbromee
 * 
 */
public class Scorer {

	private final IndividualAgenda individualAgenda;

	public Scorer(IndividualAgenda individualAgenda) {
		this.individualAgenda = individualAgenda;
	}

	public double score(List<Question> önskelista, int numSessions) {
		int antalÖnskadeFrågor = önskelista.size();
		if (individualAgenda.antalMöten() == antalÖnskadeFrågor) {
			return 1;
		}

		double score = 0;
		// för varje fråga i prio-ordning
		int prioOrdning = 1;
		for (Question q : önskelista) {
			if (individualAgenda.isPlacedIn(q)) {
				score += getDelPoängFörFråga(prioOrdning, numSessions, antalÖnskadeFrågor);
			}
			prioOrdning++;
		}
		return score;
	}

	double getDelPoängFörFråga(int prioOrdning, int totaltAntalSessioner, int antalÖnskadeFrågor) {
		switch (totaltAntalSessioner) {
		case 1:
			switch (prioOrdning) {
			case 1:
				return 1;
			case 2:
				return 0.8;
			default:
				return 0.6;
			}
		case 2:
			switch (prioOrdning) {
			case 1:
				return 0.7;
			case 2:
				return 0.3;
			default:
				return 0.1;
			}
		case 3:
			switch (prioOrdning) {
			case 1:
				return 0.5;
			case 2:
				return 0.3;
			default:
				return 0.2;
			}
		default:
			switch (prioOrdning) {
			case 1:
				return 0.4;
			case 2:
				return 0.3;
			case 3:
				return 0.2;
			default:
				int maxBesöktaSessioner = Math.min(totaltAntalSessioner, antalÖnskadeFrågor);
				return 0.1 / (maxBesöktaSessioner - 3);
			}
		}
	}

}

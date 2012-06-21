package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Question;
import se.aimday.scheduler.api.WorkshopJson;

/**
 * Ett schemalagt mötestillfälle där forskare och industrireps träffas för att diskutera en fråga
 * 
 * @author fredrikbromee
 * 
 */
public class Workshop {

	List<Forskare> participants = new ArrayList<Forskare>();
	List<ForetagsRepresentant> lyssnare = new ArrayList<ForetagsRepresentant>();
	Question question;
	private double score;

	public double getScore() {
		return score;
	}

	public Workshop(Question fråga) {
		this.question = fråga;
	}

	public boolean isAttendedBy(Forskare kanskeMed) {
		return participants.contains(kanskeMed);
	}

	public boolean harFrågeStällare(ForetagsRepresentant kanskeDär) {
		return lyssnare.contains(kanskeDär);
	}

	public void add(Forskare d) {
		participants.add(d);
	}

	public void add(Collection<ForetagsRepresentant> lyssnare) {
		this.lyssnare.addAll(lyssnare);

	}

	@Override
	public String toString() {
		return question + ":" + participants;
	}

	public Collection<Forskare> getDeltagare() {
		return Collections.unmodifiableCollection(participants);
	}

	public Question getQuestion() {
		return question;
	}

	public int getNumberOfAttendants() {
		return participants.size();
	}

	public void setScore(double score) {
		this.score = score;
	}

	public boolean isFor(Question q) {
		return question.equals(q);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((question == null) ? 0 : question.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Workshop other = (Workshop) obj;
		if (question == null) {
			if (other.question != null)
				return false;
		} else if (!question.equals(other.question))
			return false;
		return true;
	}

	private boolean harDeltagareMedVikt() {
		for (Forskare f : participants) {
			if (f.harVikt()) {
				return true;
			}
		}
		return false;
	}

	public double score(FragaMedDeltagare frågaMedDeltagare) {
		double unweighted = scoreOneWorkshopUnweighted(frågaMedDeltagare);
		if (harDeltagareMedVikt()) {
			return unweighted;
		}

		int vikt = getQuestion().getVikt();

		// vikt = 1 => liite viktigt att det finns en kliniker
		// vikt = 5 => superviktigt att det finns en kliniker
		// Poängen faller linjärt i 6 steg:
		int max = Math.max(1, 6 - vikt);
		return max * unweighted / 6;
	}

	private double scoreOneWorkshopUnweighted(FragaMedDeltagare frågaMedDeltagare) {
		if (getNumberOfAttendants() >= 2) {
			return 1;
		}
		if (frågaMedDeltagare.antalKandidater() == 1 && getNumberOfAttendants() == 1) {
			return 1;
		}
		if (frågaMedDeltagare.antalKandidater() == 0 && getNumberOfAttendants() == 0) {
			return 1;
		}

		return 0.5;
	}

	public WorkshopJson toAPI() {
		WorkshopJson workshopJson = new WorkshopJson();
		workshopJson.frageId = question.id;
		workshopJson.score = score;
		workshopJson.forskare = new ArrayList<String>();
		for (Forskare forskare : participants) {
			workshopJson.forskare.add(forskare.id);
		}
		workshopJson.foretagsrepresentanter = new ArrayList<String>();
		for (ForetagsRepresentant rep : lyssnare) {
			workshopJson.foretagsrepresentanter.add(rep.id);
		}
		return workshopJson;
	}

}

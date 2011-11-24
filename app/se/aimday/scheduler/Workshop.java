package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.Participant;
import models.Question;

/**
 * Ett schemalagt mötestillfälle där forskare och industrireps träffas för att diskutera en fråga
 * 
 * @author fredrikbromee
 * 
 */
public class Workshop {

	List<Participant> participants = new ArrayList<Participant>();
	Question question;
	private double score;

	public double getScore() {
		return score;
	}

	public Workshop(Question fråga) {
		this.question = fråga;
	}

	public boolean isAttendedBy(Participant kanskeMed) {
		return participants.contains(kanskeMed);
	}

	public void add(Participant d) {
		participants.add(d);
	}

	@Override
	public String toString() {
		return "WS: [score=" + score + ", fråga=" + question + ", deltagare=" + participants + "]";
	}

	public Collection<Participant> getDeltagare() {
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
}

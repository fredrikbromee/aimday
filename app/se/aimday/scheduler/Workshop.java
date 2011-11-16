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

	public void läggTill(Participant d) {
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
}

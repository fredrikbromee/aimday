package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.Participant;
import models.Question;


/**
 * One instance of a scheduled aimday complete with sessions and participants
 * @author fredrikbromee
 *
 */
public class AIMDay {

	private double score;
	private List<Session> sessions = new ArrayList<Session>();

	// the questions that couldn't be fit in to the schedule end up here
	private List<Question> unplacedQuestions = new ArrayList<Question>();

	public AIMDay(int antalParallellaSpår, int antalSessioner) {
		for (int i = 0; i < antalSessioner; i++) {
			sessions.add(new Session(antalParallellaSpår));
		}
	}

	public IndividualAgenda personalAgendaFor(Participant deltagare) {
		IndividualAgenda schema = new IndividualAgenda(deltagare);
		for (Session session : sessions) {
			for (Workshop möte : session.getMöten()) {
				if (möte.isAttendedBy(deltagare)) {
					schema.läggTill(möte, session);
				}
			}
		}
		return schema;
	}

	public boolean place(Workshop workshop) {
		boolean gotAPlace = false;
		for (Session session : sessions) {
			gotAPlace = session.place(workshop);
			if (gotAPlace)
				break;
		}
		return gotAPlace;
	}

	public int getNumberOfScheduledWS() {
		int num = 0;
		for (Session session : sessions) {
			num += session.getNumberOfScheduledWS();
		}
		return num;
	}

	public ArrayList<Workshop> getAllWorkshops() {
		ArrayList<Workshop> all = new ArrayList<Workshop>();
		for (Session session : sessions) {
			all.addAll(session.getAllWorkshops());
		}
		return all;
	}

	public void setScore(double score) {
		this.score = score;
	}

	@Override
	public String toString() {
		return String.format("AIMDay [score=%.2f, sessions=" + sessions + ". Failed to place " + unplacedQuestions + "]", score);
	}

	public boolean isBetterThan(AIMDay other) {
		return this.score > other.score;
	}

	public void couldNotPlace(Question q) {
		unplacedQuestions.add(q);
	}

	public Collection<Question> getAllUnplacedQuestions() {
		return Collections.unmodifiableCollection(unplacedQuestions);
	}

	public double getScore() {
		return score;
	}
}

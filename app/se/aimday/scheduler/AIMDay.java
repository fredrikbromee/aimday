package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import models.ForetagsRepresentant;
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
	private HashMap<Participant, IndividualAgenda> allAgendas;

	// the questions that couldn't be fit in to the schedule end up here
	private List<Question> unplacedQuestions = new ArrayList<Question>();
	private double scoreOld;
	private final List<Participant> allParticipants;

	public AIMDay(int antalParallellaSpår, int antalSessioner, List<Participant> allParticipants) {
		this.allParticipants = allParticipants;
		for (int i = 0; i < antalSessioner; i++) {
			sessions.add(new Session(antalParallellaSpår));
		}
	}

	public Collection<IndividualAgenda> getAllIndividualAgendas() {
		if (allAgendas != null) {
			return allAgendas.values();
		}

		allAgendas = new HashMap<Participant, IndividualAgenda>();
		for (Participant p : allParticipants) {
			allAgendas.put(p, personalAgendaFor(p));
		}

		return allAgendas.values();
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

	public void place(Question q, Participant p, Collection<ForetagsRepresentant> lyssnare) {
		boolean gotAPlace = false;
		Session sessionWithQ = getSessionFor(q);
		if (sessionWithQ != null) {
			gotAPlace = sessionWithQ.place(q, p);
			// Logger.info("Failed to place %s at %s in session %s", p, q, sessionWithQ);
			return;
		}

		// Prova att sortera sessioner så att den session med minst antal workshops kommer först! Vi vill fylla
		// sessioner lika mycket.
		for (Session session : sessions) {
			gotAPlace = session.place(q, p, lyssnare);
			if (gotAPlace)
				break;
		}
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
		// prova ett nytt format: fråga:deltagare,deltagare; fråga:deltagare:deltagare; newline
		StringBuffer sb = new StringBuffer();
		for (Session s : sessions) {
			sb.append(s.toString() + "\n");
		}
		return sb.toString();
		// return String.format("AIMDay [score=%.2f (old score=%.2f), sessions=" + sessions + ". Failed to place " +
		// unplacedQuestions + "]",
		// score, scoreOld);
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

	public Collection<IndividualAgenda> getUnsatisfied() {
		Collection<IndividualAgenda> agendas = getAllIndividualAgendas();
		ArrayList<IndividualAgenda> unsatisfied = new ArrayList<IndividualAgenda>();
		for (IndividualAgenda agenda : agendas) {
			if (!agenda.isSatisfied()) {
				unsatisfied.add(agenda);
			}
		}
		return unsatisfied;
	}

	public double getScore() {
		return score;
	}

	public void setOldScore(double scoreOld) {
		this.scoreOld = scoreOld;
	}

	public Session getSessionFor(Question q) {
		for (Session session : sessions) {
			Workshop ws = session.getWorkshop(q);
			if (ws != null) {
				return session;
			}
		}
		return null;
	}

	public Workshop getWorkshop(Question q) {
		for (Session session : sessions) {
			Workshop ws = session.getWorkshop(q);
			if (ws != null) {
				return ws;
			}
		}
		return null;
	}
}

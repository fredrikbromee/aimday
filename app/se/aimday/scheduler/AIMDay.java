package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Question;


/**
 * One instance of a scheduled aimday complete with sessions and participants
 * @author fredrikbromee
 *
 */
public class AIMDay {

	private static final Comparator<? super Session> filledComparator = new Comparator<Session>() {

		@Override
		public int compare(Session session1, Session session2) {
			return Integer.valueOf(session1.getNumberOfScheduledWS()).compareTo(session2.getNumberOfScheduledWS());
		}
	};
	private double score;
	private List<Session> sessions = new ArrayList<Session>();
	private HashMap<Forskare, IndividualAgenda> allAgendas;

	// the questions that couldn't be fit in to the schedule end up here
	private Set<Question> unplacedQuestions = new TreeSet<Question>();
	private final List<Forskare> allParticipants;
	private final int spår;

	public AIMDay(int antalParallellaSpår, int antalSessioner, List<Forskare> allParticipants,
			Collection<Question> questions) {
		this.spår = antalParallellaSpår;
		this.allParticipants = allParticipants;
		for (int i = 0; i < antalSessioner; i++) {
			sessions.add(new Session(i + 1, antalParallellaSpår));
		}
		unplacedQuestions.addAll(questions);
	}

	public int getSpår() {
		return spår;
	}

	public Collection<IndividualAgenda> getAllIndividualAgendas() {
		if (allAgendas != null) {
			return allAgendas.values();
		}

		allAgendas = new HashMap<Forskare, IndividualAgenda>();
		for (Forskare p : allParticipants) {
			allAgendas.put(p, personalAgendaFor(p));
		}

		return allAgendas.values();
	}

	public IndividualAgenda personalAgendaFor(Forskare deltagare) {
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

	public boolean place(Question q, Forskare p, Collection<ForetagsRepresentant> lyssnare) {
		boolean gotAPlace = false;
		Session sessionWithQ = getSessionFor(q);
		if (sessionWithQ != null) {
			gotAPlace = sessionWithQ.place(q, p);
			// Logger.info("Failed to place %s at %s in session %s", p, q, sessionWithQ);
			if (gotAPlace) {
				unplacedQuestions.remove(q);
			}
			return gotAPlace;
		}

		// Sortera sessioner så att den session med minst antal workshops kommer först! Vi vill fylla
		// sessioner lika mycket.
		ArrayList<Session> copy = new ArrayList<Session>(sessions);
		Collections.sort(copy, filledComparator);
		for (Session session : copy) {
			gotAPlace = session.place(q, p, lyssnare);
			if (gotAPlace)
				break;
		}
		if (gotAPlace) {
			unplacedQuestions.remove(q);
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

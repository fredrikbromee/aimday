package se.aimday.scheduler;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Konferens;
import models.Question;
import se.aimday.scheduler.api.SchemaJson;
import se.aimday.scheduler.api.SessionJson;
import se.aimday.scheduler.api.WorkshopJson;


/**
 * One instance of a scheduled aimday complete with sessions and participants
 * @author fredrikbromee
 *
 */
public class AIMDay implements Serializable {

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
	private final int maxDeltagarePerWS;

	public AIMDay(int antalParallellaSpår, int antalSessioner, List<Forskare> allParticipants,
			Collection<Question> questions, int maxDeltagarePerWS) {
		this.spår = antalParallellaSpår;
		this.allParticipants = allParticipants;
		this.maxDeltagarePerWS = maxDeltagarePerWS;
		for (int i = 0; i < antalSessioner; i++) {
			sessions.add(new Session(i + 1, antalParallellaSpår));
		}
		unplacedQuestions.addAll(questions);
	}

	private AIMDay(int antalParallellaSpår, int size, List<Forskare> deltagare, Collection<Question> frågor,
			int maxAntalDeltagareIEnWS, List<Session> existingSchedule) {
		this.spår = antalParallellaSpår;
		this.allParticipants = deltagare;
		this.maxDeltagarePerWS = maxAntalDeltagareIEnWS;
		this.sessions = existingSchedule;
		unplacedQuestions.addAll(frågor);
		for (Session session : existingSchedule) {
			for (Workshop ws : session.getAllWorkshops()) {
				unplacedQuestions.remove(ws.question);
			}
		}
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

	private IndividualAgenda personalAgendaFor(Forskare deltagare) {
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
			gotAPlace = sessionWithQ.place(q, p, maxDeltagarePerWS);
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
			gotAPlace = session.place(q, p, lyssnare, maxDeltagarePerWS);
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

	public int getMaxNumOfWorkshopsInASession() {
		int max = 0;
		for (Session session : sessions) {
			if (session.getNumberOfScheduledWS() > max) {
				max = session.getNumberOfScheduledWS();
			}
		}

		return max;
	}

	public SchemaJson toAPI() {
		SchemaJson schemaJson = new SchemaJson();
		List<SessionJson> sessioner = new ArrayList<SessionJson>();
		for (Session sess : sessions) {
			sessioner.add(sess.toAPI());
		}
		schemaJson.sessioner = sessioner;
		HashSet<String> un = new HashSet<String>();
		for (Question question : unplacedQuestions) {
			String id = question.id;
			if (question.hasZeroAttendants){
				id= id+'*';
			}
			un.add(id);
		}
		schemaJson.unplacedQuestions = un;
		HashSet<String> up = new HashSet<String>();
		for (IndividualAgenda ag : getUnsatisfied()) {
			Forskare f = ag.getParticipant();
			up.add(String.format("%s enrolled in %s questions, was placed in %s questions", f.first_name, ag.getWishedNumber(), ag.getAssignedNumber()));
/*			${un.participant.first_name} enrolled in ${un.wishedNumber} questions, 
	         was placed in  
	         ${un.assignedNumber} questions ${un.agenda} 
	*/
		}
		schemaJson.unsatisfied = up;
		return schemaJson;
	}

	public void taBortFrågorMedFörFåDeltagare(int minDeltagarePerWS) {
		allAgendas = null;
		score = 0;
		
		for (Session s : sessions) {
			unplacedQuestions.addAll(s.taBortFrågorMedFörFåDeltagare(minDeltagarePerWS));
		}
	}

	public static AIMDay fromJson(SchemaJson schema, Konferens k) {

		int antalParallellaSpår = 0;
		for (SessionJson sessionJson : schema.sessioner) {
			antalParallellaSpår = Math.max(antalParallellaSpår, sessionJson.workshops.size());
		}
		int sessionsNummer = 1;
		List<Session> theseSessions = new ArrayList<Session>();

		int maxAntalDeltagareIEnWS = 10;
		for (SessionJson sessionJson : schema.sessioner) {
			Session session = new Session(sessionsNummer, antalParallellaSpår);
			theseSessions.add(session);
			sessionsNummer++;

			for (WorkshopJson workshopJson : sessionJson.workshops) {
				List<String> foretagsrepresentanter = workshopJson.foretagsrepresentanter;
				List<ForetagsRepresentant> reps = new ArrayList<ForetagsRepresentant>();
				if (null != foretagsrepresentanter) {
					for (String id : foretagsrepresentanter) {
						reps.add(k.getFöretagsrep(id));
					}
				}
				Workshop workshop = new Workshop(k.getFråga(workshopJson.frageId));
				workshop.add(reps);

				List<String> forskare = workshopJson.forskare;
				for (String id : forskare) {
					workshop.add(k.getForskare(id));
				}
				session.addCompleteWorkshop(workshop);
				maxAntalDeltagareIEnWS = Math.max(workshop.getAntalDeltagare(), maxAntalDeltagareIEnWS);
			}
		}

		return new AIMDay(antalParallellaSpår, schema.sessioner.size(), k.getDeltagare(), k.getFrågor(),
				maxAntalDeltagareIEnWS, theseSessions);
	}

	public int getNumberOfSessions() {
		return sessions.size();
	}

	public int getMaxDeltagarePerWS() {
		return maxDeltagarePerWS;
	}

	public int getMinAntalDeltagareIEnWS() {
		int min = 0;
		for (Session session : sessions) {
			for (Workshop ws : session.getAllWorkshops()) {
				min = Math.min(min, ws.getAntalDeltagare());
			}
		}
		return min;
	}

	public double scoreIndividualAgendas() {
		double cumulativePrioScore = 0;
		for (IndividualAgenda agenda : getAllIndividualAgendas()) {
			double agendaScore = agenda.score(sessions.size());
			cumulativePrioScore += agendaScore;
			agenda.setScore(agendaScore);
		}

		return cumulativePrioScore;
	}

	public Collection<String> getAllScheduleErrors() {
		HashSet<String> allErrors = new HashSet<String>();
		Collection<IndividualAgenda> allIndividualAgendas = getAllIndividualAgendas();
		for (IndividualAgenda individualAgenda : allIndividualAgendas) {
			allErrors.addAll(individualAgenda.errors);
		}
		return allErrors;
	}

	public void placeraLåstFråga(FragaMedDeltagare fragaMedDeltagare) {
		Question question = fragaMedDeltagare.getFråga();
		Session session = sessions.get(question.getLåstSession() - 1);
		session.placeraLåstFråga(question, fragaMedDeltagare.getFrågare());
	}
}

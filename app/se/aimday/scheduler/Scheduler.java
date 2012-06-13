package se.aimday.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Konferens;
import models.Question;

/**
 * Spottar ut sig ett schema givet input om vilka frågor som skall diskuteras och vilka som vill vara med och diskutera
 * dem
 * 
 * @author fredrikbromee
 * 
 */
public class Scheduler {

	private final int generations = 2000;

	private int numParallelTracks;

	private int numSessions;

	private int maxAttendantsPerWS;

	private final Collection<Question> questions;

	private TreeMap<String, FragaMedDeltagare> frågor;

	private final List<Forskare> allParticipants = new ArrayList<Forskare>();

	private final int placeWeight;

	private final int wsWeight;

	private final int agendaWeight;

	public Scheduler(int numParallelTracks, int numSessions, int maxAttendantsPerWS, Collection<Question> questions,
			List<Forskare> erfarna, List<Forskare> oerfarna, List<ForetagsRepresentant> foretagare,
			int generations) {
		this(numParallelTracks, numSessions, maxAttendantsPerWS, new Konferens(erfarna, foretagare, questions),
				generations, 10, 10, 10);

	}

	public Scheduler(int numParallelTracks, int numSessions, int maxAttendantsPerWS, Konferens k, int generations,
			int placeWeight, int wsWeight, int agendaWeight) {
		this.numParallelTracks = numParallelTracks;
		this.numSessions = numSessions;
		this.maxAttendantsPerWS = maxAttendantsPerWS;
		this.questions = k.getFrågor();
		this.placeWeight = placeWeight;
		this.wsWeight = wsWeight;
		this.agendaWeight = agendaWeight;
		this.allParticipants.addAll(k.getDeltagare());

		frågor = new TreeMap<String, FragaMedDeltagare>();
		for (Question q : questions) {
			FragaMedDeltagare f = new FragaMedDeltagare(q);
			frågor.put(q.getQ(), f);
		}

		for (Forskare deltagare : k.getDeltagare()) {
			for (Question fråga : deltagare.getÖnskelista()) {
				frågor.get(fråga.getQ()).läggTillDeltagare(deltagare);
			}
		}

		for (ForetagsRepresentant f : k.getForetagare()) {
			for (Question fråga : f.getFrågelista()) {
				frågor.get(fråga.getQ()).läggTillFöretagare(f);
			}
		}

		// TODO lägg frågor helt utan anmälda i en egen hög

		// TODO tills vidare, hoppa över de oerfarna
	}

	public AIMDay lägg() {
		// do first try based on how many workshops each candidate has

		List<Forskare> sorteradeDeltagare = new ArrayList<Forskare>(this.allParticipants);
		Collections.sort(sorteradeDeltagare, comp);
		AIMDay schedule = schedule(sorteradeDeltagare);
		AIMDay bestSoFar = schedule;
		System.out.println("Initial schedule: \n" + schedule);

		for (int i = 0; i < generations; i++) {
			AIMDay newSchedule = getNewScheduleFrom(schedule);
			System.out.println("New schedule: \n" + newSchedule);
			if (newSchedule.isBetterThan(bestSoFar)) {
				System.out.println("Better schedule: \n" + schedule);

				bestSoFar = newSchedule;
			}
			schedule = newSchedule;
		}

		// TODO här kan man placera ut de oerfarna

		return bestSoFar;
	}

	private AIMDay getNewScheduleFrom(AIMDay schedule) {
		// Find pain points in the previous schedule
		List<IndividualAgenda> allAgendas = new ArrayList<IndividualAgenda>(schedule.getAllIndividualAgendas());
		Collections.sort(allAgendas, painPointComparator);

		List<Forskare> allPeeps = new ArrayList<Forskare>();
		for (IndividualAgenda individualAgenda : allAgendas) {
			allPeeps.add(individualAgenda.getParticipant());
		}
		// System.out.println(allPeeps);
		// Logger.info(allPeeps.toString(), allPeeps);
		return schedule(allPeeps);
	}

	private AIMDay schedule(List<Forskare> sorteradeDeltagare) {
		AIMDay schema = new AIMDay(numParallelTracks, numSessions, allParticipants, questions);

		// Tanken är här att man skulle kunna börja med att lägga ut de frågor som har forskare låsta till sig
		// Ett problem är att det kan resultera i omöjliga utläggningar (eller så placerar man dem?)JA!!!

		// hitta alla frågelåsta forskare
		// för varje frågelåst forskare
		// för varje låst fråga
		// placera ut frågan på schemat.
		List<Forskare> frågelåsta = getFrågelåstaForskare();
		for (Forskare p : frågelåsta) {
			for (String qId : p.getLåstaFrågor()) {
				FragaMedDeltagare medDeltagare = frågor.get(qId);
				schema.place(medDeltagare.getFråga(), p, medDeltagare.getFrågare());
			}
		}

		for (Forskare p : sorteradeDeltagare) {
			for (Question q : p.getRandomizedWishlist()) {
				FragaMedDeltagare medDeltagare = frågor.get(q.getQ());
				schema.place(q, p, medDeltagare.getFrågare());
			}
		}

		score(schema);
		return schema;
	}

	private List<Forskare> getFrågelåstaForskare() {
		List<Forskare> lst = new ArrayList<Forskare>();
		for (Forskare f : allParticipants) {
			if (f.ärLåstTillFrågor()) {
				lst.add(f);
			}
		}
		return lst;
	}

	/**
	 * How good is this schedule
	 * 
	 * @return a value between 0 and 1, where 1 is an ideal schedule
	 */
	private double score(AIMDay schedule) {
		// are all questions placed?
		int weightForAllQsPlaced = placeWeight;
		int allQScore = 0;
		if (allQsPlaced(schedule)) {
			allQScore = weightForAllQsPlaced;
		}

		// what is the score for all workshops?
		double cumulativeWSScore = 0;
		for (Workshop ws : schedule.getAllWorkshops()) {
			FragaMedDeltagare frågaMedDeltagare = frågor.get(ws.getQuestion().getQ());

			double wsScore = ws.score(frågaMedDeltagare);
			cumulativeWSScore += wsScore;
			ws.setScore(wsScore);
		}
		int weightWS = wsWeight;
		cumulativeWSScore = cumulativeWSScore / (numParallelTracks * numSessions) * weightWS;

		// how well have attendants wishes been filled?
		int weightPrio = agendaWeight;
		double cumulativePrioScore = 0;
		for (IndividualAgenda agenda : schedule.getAllIndividualAgendas()) {
			double agendaScore = agenda.score(numSessions);
			cumulativePrioScore += agendaScore;
			agenda.setScore(agendaScore);
		}
		cumulativePrioScore = cumulativePrioScore * weightPrio / allParticipants.size();

		double score = (allQScore + cumulativeWSScore + cumulativePrioScore) / (weightForAllQsPlaced + weightWS + weightPrio);
		schedule.setScore(score);
		return score;
	}


	private boolean allQsPlaced(AIMDay schedule) {
		return questions.size() == schedule.getNumberOfScheduledWS() + schedule.getAllUnplacedQuestions().size();
	}

	/**
	 * Sorts agendas after how good they are. Better agendas later
	 */
	private Comparator<IndividualAgenda> painPointComparator = new Comparator<IndividualAgenda>() {
		@Override
		public int compare(IndividualAgenda arg0, IndividualAgenda arg1) {
			int degreeCompare = arg0.getParticipant().compareSWO(arg1.getParticipant());
			if (degreeCompare != 0) {
				return degreeCompare;
			}
			return Double.valueOf(arg0.getScore()).compareTo(Double.valueOf(arg1.getScore()));
		}
	};

	private static final Comparator<Forskare> comp = new Comparator<Forskare>() {

		@Override
		public int compare(Forskare ett, Forskare två) {
			return Integer.valueOf(två.getÖnskelista().size()).compareTo(ett.getÖnskelista().size());
		}
	};

	public static final class Byggare {

		private final int numParallelTracks;

		private int numSessions;

		private int maxAttendantsPerWS;

		private ArrayList<Question> frågor = new ArrayList<Question>();
		private List<Forskare> erfarna = new ArrayList<Forskare>();
		private List<Forskare> oerfarna = new ArrayList<Forskare>();

		private List<ForetagsRepresentant> lyssnare = new ArrayList<ForetagsRepresentant>();

		public Byggare(int numParallelTracks) {
			this.numParallelTracks = numParallelTracks;
		}

		public Byggare medDeltagare(Forskare... deltagare) {
			for (Forskare del : deltagare) {
				if (del.ärErfaren()) {
					this.erfarna.add(del);
				} else {
					this.oerfarna.add(del);
				}
			}
			return this;
		}

		public Byggare medDeltagare(List<ForetagsRepresentant> lyssnare) {
			this.lyssnare = lyssnare;
			return this;
		}

		public Byggare mednumSessions(int numSessions) {
			this.numSessions = numSessions;
			return this;
		}

		public Byggare medMaxAntalDeltagarePerMöte(int maxAttendantsPerWS) {
			this.maxAttendantsPerWS = maxAttendantsPerWS;
			return this;
		}

		public Byggare medFrågor(Question... frågor) {
			this.frågor.clear();
			this.frågor.addAll(Arrays.asList(frågor));
			return this;
		}

		public Scheduler bygg() {
			return new Scheduler(numParallelTracks, numSessions, maxAttendantsPerWS, frågor, erfarna, oerfarna,
					lyssnare, 2000);
		}
	}
}

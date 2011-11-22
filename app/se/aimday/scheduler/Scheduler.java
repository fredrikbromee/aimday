package se.aimday.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import models.Participant;
import models.Question;

/**
 * Spottar ut sig ett schema givet input om vilka frågor som skall diskuteras och vilka som vill vara med och diskutera
 * dem
 * 
 * @author fredrikbromee
 * 
 */
public class Scheduler {

	private int numParallelTracks;

	private int numSessions;

	private int maxAttendantsPerWS;

	private final Collection<Question> questions;

	private TreeMap<String, QuestionWithParticipants> frågor;

	private final List<Participant> allParticipants = new ArrayList<Participant>();

	public Scheduler(int numParallelTracks, int numSessions, int maxAttendantsPerWS, Collection<Question> questions,
			List<Participant> erfarna, List<Participant> oerfarna) {
		this.numParallelTracks = numParallelTracks;
		this.numSessions = numSessions;
		this.maxAttendantsPerWS = maxAttendantsPerWS;
		this.questions = questions;
		this.allParticipants.addAll(erfarna);

		frågor = new TreeMap<String, QuestionWithParticipants>();
		for (Question q : questions) {
			QuestionWithParticipants f = new QuestionWithParticipants(q);
			frågor.put(q.getQ(), f);
		}

		for (Participant deltagare : erfarna) {
			for (Question fråga : deltagare.getÖnskelista()) {
				frågor.get(fråga.getQ()).läggTillDeltagare(deltagare);
			}
		}


		// TODO lägg frågor helt utan anmälda i en egen hög

		// TODO tills vidare, hoppa över de oerfarna
	}

	/**
	 * Ger en lista med bra scheman, med det bästa schemat först
	 * 
	 * @return
	 */
	public List<AIMDay> lägg() {
		// do first try based on how many candidates each question has
		List<QuestionWithParticipants> sorteradeFrågor = new ArrayList<QuestionWithParticipants>(getAllQs());
		Collections.sort(sorteradeFrågor, comp);
		AIMDay schedule = schedule(sorteradeFrågor);
		AIMDay bestSoFar = schedule;
		System.out.println("Initial schedule: \n" + schedule);

		for (int i = 0; i < 2000; i++) {
			AIMDay newSchedule = getNewScheduleFrom(schedule);
			// System.out.println("New schedule: \n" + newSchedule);
			if (newSchedule.isBetterThan(bestSoFar)) {
				bestSoFar = newSchedule;
			}
			schedule = newSchedule;
		}
		return Collections.singletonList(bestSoFar);
	}

	private AIMDay getNewScheduleFrom(AIMDay schedule) {
		// Find pain points in the previous schedule
		ArrayList<Workshop> allWorkshops = schedule.getAllWorkshops();
		Collections.sort(allWorkshops, painPointComparator);

		// and place new schedule with the previous pain points first
		ArrayList<QuestionWithParticipants> sortedQs = new ArrayList<QuestionWithParticipants>();
		for (Question unplaced : schedule.getAllUnplacedQuestions()) {
			sortedQs.add(frågor.get(unplaced.getQ()).deepClone());
		}
		for (Workshop workshop : allWorkshops) {
			sortedQs.add(frågor.get(workshop.getQuestion().getQ()).deepClone());
		}

		return schedule(sortedQs);
	}

	private AIMDay schedule(List<QuestionWithParticipants> sorteradeFrågor) {
		AIMDay schema = new AIMDay(numParallelTracks, numSessions);

		for (QuestionWithParticipants fråga : sorteradeFrågor) {
			boolean gickPlaceraUt = false;
			while (fråga.harKandidat()) {
				Workshop workshop = new Workshop(fråga.getFråga());
				gickPlaceraUt = placeraUt(schema, fråga, workshop);
				if (gickPlaceraUt)
					break;
			}
			if (!gickPlaceraUt) {
				schema.couldNotPlace(fråga.getFråga());
				// TODO ta hand om frågor som inte gick att placera ut!
				// System.out.println("Could not place " + fråga.getFråga());
			}
		}

		// TODO placera ut alla kandidater som går att placera ut (dvs de som inte bryter mot schemat).

		score(schema);
		return schema;
	}

	private Collection<QuestionWithParticipants> getAllQs() {
		Collection<QuestionWithParticipants> lst = new ArrayList<QuestionWithParticipants>();
		for (QuestionWithParticipants f : frågor.values()) {
			lst.add(f.deepClone());
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
		int weightForAllQsPlaced = 1;
		int allQScore = 0;
		if (allQsPlaced(schedule)) {
			allQScore = weightForAllQsPlaced;
		}
		// are all ws filled with experienced?
		double cumulativeWSScore = 0;
		for (Workshop ws : schedule.getAllWorkshops()) {
			double wsScore = scoreOneWorkshop(ws);
			cumulativeWSScore += wsScore;
			ws.setScore(wsScore);
		}
		int weightWS = 5;
		cumulativeWSScore = cumulativeWSScore / (numParallelTracks * numSessions) * weightWS;

		// how well have attendants wishes been filled?
		int weightPrio = 10;
		double cumulativePrioScore =0;
		for (IndividualAgenda agenda : schedule.getAllIndividualAgendas()) {
			double agendaScore = agenda.score(numSessions);
			cumulativePrioScore += agendaScore;
			agenda.setScore(agendaScore);
		}
		cumulativePrioScore = cumulativePrioScore * weightPrio / allParticipants.size();

		double scoreOld = (allQScore + cumulativeWSScore) / (weightForAllQsPlaced + weightWS);
		double score = (allQScore + cumulativeWSScore + cumulativePrioScore) / (weightForAllQsPlaced + weightWS + weightPrio);
		schedule.setScore(score);
		schedule.setOldScore(scoreOld);
		return score;
	}

	private double scoreOneWorkshop(Workshop ws) {
		if (ws.getNumberOfAttendants() == 2) {
			return 1;
		}
		QuestionWithParticipants frågaMedDeltagare = frågor.get(ws.getQuestion().getQ());
		if (frågaMedDeltagare.antalKandidater() == 1 && ws.getNumberOfAttendants() == 1) {
			return 1;
		}
		if (frågaMedDeltagare.antalKandidater() == 0 && ws.getNumberOfAttendants() == 0) {
			return 1;
		}

		return 0.5;
	}

	private boolean allQsPlaced(AIMDay schedule) {
		return questions.size() == schedule.getNumberOfScheduledWS();
	}

	private boolean placeraUt(AIMDay schema, QuestionWithParticipants fråga, Workshop workshop) {
		if (fråga.harKandidat()) {
			workshop.läggTill(fråga.taKandidat());
		}
		if (fråga.harKandidat()) {
			workshop.läggTill(fråga.taKandidat());
		}
		boolean gickPlaceraUt = schema.place(workshop);
		return gickPlaceraUt;
	}

	/**
	 * Sorts workshops after how good they are. Better workshops later
	 */
	private Comparator<Workshop> painPointComparator = new Comparator<Workshop>() {
		@Override
		public int compare(Workshop arg0, Workshop arg1) {
			return Double.valueOf(arg0.getScore()).compareTo(Double.valueOf(arg1.getScore()));
		}
	};

	private static final Comparator<QuestionWithParticipants> comp = new Comparator<QuestionWithParticipants>() {

		@Override
		public int compare(QuestionWithParticipants ett, QuestionWithParticipants två) {
			return Integer.valueOf(två.antalKandidater()).compareTo(ett.antalKandidater());
		}
	};

	public static final class Byggare {
	
		private final int numParallelTracks;
	
		private int numSessions;
	
		private int maxAttendantsPerWS;
	
		private ArrayList<Question> frågor = new ArrayList<Question>();
		private List<Participant> erfarna = new ArrayList<Participant>();
		private List<Participant> oerfarna = new ArrayList<Participant>();
	
		public Byggare(int numParallelTracks) {
			this.numParallelTracks = numParallelTracks;
		}
	
		public Byggare medDeltagare(Participant... deltagare) {
			for (Participant del : deltagare) {
				if (del.ärErfaren()) {
					this.erfarna.add(del);
				} else {
					this.oerfarna.add(del);
				}
			}
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
			return new Scheduler(numParallelTracks, numSessions, maxAttendantsPerWS, frågor, erfarna, oerfarna);
		}
	
	}

}

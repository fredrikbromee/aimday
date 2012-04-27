package se.aimday.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

import models.ForetagsRepresentant;
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

	private final int generations = 2000;

	private int numParallelTracks;

	private int numSessions;

	private int maxAttendantsPerWS;

	private final Collection<Question> questions;

	private TreeMap<String, FrågaMedDeltagare> frågor;

	private final List<Participant> allParticipants = new ArrayList<Participant>();

	public Scheduler(int numParallelTracks, int numSessions, int maxAttendantsPerWS, Collection<Question> questions,
			List<Participant> erfarna, List<Participant> oerfarna, List<ForetagsRepresentant> foretagare,
			int generations) {
		this.numParallelTracks = numParallelTracks;
		this.numSessions = numSessions;
		this.maxAttendantsPerWS = maxAttendantsPerWS;
		this.questions = questions;
		this.allParticipants.addAll(erfarna);

		frågor = new TreeMap<String, FrågaMedDeltagare>();
		for (Question q : questions) {
			FrågaMedDeltagare f = new FrågaMedDeltagare(q);
			frågor.put(q.getQ(), f);
		}

		for (Participant deltagare : erfarna) {
			for (Question fråga : deltagare.getÖnskelista()) {
				frågor.get(fråga.getQ()).läggTillDeltagare(deltagare);
			}
		}

		for (ForetagsRepresentant f : foretagare) {
			for (Question fråga : f.getFrågelista()) {
				frågor.get(fråga.getQ()).läggTillFöretagare(f);
			}
		}

		// TODO lägg frågor helt utan anmälda i en egen hög

		// TODO tills vidare, hoppa över de oerfarna
	}

	public AIMDay lägg() {
		// do first try based on how many workshops each candidate has

		List<Participant> sorteradeDeltagare = new ArrayList<Participant>(this.allParticipants);
		Collections.sort(sorteradeDeltagare, comp);
		AIMDay schedule = schedule(sorteradeDeltagare);
		AIMDay bestSoFar = schedule;
		System.out.println("Initial schedule: \n" + schedule);

		for (int i = 0; i < generations; i++) {
			AIMDay newSchedule = getNewScheduleFrom(schedule);
			// System.out.println("New schedule: \n" + newSchedule);
			if (newSchedule.isBetterThan(bestSoFar)) {
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

		List<Participant> allPeeps = new ArrayList<Participant>();
		for (IndividualAgenda individualAgenda : allAgendas) {
			allPeeps.add(individualAgenda.getParticipant());
		}
		// System.out.println(allPeeps);
		// Logger.info(allPeeps.toString(), allPeeps);
		return schedule(allPeeps);
	}

	private AIMDay schedule(List<Participant> sorteradeDeltagare) {
		AIMDay schema = new AIMDay(numParallelTracks, numSessions, allParticipants, questions);

		for (Participant p : sorteradeDeltagare) {
			for (Question q : p.getRandomizedWishlist()) {
				FrågaMedDeltagare medDeltagare = frågor.get(q.getQ());
				schema.place(q, p, medDeltagare.getFrågare());
			}
		}

		score(schema);
		return schema;
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
		int weightWS = 10;
		cumulativeWSScore = cumulativeWSScore / (numParallelTracks * numSessions) * weightWS;

		// how well have attendants wishes been filled?
		int weightPrio = 10;
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

	private double scoreOneWorkshop(Workshop ws) {
		if (ws.getNumberOfAttendants() >= 2) {
			return 1;
		}
		FrågaMedDeltagare frågaMedDeltagare = frågor.get(ws.getQuestion().getQ());
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

	// private boolean placeraUt(AIMDay schema, QuestionWithParticipants fråga, Workshop workshop) {
	// if (fråga.harKandidat()) {
	// workshop.add(fråga.taKandidat());
	// }
	// if (fråga.harKandidat()) {
	// workshop.add(fråga.taKandidat());
	// }
	// boolean gickPlaceraUt = schema.place(workshop);
	// return gickPlaceraUt;
	// }

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

	private static final Comparator<Participant> comp = new Comparator<Participant>() {

		@Override
		public int compare(Participant ett, Participant två) {
			return Integer.valueOf(två.getÖnskelista().size()).compareTo(ett.getÖnskelista().size());
		}
	};

	public static final class Byggare {

		private final int numParallelTracks;

		private int numSessions;

		private int maxAttendantsPerWS;

		private ArrayList<Question> frågor = new ArrayList<Question>();
		private List<Participant> erfarna = new ArrayList<Participant>();
		private List<Participant> oerfarna = new ArrayList<Participant>();

		private List<ForetagsRepresentant> lyssnare = new ArrayList<ForetagsRepresentant>();

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

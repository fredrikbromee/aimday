package models;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.aimday.scheduler.api.ForskareJson;
import se.aimday.scheduler.api.FragaJson;
import se.aimday.scheduler.api.InconsistentJsonException;
import se.aimday.scheduler.api.KonferensJson;
import se.aimday.scheduler.api.Lasningar.IntegerLas;
import se.aimday.scheduler.api.Lasningar.StringLas;

public class Konferens {

	private final List<Forskare> deltagare;
	private final List<ForetagsRepresentant> foretagare;
	private final Map<String, Question> frågor;

	public Konferens(List<Forskare> deltagare, List<ForetagsRepresentant> foretagare, Collection<Question> frågor) {
		HashMap<String, Question> qs = new HashMap<String, Question>();
		for (Question q : frågor) {
			qs.put(q.id, q);
		}
		this.deltagare = deltagare;
		this.foretagare = foretagare;
		this.frågor = qs;
	}

	public Konferens(List<Forskare> allParticipants, List<ForetagsRepresentant> foretagare, Map<String, Question> frågor) {
		this.deltagare = allParticipants;
		this.foretagare = foretagare;
		this.frågor = frågor;
	}

	public static Konferens fromAPI(KonferensJson konf) throws InconsistentJsonException {
		konf.förkortaSenioritetsGrader();
		Map<String, Question> allQuestions = Question.fromAPI(konf.fragor);
		List<ForetagsRepresentant> foretagare = ForetagsRepresentant.fromAPI(konf.foretagsrepresentanter);
		Map<String, Forskare> allParticipants = Forskare.fromAPI(konf.forskare, konf.senioritetsgrader, allQuestions);
		for (Question q : allQuestions.values()) {
			boolean ingenSomVillGå = true;
			for (Forskare forskare : allParticipants.values()) {
				if (forskare.villGåPå(q)) {
					ingenSomVillGå = false;
					continue;
				}
			}
			if (ingenSomVillGå) {
				q.setIngenSomVillGå(true);
			}
		}
		for (IntegerLas lås : konf.låsningar.frågesessionslås) {
			Question question = allQuestions.get(lås.id);
			if (question != null) {
				question.låsTillSession(lås.låsttill);
			}
		}
		if (konf.låsningar.frågerumslås != null) {
			for (IntegerLas lås : konf.låsningar.frågerumslås) {
				Question question = allQuestions.get(lås.id);
				if (question != null) {
					question.låsTillRum(lås.låsttill);
				}
			}
		}
		for (IntegerLas lås : konf.låsningar.forskarsessionslås) {
			Forskare forskare = allParticipants.get(lås.id);
			if (forskare != null) {
				forskare.låsTillSessioner(lås.låsttill);
			}
		}

		for (StringLas lås : konf.låsningar.forskarfrågelås) {
			Forskare forskare = allParticipants.get(lås.id);
			if (forskare != null) {
				taBortFrågorSomInteFinnsKvar(allQuestions, lås.låsttill);
				forskare.låsTillFrågor(lås.låsttill);
			}
		}

		Konferens konferens = new Konferens(new ArrayList<Forskare>(allParticipants.values()), foretagare, allQuestions);

		// Och så flyttar vi in låsningarna i grunddatat så att knockout kan visa dem också
		List<FragaJson> fragor = konf.fragor;
		for (FragaJson fraga : fragor) {
			Question question = konferens.getFråga(fraga.id);
			fraga.låst = question.getLås();
			if (question.getLåstRum() != null) {
				fraga.låstaRum = Collections.singletonList(question.getLåstRum());
			}
		}
		for (ForskareJson forskareApi : konf.forskare) {
			Forskare forskare = konferens.getForskare(forskareApi.id);
			forskareApi.låstaFrågor = forskare.getLåstaFrågor();
			forskareApi.låstaSessioner = forskare.getLåstaSessioner();
		}

		return konferens;
	}

	private static void taBortFrågorSomInteFinnsKvar(Map<String, Question> allQuestions, List<String> låstafrågor) {
		for (Iterator<String> iterator = låstafrågor.iterator(); iterator.hasNext();) {
			String frågeID = iterator.next();
			if (!allQuestions.containsKey(frågeID)) {
				iterator.remove();
			}
		}
	}

	public List<Forskare> getDeltagare() {
		return deltagare;
	}

	public List<ForetagsRepresentant> getForetagare() {
		return foretagare;
	}

	public Collection<Question> getFrågor() {
		return frågor.values();
	}

	public ForetagsRepresentant getFöretagsrep(String id) {
		for (ForetagsRepresentant f : foretagare) {
			if (f.id.equals(id)) {
				return f;
			}
		}
		return null;
	}

	public Forskare getForskare(String id) {
		for (Forskare f : deltagare) {
			if (f.id.equals(id)) {
				return f;
			}
		}
		return null;
	}

	public Question getFråga(String frageId) {
		return frågor.get(frageId);
	}
}

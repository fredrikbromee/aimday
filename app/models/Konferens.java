package models;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.aimday.scheduler.api.InconsistentJsonException;
import se.aimday.scheduler.api.KonferensJson;

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
		List<Forskare> allParticipants = null;
		List<ForetagsRepresentant> foretagare = null;
		Map<String, Question> allQuestions = null;
		foretagare = ForetagsRepresentant.fromAPI(konf.foretagsrepresentanter);
		allQuestions = Question.fromAPI(konf.fragor);
		allParticipants = Forskare.fromAPI(konf.forskare, konf.senioritetsgrader, allQuestions);
		for (Question q : allQuestions.values()) {
			boolean ingenSomVillGå = true;
			for (Forskare forskare : allParticipants) {
				if (forskare.villGåPå(q)) {
					ingenSomVillGå = false;
					continue;
				}
			}
			if (ingenSomVillGå) {
				q.setIngenSomVillGå(true);
			}
		}
		return new Konferens(allParticipants, foretagare, allQuestions);
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

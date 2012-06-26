package models;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import se.aimday.scheduler.api.InconsistentJsonException;
import se.aimday.scheduler.api.KonferensJson;

public class Konferens {

	private final List<Forskare> deltagare;
	private final List<ForetagsRepresentant> foretagare;
	private final Collection<Question> frågor;

	public Konferens(List<Forskare> deltagare, List<ForetagsRepresentant> foretagare, Collection<Question> frågor) {
		this.deltagare = deltagare;
		this.foretagare = foretagare;
		this.frågor = frågor;
	}

	public static Konferens fromAPI(KonferensJson konf) throws InconsistentJsonException {
		List<Forskare> allParticipants = null;
		List<ForetagsRepresentant> foretagare = null;
		Map<String, Question> allQuestions = null;
		foretagare = ForetagsRepresentant.fromAPI(konf.foretagsrepresentanter);
		allQuestions = Question.fromAPI(konf.fragor);
		allParticipants = Forskare.fromAPI(konf.forskare, konf.senioritetsgrader, allQuestions);
		return new Konferens(allParticipants, foretagare, allQuestions.values());
	}

	public List<Forskare> getDeltagare() {
		return deltagare;
	}

	public List<ForetagsRepresentant> getForetagare() {
		return foretagare;
	}

	public Collection<Question> getFrågor() {
		return frågor;
	}
}

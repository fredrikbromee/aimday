package models;
import java.util.ArrayList;
import java.util.List;

import se.aimday.scheduler.api.ForetagsRepresentantJson;
import se.aimday.scheduler.api.InconsistentJsonException;

/**
 * En deltagare på en konferens som vill ställa en eller fler frågor
 * 
 * @author fredrikbromee
 * 
 */
public class ForetagsRepresentant {
	
	public List<Question> fragor = new ArrayList<Question>();
	public String namn;
	private final String id;

	public ForetagsRepresentant(String namn, String id) {
		this.namn = namn;
		this.id = id;
	}

	public List<Question> getFrågelista() {
		return fragor;
	}

	@Override
	public String toString() {
		return "[" + namn + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		if (id == null)
			return result;
		result = prime * result + id.hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		ForetagsRepresentant other = (ForetagsRepresentant) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void önskarSe(Question fråga) {
		fragor.add(fråga);
	}

	public static List<ForetagsRepresentant> fromAPI(List<ForetagsRepresentantJson> företagsrepresentanter)
			throws InconsistentJsonException {
		ArrayList<ForetagsRepresentant> reps = new ArrayList<ForetagsRepresentant>();
		for (ForetagsRepresentantJson foretagsRepresentantJson : företagsrepresentanter) {
			reps.add(ForetagsRepresentant.fromApi(foretagsRepresentantJson));
		}
		return reps;
	}

	public static ForetagsRepresentant fromApi(ForetagsRepresentantJson json) throws InconsistentJsonException {
		if (json.kontakt == null){
			throw new InconsistentJsonException(String.format("Företagsrepresentant %s has no contact info", json.id));
		}
		ForetagsRepresentant rep = new ForetagsRepresentant(json.kontakt.fornamn + json.kontakt.efternamn, json.id);
		for (String qId : json.frågor) {
			rep.önskarSe(new Question(qId));
		}
		return rep;
	}
}

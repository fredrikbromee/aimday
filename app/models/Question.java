package models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.aimday.scheduler.api.FragaJson;

/**
 * En diskussionsfråga
 * 
 * @author fredrikbromee
 * 
 */
public class Question implements Comparable<Question> {

	public String id;
	private int vikt = 0;
	private List<Integer> låstaSessioner = new ArrayList<Integer>();
	private List<Integer> låstaRum = new ArrayList<Integer>();
	public boolean hasZeroAttendants;

	public Question(String id) {
		this.id = id;
	}

	public int getVikt() {
		return vikt;
	}

	public String getQ() {
		return id;
	}

	@Override
	public String toString() {
		return id;
	}

	@Override
	public int compareTo(Question other) {
		return id.compareTo(other.id);
	}

	public static Map<String, Question> fromAPI(List<FragaJson> json) {
		Map<String, Question> frågor = new HashMap<String, Question>();
		for (FragaJson fragaJson : json) {
			Question question = new Question(fragaJson.id);
			question.vikt = fragaJson.vikt;
			frågor.put(fragaJson.id, question);

		}
		return frågor;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Question other = (Question) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public void setVikt(int i) {
		this.vikt = i;
	}

	public boolean ärLåstTillRumOchSession() {
		return ärLåstTillSession() && ärLåstTillRum();
	}

	private boolean ärLåstTillSession() {
		return !låstaSessioner.isEmpty();
	}

	private boolean ärLåstTillRum() {
		return !låstaRum.isEmpty();
	}

	public boolean kanPlacerasISessionNummer(int sessionsNummer) {
		if (!ärLåstTillSession()) {
			return true;
		}
		return låstaSessioner.contains(sessionsNummer);
	}

	public void setIngenSomVillGå(boolean ingenSomVillGå) {
		this.hasZeroAttendants = true;
	}

	public void låsTillSession(List<Integer> låsttill) {
		this.låstaSessioner.addAll(låsttill);
	}

	public List<Integer> getLås() {
		return låstaSessioner;
	}

	public void låsTillRum(List<Integer> låsttill) {
		this.låstaRum.addAll(låsttill);
	}

	public int getLåstSession() {
		// när man ställer den här frågan har man i praktiken låst frågan i både x- och y-led så det finns bara en
		// session
		return låstaSessioner.get(0);
	}

	public Integer getLåstRum() {
		return låstaRum.get(0);
	}

}

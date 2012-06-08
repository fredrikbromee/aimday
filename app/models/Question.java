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
	private List<Integer> låst = new ArrayList<Integer>();

	public Question(String q) {
		this.id = q;
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
			if (null != fragaJson.låst) {
				question.låst.addAll(fragaJson.låst);
			}
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

	private boolean ärLåst() {
		return !låst.isEmpty();
	}

	public boolean kanPlacerasISessionNummer(int sessionsNummer) {
		if (!ärLåst()) {
			return true;
		}
		return låst.contains(sessionsNummer);
	}

}

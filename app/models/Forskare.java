package models;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import se.aimday.scheduler.api.ForskareJson;
import se.aimday.scheduler.api.InconsistentJsonException;

/**
 * En deltagare (forskare) på en konferens
 * 
 * @author fredrikbromee
 * 
 */
public class Forskare {
	
	public List<Question> prio = new ArrayList<Question>();
	private boolean isErfaren = true;
	public String first_name;
	private boolean isJoker;
	private int grad;
	public final String id;
	private boolean harVikt;
	private List<Integer> låstaSessioner = new ArrayList<Integer>();
	private List<String> låstaFrågor = new ArrayList<String>();

	public Forskare(String id, String förnamn, String efternamn, int grad, String gradStr) {
		this.id = id;
		this.first_name = gradStr + " " + förnamn + " " + efternamn;
		this.grad = grad;
		if (grad < 4) {
			isErfaren = true;
		}
	}

	public static Forskare erfaren(String namn) {
		return new Forskare(namn, null, namn, 1, "Hr Dr");
	}

	public static Forskare oerfaren(String namn) {
		return new Forskare(namn, null, namn, 4, "Fräulein");
	}

	public void önskarSe(Question... frågor) {
		prio.addAll(Arrays.asList(frågor));
	}

	public boolean ärErfaren() {
		return isErfaren;
	}

	public List<Question> getÖnskelista() {
		return prio;
	}

	public List<Question> getRandomizedWishlist() {
		List<Question> shuffled = new ArrayList<Question>(prio);

		if (ärLåstTillFrågor()) {
			for (Iterator<Question> iterator = shuffled.iterator(); iterator.hasNext();) {
				Question question = iterator.next();
				if (låstaFrågor.contains(question.id)) {
					iterator.remove();
				}
			}
		}
		Collections.shuffle(shuffled);
		return shuffled;
	}

	public boolean ärLåstTillFrågor() {
		return !låstaFrågor.isEmpty();
	}

	@Override
	public String toString() {
		return "[" + first_name + "]";
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
		Forskare other = (Forskare) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int compareSWO(Forskare other) {
		if (this.isJoker()) {
			return 1;
		}
		if (other.isJoker) {
			return -1;
		}
		return Integer.valueOf(grad).compareTo(other.grad);
	}

	public boolean isJoker() {
		return isJoker;
	}

	public static Map<String, Forskare> fromAPI(List<ForskareJson> json, List<String> senioritetsgrader,
			Map<String, Question> allQuestions)
			throws InconsistentJsonException {
		Map<String, Forskare> forskare = new HashMap<String, Forskare>();
		for (ForskareJson f : json) {
			if (f.grad < 1 || f.grad > senioritetsgrader.size()) {
				String errMsg = String.format(
						"Forskare %s (id:%s) har en grad som är inkorrekt eller ej angiven i senioritetsmappningen."
								+ " Hans grad är %s", f.kontakt.efternamn, f.id, f.grad);
				throw new InconsistentJsonException(errMsg);
			}
			String gradStr = senioritetsgrader.get(f.grad - 1);
			Forskare forskaren = new Forskare(f.id, f.kontakt.fornamn, f.kontakt.efternamn, f.grad, gradStr);
			forskaren.isJoker = f.joker;
			forskaren.harVikt = f.harVikt;

			for (String qId : f.frågor) {
				Question question = allQuestions.get(qId);
				if (question == null) {
					throw new InconsistentJsonException(String.format(
							"Forskare %s är anmäld till en fråga som inte finns! Frågeid: %s", forskaren.first_name,
							qId));

				}
				forskaren.önskarSe(question);
			}
			forskare.put(forskaren.id, forskaren);

		}
		return forskare;
	}

	public void setHarVikt(boolean harVikt) {
		this.harVikt = harVikt;
	}

	public boolean harVikt() {
		return harVikt;
	}

	private boolean ärLåstTillSessioner() {
		return !låstaSessioner.isEmpty();
	}

	public boolean kanPlacerasISessionNummer(int sessionsNummer) {
		if (!ärLåstTillSessioner()) {
			return true;
		}
		return låstaSessioner.contains(sessionsNummer);
	}

	public List<String> getLåstaFrågor() {
		return Collections.unmodifiableList(låstaFrågor);
	}

	public List<Integer> getLåstaSessioner() {
		return Collections.unmodifiableList(låstaSessioner);
	}

	public boolean villGåPå(Question q) {
		return prio.contains(q);
	}

	public void låsTillSessioner(List<Integer> låsttill) {
		låstaSessioner.addAll(låsttill);
	}

	public void låsTillFrågor(List<String> låsttill) {
		låstaFrågor.addAll(låsttill);
	}
}


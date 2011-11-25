package models;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import play.db.jpa.Model;

/**
 * En deltagare (forskare) på en konferens
 * 
 * @author fredrikbromee
 * 
 */
@Entity
public class Participant extends Model {
	
	enum Degree implements Comparable<Degree> {
		PROF("Prof"), DOC("Doc"), DR("Dr"), LIC("Lic");
		private final String deg;

		Degree(String deg) {
			this.deg = deg;
		}

		public static Degree degreeFrom(String prefix) {
			if (PROF.hasDegree(prefix)) {
				return PROF;
			}
			if (DOC.hasDegree(prefix)) {
				return DOC;
			}
			if (DR.hasDegree(prefix)) {
				return DR;
			}
			if (LIC.hasDegree(prefix)) {
				return LIC;
			}
			throw new RuntimeException("Did not recognize degree " + prefix);
		}

		private boolean hasDegree(String prefix) {
			return prefix.toLowerCase().contains(deg.toLowerCase());
		}

	}

	@ManyToMany
	public List<Question> prio = new ArrayList<Question>();
	private boolean is_experienced = true;
	public String first_name;
	private Degree degree;
	public boolean isJoker;

	public Degree getDegree() {
		if (degree == null) {
			degree = Degree.degreeFrom(first_name);
		}
		return degree;
	}

	public Participant(boolean ärErfaren, String namn) {
		this.is_experienced = ärErfaren;
		this.first_name = namn;
		this.degree = Degree.degreeFrom(namn);
	}

	public static Participant erfaren(String namn) {
		return new Participant(true, namn);
	}

	public static Participant oerfaren(String namn) {
		return new Participant(true, namn);
	}

	public void önskarSe(Question... frågor) {
		prio.clear();
		prio.addAll(Arrays.asList(frågor));
	}

	public boolean ärErfaren() {
		return is_experienced;
	}

	public List<Question> getÖnskelista() {
		return prio;
	}

	public List<Question> getRandomizedWishlist() {
		List<Question> shuffled = new ArrayList<Question>(prio);
		Collections.shuffle(shuffled);
		return shuffled;
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
		Participant other = (Participant) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	public int compareSWO(Participant other) {
		if (this.isJoker()) {
			return 1;
		}
		if (other.isJoker) {
			return -1;
		}
		return this.getDegree().compareTo(other.getDegree());
	}

	public boolean isJoker() {
		return isJoker;
	}

}

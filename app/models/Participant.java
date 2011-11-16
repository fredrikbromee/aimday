package models;
import java.util.ArrayList;
import java.util.Arrays;
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
	
	@ManyToMany
	public List<Question> prio = new ArrayList<Question>();
	private boolean is_experienced = true;
	public String first_name;

	public Participant(boolean ärErfaren, String namn) {
		this.is_experienced = ärErfaren;
		this.first_name = namn;
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

}

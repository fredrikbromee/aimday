package models;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.ManyToMany;

import play.db.jpa.Model;

/**
 * En deltagare på en konferens som vill ställa en eller fler frågor
 * 
 * @author fredrikbromee
 * 
 */
@Entity
public class ForetagsRepresentant extends Model {
	
	@ManyToMany
	public List<Question> fragor = new ArrayList<Question>();
	public String namn;

	public ForetagsRepresentant(String namn) {
		this.namn = namn;
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
}

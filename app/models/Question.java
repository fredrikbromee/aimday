package models;

import javax.persistence.Entity;

import play.db.jpa.Model;

/**
 * En diskussionsfråga
 * 
 * @author fredrikbromee
 * 
 */
@Entity
public class Question extends Model {

	public String q;

	public Question(String q) {
		this.q = q;
	}

	public String getQ() {
		return q;
	}

	@Override
	public String toString() {
		return q;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
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
		Question other = (Question) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

}

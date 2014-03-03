package se.aimday.scheduler.api;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * Ett konferensschema har ett antal sessioner
 * 
 * @author fredrikbromee
 * 
 */
public class SchemaJson {
	public List<SessionJson> sessioner;
	public boolean sparat;
	public Collection<String> unplacedQuestions;
	public Collection<String> unsatisfied;

	public void removeRemovedStuff(KonferensJson konf) {
		for (SessionJson s : sessioner) {
			s.removeRemovedStuff(konf);
		}
	}
}

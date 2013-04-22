package se.aimday.scheduler.api;

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

	public void removeRemovedStuff(KonferensJson konf) {
		for (SessionJson s : sessioner) {
			s.removeRemovedStuff(konf);
		}
	}
}

package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.Participant;
import models.Question;

/**
 * En konferensdag består av ett antal sessioner som följer efter varandra
 * 
 * @author fredrikbromee
 * 
 */
public class Session {
	// hashcode och equals

	private List<Workshop> workshops;
	private final int numParalllelTracks;


	public Session(int numParalllelTracks) {
		this.numParalllelTracks = numParalllelTracks;
		workshops = new ArrayList<Workshop>(numParalllelTracks);
	}


	public List<Workshop> getMöten() {
		return workshops;
	}

	public boolean place(Question q, Participant p) {
		// TODO kolla att företag inte deltar i parallellt möte

		if (isAttendedByAnyOf(Collections.singletonList(p))) {
			return false;
		}

		Workshop ws = getWorkshop(q);
		if (ws != null) {
			ws.add(p);
			return true;
		}
		ws = new Workshop(q);
		ws.add(p);

		if (workshops.size() >= numParalllelTracks) {
			return false;
		}

		workshops.add(ws);
		return true;
	}

	public boolean isAttendedByAnyOf(Collection<Participant> deltagare) {
		for (Workshop workshop : workshops) {
			for (Participant maybeThere : deltagare) {
				if (workshop.isAttendedBy(maybeThere)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Session [workshops=" + workshops + "]";
	}

	public int getNumberOfScheduledWS() {
		return workshops.size();
	}

	public Collection<Workshop> getAllWorkshops() {
		return Collections.unmodifiableCollection(workshops);
	}

	public Workshop getWorkshop(Question q) {
		for (Workshop ws : workshops) {
			if (ws.isFor(q)) {
				return ws;
			}
		}
		return null;
	}

}

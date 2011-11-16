package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.Participant;

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

	public boolean place(Workshop workshop) {
		// TODO kolla att företag inte deltar i parallellt möte

		if (isAttendedByAnyOf(workshop.getDeltagare())) {
			return false;
		}
		if (workshops.size() >= numParalllelTracks) {
			return false;
		}

		workshops.add(workshop);
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

}

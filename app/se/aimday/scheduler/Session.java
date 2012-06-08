package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import models.ForetagsRepresentant;
import models.Forskare;
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
	private final int sessionNumber; // Dagens första session har nummer 1 (inte 0)

	public Session(int sessionNumber, int numParallelTracks) {
		this.sessionNumber = sessionNumber;
		this.numParalllelTracks = numParallelTracks;
		workshops = new ArrayList<Workshop>(numParallelTracks);
	}

	public int getSessionNumber() {
		return sessionNumber;
	}

	public List<Workshop> getMöten() {
		return workshops;
	}

	public boolean place(Question q, Forskare p) {
		return place(q, p, Collections.<ForetagsRepresentant>emptyList());
	}

	public boolean place(Question q, Forskare p, Collection<ForetagsRepresentant> lyssnare) {
		if (!q.kanPlacerasISessionNummer(sessionNumber)) {
			return false;
		}
		if (!p.kanPlacerasISessionNummer(sessionNumber)) {
			return false;
		}
		if (isAttendedByAnyOf(Collections.singletonList(p))) {
			return false;
		}

		if (harFrågaStälldAv(lyssnare)) {
			return false;
		}

		Workshop ws = getWorkshop(q);
		if (ws != null) {
			ws.add(p);
			ws.add(lyssnare);
			return true;
		}
		ws = new Workshop(q);
		ws.add(p);
		ws.add(lyssnare);

		if (workshops.size() >= numParalllelTracks) {
			return false;
		}

		workshops.add(ws);
		return true;
	}

	private boolean harFrågaStälldAv(Collection<ForetagsRepresentant> lyssnare) {
		for (Workshop workshop : workshops) {
			for (ForetagsRepresentant kanskeDär : lyssnare) {
				if (workshop.harFrågeStällare(kanskeDär)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAttendedByAnyOf(Collection<Forskare> deltagare) {
		for (Workshop workshop : workshops) {
			for (Forskare maybeThere : deltagare) {
				if (workshop.isAttendedBy(maybeThere)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (Workshop s : workshops) {
			sb.append(s.toString() + ";");
		}
		return sb.toString();
		// return "Session [workshops=" + workshops + "]";
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

	public boolean harFråga(Question fråga) {
		return getWorkshop(fråga) != null;
	}


}

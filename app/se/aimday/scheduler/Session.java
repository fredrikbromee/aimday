package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Question;
import se.aimday.scheduler.api.SessionJson;
import se.aimday.scheduler.api.WorkshopJson;

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

	public boolean place(Question q, Forskare p, int maxDeltagarePerWS) {
		return place(q, p, Collections.<ForetagsRepresentant> emptyList(), maxDeltagarePerWS);
	}

	public boolean place(Question q, Forskare p, Collection<ForetagsRepresentant> lyssnare, int maxDeltagarePerWS) {
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
			if (ws.getAntalDeltagare() >= maxDeltagarePerWS) {
				return false;
			}
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

	public SessionJson toAPI() {
		SessionJson sessionJson = new SessionJson();
		sessionJson.workshops = new ArrayList<WorkshopJson>();
		for (Workshop workshop : workshops) {
			sessionJson.workshops.add(workshop.toAPI());
		}
		return sessionJson;
	}

	public Collection<? extends Question> taBortFrågorMedFörFåDeltagare(int minDeltagarePerWS) {
		Collection<Question> removed = new ArrayList<Question>();
		for (Iterator<Workshop> iterator = workshops.iterator(); iterator.hasNext();) {
			Workshop workshop = iterator.next();
			if (workshop.getAntalForskare() < minDeltagarePerWS) {
				iterator.remove();
				removed.add(workshop.question);
			}
		}
		return removed;
	}

	public void addCompleteWorkshop(Workshop workshop) {
		workshops.add(workshop);
	}

}

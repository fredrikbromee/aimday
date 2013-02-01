package se.aimday.scheduler;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

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

	// Första rummet i en session har nummer 1
	private Map<Integer, Workshop> frågorPerRum = new TreeMap<Integer, Workshop>();
	private final int numParalllelTracks;
	private final int sessionNumber; // Dagens första session har nummer 1 (inte 0)

	public Session(int sessionNumber, int numParallelTracks) {
		this.sessionNumber = sessionNumber;
		this.numParalllelTracks = numParallelTracks;
	}

	public int getSessionNumber() {
		return sessionNumber;
	}

	public Collection<Workshop> getMöten() {
		return frågorPerRum.values();
	}

	public boolean place(Question q, Forskare p, int maxDeltagarePerWS) {
		return place(q, p, Collections.<ForetagsRepresentant> emptyList(), maxDeltagarePerWS);
	}

	public void placeraLåstFråga(Question question, Collection<ForetagsRepresentant> lyssnare) {
		Workshop ws = new Workshop(question);
		ws.add(lyssnare);
		frågorPerRum.put(question.getLåstRum(), ws);
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

		if (frågorPerRum.size() >= numParalllelTracks) {
			return false;
		}

		frågorPerRum.put(förstaLedigaRum(), ws);
		return true;
	}

	private Integer förstaLedigaRum() {
		for (int i = 1; i <= numParalllelTracks; i++) {
			if (!frågorPerRum.containsKey(i)) {
				return Integer.valueOf(i);
			}
		}
		throw new RuntimeException("hittade inget ledigt rum i sessionen: " + toString());
	}

	private boolean harFrågaStälldAv(Collection<ForetagsRepresentant> lyssnare) {
		for (Workshop workshop : frågorPerRum.values()) {
			for (ForetagsRepresentant kanskeDär : lyssnare) {
				if (workshop.harFrågeStällare(kanskeDär)) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isAttendedByAnyOf(Collection<Forskare> deltagare) {
		for (Workshop workshop : frågorPerRum.values()) {
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
		for (Workshop s : frågorPerRum.values()) {
			sb.append(s.toString() + ";");
		}
		return sb.toString();
	}

	public int getNumberOfScheduledWS() {
		return frågorPerRum.size();
	}

	public Collection<Workshop> getAllWorkshops() {
		return Collections.unmodifiableCollection(frågorPerRum.values());
	}

	public Workshop getWorkshop(Question q) {
		for (Workshop ws : frågorPerRum.values()) {
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
		for (Workshop workshop : frågorPerRum.values()) {
			sessionJson.workshops.add(workshop.toAPI());
		}
		return sessionJson;
	}

	public Collection<? extends Question> taBortFrågorMedFörFåDeltagare(int minDeltagarePerWS) {
		Set<Entry<Integer, Workshop>> entrySet = frågorPerRum.entrySet();
		Collection<Question> removed = new ArrayList<Question>();
		TreeMap<Integer, Workshop> filledWorkshops = new TreeMap<Integer, Workshop>();
		for (Entry<Integer, Workshop> entry : entrySet) {
			Workshop workshop = entry.getValue();
			if (workshop.getAntalForskare() >= minDeltagarePerWS) {
				filledWorkshops.put(entry.getKey(), entry.getValue());
			} else {
				removed.add(workshop.question);
			}

		}
		frågorPerRum = filledWorkshops;
		return removed;
	}

	public void addCompleteWorkshop(Workshop workshop) {
		frågorPerRum.put(förstaLedigaRum(), workshop);
	}

}

package se.aimday.scheduler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import models.Forskare;
import models.Question;
import play.Logger;

public class IndividualAgenda {

	private final Forskare participant;
	private final Map<Session, Workshop> schema = new HashMap();
	private double score;
	public Set<String> errors = new HashSet<String>();

	public double getScore() {
		return score;
	}

	public IndividualAgenda(Forskare deltagare) {
		this.participant = deltagare;
	}

	@Override
	public String toString() {
		return "IndividualAgenda [participant=" + participant + ", schema=" + schema + ", score=" + score + "]";
	}

	public void läggTill(Workshop möte, Session session) {
		if (harMöte(session)) {
			String error = String.format("%s har mer än ett möte session %s!", participant.first_name,
					session.getSessionNumber(), möte);
			errors.add(error);
			Logger.warn(error);
		}
		schema.put(session, möte);
	}

	private boolean harMöte(Session session) {
		return schema.containsKey(session);
	}

	public int antalMöten() {
		return schema.size();
	}

	public void setScore(double agendaScore) {
		this.score = agendaScore;
	}

	public double score(int numSessions) {
		if (participant.isJoker()) {
			return 1; // alltid nöjd!
		}

		return new Scorer(this).score(participant.getÖnskelista(), numSessions);
	}
	public Forskare getParticipant() {
		return participant;
	}

	public boolean isSatisfied() {
		return score >= 1;
	}

	public int getWishedNumber() {
		return participant.getÖnskelista().size();
	}

	public int getAssignedNumber() {
		return schema.size();
	}

	public String getAgenda() {
		StringBuilder sb = new StringBuilder();
		for (Question q : participant.getÖnskelista()) {
			sb.append("Q" + q.id);
			if (isPlacedIn(q)) {
				sb.append("(S" + vilkenSession(q) + ")");
			}
			sb.append(' ');
		}
		return sb.toString();
	}

	private int vilkenSession(Question q) {
		Set<Entry<Session, Workshop>> entrySet = schema.entrySet();
		for (Entry<Session, Workshop> entry : entrySet) {
			if (entry.getValue().isFor(q)) {
				return entry.getKey().getSessionNumber();
			}
		}
		return 0;
	}

	boolean isPlacedIn(Question q) {
		return vilkenSession(q) > 0;
	}
}

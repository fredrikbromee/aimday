package se.aimday.scheduler;
import java.util.HashMap;
import java.util.Map;

import models.Participant;


public class IndividualAgenda {

	private final Participant participant;
	private final Map<Session, Workshop> schema = new HashMap();
	private double score;

	public double getScore() {
		return score;
	}

	public IndividualAgenda(Participant deltagare) {
		this.participant = deltagare;
	}

	@Override
	public String toString() {
		return "IndividualAgenda [participant=" + participant + ", schema=" + schema + ", score=" + score + "]";
	}

	public void läggTill(Workshop möte, Session session) {
		if (harMöte(session)) {
			throw new RuntimeException(String.format("Har redan ett möte denna session! Session %s  \n försöker lägga till %s", session,
					möte));
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
		int antalMöten = antalMöten();
		int max = Math.min(numSessions, participant.getÖnskelista().size());
		if (antalMöten >= max) {
			return 1;
		}
		return antalMöten() / max;
	}

	public Participant getParticipant() {
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
}

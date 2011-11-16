package se.aimday.scheduler;
import java.util.HashMap;
import java.util.Map;

import models.Participant;


public class IndividualAgenda {

	private final Participant deltagare;
	private final Map<Session, Workshop> schema = new HashMap();

	public IndividualAgenda(Participant deltagare) {
		this.deltagare = deltagare;
	}

	public void läggTill(Workshop möte, Session session) {
		if (harMöte(session)) {
			throw new RuntimeException("Har redan ett möte denna session!");
		}
		schema.put(session, möte);
	}

	private boolean harMöte(Session session) {
		return schema.containsKey(session);
	}

	public int antalMöten() {
		return schema.size();
	}

}

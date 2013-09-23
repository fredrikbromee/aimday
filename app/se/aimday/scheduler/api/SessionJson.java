package se.aimday.scheduler.api;

import java.util.Iterator;
import java.util.List;

import play.Logger;

/**
 * En session är en samling workshops som händer parallellt i tiden
 * 
 * @author fredrikbromee
 * 
 */
public class SessionJson {
	public List<WorkshopJson> workshops;

	public void removeRemovedStuff(KonferensJson konf) {
		if (workshops == null)
			return;

		for (Iterator<WorkshopJson> iterator = workshops.iterator(); iterator.hasNext();) {
			WorkshopJson ws = iterator.next();

			if (!konf.harFrågaMedId(ws.frageId)) {
				// om inte frågan finns kvar, ta bort hela workshopen
				Logger.warn("removed q %s from schedule", ws.frageId);
				iterator.remove();
			} else {
				// för varje forskare ta bort om ej finns kvar
				for (Iterator<String> forskarIterator = ws.forskare.iterator(); forskarIterator.hasNext();) {
					String forskarId = forskarIterator.next();
					if (!konf.harForskareMedId(forskarId)) {
						Logger.warn("removed scientist %s from q %s", forskarId, ws.frageId);
						forskarIterator.remove();
					}
				}
				// för varje företagsrep ta bort om ej finns kvar
				if (ws.foretagsrepresentanter != null) {
					for (Iterator<String> repsIterator = ws.foretagsrepresentanter.iterator(); repsIterator.hasNext();) {
						String repId = repsIterator.next();
						if (!konf.harRepMedId(repId)) {
							Logger.warn("removed rep %s from q %s", repId, ws.frageId);
							repsIterator.remove();
						}
					}
				}
			}
		}
	}
}

package se.aimday.scheduler.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import controllers.ScheduleRequest;

/**
 * Konferens -id/namn -lista med frågor -lista med forskare -lista med företagsrepresentanter -schema där alla är
 * utplacerade (första gången man anropar optimeraren är denna tom, tanken är att arrangörerna ska kunna ändra i schemat
 * flera gånger)
 * 
 * En fråga innehåller ett id och själva frågan En forskare har id, grad som nummer (1), grad som text (1=Prof), nåt
 * slags namn (alt förnamn och efternamn) , prioriterad önskelista, en boolean om man är joker (som man får placera
 * överallt). Joker har jag nog inte pratat om, kanske? Det är inte ett fält som forskaren anger utan arrangören väljer
 * om en forskare kan vara joker. En företagsrepresentant har id, namn och vilka frågor han vill vara med på. Den listan
 * är inte ordnad.
 * 
 * @author fredrikbromee
 * 
 */
public class KonferensJson {
	public Integer placeWeight;
	public Integer wsWeight;
	public Integer agendaWeight;
	public String id;
	public String postback_url;
	public String namn;
	public OptimeringsInformation optimeringsInformation;
	public List<String> senioritetsgrader;
	public List<FragaJson> fragor;
	public List<ForetagsRepresentantJson> foretagsrepresentanter;
	public List<ForskareJson> forskare;
	public SchemaJson schema;
	public ScheduleRequest scheduleRequest;
	public Lasningar låsningar;

	public void förkortaSenioritetsGrader() {
		this.senioritetsgrader = förkorta(senioritetsgrader);
	}

	private List<String> förkorta(List<String> senioritetsgrader) {
		List<String> listaMedFörkortningar = new ArrayList<String>();
		for (String grad : senioritetsgrader) {
			String förkortadGrad = förkorta(grad);
			listaMedFörkortningar.add(förkortadGrad);
		}
		return listaMedFörkortningar;
	}

	private static HashMap<String, String> förkortningar = new HashMap<String, String>();
	static {
		förkortningar.put("Doctor", "Dr.");
		förkortningar.put("Professor", "Prof.");
		förkortningar.put("Docent", "Doc.");
		förkortningar.put("PhD student year 4-5", "PhD");
		förkortningar.put("PhD student year 1-3", "phd");
	}

	private String förkorta(String grad) {
		String förkortning = förkortningar.get(grad.trim());
		if (förkortning != null) {
			return förkortning;
		}
		return grad;
	}

}

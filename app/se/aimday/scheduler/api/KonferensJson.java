package se.aimday.scheduler.api;

import java.util.List;

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
	public String id;
	public String namn;
	public OptimeringsInformation optimeringsInformation;
	public List<String> senioritetsgrader;
	public List<FragaJson> frågor;
	public List<ForetagsRepresentantJson> företagsrepresentanter;
	public List<ForskareJson> forskare;
	public SchemaJson schema;

	// Kvar: hur markera om forskare är klinisk? Hur markera om en fråga bör ha kliniska forskare med?
}

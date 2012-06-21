package se.aimday.scheduler.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.google.gson.Gson;

public class ApiTest {

	@Test
	public void testPrintJson() {
		KonferensJson konferens = new KonferensJson();
		konferens.id = "1";
		konferens.namn = "Aimday life science";
		konferens.företagsrepresentanter = enFöretagsRep();
		konferens.forskare = treForskare();
		konferens.fragor = fyraFrågor();
		konferens.senioritetsgrader = Arrays.asList("Överl.", "Prof.", "Doc.", "Dr.", "Doktorand");
		konferens.schema = schemaMedFyraFrågor();
		OptimeringsInformation optimeringsInformation = new OptimeringsInformation();
		optimeringsInformation.användVikter = true;
		optimeringsInformation.viktNamn = "Har klinisk erfarenhet";
		konferens.optimeringsInformation = optimeringsInformation;

		String json = new Gson().toJson(konferens);
		System.out.println(json);
	}

	private List<ForetagsRepresentantJson> enFöretagsRep() {
		List<ForetagsRepresentantJson> reps = new ArrayList<ForetagsRepresentantJson>();
		ForetagsRepresentantJson rep1 = new ForetagsRepresentantJson();
		rep1.id = "rep1";
		List<String> rep1fragor = Arrays.asList("f1", "f2");
		rep1.frågor = rep1fragor;
		rep1.kontakt = new KontaktJson("Arne", "Företagskontakt", null, "Ett företag");
		reps.add(rep1);

		return reps;
	}

	private List<ForskareJson> treForskare() {
		List<ForskareJson> forskarna = new ArrayList<ForskareJson>();
		ForskareJson forskare = new ForskareJson(2, "id1", true, true);
		forskare.kontakt = new KontaktJson("Arne", "Arnesson", "Institutionen för materialfysik",
				"Uppsala universitet");
		forskare.frågor = Arrays.asList("f1", "f2");
		forskarna.add(forskare);

		ForskareJson forskare2 = new ForskareJson(1, "id2", false);
		forskare2.kontakt = new KontaktJson("Bjarne", "Bengtsson", "Institutionen för neurologi", "Uppsala universitet");
		forskare2.frågor = Arrays.asList("f2", "f3");
		forskarna.add(forskare2);

		ForskareJson forskare3 = new ForskareJson(3, "id3", false);
		forskare3.frågor = Arrays.asList("f4");
		forskare3.kontakt = new KontaktJson("Carl", "Carlsson", "Institutionen för materialfysik", "Lunds universitet");
		forskarna.add(forskare3);

		return forskarna;
	}

	private List<FragaJson> fyraFrågor() {
		List<FragaJson> frågor = new ArrayList<FragaJson>();
		frågor.add(new FragaJson("f1", "What is the cure for the common cold?"));
		frågor.get(0).vikt = 5;
		frågor.add(new FragaJson("f2", "What is the cure for the common cancer?"));
		frågor.add(new FragaJson("f3", "What is the cure for the common arne?"));
		frågor.add(new FragaJson("f4", "What is the cure for the common bjarne?"));
		return frågor;
	}

	private SchemaJson schemaMedFyraFrågor() {
		SchemaJson schema = new SchemaJson();
		List<SessionJson> sessioner = new ArrayList<SessionJson>();

		SessionJson session1 = new SessionJson();
		session1.workshops = new ArrayList<WorkshopJson>();
		WorkshopJson workshopF1 = new WorkshopJson("f1", "id1");
		workshopF1.foretagsrepresentanter = Arrays.asList("rep1");
		session1.workshops.add(workshopF1);
		session1.workshops.add(new WorkshopJson("f2", "id1"));
		
		SessionJson session2 = new SessionJson();
		session2.workshops = new ArrayList<WorkshopJson>();
		session2.workshops.add(new WorkshopJson("f3", "id2"));
		session2.workshops.add(new WorkshopJson("f4", "id3"));
		
		sessioner.add(session1);
		sessioner.add(session2);
		schema.sessioner = sessioner;
		return schema;
	}
}



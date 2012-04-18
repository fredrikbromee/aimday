import java.util.Arrays;
import java.util.List;

import models.ForetagsRepresentant;
import models.Participant;
import models.Question;

import org.junit.Assert;
import org.junit.Test;

import se.aimday.scheduler.AIMDay;
import se.aimday.scheduler.Scheduler;
import se.aimday.scheduler.Session;

public class SchemaläggarTest {

	private static final Participant ARNE = Participant.erfaren("Prof Arne");
	private static final Participant BJARNE = Participant.erfaren("Prof Bjarne");
	private static final Participant CHRISTER = Participant.erfaren("Prof Christer");
	private static final Participant DAVID = Participant.erfaren("Prof David");
	private Scheduler läggare;
	Question fråga1 = new Question("1?");
	Question fråga2 = new Question("2?");
	Question fråga3 = new Question("3?");
	Question fråga4 = new Question("4?");

	@Test
	public void testaEnkeltSchema() {
		// Givet att alla vill vara med på allt
		ARNE.önskarSe(fråga2);
		BJARNE.önskarSe(fråga1, fråga2);
		CHRISTER.önskarSe(fråga1, fråga2);
		DAVID.önskarSe(fråga1);

		läggare = new Scheduler.Byggare(1).mednumSessions(2).medMaxAntalDeltagarePerMöte(3)
				.medDeltagare(ARNE, BJARNE, CHRISTER, DAVID).medFrågor(fråga1, fråga2).bygg();

		se.aimday.scheduler.AIMDay bästSchema = läggare.lägg();
		System.out.println("Valt schema:\n" + bästSchema);

		// placera ut resten av deltagarna också så skare bli ett
		Assert.assertEquals(1, bästSchema.getScore(), 0.05);
	}

	@Test
	public void testaSchemaMedTvåParallellaSessioner() {
		// Givet att alla vill vara med på nästan allt
		ARNE.önskarSe(fråga1, fråga2, fråga3, fråga4);
		BJARNE.önskarSe(fråga1, fråga2, fråga3, fråga4);
		CHRISTER.önskarSe(fråga1, fråga2, fråga3, fråga4);
		DAVID.önskarSe(fråga1, fråga2, fråga3, fråga4);

		ForetagsRepresentant företag1 = new ForetagsRepresentant("företag 1");
		företag1.önskarSe(fråga1);
		företag1.önskarSe(fråga2);
		ForetagsRepresentant företag2 = new ForetagsRepresentant("företag 2");
		företag2.önskarSe(fråga3);
		företag2.önskarSe(fråga4);
		List<ForetagsRepresentant> lyssnare = Arrays.asList(new ForetagsRepresentant[] { företag1, företag2 });

		läggare = new Scheduler.Byggare(2).mednumSessions(2).medMaxAntalDeltagarePerMöte(3)
				.medDeltagare(ARNE, BJARNE, CHRISTER, DAVID).medFrågor(fråga1, fråga2, fråga3, fråga4)
				.medDeltagare(lyssnare).bygg();

		AIMDay bästSchema = läggare.lägg();
		Session sessionMedFråga1 = bästSchema.getSessionFor(fråga1);
		Assert.assertTrue("Två frågor från samma företag i samma session!", sessionMedFråga1.harFråga(fråga3)
				|| sessionMedFråga1.harFråga(fråga4));
		Session sessionMedFråga2 = bästSchema.getSessionFor(fråga2);
		Assert.assertTrue("Två frågor från samma företag i samma session!", sessionMedFråga2.harFråga(fråga3)
				|| sessionMedFråga2.harFråga(fråga4));

		System.out.println("Valt schema:\n" + bästSchema);
		Assert.assertTrue(bästSchema.getScore() == 1);
	}
}

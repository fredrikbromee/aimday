import models.Participant;
import models.Question;

import org.junit.Assert;
import org.junit.Test;

import se.aimday.scheduler.Scheduler;

public class SchemaläggarTest {

	private static final Participant ARNE = Participant.erfaren("Arne");
	private static final Participant BJARNE = Participant.erfaren("Bjarne");
	private static final Participant CHRISTER = Participant.erfaren("Christer");
	private static final Participant DAVID = Participant.erfaren("David");
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

		läggare = new Scheduler.Byggare(1).mednumSessions(2).medMaxAntalDeltagarePerMöte(3).medDeltagare(ARNE, BJARNE, CHRISTER, DAVID)
				.medFrågor(fråga1, fråga2).bygg();

		se.aimday.scheduler.AIMDay bästSchema = läggare.lägg();
		System.out.println("Valt schema:\n" + bästSchema);

		// placera ut resten av deltagarna också så skare bli ett
		Assert.assertEquals(1, bästSchema.getScore(), 0.05);
	}

	@Test
	public void testaSchemaMedTvåParallellaSessioner() {
		// Givet att alla vill vara med på nästan allt
		ARNE.önskarSe(fråga1, fråga2, fråga3, fråga4);
		BJARNE.önskarSe(fråga2, fråga3, fråga4);
		CHRISTER.önskarSe(fråga1, fråga2, fråga3, fråga4);
		DAVID.önskarSe(fråga1, fråga3, fråga4);

		läggare = new Scheduler.Byggare(2).mednumSessions(2).medMaxAntalDeltagarePerMöte(3).medDeltagare(ARNE, BJARNE, CHRISTER, DAVID)
				.medFrågor(fråga1, fråga2, fråga3, fråga4).bygg();

		se.aimday.scheduler.AIMDay bästSchema = läggare.lägg();

		System.out.println("Valt schema:\n" + bästSchema);
		Assert.assertTrue(bästSchema.getScore() == 1);
	}
}

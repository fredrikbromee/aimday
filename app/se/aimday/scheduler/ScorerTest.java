package se.aimday.scheduler;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;
import models.Question;

import org.junit.Test;

public class ScorerTest {

	@Test
	public void testShortPrio() {
		List<Question> prio = Arrays.asList(new Question("ett"), new Question("två"), new Question("tre"),
				new Question("fyra"));
		MockAgenda agenda = new MockAgenda().placeIn(prio.get(0));
		Scorer scorer = new Scorer(agenda);

		Assert.assertEquals(1.0, scorer.score(prio, 1));
		Assert.assertEquals(0.7, scorer.score(prio, 2));
		agenda = agenda.placeIn(prio.get(1));
		Assert.assertEquals(1.0, scorer.score(prio, 2));
		
	}

	@Test
	public void testTypicalPrio() {
		List<Question> prio = Arrays.asList(new Question("ett"), new Question("två"), new Question("tre"),
				new Question("fyra"));
		MockAgenda agenda = new MockAgenda().placeIn(prio.get(2));
		Scorer scorer = new Scorer(agenda);

		Assert.assertEquals(0.6, scorer.score(prio, 1));
		Assert.assertEquals(0.1, scorer.score(prio, 2)); // less happy if there were more sessions
		agenda = agenda.placeIn(prio.get(1));
		Assert.assertEquals(0.5, scorer.score(prio, 4));
		agenda = agenda.placeIn(prio.get(0));
		agenda = agenda.placeIn(prio.get(3));
		Assert.assertEquals(1.0, scorer.score(prio, 4), 0.01);
		Assert.assertEquals(1.0, scorer.score(prio, 10), 0.01);
	}

	@Test
	public void testLowNumberOfQuestionsPrio() {
		List<Question> prio = Arrays.asList(new Question("ett"));
		MockAgenda agenda = new MockAgenda().placeIn(prio.get(0));
		Scorer scorer = new Scorer(agenda);

		Assert.assertEquals(1.0, scorer.score(prio, 1));
		Assert.assertEquals(1.0, scorer.score(prio, 2));
		Assert.assertEquals(1.0, scorer.score(prio, 3));
		Assert.assertEquals(1.0, scorer.score(prio, 4));
		Assert.assertEquals(1.0, scorer.score(prio, 5));

	}

	class MockAgenda extends IndividualAgenda {

		Set<Question> qs = new LinkedHashSet<Question>();

		public MockAgenda() {
			super(null);
		}

		public MockAgenda placeIn(Question question) {
			qs.add(question);
			return this;
		}

		@Override
		boolean isPlacedIn(Question q) {
			return qs.contains(q);
		}

		@Override
		public int antalMöten() {
			return qs.size();
		}

	}

}

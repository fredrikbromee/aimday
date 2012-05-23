package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import models.ForetagsRepresentant;
import models.Forskare;
import models.Question;
import play.mvc.Controller;
import se.aimday.scheduler.AIMDay;
import se.aimday.scheduler.Scheduler;
import se.aimday.scheduler.api.InconsistentJsonException;
import se.aimday.scheduler.api.KonferensJson;

import com.google.gson.Gson;

/**
 * titta på priolista i ordning (som en del av ranking)
 * 
 * @author fredrikbromee
 * 
 */
public class Application extends Controller {

	public static void index() {
		render();
    }

	public static void form() {
		render();
	}

	public static void schedule(int tracks, int sessions, int generations, String json) {

		List<Forskare> allParticipants = null;
		List<ForetagsRepresentant> foretagare = null;
		Map<String, Question> allQuestions = null;
		try {
			KonferensJson konf = new Gson().fromJson(json, KonferensJson.class);
			foretagare = ForetagsRepresentant.fromAPI(konf.företagsrepresentanter);
			allQuestions = Question.fromAPI(konf.frågor);
			allParticipants = Forskare.fromAPI(konf.forskare, konf.senioritetsgrader, allQuestions);
		} catch (InconsistentJsonException e) {
			error(e.getMessage());
		}


		generations = Math.min(100000, generations);
		System.out.println("num gs" + generations);
		Scheduler scheduler = new Scheduler(tracks, sessions, 10, allQuestions.values(), allParticipants, null,
				foretagare, generations);
		AIMDay schedule = scheduler.lägg();
		ArrayList<Integer> spår = new ArrayList<Integer>();
		for (int i = 1; i <= tracks; i++) {
			spår.add(i);
		}

		render(schedule, spår);
	}
}
package controllers;

import java.util.ArrayList;

import models.Konferens;
import play.mvc.Controller;
import se.aimday.scheduler.AIMDay;
import se.aimday.scheduler.Scheduler;
import se.aimday.scheduler.api.InconsistentJsonException;
import se.aimday.scheduler.api.KonferensJson;

import com.google.gson.Gson;

/**
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

	public static void form2() {
		render();
	}

	public static void scheduleAPI(String json) {
		try {
			KonferensJson konf = new Gson().fromJson(json, KonferensJson.class);

			// Prova att parsa en gång så att vi är säkra på att vi gillar formatet
			Konferens.fromAPI(konf);
		} catch (InconsistentJsonException e) {
			error(e.getMessage());
		}
		renderTemplate("Application/schedule.html", json);
	}

	public static void schedule(int tracks, int sessions, int generations, String json, int placeWeight, int wsWeight,
			int agendaWeight, int max_antal_deltagare, int min_antal_deltagare) {

		if (placeWeight < 0) {
			placeWeight = 10;
		}
		if (wsWeight < 0) {
			wsWeight = 10;
		}
		if (agendaWeight < 0) {
			agendaWeight = 10;
		}

		if ((max_antal_deltagare > 15 || max_antal_deltagare < 0) && max_antal_deltagare != 100) {
			throw new RuntimeException("Felaktigt antal max deltagare, fick " + max_antal_deltagare);
		}
		if (min_antal_deltagare > 15 || min_antal_deltagare < 0) {
			throw new RuntimeException("Felaktigt minimi-antal  deltagare, fick " + min_antal_deltagare);
		}


		Konferens k = null;
		KonferensJson konf = null;
		Gson gson = new Gson();
		try {
			konf = gson.fromJson(json, KonferensJson.class);
			k = Konferens.fromAPI(konf);
		} catch (InconsistentJsonException e) {
			error(e.getMessage());
		}


		generations = Math.min(100000, generations);
		ScheduleRequest scheduleRequest = new ScheduleRequest(tracks, sessions, generations, placeWeight, wsWeight,
				agendaWeight, max_antal_deltagare, min_antal_deltagare);
		System.out.println("num gs" + generations);
		Scheduler scheduler = new Scheduler(k, scheduleRequest);
		AIMDay schedule = scheduler.lägg();
		ArrayList<Integer> spår = new ArrayList<Integer>();
		for (int i = 1; i <= schedule.getMaxNumOfWorkshopsInASession(); i++) {
			spår.add(i);
		}
		konf.schema = schedule.toAPI();
		json = gson.toJson(konf);

		render(schedule, spår, json);
	}
}
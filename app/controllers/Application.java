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

	public static void scheduleAPI(String json) {
		try {
			KonferensJson konf = new Gson().fromJson(json, KonferensJson.class);

			// Prova att parsa en gång så att vi är säkra på att vi gillar formatet
			Konferens.fromAPI(konf);
		} catch (InconsistentJsonException e) {
			error(e.getMessage());
		}
		renderTemplate("schedule", json);
	}

	public static void schedule(int tracks, int sessions, int generations, String json, int placeWeight, int wsWeight,
			int agendaWeight) {

		if (placeWeight < 0) {
			placeWeight = 10;
		}
		if (wsWeight < 0) {
			wsWeight = 10;
		}
		if (agendaWeight < 0) {
			agendaWeight = 10;
		}


		Konferens k = null;
		try {
			KonferensJson konf = new Gson().fromJson(json, KonferensJson.class);
			k = Konferens.fromAPI(konf);
		} catch (InconsistentJsonException e) {
			error(e.getMessage());
		}


		generations = Math.min(100000, generations);
		System.out.println("num gs" + generations);
		Scheduler scheduler = new Scheduler(tracks, sessions, 10, k, generations, placeWeight, wsWeight, agendaWeight);
		AIMDay schedule = scheduler.lägg();
		ArrayList<Integer> spår = new ArrayList<Integer>();
		for (int i = 1; i <= tracks; i++) {
			spår.add(i);
		}

		render(schedule, spår, json);
	}
}
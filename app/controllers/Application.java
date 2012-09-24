package controllers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import models.Konferens;
import play.Logger;
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
		Logger.info("params:" + params.toString());

		Map<String, String> allSimple = params.allSimple();
		for (String key : allSimple.keySet()) {
			Logger.info("%s : %s", key, allSimple.get(key));
		}

		Logger.info("----------------------------");
		for (String key : request.headers.keySet()) {
			Logger.info("%s : %s", key, request.headers.get(key));
		}
		Logger.info("qs:" + request.querystring);

		KonferensJson konf = null;
		AIMDay schedule = null;
		try {
			Gson gson = new Gson();
			konf = gson.fromJson(json, KonferensJson.class);

			// Prova att parsa en gång så att vi är säkra på att vi gillar formatet
			Konferens k = Konferens.fromAPI(konf);

			if (konf.schema != null && konf.schema.sessioner != null) {
				schedule = AIMDay.fromJson(konf.schema, k);
			}
			json = gson.toJson(konf);
		} catch (InconsistentJsonException e) {
			String felMeddelande = e.getMessage();
			System.out.println(felMeddelande);
			renderTemplate("Application/fel.html", felMeddelande);
		}
		String postback_url = getPostBackURL(konf);

		
		if (schedule != null) {
			schedule.scoreIndividualAgendas();
			Collection<String> fel = schedule.getAllScheduleErrors();
			boolean hittadeFel = !fel.isEmpty();
			if (hittadeFel) {
				renderTemplate("Application/schedule.html", json, postback_url, hittadeFel, fel);
			}

			ArrayList<Integer> spår = getSpårArray(schedule);
			boolean sparatSchema = true;
			renderTemplate("Application/schedule.html", schedule, spår, json, postback_url, sparatSchema);
		}
		
		renderTemplate("Application/schedule.html", json, postback_url);
	}

	private static String getPostBackURL(KonferensJson konf) {
		if (konf.postback_url != null) {
			return konf.postback_url;
		}
		return "http://aimdaylabb.se.preview.binero.se/materials/parse-json/";
	}

	public static void reSchedule(String json) {
		Konferens k = null;
		KonferensJson konf = null;
		Gson gson = new Gson();
		AIMDay befintligtSchema = null;
		try {
			konf = gson.fromJson(json, KonferensJson.class);
			k = Konferens.fromAPI(konf);
			if (konf.schema == null){
				renderText("Finns inget befintligt schema att lägga till i!");
			}
			befintligtSchema = AIMDay.fromJson(konf.schema, k);
		} catch (InconsistentJsonException e) {
			String felMeddelande = e.getMessage();
			System.out.println(felMeddelande);
			renderTemplate("Application/fel.html", felMeddelande);
		}


		Scheduler scheduler = new Scheduler(k, befintligtSchema);
		AIMDay schedule = scheduler.läggInIBefintligtSchema();
		ArrayList<Integer> spår = getSpårArray(schedule);
		konf.schema = schedule.toAPI();
		json = gson.toJson(konf);
		String postback_url = getPostBackURL(konf);

		renderTemplate("Application/schedule.html", schedule, spår, json, postback_url);
	}

	private static ArrayList<Integer> getSpårArray(AIMDay schedule) {
		ArrayList<Integer> spår = new ArrayList<Integer>();
		for (int i = 1; i <= schedule.getMaxNumOfWorkshopsInASession(); i++) {
			spår.add(i);
		}
		return spår;
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
			String felMeddelande = e.getMessage();
			System.out.println(felMeddelande);
			renderTemplate("Application/fel.html", felMeddelande);
		}


		generations = Math.min(100000, generations);
		ScheduleRequest scheduleRequest = new ScheduleRequest(tracks, sessions, generations, placeWeight, wsWeight,
				agendaWeight, max_antal_deltagare, min_antal_deltagare);
		Scheduler scheduler = new Scheduler(k, scheduleRequest);
		AIMDay schedule = scheduler.lägg();
		ArrayList<Integer> spår = getSpårArray(schedule);
		konf.schema = schedule.toAPI();
		konf.scheduleRequest = scheduleRequest;
		json = gson.toJson(konf);
		String postback_url = getPostBackURL(konf);

		render(schedule, spår, json, postback_url);
	}
}
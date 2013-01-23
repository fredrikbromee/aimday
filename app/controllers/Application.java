package controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;

import models.Konferens;
import play.Logger;
import play.cache.Cache;
import play.jobs.Job;
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

	public static class RequestAndScheduleTuple implements Serializable {

		public final AIMDay schedule;
		public final ScheduleRequest scheduleRequest;
		public final String json;

		public RequestAndScheduleTuple(AIMDay schedule, ScheduleRequest scheduleRequest, String json) {
			this.schedule = schedule;
			this.scheduleRequest = scheduleRequest;
			this.json = json;
		}

	}

	public static class AimdayJob extends Job {

		@Override
		public void doJob() throws Exception {
			AtomicInteger progress = new AtomicInteger();
			Cache.set(id, progress, "10min");
			AIMDay schedule = scheduler.lägg(progress);

			RequestAndScheduleTuple tuple = new RequestAndScheduleTuple(schedule, scheduleRequest, json);
			Logger.info("Caching new schedule with id %s", id);
			Cache.set(id, tuple, "1h");

		}

		private final Scheduler scheduler;
		private final String id;
		private final ScheduleRequest scheduleRequest;
		private final String json;

		public AimdayJob(Scheduler scheduler, String id, ScheduleRequest scheduleRequest, String json) {
			this.scheduler = scheduler;
			this.id = id;
			this.scheduleRequest = scheduleRequest;
			this.json = json;

		}

	}

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

	@Deprecated
	// TODO ta bort när vi har knappen för lås alla
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

	public static void scheduleNew(int tracks, int sessions, int generations, String json, int placeWeight,
			int wsWeight,
			int agendaWeight, int max_antal_deltagare, int min_antal_deltagare) throws InterruptedException,
			ExecutionException {

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

		String randomId = UUID.randomUUID().toString();

		new AimdayJob(scheduler, randomId, scheduleRequest, json).now();
		render(randomId);
	}

	public static void progress(String id) {
		Object object = Cache.get(id.trim());

		if (object != null && object instanceof AtomicInteger) {
			int progress = ((AtomicInteger) object).get();
			renderJSON(progress);
		}
		if (object != null && object instanceof RequestAndScheduleTuple) {
			renderJSON(100);
		}
		renderJSON(-1);
	}

	public static void schedule(String id) {
		Object object = Cache.get(id.trim());
		if (object == null) {
			renderText("Ledsen, hittade inte det här schemat i cachen.");
		}
		if (object instanceof AtomicInteger) {
			String randomId = id;
			int progress = ((AtomicInteger) object).get();
			renderTemplate("Application/scheduleNew.html", progress, randomId);
		}

		RequestAndScheduleTuple tuple = (RequestAndScheduleTuple) object;
		Gson gson = new Gson();
		KonferensJson konf = gson.fromJson(tuple.json, KonferensJson.class);
		konf.schema = tuple.schedule.toAPI();
		konf.scheduleRequest = tuple.scheduleRequest;
		String postback_url = getPostBackURL(konf);
		AIMDay schedule = tuple.schedule;
		ArrayList<Integer> spår = getSpårArray(schedule);
		String json = gson.toJson(konf);
		render(schedule, spår, json, postback_url);
	}
}
package controllers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
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
import se.aimday.scheduler.api.ScheduleRequest;

import com.google.gson.Gson;

/**
 * 
 * @author fredrikbromee
 * 
 */
public class Application extends Controller {

	public static class RequestAndScheduleTuple implements Serializable {

		public final AIMDay schedule;
		public final String json;

		public RequestAndScheduleTuple(AIMDay schedule, String json) {
			this.schedule = schedule;
			this.json = json;
		}

	}

	public static class AimdayJob extends Job {

		@Override
		public void doJob() throws Exception {
			AtomicInteger progress = new AtomicInteger();
			Cache.set(id, progress, "10min");
			AIMDay schedule = scheduler.lägg(progress);

			RequestAndScheduleTuple tuple = new RequestAndScheduleTuple(schedule, json);
			Logger.info("Caching new schedule with id %s", id);
			Cache.set(id, tuple, "1h");

		}

		private final Scheduler scheduler;
		private final String id;
		private final String json;

		public AimdayJob(Scheduler scheduler, String id, String json) {
			this.scheduler = scheduler;
			this.id = id;
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

	public static void scheduleDart(String json) {
		renderTemplate("public/web/dart_scheduler.html", json);
	}
	
	public static void scheduleAPI(String json) {
		KonferensJson konf = null;
		AIMDay schedule = null;
		try {
			Gson gson = new Gson();
			konf = gson.fromJson(json, KonferensJson.class);

			// Prova att parsa en gång så att vi är säkra på att vi gillar formatet
			Konferens k = Konferens.fromAPI(konf);

			if (konf.schema != null && konf.schema.sessioner != null) {
				konf.schema.sparat = true;
				konf.schema.removeRemovedStuff(konf);
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

	private static ArrayList<Integer> getSpårArray(AIMDay schedule) {
		ArrayList<Integer> spår = new ArrayList<Integer>();
		for (int i = 1; i <= schedule.getMaxNumOfWorkshopsInASession(); i++) {
			spår.add(i);
		}
		return spår;
	}

	public static void scheduleNew(String json) throws InterruptedException,
			ExecutionException {

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

		ScheduleRequest sr = konf.scheduleRequest;
		Logger.info("New generation request. Place weight %s, ws weight %s, agenda weight %s", sr.placeWeight,
				sr.wsWeight, sr.agendaWeight);

		if ((sr.maxAntalDeltagare > 15 || sr.maxAntalDeltagare < 0) && sr.maxAntalDeltagare != 100) {
			throw new RuntimeException("Felaktigt antal max deltagare, fick " + sr.maxAntalDeltagare);
		}
		if (sr.minAntalDeltagare > 15 || sr.minAntalDeltagare < 0) {
			throw new RuntimeException("Felaktigt minimi-antal  deltagare, fick " + sr.minAntalDeltagare);
		}

		sr.generations = Math.min(100000, sr.generations);
		Scheduler scheduler = new Scheduler(k, konf.scheduleRequest);

		String randomId = UUID.randomUUID().toString();

		new AimdayJob(scheduler, randomId, json).now();
		renderJSON(gson.toJson(randomId));
	}

	public static void testProgress(String randomId) {
		render(randomId);
	}

	public static void progress(String id) {
		if (id.equals("test")) {
			renderJSON(Math.random() * 100);
		}
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
		String postback_url = getPostBackURL(konf);
		AIMDay schedule = tuple.schedule;
		ArrayList<Integer> spår = getSpårArray(schedule);
		String json = gson.toJson(konf);
		render(schedule, spår, json, postback_url);
	}
}
package controllers;

import java.util.List;

import models.Participant;
import models.Question;

import play.mvc.Controller;

import se.aimday.scheduler.AIMDay;
import se.aimday.scheduler.Scheduler;

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

	public static void schedule(int tracks, int sessions, int generations) {
		List<Participant> allParticipants = Participant.<Participant> findAll();
		List<Question> allQuestions = Question.<Question> findAll();
		
		generations = Math.min(100000, generations);
		System.out.println("num gs" + generations);
		Scheduler scheduler = new Scheduler(tracks, sessions, 10, allQuestions, allParticipants, null, generations);
		AIMDay schedule = scheduler.lägg();

		render(schedule);
	}

}
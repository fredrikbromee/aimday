package se.aimday.scheduler.api;

import java.util.List;

public class ForskareJson {
	public ForskareJson(int grad, String id, boolean isJoker) {
		this(grad, id, isJoker, false);
	}

	public ForskareJson(int grad, String id, boolean isJoker, boolean harVikt) {
		this.grad = grad;
		this.id = id;
		this.harVikt = harVikt;
		if (isJoker) {
			joker = isJoker;
		}
	}

	public boolean harVikt;
	public String id;
	public int grad;
	public List<String> frågor;
	public boolean joker;
	public KontaktJson kontakt;
}

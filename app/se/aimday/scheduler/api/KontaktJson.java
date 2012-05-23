package se.aimday.scheduler.api;

public class KontaktJson {
	public KontaktJson(String fornamn, String efternamn, String avdelning, String org) {
		super();
		this.fornamn = fornamn;
		this.efternamn = efternamn;
		this.avdelning = avdelning;
		this.org = org;
	}
	public String fornamn;
	public String efternamn;
	public String avdelning;
	public String org;
}

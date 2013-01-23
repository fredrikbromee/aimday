package se.aimday.scheduler.api;

import java.util.List;

public class Låsningar {
	public class FrågeLås {
		public String id;
		public List<String> låsttill;
	}

	public class SessionsLås {
		public String id;
		public List<Integer> låsttill;
	}

	public List<SessionsLås> frågesessionslås;
	public List<SessionsLås> forskarsessionslås;
	public List<FrågeLås> forskarfrågelås;
}

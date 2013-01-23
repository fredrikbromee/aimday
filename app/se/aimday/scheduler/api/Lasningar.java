package se.aimday.scheduler.api;

import java.util.List;

public class Lasningar {
	public class FrageLas {
		public String id;
		public List<String> låsttill;
	}

	public class SessionsLas {
		public String id;
		public List<Integer> låsttill;
	}

	public List<SessionsLas> frågesessionslås;
	public List<SessionsLas> forskarsessionslås;
	public List<FrageLas> forskarfrågelås;
}

package se.aimday.scheduler.api;

import java.util.List;

public class Lasningar {
	public class StringLas {
		public String id;
		public List<String> låsttill;
	}

	public class IntegerLas {
		public String id;
		public List<Integer> låsttill;
	}

	public List<IntegerLas> frågesessionslås;
	public List<IntegerLas> frågerumslås;
	public List<IntegerLas> forskarsessionslås;
	public List<StringLas> forskarfrågelås;
}

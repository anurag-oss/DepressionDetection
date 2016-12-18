package backend;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

public class BaseLineCreate {

	public static void main(String[] args) {
		buildBaseLine();
	}

	public static void buildBaseLine() {

		// Map<Integer, String> eventMap =
		// determineDayEvents(aggregatedAttributes);

		Map<Integer, String> eventMap = new HashMap<Integer, String>();
		eventMap.put(0, "sleeping");
		eventMap.put(1, "sleeping");
		eventMap.put(2, "sleeping");
		eventMap.put(3, "sleeping");
		eventMap.put(4, "sleeping");
		eventMap.put(5, "sleeping");
		
		eventMap.put(6, "working");
		eventMap.put(7, "working");
		eventMap.put(8, "working");
		eventMap.put(9, "working");
		eventMap.put(10, "working");
		eventMap.put(11, "working");
		
		eventMap.put(12, "eating");
		eventMap.put(13, "eating");
		eventMap.put(14, "eating");
		
		eventMap.put(15, "studying");
		eventMap.put(16, "studying");
		eventMap.put(17, "studying");
		eventMap.put(18, "studying");
		eventMap.put(19, "studying");
		eventMap.put(20, "studying");
		eventMap.put(21, "studying");
		eventMap.put(22, "studying");
		eventMap.put(23, "studying");
		eventMap.put(24, "studying");
		

		AttributesSet attr1 = new AttributesSet();
		attr1.setActivity("still");
		attr1.setLocationType("home");
		attr1.setPartOfDay("night");

		AttributesSet attr2 = new AttributesSet();
		attr2.setActivity("still");
		attr2.setLocationType("home");
		attr2.setPartOfDay("morning");

		AttributesSet attr3 = new AttributesSet();
		attr3.setActivity("still");
		attr3.setLocationType("home");
		attr3.setPartOfDay("afternoon");

		AttributesSet attr4 = new AttributesSet();
		attr4.setActivity("still");
		attr4.setLocationType("education");
		attr4.setPartOfDay("afternoon");

		Map<String, AttributesSet> eventAttr = new HashMap<String, AttributesSet>();
		eventAttr.put("sleeping", attr1);
		eventAttr.put("working", attr2);
		eventAttr.put("eating", attr3);
		eventAttr.put("studying", attr4);

		Map<Integer, Map<String, AttributesSet>> baseline = new HashMap<Integer, Map<String, AttributesSet>>();
		for (Integer key : eventMap.keySet()) {
			Map<String, AttributesSet> baselineValue = new HashMap<String, AttributesSet>();
			for (String event : eventAttr.keySet()) {
				if (eventMap.get(key).equals(event)) {
					baselineValue.put(event, eventAttr.get(event));
					baseline.put(key, baselineValue);
				}
			}

		}

		// attr.setHourOfDay(15);
		// map.put(15, attr);
		try {
			FileOutputStream fileOut = new FileOutputStream("data/baseline.ser");
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(baseline);
			out.close();
			fileOut.close();
			System.out.printf("Serialized data is saved in data/baseline.ser");
		} catch (IOException i) {
			i.printStackTrace();
		}

	}

}

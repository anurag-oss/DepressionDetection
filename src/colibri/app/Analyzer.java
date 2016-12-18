package colibri.app;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.xml.sax.SAXException;

import backend.AttributesSet;
import colibri.io.relation.RelationReaderCON;
import colibri.io.relation.RelationReaderXML;
import colibri.lib.Concept;
import colibri.lib.HybridLattice;
import colibri.lib.Lattice;
import colibri.lib.Relation;
import colibri.lib.Traversal;
import colibri.lib.TreeRelation;

/**
 * Imports a binary relation from a .con or .xml file and outputs the edges of
 * the corresponding lattice or the edges returned by the violation iterator.
 */
public class Analyzer {

	public static final String DEVIATION = "deviation";

	public void toBeExecuted(Map<Integer, AttributesSet> aggregatedAttributes) throws Exception {
		Analyzer analyzer = new Analyzer();

		String report = "A deviation has been detected, which is above the threshold.\n"+
		"The person concerned was expected to be REQD_ACTIVITY instead of FOUND_ACTIVITY at HOURS:00 hours \n";

		Map<Integer, String> dayEvents = analyzer.determineDayEvents(aggregatedAttributes);
		// initializer();
		ArrayList<String> eventDeviation = new ArrayList<>();
		for (Integer key : dayEvents.keySet()) {
			if (compare(dayEvents.get(key), key) != null) {
				eventDeviation.add(compare(dayEvents.get(key), key));
			}
		}
		String[] params = eventDeviation.toString().split(":");
		report=report.replace("REQD_ACTIVITY", params[0]).replace("FOUND_ACTIVITY", params[2]).replace("HOURS", params[1]);
		sendEmail(report);

	}

	public static void initializerTH() throws Exception {
		Map<String, Integer> thMap = new HashMap<>();
		thMap.put("sleeping", 1);
		thMap.put("working", 1);
		thMap.put("eating", 1);
		thMap.put("studying", 1);
		storeThresholdMap(thMap);
	}

	public static void initializerAGG() throws Exception {
		Map<String, Integer> aggMap = new HashMap<>();
		aggMap.put("sleeping", 0);
		aggMap.put("working", 0);
		aggMap.put("eating", 0);
		aggMap.put("studying", 0);
		storeCurrentAgg(aggMap);
	}

	@SuppressWarnings("unchecked")
	public static String compare(String event, Integer hourOfDay) throws Exception {

		String eventMissed = null;

		Map<Integer, Map<String, AttributesSet>> baseLine = null;

		try {

			ObjectInputStream in = new ObjectInputStream(new FileInputStream("data/baseline.ser"));
			Object obj = (Map<Integer, Map<String, AttributesSet>>) (in.readObject());
			baseLine = (Map<Integer, Map<String, AttributesSet>>) obj;
			in.close();
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

		Map<String, AttributesSet> eventAndAttribs = baseLine.get(hourOfDay);
		String reqdEvent = eventAndAttribs.keySet().iterator().next();
		// AttributesSet reqdAttribs=eventAndAttribs.get(reqdEvent);

		if (!reqdEvent.equals(event)) {

			// load the threshhold values
			Map<String, Integer> th = readThreshold();
			// read the aggregate till now
			Map<String, Integer> agg = readCurrentAgg();

			int theVal = th.get(reqdEvent);
			int aggVal = agg.get(reqdEvent);
			aggVal += 1;
			if (aggVal == theVal) {
				eventMissed = reqdEvent+":"+hourOfDay+":"+event;
				aggVal = 0;
			}

			agg.put(reqdEvent, aggVal);
			storeCurrentAgg(agg);

		}

		return eventMissed;

	}

	/**
	 * Function that determines the events from the lattice based on the given attributes
	 * @param aggregatedAttributes
	 * @return
	 */
	public static Map<Integer, String> determineDayEvents(Map<Integer, AttributesSet> aggregatedAttributes) {
		Map<Integer, String> dayEvents = new HashMap<>();
		Map<Integer, AttributesSet> map = aggregatedAttributes;

		for (Entry<Integer, AttributesSet> entry : map.entrySet()) {

			Integer hourOfDay = entry.getKey();
			AttributesSet attrs = entry.getValue();
			ArrayList<String> attributes = new ArrayList<>();
			attributes.add(attrs.getActivity());
			attributes.add(attrs.getLocationType());
			attributes.add(attrs.getPartOfDay());

			// Add event for each hour in events list

			if (findEvents(attributes) != null)
				dayEvents.put(hourOfDay, findEvents(attributes));
			else {

				// if events is null add a flag to indicate a deviation from the
				// normal pattern
				dayEvents.put(hourOfDay, "Deviation");

			}
		}

		for (Integer key : dayEvents.keySet()) {
			System.out.println("Key: " + key + "Value: " + dayEvents.get(key));
		}
		return dayEvents;

	}
	/**
	 * Function to build baseline
	 * @param aggregatedAttributes
	 */
	public static void buildBaseLine(Map<Integer, AttributesSet> aggregatedAttributes) {

		Map<Integer, String> eventMap =
				determineDayEvents(aggregatedAttributes);
		Map<Integer, AttributesSet> input = aggregatedAttributes;

		Map<String, AttributesSet> eventAttr = new HashMap<String,
				AttributesSet>();
		for (Integer key : input.keySet()) {
			ArrayList<String> attributes = new ArrayList<>();
			attributes.add(input.get(key).getActivity());
			attributes.add(input.get(key).getLocationType());
			attributes.add(input.get(key).getPartOfDay());
			eventAttr.put(findEvents(attributes), input.get(key));
		}

		Map<Integer, Map<String, AttributesSet>> baseline = new
				HashMap<Integer, Map<String, AttributesSet>>();
		for (Integer key : eventMap.keySet()) {
			Map<String, AttributesSet> baselineValue = new HashMap<String,
					AttributesSet>();
			for (String event : eventAttr.keySet()) {
				if (eventMap.get(key).equals(event)) {
					baselineValue.put(event, eventAttr.get(event));
					baseline.put(key, baselineValue);
				}
			}

		}

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

	/**
	 * Function to find events for a given set of attributes
	 * 
	 * @param attributes
	 * @return Set of unique events
	 */
	private static String findEvents(ArrayList<String> attributes) {
		String inputFormat = null;
		String inputFile = null;
		String outputFormat = null;

		// System.out.println(attributes);

		inputFormat = System.getProperty("input_format", "con");
		inputFile = System.getProperty("input_file", "data/concepts.con");

		if (outputFormat == null) {
			outputFormat = "xml";
		}

		Relation relation;
		relation = new TreeRelation();

		if (inputFormat.equals("xml")) {
			try {
				RelationReaderXML xmlReader = new RelationReaderXML();
				xmlReader.read(inputFile, relation);
			} catch (SAXException e) {
				System.err.println("Reading xml-file failed.");
				e.printStackTrace();
				// return;
			} catch (IOException e) {
				System.err.println("Reading xml-file failed.");
				e.printStackTrace();
				// return;
			}
		} else if (inputFormat.equals("con")) {
			try {
				RelationReaderCON conReader = new RelationReaderCON();
				conReader.read(inputFile, relation);
			} catch (IOException e) {
				System.err.println("Reading con-file failed.");
				e.printStackTrace();
			}
		} else {
			throw new IllegalArgumentException();
		}

		Lattice lattice = new HybridLattice(relation);

		Iterator<Concept> it = lattice.conceptIterator(Traversal.TOP_ATTRSIZE);

		while (it.hasNext()) {
			Concept c = it.next();
			// If the attributes are found in the current events then store it
			if (checkIfContains(attributes, c) == true && !c.getObjects().isEmpty()) {
				Iterator<Comparable> event_iterator = c.getObjects().iterator();
				String event = event_iterator.next().toString();
				return event;
			}
		}
		return null;
	}

	/**
	 * Function to check if the given attributes are present in a concept
	 * attribute list
	 * 
	 * @param attributes
	 * @param c - concept
	 * @return
	 */
	private static boolean checkIfContains(ArrayList<String> attributes, Concept c) {
		boolean found = false;
		for (String attribute : attributes) {
			try {
				if (c.getAttributes().contains(attribute.toLowerCase())) {
					found = true;
				} else {
					found = false;
					break;
				}
			} catch (NullPointerException e) {
				return found;
			}
		}
		return found;

	}

	@SuppressWarnings("unchecked")
	public static String reportCreation(Map<Integer, String> eventsMap, Map<Integer, AttributesSet> dataFromJson)
			throws Exception {

		Map<Integer, Map<String, AttributesSet>> baseLine = null;

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("data/baseline.ser"))) {
			baseLine = (Map<Integer, Map<String, AttributesSet>>) (in.readObject());
			in.close();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("HOUR \t NEEDED EVENT \t FOUND EVENT \t MISSING ATTRIBUTE \n");
		for (Iterator<Entry<Integer, String>> hourItr = eventsMap.entrySet().iterator(); hourItr.hasNext();) {

			// find corresponding event in the base line
			Entry<Integer, String> hourlyEvent = hourItr.next();
			Integer currentHour = hourlyEvent.getKey();
			Map<String, AttributesSet> neededEventMap = baseLine.get(currentHour);

			String neededEvent = neededEventMap.keySet().iterator().next();
			if (!neededEvent.equals(hourlyEvent.getValue())) {
				// we have a deviation
				// create a missing attributeList
				sb.append(currentHour + "\t" + neededEvent + "\t" + hourlyEvent.getValue() + "\t"
						+ neededEventMap.get(neededEvent).getDelta(dataFromJson.get(currentHour)) + "\n");
			}

		}
		return sb.toString();

	}

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> readThreshold() throws Exception {

		Map<String, Integer> thMap = new HashMap<String, Integer>();

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("data/threshhold.ser"))) {
			thMap = (Map<String, Integer>) in.readObject();
			in.close();
		}

		return thMap;
	}

	@SuppressWarnings("unchecked")
	public static void storeThresholdMap(Map<String, Integer> thMap) throws Exception {

		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data/threshhold.ser"))) {
			out.writeObject(thMap);
			out.close();
		}

		return;
	}

	@SuppressWarnings("unchecked")
	public static Map<String, Integer> readCurrentAgg() throws Exception {

		Map<String, Integer> agMap = new HashMap<String, Integer>();

		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream("data/agg.ser"))) {
			agMap = (Map<String, Integer>) in.readObject();
			in.close();
		}

		return agMap;
	}

	@SuppressWarnings("unchecked")
	public static void storeCurrentAgg(Map<String, Integer> agMap) throws Exception {

		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream("data/agg.ser"))) {
			out.writeObject(agMap);
			out.close();
		}

		return;
	}

	public static void sendEmail(String report) {
		String to = "anuragkiit@gmail.com";

		// Sender's email ID needs to be mentioned
		String from = "depressiondetector2014@gmail.com";
		final String username = "depressiondetector2014";// change accordingly
		final String password = "a4a3a2a1";// change accordingly

		String host = "smtp.gmail.com";

		Properties props = new Properties();
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", "587");

		// Get the Session object.
		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {
			// Create a default MimeMessage object.
			Message message = new MimeMessage(session);

			// Set From: header field of the header.
			message.setFrom(new InternetAddress(from));

			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));

			// Set Subject: header field
			message.setSubject("Alert: A deviation from general routine has been observed.");

			// Now set the actual message
			message.setText(report);

			// Send message
			Transport.send(message);

			System.out.println("Sent message successfully....");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

}

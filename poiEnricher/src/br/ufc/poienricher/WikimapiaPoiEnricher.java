package br.ufc.poienricher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class WikimapiaPoiEnricher {

	private String xml;
	private String lat;
	private String lon;
	private static final String wikimapia_key = "colocar_chave_wikimapia_aqui";

	public WikimapiaPoiEnricher(String xml) {
		this.xml = xml;
	}

	public WikimapiaPoiEnricher(String lat, String lon) {
		this.lat = lat;
		this.lon = lon;
	}

	public List<String> getPoiWikimapiaTags(String poiLat, String poiLon)
			throws SAXException, IOException, ParserConfigurationException {

		HttpClient client = new DefaultHttpClient();
		String urlWikimapia = "http://api.wikimapia.org/?function=place.getnearest"
				+ "&key="
				+ wikimapia_key
				+ "&lat="
				+ poiLat
				+ "&lon="
				+ poiLon
				+ "&language=en" + "&format=xml" + "&count=10";

		HttpGet get = new HttpGet(urlWikimapia);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent(), "UTF-8"));

		StringBuilder sb = new StringBuilder();
		String responseString;
		while ((responseString = rd.readLine()) != null) {
			sb.append(responseString);
		}

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(new InputSource(new StringReader(sb.toString())));
		Element rootElement = document.getDocumentElement();
		NodeList rootChildren = rootElement.getChildNodes();
		Node places = rootChildren.item(1);
		NodeList placesChildren = places.getChildNodes();

		List<String> result = new ArrayList<String>();
		for (int i = 0; i < placesChildren.getLength(); i++) {
			String placeName = getPlaceName(placesChildren, i);
			int placeDistance = getPlaceDistance(placesChildren, i);
			if(placeDistance <= 150){
				result.add(placeName);	
			}
			// System.out.println(placeName);
		}

		return result;
	}

	private int getPlaceDistance(NodeList placesChildren, int i) {
		Node place = placesChildren.item(i);
		NodeList placeChildren = place.getChildNodes();
		Node placeDistance = placeChildren.item(5);
		NodeList placeDistanceChildren = placeDistance.getChildNodes();
		Node distanceNode = placeDistanceChildren.item(0);
		return Integer.parseInt(distanceNode.getNodeValue());
	}

	private String getPlaceName(NodeList placesChildren, int i) {
		Node place = placesChildren.item(i);
		NodeList placeChildren = place.getChildNodes();
		Node placeTitle = placeChildren.item(1);
		NodeList placeTitleChildren = placeTitle.getChildNodes();
		Node nameNode = placeTitleChildren.item(0);
		return nameNode.getNodeValue();
	}

	public String getXml() {
		return xml;
	}

	public void setXml(String xml) {
		this.xml = xml;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLon() {
		return lon;
	}

	public void setLon(String lon) {
		this.lon = lon;
	}
}

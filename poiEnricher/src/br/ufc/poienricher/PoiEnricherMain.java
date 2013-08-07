package br.ufc.poienricher;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.client.ClientProtocolException;
import org.xml.sax.SAXException;

public class PoiEnricherMain {
	
	private static final String lat = "-3.72561782598";
	private static final String lon = "-38.499783";

	/**
	 * @param args
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 */
	public static void main(String[] args) throws InterruptedException, ExecutionException, ParserConfigurationException, SAXException {
				
		try {
			FlickrPoiEnricher fpe = new FlickrPoiEnricher(lat, lon);
			List<String> poiFLickrTags = fpe.getPoiFlickrTags(lat, lon);
			
			StringBuilder flickr_sb = new StringBuilder();
			for (String string : poiFLickrTags) {
				flickr_sb.append("'" + string + "', ");
			}
			
			System.out.println("Flickr tags: " + flickr_sb.toString());
			
			WikimapiaPoiEnricher wpe = new WikimapiaPoiEnricher(lat, lon);
			List<String> poiWikimapiaTags = wpe.getPoiWikimapiaTags(lat, lon);
			
			StringBuilder wikimapia_sb = new StringBuilder();
			for (String string : poiWikimapiaTags) {
				wikimapia_sb.append("'" + string + "', ");
			}
			
			System.out.println("Wikimapia tags: " + wikimapia_sb.toString());
			
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

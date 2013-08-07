package br.ufc.poienricher;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.beanutils.DynaBean;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class FlickrPoiEnricher {

	private String lat;
	private String lon;

	public FlickrPoiEnricher(String lat, String lon) {
		this.lat = lat;
		this.lon = lon;
	}

	private static final String flickr_key = "colocar_chave_flickr_aqui";

	public List<String> getTagsFromFlickrPhoto(String photoID)
			throws ClientProtocolException, IOException {

		String urlFlickr_photo_info = "http://ycpi.api.flickr.com/services/rest/?method=flickr.photos.getInfo"
				+ "&api_key="
				+ flickr_key
				+ "&photo_id="
				+ photoID
				+ "&format=json";

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(urlFlickr_photo_info);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent(), "UTF-8"));
		String responseString = rd.readLine();

		String jsonString = responseString.substring(14,
				responseString.length() - 1);
		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(jsonString);
		DynaBean bean = (DynaBean) JSONSerializer.toJava(jsonObject);
		DynaBean photo = (DynaBean) bean.get("photo");
		DynaBean tags = (DynaBean) photo.get("tags");
		List<DynaBean> tag = (List<DynaBean>) tags.get("tag");
		System.out.println("Processed photo: " + photoID);

		List<String> result = new ArrayList<String>();
		Integer zero = new Integer(0);
		for (DynaBean tagElement : tag) {
			if (tagElement.get("machine_tag").equals(zero)) {
				result.add((String) tagElement.get("raw"));
			}
		}

		return result;
	}

	public List<String> getFlickrPhotoIDs(String lat, String lon, String radius)
			throws ClientProtocolException, IOException {

		String urlFlickr_photos_search = "http://ycpi.api.flickr.com/services/rest/?method=flickr.photos.search"
				+ "&api_key="
				+ flickr_key
				+ "&lat="
				+ lat
				+ "&lon="
				+ lon
				+ "&radius="
				+ radius
				+ "&has_geo=1"
				+ "&min_upload_date=20100101"
				+ "&sort="
				+ "&per_page=50"
				+ "&format=json";

		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(urlFlickr_photos_search);
		HttpResponse response = client.execute(get);
		BufferedReader rd = new BufferedReader(new InputStreamReader(response
				.getEntity().getContent(), "UTF-8"));
		String responseString = rd.readLine();

		String jsonString = responseString.substring(14,
				responseString.length() - 1);
		// System.out.println(jsonString);
		JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON(jsonString);
		DynaBean bean = (DynaBean) JSONSerializer.toJava(jsonObject);
		DynaBean photos = (DynaBean) bean.get("photos");
		List<DynaBean> photoList = (List<DynaBean>) photos.get("photo");

		List<String> result = new ArrayList<String>();
		for (DynaBean photoElement : photoList) {
			result.add((String) photoElement.get("id"));
		}

		System.out.println("Total flickr photos fetched:" + result.size());

		return result;
	}

	public List<String> getPoiFlickrTags(String poiLat, String poiLon) throws ClientProtocolException, IOException, InterruptedException, ExecutionException {

		List<String> result = new ArrayList<String>();
		List<String> photoTags;
		HashMap<String, Integer> tagMap = new HashMap<String, Integer>();

		List<String> flickrPhotoIDs = getFlickrPhotoIDs(poiLat, poiLon, "0.15");

		ExecutorService workers = Executors.newCachedThreadPool();

		Collection<Callable<List<String>>> tasks = new ArrayList<Callable<List<String>>>();

		float totalPhotos = flickrPhotoIDs.size();

		for (final String photoID : flickrPhotoIDs) {

			tasks.add(new Callable<List<String>>() {

				public List<String> call() throws Exception {
					System.out.println("Thread running for photo: " + photoID);
					return getTagsFromFlickrPhoto(photoID);
				}

			});

			// photoTags = new ArrayList<String>();
			// photoTags = getTagsFromFlickrPhoto(photoID);

		}

		List<Future<List<String>>> results = workers.invokeAll(tasks);
		for (Future<List<String>> f : results) {

			List<String> list = f.get();

			for (String tag : list) {

				if (tagMap.containsKey(tag)) {
					tagMap.put(tag, tagMap.get(tag) + 1);
				} else {
					tagMap.put(tag, new Integer(0));
				}
			}

		}

		for (String key : tagMap.keySet()) {
			System.out.println("Tag: '" + key + "' appears " + tagMap.get(key)
					+ " times.");
			if (tagMap.get(key) / totalPhotos >= 0.1) {
				result.add(key);
			}
		}

		return result;
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

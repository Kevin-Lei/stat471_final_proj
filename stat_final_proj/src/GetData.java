import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

/**
 * STAT 471: Final Project
 * 
 * Use Riot's REST API to obtain data for League of Legends
 * 
 * @author Kevin
 *
 */
public class GetData {
	
	// Replace this with your API Key
	public static final String API_KEY = "385d5016-00ca-4004-8257-6c2d4815d4b4";
	public static final String MATCHLIST_URL = "https://na.api.pvp.net/api/lol/na/v2.2/matchlist/by-summoner/";
	
	
	public static void main(String args[]) throws IOException {
		int summonerID = 31670053;
		getMatches(summonerID);
	}
	
	public static void getMatches(int summonerID) throws IOException {
	  // Build the URL
		StringBuilder urlRequest = new StringBuilder(MATCHLIST_URL);
		urlRequest.append("" + summonerID);
		urlRequest.append("?");
		urlRequest.append("api_key=" + API_KEY);
		URL url = new URL(urlRequest.toString());
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		
		// Set the HTTP GET request headers
		conn.setRequestMethod("GET");
		
		int responseCode = conn.getResponseCode();
		System.out.println("Response Code: " + responseCode);
		
		BufferedReader in = new BufferedReader(
		    new InputStreamReader(conn.getInputStream()));
		StringBuffer response = new StringBuffer();
		
		// Read in the response
		String line = "";
		while ((line = in.readLine()) != null) {
		  response.append(line);
		}
		
		JSONObject matchesJson = new JSONObject(response);
		System.out.println(response);
	}
}

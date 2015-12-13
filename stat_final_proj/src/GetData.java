import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

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
	public static final String SUMMONER_URL = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
	public static final File MATCHES_FILE = new File("data/matches");
	
	public static Queue<String> matchQueue = new LinkedList<String>();
	public static Queue<String> summonerQueue = new LinkedList<String>();
	
	public static void main(String args[]) throws IOException, InterruptedException {
		String summonerID = "31670053";
	  String matchID = "1370173217";
	  getMatches(summonerID);
	  int numIter = 5;
	  for (int i = 0; i < numIter; i++) {
	    while (!matchQueue.isEmpty()) {
	      getSummoners(matchQueue.poll());
	      System.out.println("Finished getting summmoners; index: " + i);
	      break;
	    }
	    while (!summonerQueue.isEmpty()) {
	      getMatches(summonerQueue.poll());
	      System.out.println("Finished getting matches; index: " + i);
	    }
	  }
	}
	
	public static void getMatches(String summonerID) throws IOException, InterruptedException {
	  // Build the URL
		StringBuilder urlRequest = new StringBuilder(MATCHLIST_URL);
		urlRequest.append("" + summonerID);
		urlRequest.append("?");
		urlRequest.append("api_key=" + API_KEY);
		URL url = new URL(urlRequest.toString());
		HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
		
		// Set the HTTP GET request headers
		conn.setRequestMethod("GET");
		
		BufferedReader in = new BufferedReader(
		    new InputStreamReader(conn.getInputStream()));
		StringBuffer response = new StringBuffer();
		
		// Read in the response
		String line = "";
		while ((line = in.readLine()) != null) {
		  response.append(line);
		}
		
		// Create an output file of matches
		FileWriter writer = new FileWriter(MATCHES_FILE, true);
		
		// Parse the JSON
		JSONObject json = new JSONObject(response.toString());
		int numMatches = json.getInt("totalGames");
		JSONArray matches = json.getJSONArray("matches");
		for (int i = 0; i < numMatches; i++) {
		  // Write each match to a file and add it to the queue
		  JSONObject match = matches.getJSONObject(i);
		  String matchID = "" + match.getLong("matchId");
		  writer.write(matchID + "\n");
		  matchQueue.add(matchID);
		}
		writer.close();
		Thread.sleep(1000);
	}
	
	public static void getSummoners(String matchID) throws IOException, InterruptedException {
    // Build the URL
    StringBuilder urlRequest = new StringBuilder(SUMMONER_URL);
    urlRequest.append("" + matchID);
    urlRequest.append("?");
    urlRequest.append("api_key=" + API_KEY);
    URL url = new URL(urlRequest.toString());
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
	  
    // Set the HTTP GET request headers
    conn.setRequestMethod("GET");
    
    BufferedReader in = new BufferedReader(
        new InputStreamReader(conn.getInputStream()));
    StringBuffer response = new StringBuffer();
    
    // Read in the response
    String line = "";
    while ((line = in.readLine()) != null) {
      response.append(line);
    }

    // Parse the JSON
    JSONObject json = new JSONObject(response.toString());
    JSONArray participants = json.getJSONArray("participantIdentities");
    Iterator<Object> parIter = participants.iterator();
    while (parIter.hasNext()) {
      JSONObject participant = ((JSONObject) parIter.next())
          .getJSONObject("player");
      String summonerID = "" + participant.getLong("summonerId");
      if (!summonerQueue.contains(summonerID)) {
        summonerQueue.add(summonerID);
      }
    }
    Thread.sleep(1000);
	}
}

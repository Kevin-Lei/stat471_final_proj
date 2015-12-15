import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * STAT 471: Final Project
 * 
 * Use Riot's REST API to obtain data for League of Legends
 * 
 * Reference: http://loldevelopers.de.vu/
 *            https://developer.riotgames.com/api/
 * 
 * @author Kevin
 *
 */
public class GetData {
	
	// Replace this with your API Key
	public static final String API_KEY = "385d5016-00ca-4004-8257-6c2d4815d4b4";
	public static final String MATCHLIST_URL = "https://na.api.pvp.net/api/lol/na/v2.2/matchlist/by-summoner/";
	public static final String SUMMONER_URL = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
	public static final String MATCH_URL = "https://na.api.pvp.net/api/lol/na/v2.2/match/";
	public static final File MATCHES_FILE = new File("data/matches");
	public static final File DATA_FILE = new File("data/lol_data.csv");
	
	public static final String[] CHAMPIONS = {"266", "103", "84", "12", "32",
	  "34", "1", "22", "268", "432", "53", "63", "201", "51", "69", "31", "42", 
	  "122", "131", "36", "119", "245", "60", "28", "81", "9", "114", "105", "3",
	  "41", "86", "150", "79", "104", "120", "74", "420", "39", "40", "59", "24",
	  "126", "222", "429", "43", "30", "38", "55", "10", "85", "121", "203", "96", 
	  "7", "64", "89", "127", "236", "117", "99", "54", "90", "57", "11", "21", 
	  "82", "25", "267", "75", "111", "76", "56", "20", "2", "61", "80", "78", 
	  "133", "33", "421", "58", "107", "92", "68", "13", "113", "35", "98", "102", 
	  "27", "14", "15", "72", "37", "16", "50", "134", "223", "91", "44", "17", 
	  "412", "18", "48", "23", "4", "29", "77", "6", "110", "67", "45", "161", 
	  "254", "112", "8", "106", "19", "62", "101", "5", "157", "83", "154", "238", 
	  "115", "26", "143"};
	
	public static Queue<String> matchQueue = new LinkedList<String>();
	public static Queue<String> summonerQueue = new LinkedList<String>();
	
	public static void main(String args[]) throws IOException, InterruptedException {
//		String summonerID = "31670053";
//	  String matchID = "1370173217";
//	  getMatches(summonerID);
//	  int numIter = 5;
//	  for (int i = 0; i < numIter; i++) {
//	    while (!matchQueue.isEmpty()) {
//	      getSummoners(matchQueue.poll());
//	      System.out.println("Finished getting summmoners; index: " + i);
//	      break;
//	    }
//	    while (!summonerQueue.isEmpty()) {
//	      getMatches(summonerQueue.poll());
//	      System.out.println("Finished getting matches; index: " + i);
//	    }
//	  }
	  
	  writeData();
	  getFeatures("1931532264");
	}
	
	public static void writeData() throws IOException {
	  // Create the header String
	  StringBuilder header = new StringBuilder();
	  for (String champion : CHAMPIONS) {
	    header.append("championId_" + champion);
	    header.append(",");
	  }
	  
	  header.append("\n");
	  // Write the header to the data file
	  FileWriter writer = new FileWriter(DATA_FILE, true);
	  writer.write(header.toString());
	  writer.close();
	}
	
	public static void getFeatures(String matchID) throws IOException, InterruptedException {
	// Build the URL
    StringBuilder urlRequest = new StringBuilder(SUMMONER_URL);
    urlRequest.append("" + matchID);
    urlRequest.append("?");
    urlRequest.append("api_key=" + API_KEY);
    URL url = new URL(urlRequest.toString());
    HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
    
    // Set the HTTP GET request headers
    conn.setRequestMethod("GET");
    
    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      return;
    }
    
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
    JSONArray participants = json.getJSONArray("participants");
    Iterator<Object> parIter = participants.iterator();
    List<String> firstTeamChampions = new ArrayList<String>();
    List<String> secondTeamChampions = new ArrayList<String>();
    while (parIter.hasNext()) {
      JSONObject participant = (JSONObject) parIter.next();
      String championID = "" + participant.getLong("championId");
      String teamID = "" + participant.getLong("teamId");
      if (teamID.equals("100")) {
        firstTeamChampions.add(championID);
      } else if (teamID.equals("200")) {
        secondTeamChampions.add(championID);
      }
    }
    FileWriter writer = new FileWriter(DATA_FILE, true);
    for (String champion : CHAMPIONS) {
      if (firstTeamChampions.contains(champion)) {
        writer.write("1");
      } else if (secondTeamChampions.contains(champion)) {
        writer.write("-1");
      } else {
        writer.write("0");
      }
      writer.write(",");
    }
    writer.close();
    Thread.sleep(1000);
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
		
		int responseCode = conn.getResponseCode();
		if (responseCode != 200) {
		  return;
		}
		
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
    
    int responseCode = conn.getResponseCode();
    if (responseCode != 200) {
      return;
    }
    
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

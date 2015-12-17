import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
 * @author Kevin Lei
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
	  // Add in headers for championIDs
	  for (String champion : CHAMPIONS) {
	    header.append("championId_" + champion);
	    header.append(",");
	  }
	  // Add in headers for tiers (e.g. bronze, silver, gold)
	  header.append("tier_1,");
	  header.append("tier_2,");
	  // Add in headers for wards placed (both Support and Jungle)
	  header.append("sight_jungle_1,");
	  header.append("vision_jungle_1,");
	  header.append("wards_placed_jungle_1,");
	  
	  header.append("sight_support_1,");
    header.append("vision_support_1,");
    header.append("wards_placed_support_1,");
    
    header.append("sight_jungle_2,");
    header.append("vision_jungle_2,");
    header.append("wards_placed_jungle_2,");
    
    header.append("sight_support_2,");
    header.append("vision_support_2,");
    header.append("wards_placed_support_2,");
    
    // Add in headers for gold / min.
    String[] roles = { "TOP", "MIDDLE", "JUNGLE", "DUO_CARRY", "DUO_SUPPORT" };
    String[] timesGold = { "zeroToTenGold", "tenToTwentyGold", 
        "twentyToThirtyGold", "thirtyToEndGold" };
    for (int i = 1; i < 3; i++) {
      for (String role : roles) {
        for (String time : timesGold) {
          header.append(role + "_" + time + "_" + i + ",");
        }
      }
    }
    
    // Add in headers for creeps / min.
    String[] timesCreeps = { "zeroToTenCreeps", "tenToTwentyCreeps", 
        "twentyToThirtyCreeps", "thirtyToEndCreeps" };
    for (int i = 1; i < 3; i++) {
      for (String role : roles) {
        for (String time : timesCreeps) {
          header.append(role + "_" + time + "_" + i + ",");
        }
      }
    }

    // Add in the header for first blood (0 if team 1, 1 if team 2)
    header.append("firstBlood" + ",");
    
    // Add in the header for wards killed
    header.append("wards_killed_jungle_1,");
    header.append("wards_killed_support_1,");
    
    header.append("wards_killed_jungle_2,");
    header.append("wards_killed_support_2,");
    
    // Add in the header for neutral minions killed by the JUNGLE role
    header.append("neutral_jungle_1,");
    header.append("neutral_jungle_2");
    
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
    // Ensure that the match is from SEASON2015 and Ranked 5x5
    String season = json.getString("season");
    String queueType = json.getString("queueType");
    if (!season.equals("SEASON2015") || !(queueType.equals("RANKED_SOLO_5x5") 
        || queueType.equals("RANKED_TEAM_5x5"))) {
      return;
    }
    
    JSONArray participants = json.getJSONArray("participants");
    Iterator<Object> parIter = participants.iterator();
    List<String> firstTeamChampions = new ArrayList<String>();
    List<String> secondTeamChampions = new ArrayList<String>();
    
    List<String> firstTeamTier = new ArrayList<String>();
    List<String> secondTeamTier = new ArrayList<String>();
    
    // Create variables for wards created
    String sight_jungle_1 = "";
    String vision_jungle_1 = "";
    String wards_placed_jungle_1 = "";
    
    String sight_support_1 = "";
    String vision_support_1 = "";
    String wards_placed_support_1 = "";
    
    String sight_jungle_2 = "";
    String vision_jungle_2 = "";
    String wards_placed_jungle_2 = "";
    
    String sight_support_2 = "";
    String vision_support_2 = "";
    String wards_placed_support_2 = "";

    // Create variables for gold / min.
    Map<String, String> goldPerMinMap = new HashMap<String, String>();
    
    // Create variables for creeps / min.
    Map<String, String> creepsPerMinMap = new HashMap<String, String>();
    
    // Create variable for first blood
    boolean isSecTeamFirstBlood = false;
    
    // Create variables for wards killed
    String wards_killed_jungle_1 = "";
    String wards_killed_support_1 = "";

    String wards_killed_jungle_2 = "";
    String wards_killed_support_2 = "";

    // Create variables for neutral minions killed by the JUNGLE role
    String neutral_jungle_1 = "";
    String neutral_jungle_2 = "";
    
    while (parIter.hasNext()) {
      JSONObject participant = (JSONObject) parIter.next();
      // Parse the JSON for stats
      JSONObject stats = participant.getJSONObject("stats");
      String visionWards = "" + stats.getLong("visionWardsBoughtInGame");
      String sightWards = "" + stats.getLong("sightWardsBoughtInGame");
      String wardsPlaced = "" + stats.getLong("wardsPlaced");
      String wardsKilled = "" + stats.getLong("wardsKilled");
      boolean firstBlood = stats.getBoolean("firstBloodKill");
      String neutralMinionsKilled = "" + stats.getLong("neutralMinionsKilled");
      
      // Parse the JSON for timeline
      JSONObject timeline = participant.getJSONObject("timeline");
      String lane = timeline.getString("lane");
      String role = timeline.getString("role");
      // Aggregate the lane and role data
      String finalRole = "";
      if (role.equals("DUO_CARRY") || role.equals("DUO_SUPPORT")) {
        finalRole = role; // Covers DUO_CARRY and DUO_SUPPORT
      } else {
        finalRole = lane; // Covers TOP, MIDDLE, and JUNGLE
      }
      
      // Parse the JSON for gold/min. data
      JSONObject goldPerMinute = timeline.getJSONObject("goldPerMinDeltas");
      String zeroToTenGold = "" + goldPerMinute.getLong("zeroToTen");
      String tenToTwentyGold = "" + goldPerMinute.getLong("tenToTwenty");
      String twentyToThirtyGold = "" + goldPerMinute.getLong("twentyToThirty");
      String thirtyToEndGold = "" + goldPerMinute.getLong("thirtyToEnd");
      
      // Parse the JSON for creeps/min. data
      JSONObject creepsPerMinute = timeline.getJSONObject("creepsPerMinDeltas");
      String zeroToTenCreeps = "" + Math.round(creepsPerMinute.getDouble("zeroToTen")*100.0)/100.0;
      String tenToTwentyCreeps = "" + Math.round(creepsPerMinute.getDouble("tenToTwenty")*100.0)/100.0;
      String twentyToThirtyCreeps = "" + Math.round(creepsPerMinute.getDouble("twentyToThirty")*100.0)/100.0;
      String thirtyToEndCreeps = "" + Math.round(creepsPerMinute.getDouble("thirtyToEnd")*100.0)/100.0;
      
      // Parse the JSON for championId data
      String championID = "" + participant.getLong("championId");
      String teamID = "" + participant.getLong("teamId");
      // Parse the JSON for Tier data
      String tier = participant.getString("highestAchievedSeasonTier");
      
      int i = 0;
      if (teamID.equals("100")) {
        i = 1;
        firstTeamChampions.add(championID);
        firstTeamTier.add(tier);
        if (finalRole.equals("JUNGLE")) {
          sight_jungle_1 = sightWards;
          vision_jungle_1 = visionWards;
          wards_placed_jungle_1 = wardsPlaced;
          wards_killed_jungle_1 = wardsKilled;
          neutral_jungle_1 = neutralMinionsKilled;
        } else if (finalRole.equals("DUO_SUPPORT")) {
          sight_support_1 = sightWards;
          vision_support_1 = visionWards;
          wards_placed_support_1 = wardsPlaced;
          wards_killed_support_1 = wardsKilled;
        }
      } else if (teamID.equals("200")) {
        i = 2;
        secondTeamChampions.add(championID);
        secondTeamTier.add(tier);
        
        if (finalRole.equals("JUNGLE")) {
          sight_jungle_2 = sightWards;
          vision_jungle_2 = visionWards;
          wards_placed_jungle_2 = wardsPlaced;
          wards_killed_jungle_2 = wardsKilled;
          neutral_jungle_2 = neutralMinionsKilled;
        } else if (finalRole.equals("DUO_SUPPORT")) {
          sight_support_2 = sightWards;
          vision_support_2 = visionWards;
          wards_placed_support_2 = wardsPlaced;
          wards_killed_support_2 = wardsKilled;
        }
        if (firstBlood) {
          isSecTeamFirstBlood = true;
        }
      }
      // Put the gold per minute data into the map
      goldPerMinMap.put(finalRole + "_" + "zeroToTenGold" + "_" + i, zeroToTenGold);
      goldPerMinMap.put(finalRole + "_" + "tenToTwentyGold" + "_" + i, tenToTwentyGold);
      goldPerMinMap.put(finalRole + "_" + "twentyToThirtyGold" + "_" + i, twentyToThirtyGold);
      goldPerMinMap.put(finalRole + "_" + "thirtyToEndGold" + "_" + i, thirtyToEndGold);
      
      // Put the creeps per minute data into the map
      creepsPerMinMap.put(finalRole + "_" + "zeroToTenCreeps" + "_" + i, zeroToTenCreeps);
      creepsPerMinMap.put(finalRole + "_" + "tenToTwentyCreeps" + "_" + i, tenToTwentyCreeps);
      creepsPerMinMap.put(finalRole + "_" + "twentyToThirtyCreeps" + "_" + i, twentyToThirtyCreeps);
      creepsPerMinMap.put(finalRole + "_" + "thirtyToEndCreeps" + "_" + i, thirtyToEndCreeps);
    }
    FileWriter writer = new FileWriter(DATA_FILE, true);
    // Write the champion data to the file
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
    // Calculate the total tier score for each team
    int firstTotalTier = 0;
    for (String t : firstTeamTier) {
      if (t.equals("UNRANKED")) {
        firstTotalTier += 0;
      } else if (t.equals("BRONZE")) {
        firstTotalTier += 1;
      } else if (t.equals("SILVER")) {
        firstTotalTier += 2;
      } else if (t.equals("GOLD")) {
        firstTotalTier += 3;
      } else if (t.equals("PLATINUM")) {
        firstTotalTier += 4;
      } else if (t.equals("CHALLENGER")) {
        firstTotalTier += 5;
      }
    }
    int secondTotalTier = 0;
    for (String t : secondTeamTier) {
      if (t.equals("UNRANKED")) {
        secondTotalTier += 0;
      } else if (t.equals("BRONZE")) {
        secondTotalTier += 1;
      } else if (t.equals("SILVER")) {
        secondTotalTier += 2;
      } else if (t.equals("GOLD")) {
        secondTotalTier += 3;
      } else if (t.equals("PLATINUM")) {
        secondTotalTier += 4;
      } else if (t.equals("CHALLENGER")) {
        secondTotalTier += 5;
      }
    }
    writer.write("" + firstTotalTier + ",");
    writer.write("" + secondTotalTier + ",");
    
    // Write the ward data to the output file
    writer.write(sight_jungle_1 + ",");
    writer.write(vision_jungle_1 + ",");
    writer.write(wards_placed_jungle_1 + ",");

    writer.write(sight_support_1 + ",");
    writer.write(vision_support_1 + ",");
    writer.write(wards_placed_support_1 + ",");

    writer.write(sight_jungle_2 + ",");
    writer.write(vision_jungle_2 + ",");
    writer.write(wards_placed_jungle_2 + ",");

    writer.write(sight_support_2 + ",");
    writer.write(vision_support_2 + ",");
    writer.write(wards_placed_support_2 + ",");
    
    // Write the gold/min. data to the output file
    String[] roles = { "TOP", "MIDDLE", "JUNGLE", "DUO_CARRY", "DUO_SUPPORT" };
    String[] timesGold = { "zeroToTenGold", "tenToTwentyGold", 
        "twentyToThirtyGold", "thirtyToEndGold" };
    for (int i = 1; i < 3; i++) {
      for (String role : roles) {
        for (String time : timesGold) {
          writer.write(goldPerMinMap.get(role + "_" + time + "_" + i) + ",");
        }
      }
    }
    
    // Write the creeps/min. data to the output file
    String[] timesCreeps = { "zeroToTenCreeps", "tenToTwentyCreeps", 
        "twentyToThirtyCreeps", "thirtyToEndCreeps" };
    for (int i = 1; i < 3; i++) {
      for (String role : roles) {
        for (String time : timesCreeps) {
          writer.write(creepsPerMinMap.get(role + "_" + time + "_" + i) + ",");
        }
      }
    }

    // Write the first blood to the output file
    if (isSecTeamFirstBlood) {
      writer.write("1" + ",");
    } else {
      writer.write("0" + ",");
    }
    
    // Write the wards killed to the output file
    writer.write(wards_killed_jungle_1 + ",");
    writer.write(wards_killed_support_1 + ",");    
    
    writer.write(wards_killed_jungle_2 + ",");
    writer.write(wards_killed_support_2 + ",");    
    
    // Write the neutral minions killed to the output file
    writer.write(neutral_jungle_1 + ",");
    writer.write(neutral_jungle_2);
    
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

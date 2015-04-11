/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

/**
 *
 * @author david
 */
public class EnsemblRest {
    public static String SERVER = "http://rest.ensembl.org";
    public static final String GRCh37Server = "http://grch37.rest.ensembl.org/";
    public static final JSONParser PARSER = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

    public static int requestCount = 0;
    public static long lastRequestTime = System.currentTimeMillis();
    
    public List<String> getAvailableSpecies()throws ParseException, MalformedURLException, IOException, InterruptedException {
        ArrayList<String> species = new ArrayList<>();
        String endpoint = "/info/species?content-type=application/json";
        JSONObject result = (JSONObject) getJSON(endpoint);
        JSONArray allSpecies = (JSONArray) result.get("species");
        for (Object j: allSpecies){
            JSONObject s = (JSONObject) j;
            String name = (String) s.get("display_name");
            if (name != null){
                species.add(name);
            }
        }
        SpeciesComparator comp = new SpeciesComparator();
        Collections.sort(species, comp);
        return species;
    }
    
    public List<String> getGeneAndSymbolFromTranscript(String id)throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/overlap/id/" + id + "?feature=gene";
        JSONArray genes = (JSONArray) getJSON(endpoint);
        JSONObject gene = (JSONObject)genes.get(0);
        String ensid = (String) gene.get("id");
        String name = (String) gene.get("external_name");
        return Arrays.asList(ensid, name);
    }
  
    public void codingToGenomicTranscript(String species, String id, int c) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject tr = (JSONObject) getJSON(endpoint);
        if (tr.isEmpty()){
            System.out.println("No protein coding transcripts found for " + id);
            return;
        }
        List<String> geneAndSymbol = getGeneAndSymbolFromTranscript(id);  
      
        String seq = getTranscriptSequence(id, "cds");
        if (seq != null){
            if (seq.length() >= c ){
                String gCoord = cdsToGenomicCoordinate(id, c);
                if (gCoord != null){
                    System.out.println(geneAndSymbol.get(1) +" " + geneAndSymbol.get(0) 
                            + " " + id + " c." + c + " => " + gCoord);
                }
            }else{
                System.out.println("CDS coordinate " + c + " is greater than "
                        + "length of CDS (" + seq.length() + ") for " + tr);
            }
        }else{
            System.out.println("ERROR: No CDS sequence found for " + tr);
        }
    }
  
    public void codingToGenomic(String species, String symbol, int c) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String id = getGeneID(species, symbol);
        ArrayList<String> tr = getTranscriptIds(id);
        if (tr.isEmpty()){
            System.out.println("No protein coding transcripts found for " + symbol
                    + " (" + id + ")");
            return;
        }
        StringBuilder transcriptList = new StringBuilder(tr.get(0));
        for (int i = 1; i < tr.size(); i++){
            transcriptList.append(",").append(tr.get(i));
        }
        System.out.println(symbol + " => " + id + " => " + transcriptList.toString());
        for (String t : tr){
            String seq = getTranscriptSequence(t, "cds");
            if (seq != null){
                if (seq.length() >= c ){
                    String gCoord = cdsToGenomicCoordinate(t, c);
                    if (gCoord != null){
                        System.out.println(symbol + " " + id + " " + t + 
                                " c." + c + " => " + gCoord);
                    }
                }else{
                    System.out.println("CDS coordinate " + c + " is greater than "
                            + "length of CDS (" + seq.length() + ") for " + t);
                }
            }else{
                System.out.println("ERROR: No CDS sequence found for " + t);
            }
        }
    }
  
  
    public String getTranscriptSequence(String id, String type) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/sequence/id/" + id + "?type=" + type;
        JSONObject sequence = (JSONObject) getJSON(endpoint);
        if (sequence.containsKey("seq")){
            return (String) sequence.get("seq");
        }
        return null;
    }
  
    public String cdsToGenomicCoordinate(String id, int coord) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/map/cds/" + id +"/"+ coord + ".." + coord;
        JSONObject info = (JSONObject) getJSON(endpoint);
        StringBuilder mapStrings = new StringBuilder();
        if(info.isEmpty()) {
            throw new RuntimeException("Got nothing for endpoint "+endpoint);
        }
        if (info.containsKey("mappings")){
            JSONArray mappings = (JSONArray) info.get("mappings");
            for (Object map: mappings){
                JSONObject m = (JSONObject) map;
                if (m.containsKey("start") && m.containsKey("seq_region_name")){
                    String assembly = (String) m.get("assembly_name");
                    String chr = (String) m.get("seq_region_name");
                    Long start = (Long) m.get("start");
                    mapStrings.append(chr + ":" + start + " (" + assembly + ")\n");
                }
            }
            return mapStrings.toString();
        }
        return null;
    }
  
    public ArrayList<String> getTranscriptIds(String id) throws ParseException, MalformedURLException, IOException, InterruptedException {
        ArrayList<String> transcriptIds = new ArrayList<>();
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject info = (JSONObject) getJSON(endpoint);
        if(info.isEmpty()) {
          throw new RuntimeException("Got nothing for endpoint "+endpoint);
        }
        if (info.containsKey("Transcript")){
            JSONArray trs = (JSONArray) info.get("Transcript");
            for (Object t: trs){
               JSONObject j = (JSONObject) t;
               String biotype = (String)j.get("biotype");
               if (biotype.equals("protein_coding")){
                   transcriptIds.add((String) j.get("id"));
               }
            }
        }
        return transcriptIds;
    }

  
    public JSONArray getVariants(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String id = getGeneID(species, symbol);
        return (JSONArray) getJSON("/overlap/id/"+id+"?feature=variation");
    }

    public String getGeneID(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/xrefs/symbol/"+species+"/"+symbol+"?object_type=gene";
        JSONArray genes = (JSONArray) getJSON(endpoint);
        if(genes.isEmpty()) {
          throw new RuntimeException("Got nothing for endpoint "+endpoint);
        }
        JSONObject gene = (JSONObject)genes.get(0);
        return (String)gene.get("id");
    }

    public Object getJSON(String endpoint) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String jsonString = getContent(endpoint);
        return PARSER.parse(jsonString);
    }

    
    public String getContent(String endpoint) throws MalformedURLException, IOException, InterruptedException {

    if(requestCount == 15) { // check every 15
      long currentTime = System.currentTimeMillis();
      long diff = currentTime - lastRequestTime;
      //if less than a second then sleep for the remainder of the second
      if(diff < 1000) {
        Thread.sleep(1000 - diff);
      }
      //reset
      lastRequestTime = System.currentTimeMillis();
      requestCount = 0;
    }

    URL url = new URL(SERVER+endpoint);
    URLConnection connection = url.openConnection();
    HttpURLConnection httpConnection = (HttpURLConnection)connection;
    httpConnection.setRequestProperty("Content-Type", "application/json");

    InputStream response = httpConnection.getInputStream();
        int responseCode = httpConnection.getResponseCode();

        if(responseCode != 200) {
            if(responseCode == 429 && httpConnection.getHeaderField("Retry-After") != null) {
                double sleepFloatingPoint = Double.valueOf(httpConnection.getHeaderField("Retry-After"));
                double sleepMillis = 1000 * sleepFloatingPoint;
                Thread.sleep((long)sleepMillis);
                return getContent(endpoint);
            }
            throw new RuntimeException("Response code was not 200. Detected response was "+responseCode);
        }

        String output;
        Reader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
            StringBuilder builder = new StringBuilder();
            char[] buffer = new char[8192];
            int read;
            while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                builder.append(buffer, 0, read);
            }
            output = builder.toString();
        } 
        finally {
            if (reader != null) {
                try {
                    reader.close(); 
                } 
                    catch (IOException logOrIgnore) {
                    logOrIgnore.printStackTrace();
                }
            }
        }

        return output;
    }
    
    static class SpeciesComparator<T extends String> implements Comparator<T> {
        
        private static final List<String> SPECIES_ORDER = Arrays.asList(
                "Human", "Mouse", "Rat", "Zebrafish", "Fruitfly");

        public int compare(T s1, T s2) {
            if (s1 == null){
                return 1;
            }
            if (s2 == null){
                return -1;
            }
            if (SPECIES_ORDER.contains(s1)){
                if (SPECIES_ORDER.contains(s2)){
                    return SPECIES_ORDER.indexOf(s1) - SPECIES_ORDER.indexOf(s2);
                }
                return -1;
            }else if (SPECIES_ORDER.contains(s2)){
                return 1;
            }
            return s1.compareTo(s2);
        }
    }
}

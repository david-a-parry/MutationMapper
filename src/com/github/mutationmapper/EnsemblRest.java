/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import com.github.mutationmapper.TranscriptDetails.Exon;
import java.io.BufferedReader;
import java.io.DataOutputStream;
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
import java.util.HashMap;
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
    
    public String getDna(String chrom, int start, int end, String species)throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = String.format("/sequence/region/%s/%s:%d..%d:1?", 
                species, chrom, start, end);
        JSONObject info = (JSONObject) getJSON(endpoint);
        if(info.isEmpty()) {
          throw new RuntimeException("Got nothing for endpoint "+ endpoint);
        }
        if (info.containsKey("seq")){
            return (String) info.get("seq");
        }
        return null;
    }
    
    public GeneDetails getGeneDetails(String id) throws ParseException, MalformedURLException, IOException, InterruptedException {
        GeneDetails gene = new GeneDetails();
        ArrayList<TranscriptDetails> transcripts = new ArrayList<>();
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject info = (JSONObject) getJSON(endpoint);
        if(info.isEmpty()) {
          throw new RuntimeException("Got nothing for endpoint "+ endpoint);
        }
        if (info.containsKey("display_name")){
            gene.setSymbol((String) info.get("display_name"));
        }
        if (info.containsKey("id")){
            gene.setId((String) info.get("id"));
        }
        if (info.containsKey("seq_region_name")){
            gene.setChromosome((String) info.get("seq_region_name"));
        }
        if (info.containsKey("strand")){
            Long strand = (Long) info.get("strand");
            gene.setStrand(strand.intValue());
        }
        if (info.containsKey("start")){
            Long start = (Long) info.get("start");
            gene.setStart(start.intValue());
        }
        if (info.containsKey("end")){
            Long end = (Long) info.get("end");
            gene.setEnd(end.intValue());
        }
        if (info.containsKey("assembly_name")){
            gene.setGenomeBuild((String) info.get("assembly_name"));
        }
        if (info.containsKey("Transcript")){
            JSONArray trs = (JSONArray) info.get("Transcript");
            for (Object t: trs){
               JSONObject j = (JSONObject) t;
               TranscriptDetails trans = getTranscriptDetailsFromJson(j);
               trans.setSymbol(gene.getSymbol());
               trans.setId(gene.getId());
               trans.setGenomeBuild(gene.getGenomeBuild());
               transcripts.add(trans);
            }
        }
        gene.setTranscripts(transcripts);
        return gene;
    }
    
    public TranscriptDetails getTranscriptDetails(String id) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject j = (JSONObject) getJSON(endpoint);
        if(j.isEmpty()) {
          throw new RuntimeException("Got nothing for endpoint "+ endpoint);
        }
        return getTranscriptDetailsFromJson(j);
    }
    
    private TranscriptDetails getTranscriptDetailsFromJson(JSONObject j){
        TranscriptDetails trans = new TranscriptDetails();        
        trans.setTranscriptId((String) j.get("id"));
        String biotype = (String)j.get("biotype");
        if (j.containsKey("strand")){
           if ((Long) j.get("strand") > 0){
               trans.setStrand(1);
           }else{
               trans.setStrand(-1);
           }
       }
       //get exons
       if (j.containsKey("Exon")){
           JSONArray exons = (JSONArray) j.get("Exon");
           for (Object e: exons){
               JSONObject jxon = (JSONObject) e;
               TranscriptDetails.Exon exon = trans.new Exon();
               Long start = (Long) jxon.get("start");
               Long end = (Long) jxon.get("end");
               exon.setStart(start.intValue());
               exon.setEnd(end.intValue());
               trans.getExons().add(exon);
           }
           //sort and number exons
           Collections.sort(trans.getExons());
           for (int i = 0; i < trans.getExons().size(); i++){
               if (trans.getStrand().equals("-")){
                   trans.getExons().get(i).setOrder(
                           trans.getExons().size() - i);
               }else{
                   trans.getExons().get(i).setOrder(i+1);
               }
           }
       }
       
       if (j.containsKey("seq_region_name")){
            trans.setChromosome((String) j.get("seq_region_name"));
        }

      //get transcription start and end
       if (j.containsKey("start")){
           Long start = (Long)j.get("start");
           trans.setTxStart(start.intValue());
       }
       if (j.containsKey("end")){
           Long end = (Long)j.get("end");
           trans.setTxEnd(end.intValue());
       }

       //get translation start and end if coding                   
       if (biotype.equals("protein_coding") && j.containsKey("Translation")){
           JSONObject p = (JSONObject) j.get("Translation");
           trans.setProteinId((String) p.get("id"));
           Long start = (Long) p.get("start");
           Long end = (Long) p.get("end");
           trans.setCdsStart(start.intValue());
           trans.setCdsEnd(end.intValue());
           Long length = (Long) p.get("length");
           trans.setProteinLength(length.intValue());
       }
        return trans;
    }
    
    public List<String> getGeneAndSymbolFromTranscript(String id)throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/overlap/id/" + id + "?feature=gene";
        JSONArray genes = (JSONArray) getJSON(endpoint);
        JSONObject gene = (JSONObject)genes.get(0);
        String ensid = (String) gene.get("id");
        String name = (String) gene.get("external_name");
        return Arrays.asList(ensid, name);
    }
    
    /*returns arraylist of hashmaps of gene symbol, gene ID,
      transcript ID, chromosome, genomic coordinate, genome assembly
    */
    public HashMap<String, String> codingToGenomicTranscript
            (String species, String id, int c) throws ParseException, MalformedURLException, IOException, InterruptedException {
        HashMap<String, String> mapping = new HashMap<>();
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject tr = (JSONObject) getJSON(endpoint);
        if (tr.isEmpty()){
            System.out.println("No protein coding transcripts found for " + id);
            return null;
        }else{
            String biotype = (String)tr.get("biotype");
           if (! biotype.equals("protein_coding")){
              return null;
           }
        }
      
        String seq = getTranscriptSequence(id, "cds");
        if (seq != null){
            if (seq.length() >= c ){
                HashMap<String, String> gCoord = cdsToGenomicCoordinate(id, c);
                if (gCoord != null){
                    mapping.put("transcript", id);
                    mapping.put("chromosome", gCoord.get("chromosome"));
                    mapping.put("coordinate", gCoord.get("coordinate"));
                    mapping.put("assembly", gCoord.get("assembly"));
                }
            }else{
                System.out.println("CDS coordinate " + c + " is greater than "
                        + "length of CDS (" + seq.length() + ") for " + tr);
            }
        }else{
            System.out.println("ERROR: No CDS sequence found for " + tr);
        }
        return mapping;
    }
    
    /*returns arraylist of hashmaps of gene symbol, gene ID,
      transcript ID, chromosome, genomic coordinate, genome assembly
    */
    public ArrayList<HashMap<String, String>> codingToGenomic(String species, String symbol, int c) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String id = getGeneID(species, symbol);
        ArrayList<HashMap<String, String>> coordinates = new ArrayList<>();
        ArrayList<String> tr = getTranscriptIds(id);
        if (tr.isEmpty()){
            System.out.println("No transcripts found for " + symbol
                    + " (" + id + ")");
            return null;
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
                    HashMap<String, String> gCoord = cdsToGenomicCoordinate(t, c);
                    if (gCoord != null){
                        HashMap<String, String> mapping = new HashMap<>();
                        mapping.put("gene", id);
                        mapping.put("transcript", t);
                        mapping.put("chromosome", gCoord.get("chromosome"));
                        mapping.put("coordinate", gCoord.get("coordinate"));
                        mapping.put("assembly", gCoord.get("assembly"));
                        coordinates.add(mapping);
                    }
                }else{
                    System.out.println("CDS coordinate " + c + " is greater than "
                            + "length of CDS (" + seq.length() + ") for " + t);
                }
            }else{
                System.out.println("WARNING: No CDS sequence found for " + t);
            }
        }
        return coordinates;
    }
  
  
    public String getTranscriptSequence(String id, String type) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/sequence/id/" + id + "?type=" + type;
        try{
            JSONObject sequence = (JSONObject) getJSON(endpoint);
            if (sequence.containsKey("seq")){
                return (String) sequence.get("seq");
            }
        }catch(ParseException | IOException | InterruptedException ex){
            ex.printStackTrace();
        }
        return null;
    }
    
    public String getTranscriptSequences(ArrayList<String> ids, String type)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/sequence/id/";
        ArrayList<String> quoted = new ArrayList<>();
        for (String i: ids){
            quoted.add("\"" + i + "\"");
        }
        String s = String.join(", ", quoted);
        String postBody = "{ \"ids\" : [ " + s + " ] }";
        JSONArray seqs = (JSONArray) getPostJSON(endpoint, postBody);
        return seqs.toString();
    }
  
    
    //returns hashmap of chromosome, coordinate and assembly
    public HashMap<String, String> cdsToGenomicCoordinate(String id, int coord) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/map/cds/" + id +"/"+ coord + ".." + coord;
        JSONObject info = (JSONObject) getJSON(endpoint);
        HashMap mapStrings = new HashMap<>();
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
                    mapStrings.put("chromosome", chr);
                    mapStrings.put("coordinate", Long.toString(start));
                    mapStrings.put("assembly", assembly);
                }
            }
            return mapStrings;
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
               transcriptIds.add((String) j.get("id"));
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

    public Object getPostJSON(String endpoint, String postBody) 
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        String jsonString = getPostContent(endpoint, postBody);
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
    
    public String getPostContent(String endpoint, String postBody) throws MalformedURLException, IOException, InterruptedException {

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
        httpConnection.setRequestMethod("POST");
        httpConnection.setRequestProperty("Content-Type", "application/json");
        httpConnection.setRequestProperty("Accept", "application/json");
        httpConnection.setRequestProperty("Content-Length", Integer.toString(postBody.getBytes().length));
        httpConnection.setUseCaches(false);
        httpConnection.setDoInput(true);
        httpConnection.setDoOutput(true);
        
        DataOutputStream wr = new DataOutputStream(httpConnection.getOutputStream());
        wr.writeBytes(postBody);
        wr.flush();
        wr.close();

        InputStream response = connection.getInputStream();
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

/*
 * Copyright (C) 2015 David A. Parry <d.a.parry@leeds.ac.uk>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
    public static final String DEFAULTSERVER = "http://rest.ensembl.org";
    public static final String GRCh37Server = "http://grch37.rest.ensembl.org/";
    public static final JSONParser PARSER = new JSONParser(JSONParser.MODE_JSON_SIMPLE);

    public static int requestCount = 0;
    public static long lastRequestTime = System.currentTimeMillis();
    
    public void setDefaultServer(){
        SERVER = DEFAULTSERVER;
    }
    
    public void setGRCh37Server(){
        SERVER = GRCh37Server;
    }
    
    public String getServer(){
        return SERVER;
    }
    
    public HashMap<String, String> getAvailableSpecies()
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        HashMap<String, String> species = new HashMap<>();
        String endpoint = "/info/species?content-type=application/json";
        JSONObject result = (JSONObject) getJSON(endpoint);
        JSONArray allSpecies = (JSONArray) result.get("species");
        for (Object j: allSpecies){
            JSONObject s = (JSONObject) j;
            String d_name = (String) s.get("display_name");
            String s_name = (String) s.get("name");
            if (d_name != null && s_name != null){
                species.put(d_name, s_name);
            }
        }
        /*
        SpeciesComparator comp = new SpeciesComparator();
        Collections.sort(species, comp);
        */
        return species;
    }
    
    public String getDna(String chrom, int start, int end, String species)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        return getDna(chrom, start, end, species, 1);
    }
    
    public String getDna(String chrom, int start, int end, String species, int strand)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = String.format("/sequence/region/%s/%s:%d..%d:%d?", 
                species, chrom, start, end, strand);
        JSONObject info = (JSONObject) getJSON(endpoint);
        if(info.isEmpty()) {
          throw new RuntimeException(String.format("Could not retrieve DNA for "
                  + "%s/%s$d-%d.\nRetrieval from URL %s%s returned nothing.", 
                  species, chrom, start, end, SERVER, endpoint));
        }
        if (info.containsKey("seq")){
            return (String) info.get("seq");
        }
        throw new RuntimeException(String.format("Could not retrieve DNA for "
                  + "%s/%s$d-%d.\nRetrieval from URL %s%s did not provide "
                + "sequence information.", 
                  species, chrom, start, end, SERVER, endpoint));
    }
    
    public GeneDetails getGeneDetails(String id) throws ParseException, MalformedURLException, IOException, InterruptedException {
        GeneDetails gene = new GeneDetails();
        ArrayList<TranscriptDetails> transcripts = new ArrayList<>();
        String endpoint = "/lookup/id/"+id+"?expand=1";
        JSONObject info = (JSONObject) getJSON(endpoint);
        if(info.isEmpty()) {
          throw new RuntimeException(String.format("Could not retrieve gene details "
                  + " for %s.\nRetrieval from URL %s%s returned nothing.", 
                  id, SERVER, endpoint));
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
    
    public TranscriptDetails getTranscriptDetails(String id) 
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        /*
        need to get gene details first because rest won't return translation 
        details from a transcript ID
        */
        id = id.replaceFirst("\\.\\d+$", "");
        List<String> idName = getGeneAndSymbolFromTranscript(id);
        GeneDetails gene = getGeneDetails(idName.get(0));
        for (TranscriptDetails t: gene.getTranscripts()){
            if (t.getTranscriptId().equalsIgnoreCase(id)){
                return t;
            }
        }
        return new TranscriptDetails();
    }
    
    private TranscriptDetails getTranscriptDetailsFromJson(JSONObject j)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        TranscriptDetails trans = new TranscriptDetails();        
        trans.setTranscriptId((String) j.get("id"));
        String biotype = (String)j.get("biotype");
        trans.setBiotype(biotype);
        if (j.containsKey("is_canonical")){
            String isCanon = j.get("is_canonical").toString();
            if (Integer.parseInt(isCanon) > 0){
                trans.setIsCanonical(true);
            }else{
                trans.setIsCanonical(false);
            }
        }
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
               if (trans.getStrand() < 0){
                   trans.getExons().get(i).setOrder(
                           trans.getExons().size() - i);
               }else{
                   trans.getExons().get(i).setOrder(i+1);
               }
           }
       }
       
       //get chromosome
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
       if (j.containsKey("Translation")){
           JSONObject p = (JSONObject) j.get("Translation");
           trans.setProteinId((String) p.get("id"));
           Long start = (Long) p.get("start");
           Long end = (Long) p.get("end");
           trans.setCdsStart(start.intValue());
           trans.setCdsEnd(end.intValue());
           Long length = (Long) p.get("length");
           trans.setProteinLength(length.intValue());
       }else{
           //if using a transcript id for some reason rest won't return translation
       }
       return trans;
    }
    
    public List<String> getGeneAndSymbolFromTranscript(String id)throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/lookup/id/" + id ;
        JSONObject gene = (JSONObject) getJSON(endpoint);
        String symbol = new String();
        if (gene.containsKey("display_name")){
            String display_name = (String) gene.get("display_name");
            symbol = display_name.split("-")[0];
        }
        endpoint = "/overlap/id/" + id + "?feature=gene";
        JSONArray genes = (JSONArray) getJSON(endpoint);
        if (genes.isEmpty()){
            throw new RuntimeException(String.format("Could not get gene details "
                  + "for transcript %s.\nRetrieval from URL %s%s returned nothing.", 
                  id, SERVER, endpoint));
        }
        for (Object o: genes){
            JSONObject g = (JSONObject) o;
            String name = (String) g.get("external_name");
            if (name.equalsIgnoreCase(symbol)){
                String ensid = (String) g.get("id");
                return (Arrays.asList(ensid, symbol));
            }
        }
        //failed to get ensgene id 
        return (Arrays.asList("", symbol));
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
            throw new RuntimeException(String.format("Could not get transcript details "
                  + "for %s.\nRetrieval from URL %s%s returned nothing.", 
                  id, SERVER, endpoint));
        }
        String biotype = (String)tr.get("biotype");
        mapping.put("transcript", id);
        String seq;
        String cdsOrCdna;
        if (! tr.containsKey("Translation")){
           cdsOrCdna = "cdna";
        }else{
            cdsOrCdna = "cds";
        }
        seq = getTranscriptSequence(id, cdsOrCdna);
            
        if (seq != null){
            if (seq.length() >= c ){
                HashMap<String, String> gCoord = transcriptToGenomicCoordinate(id, c, cdsOrCdna);
                if (gCoord != null){
                    mapping.put("transcript", id);
                    mapping.put("chromosome", gCoord.get("chromosome"));
                    mapping.put("coordinate", gCoord.get("coordinate"));
                    mapping.put("assembly", gCoord.get("assembly"));
                }
            }else{
                //System.out.println("CDS coordinate " + c + " is greater than "
                //        + "length of CDS (" + seq.length() + ") for " + id);
                mapping.put("chromosome", "coordinate greater than length "
                        + "of " + cdsOrCdna.toUpperCase());
                mapping.put("coordinate", "");
                mapping.put("assembly", String.format("%d", seq.length()));
            }
        }else{
            mapping.put("chromosome", "No " + cdsOrCdna.toUpperCase() + 
                    " sequence found(?)");
            mapping.put("coordinate", "");
            mapping.put("assembly", "");
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
            HashMap<String, String> mapping = codingToGenomicTranscript(species, t, c);
            coordinates.add(mapping);
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
    public HashMap<String, String> transcriptToGenomicCoordinate(String id, int coord,
            String type)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/map/" + type + "/" + id +"/"+ coord + ".." + coord;
        JSONObject info = (JSONObject) getJSON(endpoint);
        HashMap mapStrings = new HashMap<>();
        if(info.isEmpty()) {
            throw new RuntimeException(String.format("Mapping of CDS coordinate "
                  + "%d for %s failed.\nURL %s%s returned nothing.", 
                  coord, id, SERVER, endpoint));
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
            throw new RuntimeException(String.format("Could not retrieve transcript"
                  + " IDs for %s.\nURL %s%s returned nothing.", 
                  id, SERVER, endpoint));
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
    
    public ArrayList<String> getRefSeqIds(String id)
            throws ParseException, MalformedURLException, IOException, InterruptedException {
        ArrayList<String> transcriptIds = new ArrayList<>();
        String endpoint = String.format("/xrefs/id/%s?external_db=RefSeq_mRNA", id);
        JSONArray info = (JSONArray) getJSON(endpoint);
        for (Object o: info){
            JSONObject j = (JSONObject) o;
            if (j.containsKey("display_id")){
                transcriptIds.add((String) j.get("display_id"));
            }
        }
        return transcriptIds;
    }
    
    public HashMap<String, String> getEnsemblFromRefSeqId(String id)throws ParseException, 
            MalformedURLException, IOException, InterruptedException {
        HashMap<String, String> ensIds = new HashMap<>();
        String endpoint = String.format("/xrefs/symbol/homo_sapiens/%s", id);
        JSONArray info = (JSONArray) getJSON(endpoint);
        for (Object o: info){
            JSONObject j = (JSONObject) o;
            if (j.containsKey("id") && j.containsKey("type")){
                ensIds.put((String) j.get("type"), (String) j.get("id"));
            }
        }
        return ensIds;
    }
    
    //we use the POST method because other methods don't cope with larger indels
    public HashMap<String, HashMap<String, String>> getVepConsequence(String chrom, int coord, 
            String species, String ref, String alt) throws ParseException, 
            MalformedURLException, IOException, InterruptedException {
        HashMap<String, HashMap<String, String>> results = new HashMap<>();
        String endpoint = "/vep/" + species +"/region?hgvs=1;canonical=1;"
                + "numbers=1;xref_refseq=1;miRNA=1;domains=1;ExAC=1;Blosum62=1;"
                + "MaxEntScan=1;GeneSplicer=1";
        String post = String.format("{ \"variants\" : [\"%s  %d  . %s %s . . .\" ] }", 
                chrom, coord, ref, alt);
        JSONArray info = (JSONArray) getPostJSON(endpoint, post);
        if (info.isEmpty()){
            throw new RuntimeException(String.format("Could not retrieve variant "
                  + " consequences for %s:%d %s/%s.\nURL %s%s with POST %s returned nothing.", 
                  chrom, coord, ref, alt, SERVER, endpoint, post));
        }
        for (Object j: (JSONArray) info){
            JSONObject result = (JSONObject) j;
            if (result.containsKey("transcript_consequences")){
                for (Object k: (JSONArray) result.get("transcript_consequences")){
                    JSONObject cons = (JSONObject) k;
                    HashMap<String, String> consMap = new HashMap<>();
                    for (String key: cons.keySet()){
                        //results.put(key, cons.get(key).toString());
                        consMap.put(key, cons.get(key).toString());
                    }
                    consMap.put("ref", ref);
                    consMap.put("alt", alt);
                    results.put(cons.get("transcript_id").toString(), consMap);
                }
            }
            if (result.containsKey("colocated_variants")){
                for (Object k: (JSONArray) result.get("colocated_variants")){
                    HashMap<String, String> snpMap = new HashMap<>();
                    JSONObject snp = (JSONObject) k;
                    for (String key: snp.keySet()){
                        //results.put(key, cons.get(key).toString());
                        snpMap.put(key, snp.get(key).toString());
                    }
                    results.put("snps_" + snpMap.get("id"), snpMap);
                }
                
            }
            if (result.containsKey("most_severe_consequence")){
                //complicated by the fact that we are just returning a single HashMap of HashMaps.
                HashMap<String, String> msc = new HashMap<>();
                String csq = (String) result.get("most_severe_consequence");
                msc.put("most_severe_consequence", csq);
                results.put("Consequence", msc);
            }
        }
        return results;
    }
    
    public JSONArray getVariants(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String id = getGeneID(species, symbol);
        return (JSONArray) getJSON("/overlap/id/"+id+"?feature=variation");
    }

    public String getGeneID(String species, String symbol) throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/xrefs/symbol/"+species+"/"+symbol+"?object_type=gene";
        JSONArray genes = (JSONArray) getJSON(endpoint);
        if(genes.isEmpty()) {
            throw new RuntimeException(String.format("Could not retrieve gene ID"
                  + " for %s (%s).\nRetrieval from URL %s%s returned nothing.", 
                  symbol, species, SERVER, endpoint));
        }
        ArrayList<String> mismatches = new ArrayList<>();
        //sometimes a synonym might be similar enough to get you the wrong gene - e.g. PRF1 vs PRF-1
        for (Object o : genes){
            JSONObject gene = (JSONObject) o;
            String sym = getGeneSymbol( (String) gene.get("id"));
            if (sym.equalsIgnoreCase(symbol)){
                return (String)gene.get("id");
            }else if (!sym.isEmpty()){
                mismatches.add(sym);
            }
        }
        if (!mismatches.isEmpty()){
            String msg = String.format("Could not find an exact match"
                  + " for gene symbol %s. Found %s.", symbol, String.join(", ", mismatches));
            if (species.equalsIgnoreCase("Caenorhabditis_elegans") && 
                    symbol.matches("\\w+(.\\d){1,2}")){
                msg = msg + ". Please note that C. elegans transcript IDs are not supported.";
            }
            throw new RuntimeException(msg);
        }else{
            throw new RuntimeException(String.format("Could not find a match"
                  + " for gene symbol %s.", symbol)); 
        }
    }
    
    public String getGeneSymbol(String id)
             throws ParseException, MalformedURLException, IOException, InterruptedException {
        String endpoint = "/lookup/id/"+id+"?object_type=gene";
        JSONObject gene = (JSONObject) getJSON(endpoint);
        if (gene.containsKey("display_name")){
            return (String) gene.get("display_name");
        }
        return "";
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
            throw new RuntimeException(String.format("Error retrieving content from %s%s. "
                    + "Detected response was %s", SERVER, endpoint, responseCode));
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
                }catch (IOException logOrIgnore) {
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
            throw new RuntimeException(String.format("Error retrieving content from %s%s. "
                    + "Detected response was %s", SERVER, endpoint, responseCode));
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
    
}

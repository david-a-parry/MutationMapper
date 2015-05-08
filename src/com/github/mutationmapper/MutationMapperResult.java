    /*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.util.HashMap;

/**
 *
 * @author david
 */
public class MutationMapperResult {
    private String index;
    private String chromosome;
    private Integer coordinate;
    private String genome;
    private String geneSymbol;
    private String geneId;
    private String transcript;
    private String cdsCoordinate;
    private String matchingSequence;
    private String mutation;
    private String refAllele;
    private String varAllele;
    private String consequence;
    private String cdsConsequence;
    private String proteinConsequence;
    private String polyphenResult;
    private String siftResult;
    private String mostSevereConsequence;
    private String knownIds;
    private Double knownFreq;
    private String biotype;
    private String refSeqIds;
    private boolean isCanonical = false;
    private String exonIntronNumber;
    private HashMap<String, String> vepResults;
    private String description;
    
    public void setIndex(String i){
        index = i;
    }
    
    public void setChromosome(String chrom){
        chromosome = chrom;
    }
    
    public void setCoordinate(Integer coord){
        coordinate = coord;
    }
    
    public void setGenome(String build){
        genome = build;
    }
    
    public void setGeneSymbol(String symbol){
        geneSymbol = symbol;
    }
    
    public void setGeneId(String id){
        geneId = id;
    }
    
    public void setTranscript(String id){
        transcript = id;
    }
    
    public void setCdsCoordinate(String c){
        cdsCoordinate = c;
    }
    
    public void setMatchingSequence(String seq){
        matchingSequence = seq;
    }
    
    public void setMutation(String seq){
        mutation = seq;
    }
    
    public void setRefAllele(String ref){
        refAllele = ref;
    }
    
    public void setVarAllele(String var){
        varAllele = var;
    }
    
    public void setConsequence(String cons){
        consequence = cons;
    }
    
    public void setCdsConsequence(String ccons){
        cdsConsequence = ccons;
    }
    
    public void setProteinConsequence(String pcons){
        proteinConsequence = pcons;
    }
    
    public void setPolyphenResult(String result){
        polyphenResult = result;
    }
    
    public void setSiftResult(String result){
        siftResult = result;
    }
    
    public void setMostSevereConsequence(String consequence){
        mostSevereConsequence = consequence;
    }
    
    public void setKnownIds(String id){
        knownIds = id;
    }
    
    public void setKnownFreq(Double freq){
        knownFreq = freq;
    }
    
    public void setBiotype(String type){
        biotype = type;
    }
    
    public void setRefSeqIds(String refId){
        refSeqIds = refId;
    }
    
    public void setIsCanonical(boolean canonical){
        isCanonical = canonical;
    }
    
    public void setExonIntronNumber(String num){
        exonIntronNumber = num;
    }
    
    public void setVepResults(HashMap<String, String> results){
        vepResults = results;
    }
    
    public void setDescription(String d){
        description = d;
    }
    
    public String getIndex(){
        return index;
    }
    
    public String getChromosome(){
        return chromosome;
    }
    
    public Integer getCoordinate(){
        return coordinate;
    }
    
    public String getGenome(){
        return genome;
    }
    
    public String getGeneSymbol(){
        return geneSymbol;
    }
    
    public String getGeneId(){
        return geneId;
    }
    
    public String getTranscript(){
        return transcript;
    }
    
    public String getCdsCoordinate(){
        return cdsCoordinate;
    }
    
    public String getMatchingSequence(){
        return matchingSequence;
    }
    
    public String getMutation(){
        return mutation;
    }
    
    public String getRefAllele(){
        return refAllele;
    }
    
    public String getVarAllele(){
        return varAllele;
    }
    
    public String getConsequence(){
        return consequence;
    }
    
    public String getCdsConsequence(){
        return cdsConsequence;
    }
    
    public String getProteinConsequence(){
        return proteinConsequence;
    }
    
    public String getPolyphenResult(){
        return polyphenResult;
    }
    
    public String getSiftResult(){
        return siftResult;
    }
    
    public String getMostSevereConsequence(){
        return mostSevereConsequence;
    }
    
    public String getSeqInput(){
        if (matchingSequence != null){
            if (mutation != null){
                return matchingSequence + "/" + mutation; 
            }
            return matchingSequence;
        }
        return null;
    }
    
    public String getKnownIds(){
        return knownIds;
    }
    
    public Double getKnownFreq(){
        return knownFreq;
    }
    
    public String getBiotype(){
        return biotype;
    }
    
    public String getRefSeqIds(){
        return refSeqIds;
    }
    
    public String getRefSeqIfAvailable(){
        if (refSeqIds != null && !refSeqIds.isEmpty()){
            return refSeqIds;
        }
        return transcript;
    }
    
    public boolean getIsCanonical(){
        return isCanonical;
    }
    
    public String getExonIntronNumber(){
        return exonIntronNumber;
    }
    
    public String getGenomicCoordinate(){
        if (chromosome == null ){
            return "";
        }
        if (coordinate == null){//we use this as an indicator that we didn't get a coordinate but a message instead
            return chromosome  + " (" + genome + ")";
        }
        String c;
        if (matchingSequence != null && !matchingSequence.isEmpty()){
            int endPos = coordinate + matchingSequence.length() - 1;
            c = String.format("%d-%d", coordinate, endPos);
        }else{
            c = coordinate.toString();
        }
        if (genome == null){
            return chromosome + ":" + c;
        }
        return chromosome + ":" + c + " (" + genome + ")";
    }
    
    public String getKnownVar(){
        if (knownIds == null){
            return "";
        }
        if (knownFreq == null){
            return knownIds;
        }
        return knownIds + " (" + knownFreq + ")";
    }
    
   
    public String getConsequences(){
        if (cdsConsequence == null){
            return null;
        }
        if (proteinConsequence == null){
            return cdsConsequence;
        }
        return cdsConsequence + ";" + proteinConsequence;
    }
    
    public HashMap<String, String> getVepResults(){
        return vepResults;
    }
    
    public String getDescription(){
        return description;
    }
}

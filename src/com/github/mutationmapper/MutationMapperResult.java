/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author david
 */
public class MutationMapperResult {
    private Integer index;
    private String chromosome;
    private Integer coordinate;
    private String genome;
    private String geneSymbol;
    private String geneId;
    private ArrayList<String> transcripts;
    private HashMap<String, Integer> cdsCoordinates;
    private String matchingSequence;
    private String mutation;
    private String refAllele;
    private String varAllele;
    private HashMap<String, String> cdsConsequences;
    private HashMap<String, String> proteinConsequences;
    private String knownId;
    private Double knownFreq;
    
    MutationMapperResult(){
        this (null, null, null, null, null, null, null, null, 
              null, null, null, null, null, null, null);
    }
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, ArrayList<String> transIds, HashMap<String, Integer> cdsCoords){
        this (chrom, coord, build, symbol, id, transIds, cdsCoords, null, null, 
                null, null, null, null, null, null);
    }
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, ArrayList<String> transIds, HashMap<String, Integer> cdsCoords, String seq, String mut,
            String ref, String var ){
        this (chrom, coord, build, symbol, id, transIds, cdsCoords, seq, mut, ref, 
                var, null, null, null, null);
    }
    
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, ArrayList<String> transIds, HashMap<String, Integer> cdsCoords, String seq, String mut, 
            String ref, String var, HashMap<String, String> cCons, HashMap<String, String> pCons, String snpId, Double freq){
        
        chromosome = chrom;
        coordinate = coord;
        genome = build;
        geneSymbol = symbol;
        geneId = id;
        if (transIds == null){
            transcripts = new ArrayList<>();
        }else{
            transcripts = transIds;            
        }
        if (cdsCoords == null){
            cdsCoordinates = new HashMap<>();
        }else{
            cdsCoordinates = cdsCoords;
        }
        matchingSequence = seq;
        mutation = mut;
        refAllele = ref;
        varAllele = var;
        if (cCons == null){
            cdsConsequences = new HashMap<>();
        }else{
            cdsConsequences = cCons;
        }
        if (cCons == null){
            proteinConsequences = new HashMap<>();
        }else{
            proteinConsequences = pCons;
        }            
        knownId = snpId;
        knownFreq = freq;
    }
    
    public void setIndex(Integer i){
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
    
    public void setTranscripts(ArrayList<String> ids){
        transcripts = ids;
    }
    
    public void setCdsCoordinates(HashMap<String, Integer> c){
        cdsCoordinates = c;
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
    
    public void setCdsConsequences(HashMap<String, String> cons){
        cdsConsequences = cons;
    }
    
    public void setProteinConsequences(HashMap<String, String> pcons){
        proteinConsequences = pcons;
    }
    
    public void setKnownId(String id){
        knownId = id;
    }
    
    public void setKnownFreq(Double f){
        knownFreq = f;
    }
    
    public Integer getIndex(){
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
    
    public ArrayList<String> getTranscripts(){
        return transcripts;
    }
    
    public HashMap<String, Integer> getCdsCoordinates(){
        return cdsCoordinates;
    }
    
    public String getMatchingSequences(){
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
    
    public HashMap<String, String> getCdsConsequences(){
        return cdsConsequences;
    }

    public HashMap<String, String> getProteinConsequences(){
        return proteinConsequences;
    }
    
    public String getKnownId(){
        return knownId;
    }
    
    public Double getKnownFreq(){
        return knownFreq;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

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
    private String transcript;
    private Integer cdsCoordinate;
    private String matchingSequence;
    private String mutation;
    private String refAllele;
    private String varAllele;
    private String cdsConsequence;
    private String proteinConsequence;
    private String knownId;
    private Double knownFreq;
    
    MutationMapperResult(){
        this (null, null, null, null, null, null, null, 
              null, null, null, null, null, null, null);
    }
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, String transId, Integer cdsCoord){
        this (chrom, coord, build, symbol, id, transId, cdsCoord, null, null, 
                null, null, null, null, null);
    }
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, String transId, Integer cdsCoord, String seq, String mut,
            String ref, String var ){
        this (chrom, coord, build, symbol, id, transId, cdsCoord, seq, mut, ref, 
                var, null, null, null);
    }
    
    
    MutationMapperResult(String chrom, Integer coord, String build, String symbol,
            String id, String transId, Integer cdsCoord, String seq, String mut, 
            String ref, String var, String pCons, String snpId, Double freq){
        
        chromosome = chrom;
        coordinate = coord;
        genome = build;
        geneSymbol = symbol;
        geneId = id;
        transcript = transId;
        cdsCoordinate = cdsCoord;
        matchingSequence = seq;
        mutation = mut;
        refAllele = ref;
        varAllele = var;
        proteinConsequence = pCons;
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
    
    public void setTranscript(String id){
        transcript = id;
    }
    
    public void setCdsCoordinate(Integer c){
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
    
    public void setProteinConsequence(String pcons){
        proteinConsequence = pcons;
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
    
    public String getTranscript(){
        return transcript;
    }
    
    public Integer getCdsCoordinate(){
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
    
    public String getProteinConsequence(){
        return proteinConsequence;
    }
    
    public String getKnownId(){
        return knownId;
    }
    
    public Double getKnownFreq(){
        return knownFreq;
    }
    
}

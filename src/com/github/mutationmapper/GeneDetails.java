/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.mutationmapper;

import java.util.List;

/**
 *
 * @author david
 */
public class GeneDetails {
    private String symbol;
    private String id;
    private String chrom;
    private Integer strand;
    private Integer start;
    private Integer end;
    private List<TranscriptDetails> transcripts;
    private String genomeBuild;
    
    public void setSymbol(String geneSymbol){
        symbol = geneSymbol;
    }
    public void setId(String geneId){
        id = geneId;
    }
    public void setChromosome(String chromosome){
        chrom = chromosome;
    }
    public void setStrand(Integer geneStrand){
        strand = geneStrand;
    }
    public void setStart(Integer geneStart){
        start = geneStart;
    }
    public void setEnd(Integer geneEnd){
        end = geneEnd;
    }
    public void setTranscripts(List<TranscriptDetails> tr){
        transcripts = tr;
    }
    public void setGenomeBuild(String genome){
        genomeBuild = genome;
    }
    
    public String getSymbol(){
        return symbol;
    }
    public String getId(){
        return id;
    }
    public Integer getStrand(){
        return strand;
    }
    public String getChromosome(){
        return chrom;
    }
    public Integer getStart(){
        return start;
    }
    public Integer getEnd(){
        return end;
    }    
    public List<TranscriptDetails> getTranscripts(){
        return transcripts;
    }
    public String getGenomeBuild(){
        return genomeBuild;
    }
    
}

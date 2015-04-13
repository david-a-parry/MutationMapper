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
public class TranscriptDetails extends GeneDetails {
    
    String transcriptId;
    String proteinId;
    Integer proteinLength;
    Boolean isCanonical;
    
    public void setTranscriptId(String id){
        transcriptId = id;
    }
    
    public void setProteinId(String id){
        proteinId = id;
    }
    
    public void setProteinLength(Integer l){
        proteinLength = l;
    }
    
    public void setIsCanonical(boolean canonical){
        isCanonical = canonical;
    }
    
    public String getTranscriptId(){
        return transcriptId;
    }
    
    public String getProteinId(){
        return proteinId;
    }
    
    public Integer getProteinLength(){
        return proteinLength;
    }
    
    public Boolean getIsCanonical(){
        return isCanonical;
    }
    
    public Integer getCodingLength(){
        int length = 0;
        for (Exon c: getCodingRegions()){
            length += c.getLength();
        }
        return length;
    }
}

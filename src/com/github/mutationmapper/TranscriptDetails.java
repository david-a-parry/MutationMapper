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
    
    public void setTranscriptId(String id){
        transcriptId = id;
    }
    
    public String getTranscriptId(){
        return transcriptId;
    }
}

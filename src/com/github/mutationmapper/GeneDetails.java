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

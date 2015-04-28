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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author David A. Parry <d.a.parry@leeds.ac.uk>
 */

// all coordinates are 0-based
public class TranscriptDetails {
    private String symbol;
    private String id;
    private String chrom;
    private Integer strand;
    private Integer txStart;
    private Integer txEnd;
    private Integer cdsStart;
    private Integer cdsEnd;
    private Integer totalExons;
    private String transcriptId;
    private String proteinId;
    private Integer proteinLength;
    private Boolean isCanonical;
    private String genomeBuild;
    private String biotype;
    private ArrayList<Exon> exons = new ArrayList<>();
    
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
    public void setTxStart(Integer geneTxStart){
        txStart = geneTxStart;
    }
    public void setTxEnd(Integer geneTxEnd){
        txEnd = geneTxEnd;
    }
    public void setCdsStart(Integer geneCdsStart){
        cdsStart = geneCdsStart;
    }
    public void setCdsEnd(Integer geneCdsEnd){
        cdsEnd = geneCdsEnd;
    }
    public void setTotalExons(Integer geneTotalExons){
        totalExons = geneTotalExons;
    }
    public void setTotalExons(){
        totalExons = exons.size();
    }
    public void setExons(ArrayList<Exon> geneExons){
        exons = geneExons;
    }
    
    public void setExons(String starts, String ends) throws GeneExonsException{
        List<String> s = Arrays.asList(starts.split(","));
        List<String> e = Arrays.asList(ends.split(","));
        if (s.isEmpty() || e.isEmpty()){
            throw new GeneExonsException("No exons found in strings passed to setExons method.");
        }
        if (s.size() != e.size()){
            throw new GeneExonsException("Number of exons starts does not equal "
                    + "number of exon ends from strings passed to setExons method.");
        }
        for (int i = 0; i < s.size(); i++){
            int order;
            if (strand.equals(-1)){
                order = s.size() - i;
            }else{
                order = i + 1;
            }
            Exon exon = new Exon(Integer.parseInt(s.get(i)), 
                    Integer.parseInt(e.get(i)), order);
            exons.add(exon);
        }
    }
    
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
    
    public void setBiotype(String b){
        biotype = b;
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
    public Integer getTxStart(){
        return txStart;
    }
    public Integer getTxEnd(){
        return txEnd;
    }
    public Integer getCdsStart(){
        return cdsStart;
    }
    public Integer getCdsEnd(){
        return cdsEnd;
    }
    public Integer getTotalExons(){
        return totalExons;
    }
    public ArrayList<Exon> getExons(){
        return exons;
    }
    
    public ArrayList<Exon> getCodingRegions(){
        ArrayList<Exon> codingExons = new ArrayList<>();
        for (Iterator<Exon> it = exons.iterator(); it.hasNext();) {            
            Exon exon = it.next();
            if (exon.getEnd() < cdsStart){
                //whole exon before cds, skip
                //whole exon before cds, skip
            }else if (exon.getStart() < cdsStart){//cds start is in this exon
                if (exon.getEnd() <= cdsEnd){
                    codingExons.add(new Exon(cdsStart, exon.getEnd(), 
                            exon.getOrder()));
                }else{
                    codingExons.add(new Exon(cdsStart, cdsEnd, exon.getOrder()));
                }
            }else if (exon.getStart() < cdsEnd){//within cds
                if (exon.getEnd() > cdsEnd){
                    codingExons.add(new Exon(exon.getStart(), cdsEnd, 
                            exon.getOrder()));
                }else{
                    codingExons.add(new Exon(exon.getStart(), exon.getEnd(),
                    exon.getOrder()));
                }
            }else{
                //outside CDS
                //outside CDS
            }
        }
        return codingExons;
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
    
    public boolean isCoding(){
        if (cdsStart == null || cdsEnd == null){
            return false;
        }
        return cdsStart < cdsEnd;
    }
    
    public String getBiotype(){
        return biotype;
    }
    
    public String getGenomeBuild(){
        return genomeBuild;
    }
    
    
    public String getCdsPosition(String chrom, int pos){
        if (! isCoding()){
            return "non-coding (" + getBiotype() + ")";
        }
        if (pos < getTxStart() || pos > getTxEnd()){
            return "outside transcribed region";
        }
        if (pos < getCdsStart() || pos  > getCdsEnd()){
            return getUtrPosition(chrom, pos);
        }
        ArrayList<Exon> cds = getCodingRegions();
        if (!getChromosome().equals(chrom)){
            return "chromosome does not match";
        }
        
        int cds_pos = 0;
        int intron_pos = 0;
        StringBuilder cds_string = new StringBuilder();

        boolean isExonic = false;
        
        for (int i = 0; i < cds.size(); i++){
            if ( pos < cds.get(i).getStart()){//pos is before this exons start
                break;
            }
            if (pos > cds.get(i).getEnd()){//pos is after this exon
                cds_pos += cds.get(i).getLength();
            }else{//pos is within exon
                cds_pos += pos - cds.get(i).getStart() + 1;
                isExonic = true; 
                break;
            }
        }
        
        if (!isExonic){
            for (int i = 0; i < cds.size() -1; i++){
                if (pos >= cds.get(i).getEnd() && pos < cds.get(i+1).getStart()){
                    Integer donor_pos = pos - cds.get(i).getEnd();
                    Integer acceptor_pos = pos - cds.get(i+1).getStart();
                    if (Math.abs(donor_pos) <= Math.abs(acceptor_pos)){
                        intron_pos = donor_pos;
                    }else{
                        intron_pos = acceptor_pos;
                        if (cds_pos < getCodingLength()){
                            cds_pos++;
                        }else{
                            /*
                            intronic position is actually after STOP codon
                            but pre the first UTR exon
                            */
                            if (getStrand() < 0){// on - strand so actually 5'UTR
                                intron_pos *= -1;
                                cds_string.append("c.-1");
                            }else{//if on + strand is 3' UTR
                                cds_string.append("c.*1");
                            }
                            if (intron_pos > 0){
                                cds_string.append("+");
                            }
                            cds_string.append(intron_pos);
                            return cds_string.toString();
                        }
                    }
                }
            }
        }
        
        if (getStrand() < 0){
            intron_pos *= -1;
            cds_pos = getCodingLength() - cds_pos + 1;
        }
        cds_string.append("c.").append(cds_pos);
        if (intron_pos != 0){
            if (intron_pos > 0){
                cds_string.append("+");
            }
            cds_string.append(intron_pos);
        }
        return cds_string.toString();
    }
    
    public String getUtrPosition(String chrom, int pos){
        if (! isCoding()){
            return "non-coding (" + getBiotype() + ")";
        }
        if (pos > getCdsStart() && pos  <= getCdsEnd()){
            //Should we throw an exepction here?
            return "not UTR";
        }
        if (!getChromosome().equals(chrom)){
            return "chromosome does not match";
        }
        StringBuilder utr_string = new StringBuilder();
        int utr_pos = 0;
        int intron_pos = 0;
        
        ArrayList<Exon> exons = getExons();
        boolean isExonic = false;
        if (pos < getCdsStart()){
            int utr_length = 0;
            for (int i = 0; i < exons.size(); i++){
                if (exons.get(i).getStart() > getCdsStart()){//exon is in CDS
                    break;
                }
                if (exons.get(i).getEnd() < getCdsStart()){//whole exon is pre-CDS
                    utr_length += exons.get(i).getLength();
                    if (pos > exons.get(i).getEnd()){
                        utr_pos += exons.get(i).getLength();
                    }else if(pos >= exons.get(i).getStart()){
                        utr_pos += pos - exons.get(i).getStart() ;
                        isExonic = true;
                    }
                }else{//cds start is within exon
                    utr_length += getCdsStart() - exons.get(i).getStart() ;
                    if (pos >= exons.get(i).getStart()){
                        utr_pos += pos - exons.get(i).getStart() ;
                        isExonic = true;
                    }
                    break;
                }
            }
            utr_pos = utr_length - utr_pos;
            utr_pos *= -1;
        }else{
            for (int i = 0; i < exons.size(); i++){
                if (exons.get(i).getEnd() < getCdsEnd()){//exon is before or within CDS
                    continue;
                }
                if (exons.get(i).getStart() > getCdsEnd()){//whole exon is post CDS
                    if (pos > exons.get(i).getEnd()){
                        utr_pos += exons.get(i).getLength();
                    }else if (pos >= exons.get(i).getStart() ){
                        utr_pos += pos - exons.get(i).getStart();
                        isExonic = true;
                        break;
                    }
                }else{//cds end is in exon
                    if (pos >= getCdsEnd() && pos <= exons.get(i).getEnd()){
                        utr_pos += pos - getCdsEnd();
                        isExonic = true;
                        break;
                    }
                }
            }
        }
        
        if (!isExonic){
            for (int i = 0; i < exons.size() -1; i++){
                if (pos >= exons.get(i).getEnd() && pos < exons.get(i+1).getStart()){
                    Integer donor_pos = pos - exons.get(i).getEnd();
                    Integer acceptor_pos = pos - exons.get(i+1).getStart();
                    if (Math.abs(donor_pos) <= Math.abs(acceptor_pos)){
                        intron_pos = donor_pos;
                    }else{
                        intron_pos = acceptor_pos;
                        if (utr_pos != -1){
                            utr_pos++;
                        }else{
                            /*
                            intronic position is actually before first coding base
                            */
                            if (getStrand() < 0){// on - strand so actually just after last coding base
                                intron_pos *= -1;
                                utr_string.append("c.").append(getCodingLength());
                            }else{//if on + strand is 3' UTR
                                utr_string.append("c.1");
                            }
                            if (intron_pos > 0){
                                utr_string.append("+");
                            }
                            utr_string.append(intron_pos);
                            return utr_string.toString();
                        }
                    }
                }
            }
        }
        
        if (getStrand() < 0){
            intron_pos *= -1;
            utr_pos = getCodingLength() - utr_pos + 1;
        }
        if (utr_pos > 0){
            utr_string.append("c.*").append(utr_pos);
        }else{
            utr_string.append("c.").append(utr_pos);
        }
        if (intron_pos != 0){
            if (intron_pos > 0){
                utr_string.append("+");
            }
            utr_string.append(intron_pos);
        }
        return utr_string.toString();
        
        
    }
    

    
    public class Exon implements Comparable<Exon> {
        private Integer start;
        private Integer end;
        private Integer order;
        
        Exon(){
            this(null, null, null);
        }
        Exon(Integer exonStart, Integer exonEnd){
            this(exonStart, exonEnd, null);
        }
        Exon(Integer exonStart, Integer exonEnd, Integer exonOrder){
            start = exonStart;
            end = exonEnd;
            order = exonOrder;
        }
        
        public void setStart(Integer exonStart){
            start = exonStart;
        }
        public void setEnd(Integer exonEnd){
            end = exonEnd;
        }
        public void setOrder(Integer exonOrder){
            order = exonOrder;
        }
        public Integer getStart(){
            return start;
        }
        public Integer getEnd(){
            return end;
        }
        public Integer getOrder(){
            return order;
        }
        public Integer getLength(){
            if (start != null && end != null){
                return end - start + 1;
            }else{
                return null;
            }
        }
        
        public boolean isCodingExon(){
            if (! isCoding()){
                return false;
            }
            return start <= cdsEnd && end >= cdsStart;
        }
        
        public Exon getExonCodingRegion(){
            if (! isCodingExon()){
                return null;
            }
            if (end < cdsStart){
                return null;
            }else if (start < cdsStart){//cds start is in this exon
                if (end <= cdsEnd){
                    return new Exon(cdsStart, end, order);
                }else{
                    return new Exon(cdsStart, cdsEnd, order);
                }
            }else if (start < cdsEnd){//within cds
                if (end > cdsEnd){
                    return new Exon(start, cdsEnd, order); 
                }else{
                    return new Exon(start, end, order);
                }
            }else{//outside CDS
                return null;
            }
        }

        @Override
        public int compareTo(Exon o) {
            if (this.getStart() - o.getStart() != 0){
                return this.getStart() - o.getStart();
            }
            return this.getEnd() - o.getEnd();
        }
    }
    
    
    
    public class GeneExonsException extends Exception{
        public GeneExonsException() { super(); }
        public GeneExonsException(String message) { super(message); }
        public GeneExonsException(String message, Throwable cause) { super(message, cause); }
        public GeneExonsException(Throwable cause) { super(cause); }
    }
}

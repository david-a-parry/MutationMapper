/*
* Copyright (C) 2015 David A. Parry
 * d.a.parry@leeds.ac.uk
 * 
    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.github.mutationmapper;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author David A. Parry
 * 
 * This comparator sorts VEP consequences in order of severity
 */
public class SpeciesComparator implements Comparator<String>{
    private static final List<String> CONS_ORDER = Arrays.asList("transcript_ablation", 
            "splice_acceptor_variant", "splice_donor_variant", "stop_gained", 
            "frameshift_variant", "stop_lost", "initiator_codon_variant", 
            "transcript_amplification", "inframe_insertion", "inframe_deletion", 
            "missense_variant", "splice_region_variant", 
            "incomplete_terminal_codon_variant", "stop_retained_variant", 
            "synonymous_variant", "coding_sequence_variant", 
            "mature_miRNA_variant", "5_prime_UTR_variant", 
            "3_prime_UTR_variant", "non_coding_transcript_exon_variant", 
            "intron_variant", "NMD_transcript_variant", 
            "non_coding_transcript_variant", "upstream_gene_variant", 
            "downstream_gene_variant", "TFBS_ablation", "TFBS_amplification", 
            "TF_binding_site_variant", "regulatory_region_ablation", 
            "regulatory_region_amplification", "regulatory_region_variant", 
            "feature_elongation", "feature_truncation", "intergenic_variant");
    @Override
    public int compare(String s1, String s2){
        if (CONS_ORDER.contains(s1)){
            if (CONS_ORDER.contains(s2)){
                return CONS_ORDER.indexOf(s1) - CONS_ORDER.indexOf(s2);
            }else{
                return -1;
            }
        }else if (CONS_ORDER.contains(s2)){
            return 1;
        }else{
            return s1.compareTo(s2);
        }
    }
}

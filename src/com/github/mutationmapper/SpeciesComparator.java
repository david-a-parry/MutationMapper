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
    private static final List<String> SPECIES_ORDER = Arrays.asList(
                "Human", "Mouse", "Rat", "Zebrafish", "Fruitfly");

    @Override
    public int compare(String s1, String s2) {
        if (s1 == null){
            return 1;
        }
        if (s2 == null){
            return -1;
        }
        if (SPECIES_ORDER.contains(s1)){
            if (SPECIES_ORDER.contains(s2)){
                return SPECIES_ORDER.indexOf(s1) - SPECIES_ORDER.indexOf(s2);
            }
            return -1;
        }else if (SPECIES_ORDER.contains(s2)){
            return 1;
        }
        return s1.compareTo(s2);
    }
        
}

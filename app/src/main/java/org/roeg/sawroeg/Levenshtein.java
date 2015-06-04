package org.roeg.sawroeg;

/**
 * This file is published under the MIT License.
 */

import java.util.ArrayList;

/**
 * @author Richard Coan
 */
public class Levenshtein {
    
    /** 
     * Calculates the Levenshtein distance and returns an ArrayList 
     * containing both words and the distance.
     * 
     * @param s
     * @param u
     * @return ArrayList<String[]>
     */
    protected ArrayList<String[]> calculate(String[] s, String[] u) {
        ArrayList<String[]> data = new ArrayList<String[]>();
                
        for(int x = 0; x < s.length - 1; x++) {
            if(s[x].length() != 0 && u[x].length() != 0 ) {
                int dist = this.distance(s[x], s[x].length(), u[x], u[x].length());
                data.add(new String[] {s[x], u[x], Integer.toString(dist)});
            }
        }
        
        return data;
    }
    
    /**
     * Recursive Levenshtein distance calculation using two strings.
     * Based on the Pseudocode available at:
     * http://en.wikipedia.org/wiki/Levenshtein_distance
     * 
     * @param s
     * @param u
     * @return distance
     */
    protected static int distance(String s, int len_s, String u, int len_u) {
        // Testing for empty strings
        if(len_s <= 0) return len_u;
        if(len_u <= 0) return len_s;
        
        // Testing if last characters match
        int cost = 1;
        if(s.charAt(len_s - 1) == u.charAt(len_u - 1) ) cost = 0;
        
        // return minimum of delete char from s, from t, and from both
        return Math.min(distance(s, len_s - 1, u, len_u) + 1, 
               Math.min(distance(s, len_s, u, len_u - 1) + 1,
                        distance(s, len_s - 1, u, len_u - 1) + cost)
        );
    }  
}
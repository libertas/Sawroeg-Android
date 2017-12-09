package org.roeg.sawroeg;

public class Levenshtein {
    public static int distance(String s, String u) {
        int m = s.length();
        int n = u.length();

        int[][] matrix = new int[m + 1][n + 1];

        for(int i = 0; i < m + 1; i++) {
            matrix[i][0] = i;
        }

        for(int i = 0; i < n + 1; i++) {
            matrix[0][i] = i;
        }

        for(int i = 0; i < m; i++) {
            for(int j = 0; j < n; j++) {
                int cost = (s.charAt(i) == u.charAt(j))? 0: 1;
                matrix[i + 1][j + 1] = Math.min(matrix[i][j] + cost,
                        Math.min(matrix[i][j + 1] + 1, matrix[i + 1][j] + 1));
            }
        }

        return matrix[m][n];
    }
}

package com.example.agrinova;

import java.util.List;

public class PlantNetResponse {
    private List<Result> results;

    public List<Result> getResults() {
        return results;
    }

    public static class Result {
        private Species species;
        private double score;

        public Species getSpecies() {
            return species;
        }

        public double getScore() {
            return score;
        }
    }

    public static class Species {
        private String scientificNameWithoutAuthor;
        private List<String> commonNames;

        public String getScientificName() {
            return scientificNameWithoutAuthor;
        }

        public List<String> getCommonNames() {
            return commonNames;
        }
    }
}

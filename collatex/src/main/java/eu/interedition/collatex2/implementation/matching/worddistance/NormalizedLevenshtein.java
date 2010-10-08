package eu.interedition.collatex2.implementation.matching.worddistance;


public class NormalizedLevenshtein implements WordDistance {

  @Override
  public float distance(String word1, String word2) {
    int levenshteinDistance = Levenshtein.getLevenshteinDistance(word1.toLowerCase(), word2.toLowerCase());
    float normalizedLev = (2.0f * levenshteinDistance) / (word1.length() + word2.length());
    return normalizedLev;
  }
  
  
}

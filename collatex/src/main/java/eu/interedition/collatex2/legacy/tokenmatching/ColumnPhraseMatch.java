package eu.interedition.collatex2.legacy.tokenmatching;

import eu.interedition.collatex2.interfaces.IColumns;
import eu.interedition.collatex2.interfaces.IMatch;
import eu.interedition.collatex2.interfaces.IPhrase;

public class ColumnPhraseMatch implements IMatch {

  private final IColumns a;
  private final IPhrase b;

  public ColumnPhraseMatch(final IColumns columnsA, final IPhrase witnessPhrase) {
    this.a = columnsA;
    this.b = witnessPhrase;
  }

  @Override
  public String getNormalized() {
    return b.getNormalized();
  }

  @Override
  public IColumns getColumns() {
    return a;
  }

  @Override
  public IPhrase getPhrase() {
    return b;
  }

  @Override
  public String toString() {
    return getColumns() + "->" + getPhrase();
  }
}

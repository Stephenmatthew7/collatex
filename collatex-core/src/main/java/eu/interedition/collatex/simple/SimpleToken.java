/**
 * CollateX - a Java library for collating textual sources,
 * for example, to produce an apparatus.
 *
 * Copyright (C) 2010 ESF COST Action "Interedition".
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

package eu.interedition.collatex.simple;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Ordering;
import eu.interedition.collatex.VariantGraph;
import eu.interedition.collatex.Witness;
import eu.interedition.collatex.Token;

import java.util.Set;

import static com.google.common.collect.Iterables.getFirst;
import static java.util.Collections.singleton;

public class SimpleToken implements Token, Comparable<SimpleToken> {
  public static final Function<VariantGraph.Vertex, String> TO_CONTENTS = new Function<VariantGraph.Vertex, String>() {
    @Override
    public String apply(VariantGraph.Vertex input) {
      final Set<Witness> witnesses = input.witnesses();
      if (witnesses.isEmpty()) {
        return "";
      }
      final StringBuilder contents = new StringBuilder();
      for (SimpleToken token : Ordering.natural().sortedCopy(Iterables.filter(input.tokens(singleton(getFirst(witnesses, null))), SimpleToken.class))) {
        contents.append(token.getContent()).append(" ");
      }
      return contents.toString().trim();
    }
  };
  public static int nextId = 0;
  public static final SimpleToken START = new SimpleToken(SimpleWitness.SUPERBASE, -1, "", "#");
  public static final SimpleToken END = new SimpleToken(SimpleWitness.SUPERBASE, Integer.MAX_VALUE, "", "#");

  private final int id;
  private Witness witness;
  private int index;
  private String content;
  private String normalized;

  public SimpleToken(Witness witness, int index, String content, String normalized) {
    synchronized (SimpleToken.class) {
      this.id = (nextId == Integer.MAX_VALUE ? 0 : nextId++);
    }
    this.witness = witness;
    this.index = index;
    this.content = content;
    this.normalized = normalized;
  }

  public int getId() {
    return id;
  }

  public int getIndex() {
    return index;
  }

  public String getContent() {
    return content;
  }

  @Override
  public Witness getWitness() {
    return witness;
  }

  public String getNormalized() {
    return normalized;
  }

  @Override
  public String toString() {
    return new StringBuilder(witness.toString()).append(":").append(index).append(":'").append(normalized).append("'").toString();
  }

  public static String toString(Iterable<? extends Token> tokens) {
    final StringBuilder normalized = new StringBuilder();
    for (SimpleToken token : Iterables.filter(tokens, SimpleToken.class)) {
      normalized.append(token.getNormalized()).append(" ");
    }
    return normalized.toString().trim();
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getIndex(), getWitness());
  }

  @Override
  public boolean equals(Object obj) {
    if (obj != null && obj instanceof SimpleToken) {
      final SimpleToken other = (SimpleToken) obj;
      return getIndex() == other.getIndex() && getWitness().equals(other.getWitness());
    }
    return super.equals(obj);
  }

  @Override
  public int compareTo(SimpleToken o) {
    Preconditions.checkArgument(witness.equals(o.getWitness()));
    return (index - o.index);
  }
}

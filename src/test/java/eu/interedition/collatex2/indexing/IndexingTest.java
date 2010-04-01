package eu.interedition.collatex2.indexing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mortbay.log.Log;

import com.google.common.base.Join;
import com.google.common.collect.Lists;

import eu.interedition.collatex2.implementation.Factory;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.IWitnessIndex;

public class IndexingTest {
  private Factory factory;

  @Before
  public void setup() {
    factory = new Factory();
  }

  @Test
  public void test1() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final IWitnessIndex index = Factory.createWitnessIndexMap(Lists.newArrayList(a)).get(a.getSigil());
    assertEquals(6, index.size());
    assertContains(index, "# tobe");
    assertContains(index, "tobe or");
    assertContains(index, "or");
    assertDoesNotContain(index, "or not");
    assertContains(index, "not");
    assertDoesNotContain(index, "or tobe");
    assertContains(index, "not tobe");
    assertContains(index, "tobe #");
  }

  @Ignore
  @Test
  public void test2() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    Log.info("witness = [the big black cat and the big black rat]");
    final IWitnessIndex index = Factory.createWitnessIndexMap(Lists.newArrayList(a)).get(a.getSigil());
    assertContains(index, "# the big black");
    assertContains(index, "the big black cat");
    assertContains(index, "cat");
    assertContains(index, "and");
    assertContains(index, "and the big black");
    assertContains(index, "the big black rat");
    assertContains(index, "rat");
    assertEquals(7, index.size());
  }

  @Test
  public void test1a() {
    final IWitness a = factory.createWitness("A", "tobe or not tobe");
    final IWitnessIndex index = Factory.createWitnessIndexMap(Lists.newArrayList(a)).get("A");
    assertEquals(6, index.size());
    assertContains(index, "# tobe");
    assertContains(index, "tobe or");
    assertContains(index, "or");
    assertDoesNotContain(index, "or not");
    assertContains(index, "not");
    assertDoesNotContain(index, "or tobe");
    assertContains(index, "not tobe");
    assertContains(index, "tobe #");
  }

  @Ignore
  @Test
  public void test2a() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitnessIndex index = Factory.createWitnessIndexMap(Lists.newArrayList(a)).get("A");
    assertContains(index, "# the big black");
    assertContains(index, "the big black cat");
    assertContains(index, "cat");
    assertContains(index, "and");
    assertContains(index, "and the big black");
    assertContains(index, "the big black rat");
    assertContains(index, "rat");
    assertEquals(7, index.size());
  }

  @Ignore
  @Test
  public void testTwoWitnesses() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    final IWitness b = factory.createWitness("B", "and the big black cat ate the big rat");
    Log.info("witness a = [the big black cat and the big black rat]");
    Log.info("witness b = [and the big black cat ate the big rat]");
    final Map<String, IWitnessIndex> indexMap = Factory.createWitnessIndexMap(Lists.newArrayList(a, b));
    final IWitnessIndex indexA = indexMap.get(a.getSigil());
    final IWitnessIndex indexB = indexMap.get(b.getSigil());
    assertContains(indexA, "# the big black");
    assertContains(indexB, "# the big black");
    assertContains(indexA, "the big black cat");
    assertContains(indexB, "the big black cat");
    assertContains(indexA, "cat");
    assertContains(indexA, "and");
    assertContains(indexB, "and");
    assertContains(indexA, "and the big black");
    assertContains(indexA, "the big black rat");
    assertContains(indexA, "rat");
    assertEquals(7, indexA.size());
  }

  //  @Test
  //  public void test3() {
  //    final IWitness a = factory.createWitness("A", "X C A B Y C A Z A B W");
  //    Log.info("witness = [X C A B Y C A Z A B W]");
  //    final IWitnessIndex index = Factory.createWitnessIndex(a);
  //    assertContains(index,"# the big black"));
  //    assertContains(index,"the big black cat"));
  //    assertContains(index,"and"));
  //    assertContains(index,"and the big black"));
  //    assertContains(index,"the big black rat"));
  //    assertEquals(5, index.size());
  //  }

  private void assertContains(final IWitnessIndex index, final String phrase) {
    assertTrue("phrase '" + phrase + "' not found in index [" + Join.join(", ", index.getPhrases()) + "]", index.contains(phrase));
  }

  private void assertDoesNotContain(final IWitnessIndex index, final String phrase) {
    assertFalse("phrase '" + phrase + "' found in index " + index.getPhrases().iterator().next().getSigil() + ", shouldn't be there!", index.contains(phrase));
  }
}

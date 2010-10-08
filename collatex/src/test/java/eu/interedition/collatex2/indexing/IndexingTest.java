package eu.interedition.collatex2.indexing;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import eu.interedition.collatex2.implementation.CollateXEngine;
import eu.interedition.collatex2.interfaces.IWitness;
import eu.interedition.collatex2.interfaces.ITokenIndex;

public class IndexingTest {
  private CollateXEngine factory;
  private Logger log = LoggerFactory.getLogger(IndexingTest.class);
  
  @Before
  public void setup() {
    factory = new CollateXEngine();
  }

  @Ignore
  @Test
  public void test2() {
    final IWitness a = factory.createWitness("A", "the big black cat and the big black rat");
    log.debug("witness = [the big black cat and the big black rat]");
    final ITokenIndex index = CollateXEngine.createWitnessIndex(a);
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
    final ITokenIndex index = CollateXEngine.createWitnessIndex(a);
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
    final ITokenIndex index = CollateXEngine.createWitnessIndex(a);
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
    log.debug("witness a = [the big black cat and the big black rat]");
    log.debug("witness b = [and the big black cat ate the big rat]");
    final ITokenIndex indexA = CollateXEngine.createWitnessIndex(a);
    final ITokenIndex indexB = CollateXEngine.createWitnessIndex(b);
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

  private void assertContains(final ITokenIndex index, final String phrase) {
    assertTrue("phrase '" + phrase + "' not found in index [" + Joiner.on(", ").join(index.keys()) + "]", index.contains(phrase));
  }

  private void assertDoesNotContain(final ITokenIndex index, final String phrase) {
    assertFalse("phrase '" + phrase + "' found in index, shouldn't be there!", index.contains(phrase));
  }
}

import es.urjc.grafo.EDA.searchtree.AVLTreeMap;
import es.urjc.grafo.EDA.searchtree.AbstractBalanceableTreeMap;
import es.urjc.grafo.EDA.util.Entry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Arrays;

public class TestAVLTreeMap {

    private static Method sanityCheck = null;
    private static Method dumpTree = null;
    private static String expectedTree = "<2, Two>\n" +
            "  <1, One>\n" +
            "    leaf\n" +
            "    leaf\n" +
            "  <3, Three>\n" +
            "    leaf\n" +
            "    <4, SecondFour>\n" +
            "      leaf\n" +
            "      leaf\n";
    private static String expectedTree2 = "<3, Three>\n" +
            "  <2, Two>\n" +
            "    <1, One>\n" +
            "      leaf\n" +
            "      leaf\n" +
            "    leaf\n" +
            "  <6, Six>\n" +
            "    <5, Five>\n" +
            "      leaf\n" +
            "      leaf\n" +
            "    <7, Seven>\n" +
            "      leaf\n" +
            "      leaf\n";

    @BeforeAll
    public static void setUp() throws NoSuchMethodException {
        sanityCheck = AVLTreeMap.class.getDeclaredMethod("sanityCheck");
        sanityCheck.setAccessible(true);
        dumpTree = AbstractBalanceableTreeMap.class.getDeclaredMethod("dump");
        dumpTree.setAccessible(true);
    }

    private AVLTreeMap<Integer, String> setUpTree() {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        avl.put(1, "One");
        avl.put(2, "Two");
        avl.put(3, "Three");
        avl.put(4, "Four");
        avl.put(5, "Five");
        avl.put(6, "Six");
        avl.put(7, "Seven");
        return avl;
    }

    @Test
    public void testInsertAndReplaceStructure() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        Assertions.assertNull(avl.put(1, "One"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(2, "Two"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(3, "Three"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(4, "Four"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(4, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));

        // Replace existing key value
        Assertions.assertEquals("Four", avl.put(4, "SecondFour"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(4, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));

        // Check inner structure dump
        Assertions.assertEquals(expectedTree, dumpTree.invoke(avl).toString());
    }

    @Test
    public void testRemoveAndStructure() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = setUpTree();
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(7, avl.size());

        // Remove internal node (with two children in initial insertion order)
        Assertions.assertEquals("Four", avl.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(6, avl.size());

        // Removing non-existing should return null and not change size
        Assertions.assertNull(avl.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(6, avl.size());

        // Check inner structure dump
        Assertions.assertEquals(expectedTree2, dumpTree.invoke(avl).toString());
    }

    @Test
    public void testGetAndDuplicates() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        Assertions.assertNull(avl.get(10));
        Assertions.assertNull(avl.put(10, "Ten"));
        Assertions.assertEquals("Ten", avl.get(10));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(1, avl.size());

        Assertions.assertEquals("Ten", avl.put(10, "TEN"));
        Assertions.assertEquals("TEN", avl.get(10));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(1, avl.size());

        // Add more and verify
        avl.put(5, "Five");
        avl.put(15, "Fifteen");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(3, avl.size());
    }

    @Test
    public void testFirstAndLastEntryOnEmptyAndNonEmpty() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> empty = new AVLTreeMap<>();
        Assertions.assertNull(empty.firstEntry());
        Assertions.assertNull(empty.lastEntry());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(empty));

        AVLTreeMap<Integer, String> avl = setUpTree();
        Assertions.assertEquals(1, avl.firstEntry().getKey());
        Assertions.assertEquals(7, avl.lastEntry().getKey());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
    }

    @Test
    public void testNavigationalEntries() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = setUpTree();
        // exact hits
        Assertions.assertEquals(4, avl.floorEntry(4).getKey());
        Assertions.assertEquals(4, avl.ceilingEntry(4).getKey());
        Assertions.assertEquals(3, avl.previousEntry(4).getKey());
        Assertions.assertEquals(5, avl.nextEntry(4).getKey());

        // boundaries
        Assertions.assertNull(avl.previousEntry(1));
        Assertions.assertNull(avl.nextEntry(7));
        Assertions.assertNull(avl.floorEntry(0));
        Assertions.assertNull(avl.ceilingEntry(8));

        // in-between (no exact match): remove a key to create a gap
        Assertions.assertEquals("Four", avl.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(3, avl.floorEntry(4).getKey());
        Assertions.assertEquals(5, avl.ceilingEntry(4).getKey());
        Assertions.assertEquals(3, avl.previousEntry(4).getKey());
        Assertions.assertEquals(5, avl.nextEntry(4).getKey());
    }

    @Test
    public void testSubMapRangesAndIteration() {
        AVLTreeMap<Integer, String> avl = setUpTree();
        List<Integer> expectedKeys = new ArrayList<>(3);
        expectedKeys.add(3);
        expectedKeys.add(4);
        expectedKeys.add(5);

        List<Integer> seen = new ArrayList<>(3);
        for (Entry<Integer, String> e : avl.subMap(3, 6)) {
            seen.add(e.getKey());
        }
        Assertions.assertEquals(expectedKeys, seen);

        // Empty ranges
        Assertions.assertFalse(avl.subMap(5, 5).iterator().hasNext());
        Assertions.assertFalse(avl.subMap(8, 10).iterator().hasNext());
        Assertions.assertFalse(avl.subMap(-3, -1).iterator().hasNext());
    }

    @Test
    public void testEntrySetSnapshotSortedAndIndependent() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = setUpTree();
        List<Integer> snapshotKeys = new ArrayList<>();
        for (Entry<Integer, String> e : avl.entrySetSnapshot()) {
            snapshotKeys.add(e.getKey());
        }

        // Sorted order expected
        Assertions.assertEquals(List.of(1,2,3,4,5,6,7), snapshotKeys);

        // Modify original map after taking the snapshot
        avl.put(8, "Eight");
        avl.remove(1);
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));

        // Snapshot remains unchanged
        Assertions.assertEquals(List.of(1,2,3,4,5,6,7), snapshotKeys);
        // And now current in-order should reflect changes
        List<Integer> nowKeys = new ArrayList<>();
        for (Entry<Integer, String> e : avl.entrySetSnapshot()) {
            nowKeys.add(e.getKey());
        }
        Assertions.assertEquals(List.of(2,3,4,5,6,7,8), nowKeys);
    }

    @Test
    public void testLeftRightRotationStructure() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        avl.put(30, "V30");
        avl.put(10, "V10");
        avl.put(20, "V20"); // triggers left-right rotation
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        String expected = "<20, V20>\n" +
                "  <10, V10>\n" +
                "    leaf\n" +
                "    leaf\n" +
                "  <30, V30>\n" +
                "    leaf\n" +
                "    leaf\n";
        Assertions.assertEquals(expected, dumpTree.invoke(avl).toString());
    }

    @Test
    public void testRightLeftRotationStructure() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        avl.put(10, "V10");
        avl.put(30, "V30");
        avl.put(20, "V20"); // triggers right-left rotation
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        String expected = "<20, V20>\n" +
                "  <10, V10>\n" +
                "    leaf\n" +
                "    leaf\n" +
                "  <30, V30>\n" +
                "    leaf\n" +
                "    leaf\n";
        Assertions.assertEquals(expected, dumpTree.invoke(avl).toString());
    }

    @Test
    public void testModerateStressInsertAndRemove() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        int N = 50;
        for (int i = 1; i <= N; i++) avl.put(i, "V" + i);
        Assertions.assertEquals(N, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));

        // Verify in-order ascending via snapshot
        int prev = 0;
        for (Entry<Integer, String> e : avl.entrySetSnapshot()) {
            Assertions.assertTrue(e.getKey() > prev);
            prev = e.getKey();
        }

        // Remove every 3rd key
        int removed = 0;
        for (int i = 3; i <= N; i += 3) {
            Assertions.assertNotNull(avl.remove(i));
            removed++;
            Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        }
        Assertions.assertEquals(N - removed, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
    }

    @Test
    public void AVLTreeInsert() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        Assertions.assertNull(avl.put(1, "One"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(2, "Two"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(3, "Three"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertNull(avl.put(4, "Four"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(4, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals("Four", avl.put(4, "SecondFour"));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(4, avl.size());
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(expectedTree, dumpTree.invoke(avl).toString());
    }

    @Test
    public void AVLTreeRemove() throws InvocationTargetException, IllegalAccessException {
        AVLTreeMap<Integer, String> avl = new AVLTreeMap<>();
        avl.put(1, "One");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(2, "Two");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(3, "Three");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(4, "Four");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(5, "Five");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(6, "Six");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        avl.put(7, "Seven");
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(7, avl.size());
        Assertions.assertEquals("Four", avl.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(6, avl.size());
        Assertions.assertNull(avl.remove(4));
        Assertions.assertTrue((Boolean) sanityCheck.invoke(avl));
        Assertions.assertEquals(6, avl.size());
        Assertions.assertEquals(expectedTree2, dumpTree.invoke(avl).toString());
    }

    @Test
    public void AVLTreeFirstEntry(){
        AVLTreeMap<Integer, String> avl = setUpTree();
        Assertions.assertEquals(1, avl.firstEntry().getKey());
    }

    @Test
    public void AVLTreeLastEntry(){
        AVLTreeMap<Integer, String> avl = setUpTree();
        Assertions.assertEquals(7, avl.lastEntry().getKey());
    }

    @Test
    public void AVLTreeSubMap(){
        AVLTreeMap<Integer, String> avl = setUpTree();
        List<String> entries = new ArrayList<>(3);
        entries.add("Three");
        entries.add("Four");
        entries.add("Five");
        for (Entry<Integer, String> entry : avl.subMap(3, 6)){
            Assertions.assertEquals(entries.removeFirst(), entry.getValue());
        }
    }
}

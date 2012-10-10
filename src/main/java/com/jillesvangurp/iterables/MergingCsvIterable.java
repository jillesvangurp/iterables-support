package com.jillesvangurp.iterables;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Merge two sorted csv files on primary and foreign key fields and iterate over the groups of records.
 */
public class MergingCsvIterable implements Iterable<List<List<String>>>{

    private final CSVLineIterable primary;
    private final CSVLineIterable secondary;
    private final int primaryKeyIndex;
    private final int foreignKeyIndex;

    public MergingCsvIterable(CSVLineIterable primary, CSVLineIterable secondary, int primaryKyIndex, int foreignKeyIndex) {
        this.primary = primary;
        this.secondary = secondary;
        primaryKeyIndex = primaryKyIndex;
        this.foreignKeyIndex = foreignKeyIndex;
    }

    @Override
    public Iterator<List<List<String>>> iterator() {
        final Iterator<List<String>> primaryIterator = primary.iterator();
        final PeekableIterator<List<String>> secondaryIterator = new PeekableIterator<>(secondary.iterator());

        return new Iterator<List<List<String>>>() {

            @Override
            public boolean hasNext() {
                return primaryIterator.hasNext();
            }

            @Override
            public List<List<String>> next() {
                List<List<String>> records = new ArrayList<>();
                List<String> primaryRecord = primaryIterator.next();
                String pk = primaryRecord.get(primaryKeyIndex);
                records.add(primaryRecord);
                List<String> secondaryRecord;
                while(secondaryIterator.hasNext()) {
                    secondaryRecord = secondaryIterator.peek();
                    String fk = secondaryRecord.get(foreignKeyIndex);

//                    int compareTo = fk.compareTo(pk);
                    // assume the ids are integers
                    // TODO make comparator configurable
                    int compareTo = Integer.valueOf(fk) - Integer.valueOf(pk);
                    if(compareTo == 0) {
                        records.add(secondaryRecord);
                    } else if(compareTo > 0) {
                        // don't move secondary iterator forward to ensure we see the secondary record again for the next primary record.
                        break;
                    }
                    // we've peeked, now move it forward
                    secondaryIterator.next();
                }

                return records;
            }

            @Override
            public void remove() {
                primaryIterator.remove();
            }
        };
    }

}

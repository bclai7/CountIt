package com.takezeroapps.countit;

import java.util.Comparator;

enum MulticounterComparator implements Comparator<Multicounter> {
    NAME_SORT {
        public int compare(Multicounter m1, Multicounter m2) {
            return m1.getName().compareToIgnoreCase(m2.getName());
        }},
    CREATED_SORT {
        public int compare(Multicounter m1, Multicounter m2) {
            return Integer.valueOf(m1.getCreatedTimeStamp().compareTo(m2.getCreatedTimeStamp()));
        }},
    MODIFIED_SORT {
        public int compare(Multicounter m1, Multicounter m2) {
            return Integer.valueOf(m1.getModifiedTimeStamp().compareTo(m2.getModifiedTimeStamp()));
        }};

    public static Comparator<Multicounter> decending(final Comparator<Multicounter> other) {
        return new Comparator<Multicounter>() {
            public int compare(Multicounter m1, Multicounter m2) {
                return -1 * other.compare(m1, m2);
            }
        };
    }

    public static Comparator<Multicounter> getComparator(final MulticounterComparator... multipleOptions) {
        return new Comparator<Multicounter>() {
            public int compare(Multicounter m1, Multicounter m2) {
                for (MulticounterComparator option : multipleOptions) {
                    int result = option.compare(m1, m2);
                    if (result != 0) {
                        return result;
                    }
                }
                return 0;
            }
        };
    }
}
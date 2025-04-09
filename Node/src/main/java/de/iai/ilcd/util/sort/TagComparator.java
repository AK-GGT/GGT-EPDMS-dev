package de.iai.ilcd.util.sort;

import de.iai.ilcd.model.tag.Tag;

import java.util.Comparator;

public class TagComparator implements Comparator<Tag> {

    @Override
    public int compare(Tag tag1, Tag tag2) {
        return tag1.getName().compareTo(tag2.getName());
    }

}

package com.xenonclient.section;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Manages all registered sections for the Xenon Client.
 */
public class SectionManager {

    private final List<Section> sections = new ArrayList<>();

    public void registerDefaults() {
        register(new Section("Combat", "Combat related features", 0xFFE74C3C, "\u2694"));
        register(new Section("Movement", "Movement enhancements", 0xFF3498DB, "\u27A1"));
        register(new Section("Render", "Visual modifications", 0xFF2ECC71, "\uD83D\uDC41"));
        register(new Section("Player", "Player utilities", 0xFFF39C12, "\uD83D\uDC64"));
        register(new Section("World", "World interactions", 0xFF9B59B6, "\uD83C\uDF0D"));
        register(new Section("Misc", "Miscellaneous features", 0xFF1ABC9C, "\u2699"));
    }

    public void register(Section section) {
        sections.add(section);
    }

    public List<Section> getSections() {
        return Collections.unmodifiableList(sections);
    }

    public Section getSection(String name) {
        for (Section section : sections) {
            if (section.getName().equalsIgnoreCase(name)) {
                return section;
            }
        }
        return null;
    }

    public int getSectionCount() {
        return sections.size();
    }
}

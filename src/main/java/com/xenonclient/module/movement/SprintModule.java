package com.xenonclient.module.movement;

import com.xenonclient.module.Module;

/**
 * Auto Sprint module - automatically sprints when moving forward.
 * Fully legit: just sets the sprint state when the player is moving,
 * same as holding the sprint key.
 */
public class SprintModule extends Module {

    public SprintModule() {
        super("Sprint", "Automatically sprints when moving forward", "Movement");
    }
}

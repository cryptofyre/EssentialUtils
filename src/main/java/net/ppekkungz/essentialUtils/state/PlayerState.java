package net.ppekkungz.essentialUtils.state;

/**
 * Simplified player state for EssentialUtils.
 * ARMED state removed - features now activate directly.
 */
public enum PlayerState {
    /** Player is not actively using any feature */
    IDLE,
    
    /** Player is actively processing blocks (tree felling, vein mining, etc.) */
    ACTIVE
}

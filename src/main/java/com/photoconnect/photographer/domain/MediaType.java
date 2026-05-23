package com.photoconnect.photographer.domain;

/**
 * What kind of media a portfolio item is.
 * <ul>
 *   <li>{@link #IMAGE} — still photo</li>
 *   <li>{@link #VIDEO} — longer-form video</li>
 *   <li>{@link #REEL}  — short vertical video</li>
 * </ul>
 */
public enum MediaType {
    IMAGE,
    VIDEO,
    REEL
}

package com.fabio.domain;

/**
 * Created by jt on 6/13/17.
 */
public enum Difficulty {

    EASY("Easy"), MODERATE("Moderate"), KIND_OF_HARD("Kind of hard"), HARD("Hard");

    public String text;

    Difficulty(String text) {
        this.text = text;
    }
}

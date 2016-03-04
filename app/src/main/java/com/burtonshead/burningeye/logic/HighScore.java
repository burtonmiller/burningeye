package com.burtonshead.burningeye.logic;

public class HighScore
{
    public String name;
    public long score;

    public HighScore()
    {
    }

    public HighScore(long s, String n)
    {
        this.score = s;
        this.name = n;
    }
}

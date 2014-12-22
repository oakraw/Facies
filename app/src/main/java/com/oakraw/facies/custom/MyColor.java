package com.oakraw.facies.custom;

import android.graphics.Color;

/**
 * Created by Rawipol on 12/21/14 AD.
 */
public class MyColor {
    final int a;
    final int r;
    final int g;
    final int b;
    public MyColor (int a, int r, int g, int b)
    {
        this.a = a;
        this.r = r;
        this.g = g;
        this.b = b;
    }
    public MyColor (int input)
    {
        this.a = Color.alpha(input);
        this.r = Color.red(input);
        this.g = Color.green(input);
        this.b = Color.blue(input);
    }
    public int compareToAnother(MyColor other)
    {
        int diff_r = Math.abs(r - other.r);
        int diff_g = Math.abs(g - other.g);
        int diff_b = Math.abs(b - other.b);

        if (diff_r >= diff_g && diff_r >= diff_b)
            return diff_r;
        else if (diff_g >= diff_r && diff_g >= diff_b)
            return diff_g;
        else
            return diff_b;
    }
    public String toString ()
    {
        return "(a, r, g, b) = " + "(" + a + ", " + r + ", " + g + ", " + b + ")";
    }
}
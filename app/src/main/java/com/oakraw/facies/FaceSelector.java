package com.oakraw.facies;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.Log;

import com.oakraw.facies.custom.MyColor;

/**
 * Created by Narat on 12/21/2014.
 */
public class FaceSelector {

    int seed_x;
    int seed_y;
    Bitmap out;

    public FaceSelector (Bitmap input)
    {
        out = AdaptiveThreshold(input);
    }

    public Bitmap getResult ()
    {
        return out;
    }

    private Bitmap AdaptiveThreshold(Bitmap input) {
        int startThreshold = 10;
        int threshold = startThreshold;
        int m = input.getWidth();
        int n = input.getHeight();
        int changeCounter = 0;
        int fillingFraction = 5;
        int bestKnowThreshold = startThreshold;
        double bestKnowThresholdValueDiff = 1.0;
        double lowerAcceptedCountRatio = 0.4;
        double upperAcceptedCountRatio = 0.45;
        seed_x = (int) Math.floor(input.getWidth() / 2);
        seed_y = (int) Math.floor(input.getHeight() / 2);
        boolean[][] mask;
        int maskCount;

        int seed;
        int center = input.getPixel(seed_x, seed_y);

        // In case of the center point is a part of glasses, we check a little top and bottom
        int top = input.getPixel(seed_x, seed_y + (int) Math.floor(n / 10));
        int bottom = input.getPixel(seed_x, seed_y - (int) Math.floor(n / 10));
        int diffTop = new MyColor(top).compareToAnother(new MyColor(center));
        int diffBottom = new MyColor(bottom).compareToAnother(new MyColor(center));
        int diffBoth = new MyColor(bottom).compareToAnother(new MyColor(top));
        if (diffTop <= 20 || diffBottom <= 20) {
            Log.v("mixthF", "seed is selected: using center");
            seed = center;
        } else if (diffBoth <= 30) {
            Log.v("mixthF", "seed is selected: using bottom");
            seed = bottom;
        } else {
            Log.v("mixthF", "seed is selected: using center... no other choices!");
            seed = center;
        }

        while (true) {
            // Initialize variables in loop
            maskCount = 0;
            mask = new boolean[m][n];
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    mask[i][j] = false;
                }
            }

            // Find lower and upper bound colors using corresponding threshold
            MyColor UpperBound = getRGBFromInt(seed, threshold / 2);
            MyColor LowerBound = getRGBFromInt(seed, threshold / 2 * (-1));

            // Enable pixels between bounds
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    MyColor temp = getRGBFromInt(input.getPixel(i, j), 0);
                    //Log.v("mixth", "Compare to UpperBound = " + temp.compareToAnother(UpperBound));
                    //Log.v("mixth", "Compare to LowerBound = " + temp.compareToAnother(LowerBound));
                    if (temp.compareToAnother(UpperBound) <= threshold && temp.compareToAnother(LowerBound) <= threshold) {
                        mask[i][j] = true;
                    }
                }
            }

            // Fill up using x-axis
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < n; j++) {
                    try {
                        if (mask[i][j] == true) {
                            if (i + 1 < n) {

                                if (mask[i + 1][j] != true) {
                                    for (int k = i + 1; k < m && k - i + 1 < (int) Math.floor(m / fillingFraction); k++) {
                                        if (mask[k][j] == true) {
                                            for (int l = i; l < k; l++) {
                                                mask[l][j] = true;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        //Log.v("mixthF", "i = " + i + ", j = " + j);
                    }
                }
            }

            // Fill up using y-axis
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < m; i++) {
                    try {
                        if (mask[i][j] == true) {
                            if (j + 1 < n) {

                                if (mask[i][j + 1] != true) {
                                    for (int k = j + 1; k < n && k - j + 1 < (int) Math.floor(n / fillingFraction); k++) {
                                        if (mask[i][k] == true) {
                                            for (int l = j; l < k; l++) {
                                                mask[i][l] = true;
                                            }
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        //Log.v("mixthF", "i = " + i + ", j = " + j);
                    }
                }
            }

            // Count enable pixels
            for (int i = 0; i < mask.length; i++) {
                for (int j = 0; j < mask[i].length; j++) {
                    if (mask[i][j])
                        maskCount++;
                }
            }

            // Timeout for finding a great ratio, using the best one we have found
            if (changeCounter > 20) {
                Log.v("mixthF", "Timeout - using: " + bestKnowThreshold + ", " + bestKnowThresholdValueDiff);
                if (threshold == bestKnowThreshold)
                    break;
                else {
                    threshold = bestKnowThreshold;
                    continue;
                }
            }

            // Check if this threshold matches our satisfaction, change if not
            double test1 = (double) m * (double) n;
            double test2 = (double) maskCount / test1;
            if (test2 < 0.5)
                threshold += 5;
            else if (test2 > 0.6)
                threshold -= 5;
            else
                break;

            if (Math.abs(test2 - lowerAcceptedCountRatio) < bestKnowThresholdValueDiff ||
                    Math.abs(test2 - upperAcceptedCountRatio) < bestKnowThresholdValueDiff) {
                bestKnowThreshold = threshold;
                if (Math.abs(test2 - lowerAcceptedCountRatio) < Math.abs(test2 - upperAcceptedCountRatio))
                    bestKnowThresholdValueDiff = Math.abs(test2 - lowerAcceptedCountRatio);
                else
                    bestKnowThresholdValueDiff = Math.abs(test2 - upperAcceptedCountRatio);
            }

            changeCounter++;
        }

        Bitmap tempalpha = fadeFromOrigins(input, mask);
        Paint alphaP = new Paint();
        alphaP.setAntiAlias(true);
        alphaP.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        Bitmap output = Bitmap.createBitmap(m, n, Bitmap.Config.ARGB_8888);
        Canvas printcanvas = new Canvas(output);
        printcanvas.drawBitmap(input, 0, 0, null);
        printcanvas.drawBitmap(tempalpha, 0, 0, alphaP);
        return andOperation(output, mask);
    }

    private Bitmap fadeFromOrigins(Bitmap input, boolean[][] mask) {
        Bitmap tempalpha = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        int top = 0;
        int bottom = 0;
        int left = 0;
        int right = 0;
        int m = input.getWidth();
        int n = input.getHeight();
        for (int i = 0; i < m; i++)
            for (int j = 0; j < n; j++)
                tempalpha.setPixel(i, j, Color.argb(0, 255, 255, 255));
        int seed_x = (int) Math.floor(input.getWidth() / 2);
        int seed_y = (int) Math.floor(input.getHeight() / 2);

        // Finding the edge from the seed pixel
        for (int i = seed_x; i > 1; i--) {
            if (mask[i][seed_y] == true) {
                left = i;
            } else
                break;
        }

        for (int i = seed_x; i < m; i++) {
            if (mask[i][seed_y] == true) {
                right = i;
            } else
                break;
        }

        for (int j = seed_y; j > 1; j--) {
            if (mask[seed_x][j] == true) {
                top = j;
            } else
                break;
        }

        for (int j = seed_y; j < n; j++) {
            if (mask[seed_x][j] == true) {
                bottom = j;
            } else
                break;
        }
        Log.v("mixthF", "Fadding (top, right, bottom, left) = " + "(" + top + ", " + right + ", " + bottom + ", " + left + ")");

        // Calculate gradient per pixel
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < n; j++) {
                if (mask[i][j] == true) {
                    if (i >= left && i <= right && j >= top && j <= bottom) {
                        double temp_x = 128;
                        double temp_y = 127;
                        double temp = 255;
                        int fadeStartPercent = 50;
                        int fadeEndPercent = 100;
                        //Quadant 4
                        if (i >= seed_x && j >= seed_y) {
                            temp_x = (right - seed_x);
                            temp_y = (bottom - seed_y);
                        }
                        // Quadant 3
                        else if (i <= seed_x && j>= seed_y) {
                            temp_x = (seed_x - left);
                            temp_y = (bottom - seed_y);
                        }
                        // Quadant 2
                        else if (i <= seed_x && j <= seed_y) {
                            temp_x = (seed_x - left);
                            temp_y = (seed_y - top);
                        }
                        // Quadant 1
                        else {
                            temp_x = (right - seed_x);
                            temp_y = (seed_y - top);
                        }
                        double radius = 0;
                        if (temp_x > temp_y)
                            radius = temp_x;
                        else
                            radius = temp_y;
                        double fadelength = ((double) radius * ((double) fadeEndPercent / 100));
                        double fadestart = ((double) radius * ((double) fadeStartPercent / 100));
                        int a = Math.abs(seed_x - i);
                        int b = Math.abs(seed_y - j);
                        double dist = Math.sqrt((a * a) + (b * b));

                        if (dist <= fadestart) {
                            tempalpha.setPixel(i, j, Color.argb(255, 255, 255, 255));
                        } else {
                            int fadeoff = 255 - (int) ((dist - fadestart) * (255 / (fadelength - fadestart)));
                            if (dist > radius * (fadeEndPercent / 100)) fadeoff = 0;
                            tempalpha.setPixel(i, j, Color.argb(fadeoff, 255, 255, 255));
                        }

                    } else {
                        tempalpha.setPixel(i, j, Color.argb(0, 255, 255, 255));
                    }
                }
            }
        }

        // Average alpha to the left of the seed
        for (int i = seed_x, j = seed_y; i > 1; i--)
        {
            int sum = 0;
            sum += Color.alpha(tempalpha.getPixel(i - 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j));
            sum += Color.alpha(tempalpha.getPixel(i, j));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j + 1));

            tempalpha.setPixel(i, j, Color.argb((int) Math.floor(sum / 9), 255, 255, 255));
        }

        // Average alpha to the right of the seed
        for (int i = seed_x, j = seed_y; i < m - 1; i++)
        {
            int sum = 0;
            sum += Color.alpha(tempalpha.getPixel(i - 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j));
            sum += Color.alpha(tempalpha.getPixel(i, j));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j + 1));

            tempalpha.setPixel(i, j, Color.argb((int) Math.floor(sum / 9), 255, 255, 255));
        }

        // Average alpha to the top of the seed
        for (int i = seed_x, j = seed_y; j > 1; j--)
        {
            int sum = 0;
            sum += Color.alpha(tempalpha.getPixel(i - 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j));
            sum += Color.alpha(tempalpha.getPixel(i, j));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j + 1));

            tempalpha.setPixel(i, j, Color.argb((int) Math.floor(sum / 9), 255, 255, 255));
        }

        // Average alpha to the bottom of the seed
        for (int i = seed_x, j = seed_y; j < n - 1; j++)
        {
            int sum = 0;
            sum += Color.alpha(tempalpha.getPixel(i - 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j - 1));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j));
            sum += Color.alpha(tempalpha.getPixel(i, j));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j));
            sum += Color.alpha(tempalpha.getPixel(i - 1, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i, j + 1));
            sum += Color.alpha(tempalpha.getPixel(i + 1, j + 1));

            tempalpha.setPixel(i, j, Color.argb((int) Math.floor(sum / 9), 255, 255, 255));
        }
        return tempalpha;
    }

    private Bitmap andOperation(Bitmap input, boolean[][] mask) {
        Bitmap output = Bitmap.createBitmap(input.getWidth(), input.getHeight(), Bitmap.Config.ARGB_8888);
        for (int i = 0; i < input.getWidth(); i++) {
            for (int j = 0; j < input.getHeight(); j++) {
                if (mask[i][j]) {
                    output.setPixel(i, j, input.getPixel(i, j));
                }

            }
        }
        return output;
    }

    private MyColor getRGBFromInt(int input, int offset) {
        int temp_a = Color.alpha(input);
        temp_a += offset;
        int temp_r = Color.red(input);
        temp_r += offset;
        int temp_g = Color.green(input);
        temp_g += offset;
        int temp_b = Color.blue(input);
        temp_b += offset;

        return new MyColor(temp_a, temp_r, temp_b, temp_g);
    }
}
/**
 * Author: Dre Harm
 * Date: 2/6/25
 * Purpose: Because GDX doesn't have perlin noise built in and I forgot to download the extension during setup I have to make it.
 * Adapted from Coding Trains' p5.js tutorial which can be found here -> https://thecodingtrain.com/tracks/the-nature-of-code-2/noc/perlin/intro-to-perlin-noise
 */

package com.asteroids.game;

import java.util.Random;

public class PerlinNoise {
    private static final int PERMUTATION[] = new int[512];
    private static final Random random = new Random();

    static {
        int[] p = new int[256];
        for (int i = 0; i < 256; i++) p[i] = i;

        for (int i = 0; i < 256; i++) {
            int r = random.nextInt(256 - i) + i;
            int temp = p[i];
            p[i] = p[r];
            p[r] = temp;
            PERMUTATION[i] = PERMUTATION[i + 256] = p[i];
        }
    }

    private static float fade(float t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static float grad(int hash, float x) {
        return ((hash & 1) == 0 ? x : -x);
    }

    public static float noise(float x) {
        int X = (int) Math.floor(x) & 255;
        x -= Math.floor(x);
        float u = fade(x);
        return lerp(u, grad(PERMUTATION[X], x), grad(PERMUTATION[X + 1], x - 1));
    }

    private static float lerp(float t, float a, float b) {
        return a + t * (b - a);
    }
}

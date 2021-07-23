package com.stho.mobicompass;

class Direction {

    static String getName(float degree) {
         while (degree < 0f) {
            degree += 360f;
        }
        degree += 5.625f;
        int points = (int) (degree / 11.25f);
        return getNameForPoints(points);
    }

    // @formatter:off
    private static String getNameForPoints(int points) {
        switch (points) {
            case 0: return "North";
            case 1: return "North by east";
            case 2: return "North-northeast";
            case 3: return "Northeast by north";
            case 4: return "Northeast";
            case 5: return "Northeast by east";
            case 6: return "East-northeast";
            case 7: return "East by north";
            case 8: return "East";
            case 9: return "East by south";
            case 10: return "East-southeast";
            case 11: return "Southeast by east";
            case 12: return "Southeast";
            case 13: return "Southeast by south";
            case 14: return "South-southeast";
            case 15: return "South by east";
            case 16: return "South";
            case 17: return "South by west";
            case 18: return "South-southwest";
            case 19: return "Southwest by south";
            case 20: return "Southwest";
            case 21: return "Southwest by west";
            case 22: return "West-southwest";
            case 23: return "West by south";
            case 24: return "West";
            case 25: return "West by north";
            case 26: return "West-northwest";
            case 27: return "Northwest by west";
            case 28: return "Northwest";
            case 29: return "Northwest by north";
            case 30: return "North-northwest";
            case 31: return "North by west";
        }
        return "North";
    }
    // @formatter:on
}

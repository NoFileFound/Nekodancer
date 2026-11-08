package org.nekodancer.utils;

// Imports
import java.util.Locale;

public final class Utils {
    /**
     * Formats the room name.
     * @param roomName The room name to format.
     * @param roomCommunity The room's community.
     * @param isStaff Is the player a staff member.
     * @return The formatted room name.
     */
    public static String formatRoomName(String roomName, String roomCommunity, boolean isStaff) {
        if (roomName.isEmpty())
            return roomName;

        roomCommunity = getCommunityFromLanguage(roomCommunity).toUpperCase();
        roomName = roomName.replace("<", "lt;");
        int sep = roomName.indexOf('-');
        if (sep != -1) {
            String prefix = roomName.substring(0, sep).toUpperCase();
            String name = roomName.substring(sep + 1);
            if (name.startsWith("*"))
                return "int-" + name;

            if (isStaff || prefix.equals(roomCommunity))
                return roomName;

            if (!getCommunityFromLanguage(prefix).equals("INT"))
                roomName = name;
        }

        return (roomName.startsWith("*") ? "INT" : roomCommunity) + '-' + roomName;
    }

    /**
     * Gets the game community based on the spoken language.
     * @param languageCode The language's iso 2 code.
     * @return The community.
     */
    public static String getCommunityFromLanguage(String languageCode) {
        return switch (languageCode.toUpperCase()) {
            case "AF" -> "ZA";
            case "AZ" -> "AZ";
            case "ID" -> "ID";
            case "UR" -> "PK";
            case "BI" -> "VU";
            case "MS" -> "MY";
            case "CA" -> "ES";
            case "DE" -> "DE";
            case "EN" -> "GB";
            case "ES" -> "ES";
            case "FR" -> "FR";
            case "HR" -> "HR";
            case "IT" -> "IT";
            case "SW" -> "TZ";
            case "NL" -> "NL";
            case "PT" -> "PT";
            case "RO" -> "RO";
            case "QU" -> "PE";
            case "ST" -> "ZA";
            case "TN" -> "ZA";
            case "SQ" -> "AL";
            case "SS" -> "SZ";
            case "ZH" -> "CN";
            case "TI" -> "ET";
            case "TA" -> "IN";
            case "BN" -> "BD";
            case "HI" -> "IN";
            case "NE" -> "NP";
            case "FA" -> "IR";
            case "AR" -> "SA";
            case "SR" -> "RS";
            case "RU" -> "RU";
            case "EL" -> "GR";
            case "YO" -> "NG";
            case "TR" -> "TR";
            case "SV" -> "SE";
            case "SO" -> "SO";
            case "BS" -> "BA";
            case "NY" -> "MW";
            case "DA" -> "DK";
            case "ET" -> "EE";
            case "NA" -> "NR";
            case "TO" -> "TO";
            case "MG" -> "MG";
            case "SM" -> "WS";
            case "MH" -> "MH";
            case "KL" -> "GL";
            case "RN" -> "BI";
            case "RW" -> "RW";
            case "HT" -> "HT";
            case "LV" -> "LV";
            case "LT" -> "LT";
            case "LB" -> "LU";
            case "HU" -> "HU";
            case "MT" -> "MT";
            case "NO" -> "NO";
            case "UZ" -> "UZ";
            case "PL" -> "PL";
            case "BR" -> "BR";
            case "SK" -> "SK";
            case "SL" -> "SI";
            case "FI" -> "FI";
            case "TL" -> "PH";
            case "VI" -> "VN";
            case "TK" -> "TM";
            case "FJ" -> "FJ";
            case "WO" -> "SN";
            case "IS" -> "IS";
            case "CS" -> "CZ";
            case "BE" -> "BY";
            case "KY" -> "KG";
            case "MK" -> "MK";
            case "MN" -> "MN";
            case "TG" -> "TJ";
            case "UK" -> "UA";
            case "BG" -> "BG";
            case "KK" -> "KZ";
            case "HY" -> "AM";
            case "HE" -> "IL";
            case "DV" -> "MV";
            case "TH" -> "TH";
            case "LO" -> "LA";
            case "DZ" -> "BT";
            case "MY" -> "MM";
            case "KA" -> "GE";
            case "AM" -> "ET";
            case "KM" -> "KH";
            case "CN" -> "CN";
            case "JA" -> "JP";
            case "KO" -> "KR";
            default -> "INT";
        };
    }

    /**
     * Gets the country's name by country's code.
     * @param countryCode The country's code.
     * @return The country's name.
     */
    public static String getCountryNameByCode(String countryCode) {
        if (countryCode == null || countryCode.isBlank())
            return "null";

        String countryName = new Locale.Builder().setRegion(countryCode.toUpperCase()).build().getDisplayCountry(Locale.ENGLISH);
        return countryName.isEmpty() ? "null" : countryName;
    }

    /**
     * Gets the unix timestamp.
     * @return Seconds.
     */
    public static long getUnixTime() {
        return System.currentTimeMillis() / 1000;
    }
}
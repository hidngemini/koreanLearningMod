package com.hidngemini.speakit.utils;

import io.github.jaewonpro.HangulUtils;
import java.util.List;

public class HangulPhoneticConverter {

    public static String toPhonetic(String koreanText) {
        // handle empty input
        if (koreanText == null || koreanText.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        for (char c : koreanText.toCharArray()) {
            // dissasemble each character into a list of jamos
            List<Character> jamos = HangulUtils.disassembleToList(c);
            String result = mapJamosToPhonetic(jamos);

            if (result.equals(" ")) {
                sb.deleteCharAt(sb.length()-1); // remove dash after previous syllable if there's a space
            }

            // if can't be disassembled (which means it's not hangul), just return
            if (jamos.size() == 1 && jamos.get(0) == c) {
                sb.append(c);
                continue;
            }

            // add each phonetic to our stringbuilder
            sb.append(result);
            
            // only append a dash if the syllable wasn't a space
            if (!result.equals(" ")) {
                sb.append("-"); 
            }
        }
        sb.deleteCharAt(sb.length()-1); // remove last dash

        return sb.toString();
    }

    private static String mapJamosToPhonetic(List<Character> jamos) {
        StringBuilder syllablePhonetic = new StringBuilder();

        // first consonant
        if (jamos.size() > 0) {
            syllablePhonetic.append(mapInitial(jamos.get(0)));
        }

        // vowel
        if (jamos.size() > 1) {
            syllablePhonetic.append(mapVowel(jamos.get(1)));
        }

        // last consonant (including the batchim ones)
        if (jamos.size() > 2) {
            StringBuilder batchimChars = new StringBuilder();
            for (int i = 2; i < jamos.size(); i++) {
                batchimChars.append(jamos.get(i));
            }
            syllablePhonetic.append(mapBatchim(batchimChars.toString()));
        }

        return syllablePhonetic.toString();
    }

    private static String mapInitial(char jamo) {
        switch (jamo) {
            // phonetic readings of each possible initial consonant
            case 'ㄱ': return "g";
            case 'ㄲ': return "gg";
            case 'ㄴ': return "n";
            case 'ㄷ': return "d";
            case 'ㄸ': return "dd";
            case 'ㄹ': return "r";
            case 'ㅁ': return "m";
            case 'ㅂ': return "b";
            case 'ㅃ': return "bb";
            case 'ㅅ': return "s";
            case 'ㅆ': return "ss";
            case 'ㅇ': return "";
            case 'ㅈ': return "j";
            case 'ㅉ': return "jj";
            case 'ㅊ': return "ch";
            case 'ㅋ': return "k";
            case 'ㅌ': return "t";
            case 'ㅍ': return "p";
            case 'ㅎ': return "h";
            default: return String.valueOf(jamo);
        }
    }

    private static String mapVowel(char jamo) {
        switch (jamo) {
            // phonetic mappings of all vowels (and compound vowels)
            case 'ㅏ': return "a";
            case 'ㅐ': return "ae";
            case 'ㅑ': return "ya";
            case 'ㅒ': return "yae";
            case 'ㅓ': return "eo";
            case 'ㅔ': return "e";
            case 'ㅕ': return "yeo";
            case 'ㅖ': return "ye";
            case 'ㅗ': return "o";
            case 'ㅘ': return "wa";
            case 'ㅙ': return "wae";
            case 'ㅚ': return "oe";
            case 'ㅛ': return "yo";
            case 'ㅜ': return "u";
            case 'ㅝ': return "weo";
            case 'ㅞ': return "we";
            case 'ㅟ': return "wi";
            case 'ㅠ': return "yu";
            case 'ㅡ': return "eu";
            case 'ㅢ': return "ui";
            case 'ㅣ': return "i";
            default: return String.valueOf(jamo);
        }
    }

    private static String mapBatchim(String batchimJamos) {
        switch (batchimJamos) {
            // phonetic mapping of all ending consonants (except i simplified it because i don't quite know enough to confidently do otherwise)
            case "ㄱ": case "ㄲ": case "ㄳ": case "ㄺ": return "k";
            case "ㄴ": case "ㄵ": case "ㄶ": return "n";
            case "ㄷ": case "ㅅ": case "ㅆ": case "ㅈ": case "ㅊ": case "ㅌ": case "ㅎ": return "t";
            case "ㄹ": case "ㄽ": case "ㄾ": case "ㅀ": return "l";
            case "ㅁ": case "ㄻ": return "m";
            case "ㅂ": case "ㄿ": case "ㅄ": return "p";
            case "ㄼ": return "l";
            case "ㅇ": return "ng";
            default: return "";
        }
    }
}
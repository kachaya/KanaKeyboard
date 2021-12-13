package io.github.kachaya.kkbd;

import java.util.HashMap;

public class Converter {
    // 送り仮名をSKK辞書検索用のアスキー１文字にするテーブル
    private static final HashMap<String, String> okuriMap = new HashMap<String, String>() {
        {
            put("あ", "a");
            put("い", "i");
            put("う", "u");
            put("え", "e");
            put("お", "o");
            put("か", "k");
            put("き", "k");
            put("く", "k");
            put("け", "k");
            put("こ", "k");
            put("っか", "k");
            put("っき", "k");
            put("っく", "k");
            put("っけ", "k");
            put("っこ", "k");
            put("さ", "s");
            put("し", "s");
            put("す", "s");
            put("せ", "s");
            put("そ", "s");
            put("っさ", "s");
            put("っし", "s");
            put("っす", "s");
            put("っせ", "s");
            put("っそ", "s");
            put("た", "t");
            put("ち", "t");
            put("つ", "t");
            put("て", "t");
            put("と", "t");
            put("った", "t");
            put("っち", "t");
            put("っつ", "t");
            put("って", "t");
            put("っと", "t");
            put("な", "n");
            put("に", "n");
            put("ぬ", "n");
            put("ね", "n");
            put("の", "n");
            put("は", "h");
            put("ひ", "h");
            put("ふ", "h");
            put("へ", "h");
            put("ほ", "h");
            put("っは", "h");
            put("っひ", "h");
            put("っふ", "h");
            put("っへ", "h");
            put("っほ", "h");
            put("ま", "m");
            put("み", "m");
            put("む", "m");
            put("め", "m");
            put("も", "m");
            put("っま", "m");
            put("っみ", "m");
            put("っむ", "m");
            put("っめ", "m");
            put("っも", "m");
            put("や", "y");
            put("ゆ", "y");
            put("よ", "y");
            put("っや", "y");
            put("っゆ", "y");
            put("っよ", "y");
            put("ら", "r");
            put("り", "r");
            put("る", "r");
            put("れ", "r");
            put("ろ", "r");
            put("っら", "r");
            put("っり", "r");
            put("っる", "r");
            put("っれ", "r");
            put("っろ", "r");
            put("わ", "w");
            put("っわ", "w");
            put("が", "g");
            put("ぎ", "g");
            put("ぐ", "g");
            put("げ", "g");
            put("ご", "g");
            put("っが", "g");
            put("っぎ", "g");
            put("っぐ", "g");
            put("っげ", "g");
            put("っご", "g");
            put("ざ", "z");
            put("じ", "z");
            put("ず", "z");
            put("ぜ", "z");
            put("ぞ", "z");
            put("っざ", "z");
            put("っじ", "z");
            put("っず", "z");
            put("っぜ", "z");
            put("っぞ", "z");
            put("だ", "d");
            put("ぢ", "d");
            put("づ", "d");
            put("で", "d");
            put("ど", "d");
            put("っだ", "d");
            put("っぢ", "d");
            put("っづ", "d");
            put("っで", "d");
            put("っど", "d");
            put("ば", "b");
            put("び", "b");
            put("ぶ", "b");
            put("べ", "b");
            put("ぼ", "b");
            put("っば", "b");
            put("っび", "b");
            put("っぶ", "b");
            put("っべ", "b");
            put("っぼ", "b");
            put("ぱ", "p");
            put("ぴ", "p");
            put("ぷ", "p");
            put("ぺ", "p");
            put("ぽ", "p");
            put("っぱ", "p");
            put("っぴ", "p");
            put("っぷ", "p");
            put("っぺ", "p");
            put("っぽ", "p");
        }
    };
    // 全角濁点'゛'結合用 consonant
    private static final HashMap<Character, Character> dakutenMap = new HashMap<Character, Character>() {
        {
            put('う', 'ゔ');

            put('か', 'が');
            put('き', 'ぎ');
            put('く', 'ぐ');
            put('け', 'げ');
            put('こ', 'ご');

            put('さ', 'ざ');
            put('し', 'じ');
            put('す', 'ず');
            put('せ', 'ぜ');
            put('そ', 'ぞ');

            put('た', 'だ');
            put('ち', 'ぢ');
            put('つ', 'づ');
            put('て', 'で');
            put('と', 'ど');

            put('は', 'ば');
            put('ひ', 'び');
            put('ふ', 'ぶ');
            put('へ', 'べ');
            put('ほ', 'ぼ');

            put('ウ', 'ヴ');

            put('カ', 'ガ');
            put('キ', 'ギ');
            put('ク', 'グ');
            put('ケ', 'ゲ');
            put('コ', 'ゴ');

            put('サ', 'ザ');
            put('シ', 'ジ');
            put('ス', 'ズ');
            put('セ', 'ゼ');
            put('ソ', 'ゾ');

            put('タ', 'ダ');
            put('チ', 'ヂ');
            put('ツ', 'ヅ');
            put('テ', 'デ');
            put('ト', 'ド');

            put('ハ', 'バ');
            put('ヒ', 'ビ');
            put('フ', 'ブ');
            put('ヘ', 'ベ');
            put('ホ', 'ボ');
        }
    };
    // 全角半濁点'゜'結合用
    private static final HashMap<Character, Character> handakutenMap = new HashMap<Character, Character>() {
        {
            put('は', 'ぱ');
            put('ひ', 'ぴ');
            put('ふ', 'ぷ');
            put('へ', 'ぺ');
            put('ほ', 'ぽ');

            put('ハ', 'パ');
            put('ヒ', 'ピ');
            put('フ', 'プ');
            put('ヘ', 'ペ');
            put('ホ', 'ポ');
        }
    };

    public static String getOkuriAscii(String okuri) {
        return okuriMap.get(okuri);
    }

    // 濁点
    public static Character combineDakuten(char ch) {
        return dakutenMap.get(ch);
    }

    // 半濁点
    public static Character combineHandakuten(char ch) {
        return handakutenMap.get(ch);
    }

    // 全角ひらがな変換
    public static char toWideHiragana(char ch) {
        if (ch >= 'ァ' && ch <= 'ヶ') {
            return (char) (ch - 'ァ' + 'ぁ');
        }
        return ch;
    }

    // 全角ひらがな変換
    public static String toWideHiragana(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length(); i++) {
            sb.append(toWideHiragana(cs.charAt(i)));
        }
        return sb.toString();
    }

    // 全角カタカナ変換
    public static char toWideKatakana(char ch) {
        if (ch >= 'ぁ' && ch <= 'ゖ') {
            return (char) (ch - 'ぁ' + 'ァ');
        }
        return ch;
    }

    // 全角カタカナ変換
    public static String toWideKatakana(CharSequence cs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cs.length(); i++) {
            sb.append(toWideKatakana(cs.charAt(i)));
        }
        return sb.toString();
    }
}

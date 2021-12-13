package io.github.kachaya.kkbd;

import android.content.SharedPreferences;
import android.inputmethodservice.InputMethodService;
import android.text.InputType;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;

import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Set;

public class InputService extends InputMethodService {

    public final static int INPUT_MODE_QWERTY_HALF = 0;
    public final static int INPUT_MODE_QWERTY_WIDE = 1;
    public final static int INPUT_MODE_HIRAGANA_WIDE = 2;
    public final static int INPUT_MODE_KATAKANA_WIDE = 3;
    public final static int INPUT_MODE_KATAKANA_HALF = 4;
    public final static int SHIFT_STATE_NONE = 0;
    public final static int SHIFT_STATE_SINGLE = 1;
    public final static int SHIFT_STATE_LOCK = 2;
    private final static int SHIFT_STATE_NUM = 3;
    private final String TAG = "InputService";
    private final StringBuilder mComposing = new StringBuilder();
    private final ArrayList<String> mCandidateKey = new ArrayList<>();    // 辞書検索キー
    private final ArrayList<String> mCandidateValue = new ArrayList<>();   // 表示用
    private int mInputMode;
    private int mShiftState;
    private boolean mShiftToggle;
    private InputView mInputView;
    private Dictionary mDictionary;
    private int mCandidateIndex = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        mDictionary = new Dictionary(this);
    }

    @Override
    public void onDestroy() {
        mDictionary.commit();
        super.onDestroy();
    }

    @Override
    public boolean onEvaluateFullscreenMode() {
        return false;   // フルスクリーンモード無効
    }

    @Override
    public void onInitializeInterface() {
        //Log.d(TAG, "onInitializeInterface");
        super.onInitializeInterface();
        mInputView = new InputView(this, null);
    }

    @Override
    public View onCreateInputView() {
        return mInputView;
    }

    @Override
    public void onStartInput(EditorInfo ei, boolean restarting) {
        //Log.d(TAG, "onStartInput restarting=" + restarting);
        super.onStartInput(ei, restarting);
    }

    @Override
    public void onFinishInput() {
        //Log.d(TAG, "onFinishInput");
        startHalfLatinMode();
        mDictionary.commit();
        super.onFinishInput();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        //Log.d(TAG, "onFinishInputView finishingInput=" + finishingInput);
        startHalfLatinMode();
        super.onFinishInputView(finishingInput);
    }

    @Override
    public void onStartInputView(EditorInfo attribute, boolean restarting) {
        super.onStartInputView(attribute, restarting);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mShiftToggle = sharedPreferences.getBoolean("shift_toggle", true);

        boolean startKana = sharedPreferences.getBoolean("start_kana", true);
        if (startKana) {
            switch (attribute.inputType & InputType.TYPE_MASK_CLASS) {
                case InputType.TYPE_CLASS_NUMBER:
                case InputType.TYPE_CLASS_DATETIME:
                case InputType.TYPE_CLASS_PHONE:
                    startKana = false;
                    break;
                case InputType.TYPE_CLASS_TEXT:
                    switch (attribute.inputType & InputType.TYPE_MASK_VARIATION) {
                        case InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS:
                        case InputType.TYPE_TEXT_VARIATION_PASSWORD:
                        case InputType.TYPE_TEXT_VARIATION_URI:
                        case InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:
                        case InputType.TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS:
                        case InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD:
                            startKana = false;
                            break;
                        default:
                            break;
                    }
                default:
                    break;
            }
        }
        if (startKana) {
            startHiraganaMode();
        } else {
            startHalfLatinMode();
        }
    }

    private void startHalfLatinMode() {
        resetComposing();
        mInputMode = INPUT_MODE_QWERTY_HALF;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startWideLatinMode() {
        resetComposing();
        mInputMode = INPUT_MODE_QWERTY_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startHiraganaMode() {
        resetComposing();
        mInputMode = INPUT_MODE_HIRAGANA_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startKatakanaMode() {
        resetComposing();
        mInputMode = INPUT_MODE_KATAKANA_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void icCommitText(CharSequence cs) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            String text;
            if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                text = Converter.toWideKatakana(cs);
            } else {
                text = cs.toString();
            }
            ic.commitText(text, 1);
        }
    }

    private void icCommitChar(char ch) {
        sendKeyChar(ch);
    }

    private void icSetComposingText(CharSequence cs) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            String text;
            if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                text = Converter.toWideKatakana(cs);
            } else {
                text = cs.toString();
            }
            ic.setComposingText(text, 1);
        }
    }

    private void icSendDpadLeftKey() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_LEFT);
    }

    private void icSendDpadRightKey() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DPAD_RIGHT);
    }

    private void icSendEnterKey() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER);
    }

    private void icSendDelKey() {
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL);
    }

    // 文字入力処理
    public void processChar(char ch) {

        switch (mInputMode) {
            case INPUT_MODE_QWERTY_HALF:
            case INPUT_MODE_QWERTY_WIDE:
            case INPUT_MODE_KATAKANA_HALF:
                icCommitChar(ch);
                return;
            case INPUT_MODE_HIRAGANA_WIDE:
            case INPUT_MODE_KATAKANA_WIDE:
                break;
            default:
                return;
        }

        // 候補選択済みで新しい文字が入力されたら確定
        if (mCandidateIndex >= 0) {
            icCommitText(mCandidateValue.get(mCandidateIndex));
            addDictionary(mComposing, mCandidateValue.get(mCandidateIndex));
            resetComposing();
        }

        // 濁点・半濁点処理
        int len = mComposing.length();
        if (len > 0) {
            Character c;
            if (ch == '゛') {
                c = Converter.combineDakuten(mComposing.charAt(len - 1));
                if (c != null) {
                    mComposing.deleteCharAt(len - 1);
                    ch = c;
                }
            } else if (ch == '゜') {
                c = Converter.combineHandakuten(mComposing.charAt(len - 1));
                if (c != null) {
                    mComposing.deleteCharAt(len - 1);
                    ch = c;
                }
            }
        }

        mComposing.append(ch);
        if (mShiftState == SHIFT_STATE_SINGLE) {
            mShiftState = SHIFT_STATE_NONE;
            mInputView.setKeyboard(mInputMode, mShiftState);
        }
        updateSuggestion();
    }


    // スペースキー
    public void handleSpace() {
        //Log.d(TAG, "handleSpace");

        if (mComposing.length() == 0) {
            char c;
            switch (mInputMode) {
                default:
                case INPUT_MODE_QWERTY_HALF:
                case INPUT_MODE_KATAKANA_HALF:
                    c = ' ';
                    break;
                case INPUT_MODE_QWERTY_WIDE:
                case INPUT_MODE_HIRAGANA_WIDE:
                case INPUT_MODE_KATAKANA_WIDE:
                    c = '\u3000';       // TODO:設定で半角スペース
                    break;
            }
            sendKeyChar(c);
        } else {
            if (mCandidateIndex >= 0) {     // 変換中
                selectNextCandidate();
            } else {
                startConversion();          // 変換開始
            }
        }
    }

    // キーボード切り替え
    public void handleKeyboard() {
        //Log.d(TAG, "handleKeyboard");
        switch (mInputMode) {
            case INPUT_MODE_QWERTY_HALF:
            case INPUT_MODE_QWERTY_WIDE:
                mInputMode = INPUT_MODE_HIRAGANA_WIDE;
                break;
            case INPUT_MODE_HIRAGANA_WIDE:
            case INPUT_MODE_KATAKANA_WIDE:
            case INPUT_MODE_KATAKANA_HALF:
                mInputMode = INPUT_MODE_QWERTY_HALF;
                break;
            default:
                break;

        }
        resetComposing();
        mShiftState = SHIFT_STATE_NONE;     // Shift状態はリセット
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    // 入力モード切り替え
    public void handleMode() {
        //Log.d(TAG, "handleMode");
        switch (mInputMode) {
            case INPUT_MODE_QWERTY_HALF:
                mInputMode = INPUT_MODE_QWERTY_WIDE;    // a1→Ｗ
                break;
            case INPUT_MODE_QWERTY_WIDE:
                mInputMode = INPUT_MODE_QWERTY_HALF;    // Ｗ→a1
                break;
            case INPUT_MODE_HIRAGANA_WIDE:
                mInputMode = INPUT_MODE_KATAKANA_WIDE;  // あ→ア
                break;
            case INPUT_MODE_KATAKANA_WIDE:
                mInputMode = INPUT_MODE_KATAKANA_HALF;  // ア→ｶﾅ
                break;
            case INPUT_MODE_KATAKANA_HALF:
                mInputMode = INPUT_MODE_HIRAGANA_WIDE;  // ｶﾅ→あ
                break;
            default:
                break;
        }
        resetComposing();
        mShiftState = SHIFT_STATE_NONE;     // Shift状態はリセット
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    // Shift
    public void handleShift() {
        if (mShiftToggle) {
            if (mShiftState == SHIFT_STATE_NONE) {
                mShiftState = SHIFT_STATE_LOCK;
            } else {
                mShiftState = SHIFT_STATE_NONE;
            }
        } else {
            mShiftState = (mShiftState + 1) % SHIFT_STATE_NUM;
        }
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    // カーソル左
    public void handleCursorLeft() {
        //Log.d(TAG, "handleCursorLeft");
        if (mComposing.length() == 0) {
            icSendDpadLeftKey();
        } else {
            if (mCandidateIndex >= 0) {
                selectPrevCandidate();
            }
        }
    }

    // カーソル右
    public void handleCursorRight() {
        //Log.d(TAG, "handleCursorRight");
        if (mComposing.length() == 0) {
            icSendDpadRightKey();
        } else {
            if (mCandidateIndex >= 0) {
                selectNextCandidate();
            }
        }
    }

    // Backspace
    public void handleBackspace() {
        int len = mComposing.length();
        if (len == 0) {
            icSendDelKey();
        } else {
            if (mCandidateIndex >= 0) {
                // 変換中
                resetCandidate();
                updateSuggestion();
            } else {
                mComposing.deleteCharAt(len - 1);
                if (mComposing.length() == 0) {
                    resetComposing();
                } else {
                    updateSuggestion(); // 変換中でもSuggestionに戻る
                }
            }
        }
    }

    // Enter
    public void handleEnter() {
        if (mComposing.length() == 0) {
            icSendEnterKey();
        } else {
            if (mCandidateIndex >= 0) {
                // 辞書変換中
                clickCandidate(mCandidateIndex);
            } else {
                icCommitText(mComposing);
                addDictionary(mComposing, mComposing);
                resetComposing();
            }
        }
    }

    // 入力ビューからの候補ボタンクリックは確定
    public void clickCandidate(int index) {
        //Log.d(TAG, "clickCandidate(" + index + ")");
        int num = mCandidateValue.size();
        if (index < num) {
            mCandidateIndex = index;
            String key = mCandidateKey.get(index);
            String value = mCandidateValue.get(index);
            icCommitText(value);
            addDictionary(key, value);
            resetComposing();
        }
    }

    private void selectNextCandidate() {
        //Log.d("selectNextCandidate", "mCandidateIndex=" + mCandidateIndex);
        int num = mCandidateValue.size();
        if (num > 0) {
            mCandidateIndex = (mCandidateIndex + 1) % num;  // 次候補
            icSetComposingText(mCandidateValue.get(mCandidateIndex));
            mInputView.selectCandidate(mCandidateIndex);
        }
    }

    private void selectPrevCandidate() {
        //Log.d("selectPrevCandidate", "mCandidateIndex=" + mCandidateIndex);
        int num = mCandidateValue.size();
        if (num > 0) {
            mCandidateIndex = (mCandidateIndex + num - 1) % num;    // 前候補
            icSetComposingText(mCandidateValue.get(mCandidateIndex));
            mInputView.selectCandidate(mCandidateIndex);
        }
    }

    private void resetComposing() {
        mInputView.setSpaceButtonLabel("␣");
        icSetComposingText("");
        mComposing.setLength(0);
        resetCandidate();
    }

    private void resetCandidate() {
        mCandidateIndex = -1;   // 未選択
        mCandidateKey.clear();
        mCandidateValue.clear();
        mInputView.clearCandidates();
    }

    private void addCandidate(String key, String value) {
        //Log.d("addCandidate", "key=" + key + ",value=" + value);
        if (mCandidateValue.contains(value)) {
            //Log.d("contains", "key=" + key + ",value=" + value);
            return;
        }
        mCandidateKey.add(key);
        mCandidateValue.add(value);
    }

    private void updateSuggestion() {
        resetCandidate();
        String hiragana = Converter.toWideHiragana(mComposing);
        String katakana = Converter.toWideKatakana(hiragana);

        addCandidate(hiragana, hiragana);
        addCandidate(hiragana, katakana);

        ArrayList<String> keys = mDictionary.getUserKeys(hiragana);
        keys.sort(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            char ch = key.charAt(key.length() - 1);
            if (ch >= 'a' && ch <= 'z') {
                continue;   // 送りあり
            }
            String[] values = mDictionary.searchUserDic(key);
            for (String value : values) {
                addCandidate(key, value);
            }
        }

        mInputView.setCandidates(mCandidateValue);
        icSetComposingText(mComposing);
        mInputView.setSpaceButtonLabel("変換");
    }

    private void startConversion() {
        resetCandidate();

        String hiragana = Converter.toWideHiragana(mComposing);
        String katakana = Converter.toWideKatakana(hiragana);
        Set<String> lhs = new LinkedHashSet<>();
        String[] ss;
        int len = hiragana.length();
        String firstKey;
        String secondKey;
        String ascii;
        String okuri;

        // 入力したものそのままでユーザ辞書検索
        ss = mDictionary.searchUserDic(hiragana);
        if (ss != null) {
            lhs.addAll(Arrays.asList(ss));
        }
        // 後ろから分割して辞書検索(ユーザ辞書)
        for (int pos = len - 1; pos > 0; pos--) {
            firstKey = hiragana.substring(0, pos);
            secondKey = hiragana.substring(pos, len);
            if (secondKey.charAt(0) == 'っ') {
                if (secondKey.length() >= 2) {
                    okuri = secondKey.substring(0, 2);
                } else {
                    okuri = "";
                }
            } else {
                okuri = secondKey.substring(0, 1);
            }
            ascii = Converter.getOkuriAscii(okuri);
            if (ascii != null) {
                ss = mDictionary.searchUserDic(firstKey + ascii);
                if (ss != null) {
                    for (String s : ss) {
                        lhs.add(s + secondKey);
                    }
                }
            }
            ss = mDictionary.searchUserDic(firstKey);
            if (ss != null) {
                for (String s : ss) {
                    lhs.add(s + secondKey);
                }
            }
        }

        // 入力したものそのままでメイン辞書検索
        ss = mDictionary.searchMainDic(hiragana);
        if (ss != null) {
            lhs.addAll(Arrays.asList(ss));
        }
        // 後ろから分割して辞書検索(メイン辞書)
        for (int pos = len - 1; pos > 0; pos--) {
            firstKey = hiragana.substring(0, pos);
            secondKey = hiragana.substring(pos, len);
            if (secondKey.charAt(0) == 'っ') {
                if (secondKey.length() >= 2) {
                    okuri = secondKey.substring(0, 2);
                } else {
                    okuri = "";
                }
            } else {
                okuri = secondKey.substring(0, 1);
            }
            ascii = Converter.getOkuriAscii(okuri);
            if (ascii != null) {
                ss = mDictionary.searchMainDic(firstKey + ascii);
                if (ss != null) {
                    for (String s : ss) {
                        lhs.add(s + secondKey);
                    }
                }
            }
            ss = mDictionary.searchMainDic(firstKey);
            if (ss != null) {
                for (String s : ss) {
                    lhs.add(s + secondKey);
                }
            }
        }
        //Log.d("startConversion", lhs.toString());

        if (!lhs.contains(hiragana)) {
            addCandidate(hiragana, hiragana);
        }
        if (!lhs.contains(katakana)) {
            addCandidate(hiragana, katakana);
        }
        for (String s : lhs) {
            addCandidate(hiragana, s);
        }
        //Log.d("startConversion2", mCandidateValue.toString());

        mInputView.setCandidates(mCandidateValue);
        mCandidateIndex = 0;
        mInputView.selectCandidate(mCandidateIndex);
        icSetComposingText(mCandidateValue.get(mCandidateIndex));

        mInputView.setSpaceButtonLabel("選択");
    }

    private void addDictionary(CharSequence key, CharSequence val) {
        String yomi = Converter.toWideHiragana(key);
        String kanji = val.toString();
        //Log.d("addDictionary", "key=" + key + ",val=" + val + ",yomi=" + yomi + ",kanji=" + kanji);
        mDictionary.add(yomi, kanji);
    }
}

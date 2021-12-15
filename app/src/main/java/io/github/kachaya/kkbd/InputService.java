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
import java.util.Comparator;

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

    //
    private final StringBuilder mComposing = new StringBuilder();

    // 候補
    private final ArrayList<String> mCandidateKey = new ArrayList<>();    // 辞書検索キー
    private final ArrayList<String> mCandidateValue = new ArrayList<>();   // 辞書登録語句
    private final ArrayList<String> mCandidateText = new ArrayList<>();   // 表示用
    private int mCandidateNum;
    private int mCandidateIndex;
    //
    private int mInputMode;
    private int mShiftState;
    private boolean mShiftToggle;
    private InputView mInputView;
    private Dictionary mDictionary;

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
        startLatinHalfMode();
        mDictionary.commit();
        super.onFinishInput();
    }

    @Override
    public void onFinishInputView(boolean finishingInput) {
        //Log.d(TAG, "onFinishInputView finishingInput=" + finishingInput);
        startLatinHalfMode();
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
            startHiraganaWideMode();
        } else {
            startLatinHalfMode();
        }
    }

    private void startLatinHalfMode() {
        resetComposing();
        mInputMode = INPUT_MODE_QWERTY_HALF;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startLatinWideMode() {
        resetComposing();
        mInputMode = INPUT_MODE_QWERTY_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startHiraganaWideMode() {
        resetComposing();
        mInputMode = INPUT_MODE_HIRAGANA_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startKatakanaWideMode() {
        resetComposing();
        mInputMode = INPUT_MODE_KATAKANA_WIDE;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void startKatakanaHalfMode() {
        resetComposing();
        mInputMode = INPUT_MODE_KATAKANA_HALF;
        mShiftState = SHIFT_STATE_NONE;
        mInputView.setKeyboard(mInputMode, mShiftState);
    }

    private void icCommitText(CharSequence cs) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.commitText(cs, 1);
        }
    }

    private void icCommitChar(char ch) {
        sendKeyChar(ch);
    }

    private void icSetComposingText(CharSequence cs) {
        InputConnection ic = getCurrentInputConnection();
        if (ic != null) {
            ic.setComposingText(cs, 1);
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
            commitCandidate();
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
        if (mCandidateIndex >= 0) {
            commitCandidate();
        } else {
            icCommitText(mComposing);
        }
        switch (mInputMode) {
            case INPUT_MODE_QWERTY_HALF:
            case INPUT_MODE_QWERTY_WIDE:
                startHiraganaWideMode();
                break;
            case INPUT_MODE_HIRAGANA_WIDE:
            case INPUT_MODE_KATAKANA_WIDE:
            case INPUT_MODE_KATAKANA_HALF:
                startLatinHalfMode();
                break;
            default:
                break;
        }
    }

    // 入力モード切り替え
    public void handleMode() {
        if (mCandidateIndex >= 0) {
            commitCandidate();
        } else {
            icCommitText(mComposing);
        }
        switch (mInputMode) {
            case INPUT_MODE_QWERTY_HALF:
                startLatinWideMode();
                break;
            case INPUT_MODE_QWERTY_WIDE:
                startLatinHalfMode();
                break;
            case INPUT_MODE_HIRAGANA_WIDE:
                startKatakanaWideMode();
                break;
            case INPUT_MODE_KATAKANA_WIDE:
                startKatakanaHalfMode();
                break;
            case INPUT_MODE_KATAKANA_HALF:
                startHiraganaWideMode();
                break;
            default:
                break;
        }
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
                commitCandidate();
            } else {
                icCommitText(mComposing);
            }
            resetComposing();
        }
    }

    private void selectNextCandidate() {
        if (mCandidateNum > 0) {
            mCandidateIndex = (mCandidateIndex + 1) % mCandidateNum;  // 次候補
            icSetComposingText(mCandidateValue.get(mCandidateIndex));
            mInputView.selectCandidate(mCandidateIndex);
        }
    }

    private void selectPrevCandidate() {
        if (mCandidateNum > 0) {
            mCandidateIndex = (mCandidateIndex + mCandidateNum - 1) % mCandidateNum;    // 前候補
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
        mCandidateNum = 0;
        mCandidateIndex = -1;   // 未選択
        mCandidateKey.clear();
        mCandidateValue.clear();
        mCandidateText.clear();
        mInputView.clearCandidates();
    }

    private void addCandidate(String key, String value, String text) {
        if (!mCandidateText.contains(text)) {
            mCandidateKey.add(key);
            mCandidateValue.add(value);
            mCandidateText.add(text);
        }
    }

    private void addCandidate(int index, String key, String value, String text) {
        if (!mCandidateText.contains(text)) {
            mCandidateKey.add(index, key);
            mCandidateValue.add(index, value);
            mCandidateText.add(index, text);
        }
    }

    private void commitCandidate() {
        String key = mCandidateKey.get(mCandidateIndex);
        String value = mCandidateValue.get(mCandidateIndex);
        String text = mCandidateText.get(mCandidateIndex);
        mDictionary.add(key, value);
        icCommitText(text);
    }

    // 入力ビューからの候補ボタンクリックは確定
    public void clickCandidate(int index) {
        mCandidateIndex = index;
        commitCandidate();
        resetComposing();
    }

    // 提案
    private void updateSuggestion() {
        ArrayList<String> keys;

        String hiragana = Converter.toWideHiragana(mComposing);
        String katakana = Converter.toWideKatakana(hiragana);
        String text;

        resetCandidate();

        addCandidate(hiragana, hiragana, hiragana);
        addCandidate(hiragana, katakana, katakana);

        // ユーザ辞書から
        keys = mDictionary.getUserKeys(hiragana);
        keys.sort(Comparator.comparingInt(String::length).reversed().thenComparing(Comparator.naturalOrder()));

        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            char ch = key.charAt(key.length() - 1);
            if (ch >= 'a' && ch <= 'z') {
                continue;   // 送りなしエントリだけが対象
            }
            String[] values = mDictionary.searchUserDic(key);
            if (values != null) {
                for (String value : values) {
                    text = value;
                    if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                        text = Converter.toWideKatakana(text);
                    }
                    addCandidate(key, value, text);
                }
            }
        }

        mCandidateIndex = -1;
        mCandidateNum = mCandidateText.size();
        mInputView.setCandidates(mCandidateText);
        icSetComposingText(mComposing);

        mInputView.setSpaceButtonLabel("変換");
    }

    private void startConversion() {
        resetCandidate();

        String key;
        int len;
        String firstKey;
        String secondKey;
        String ascii;
        String okuri;
        String[] values;
        String text;

        key = Converter.toWideHiragana(mComposing);
        len = key.length();

        // 入力したものでユーザ辞書検索
        values = mDictionary.searchUserDic(key);
        if (values != null) {
            for (String value : values) {
                text = value;
                if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                    text = Converter.toWideKatakana(text);
                }
                addCandidate(key, value, text);
            }
        }
        // 後ろから分割してユーザ辞書検索
        for (int pos = len - 1; pos > 0; pos--) {
            firstKey = key.substring(0, pos);
            secondKey = key.substring(pos, len);
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
                values = mDictionary.searchUserDic(firstKey + ascii);
                if (values != null) {
                    for (String value : values) {
                        text = value + secondKey;
                        if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                            text = Converter.toWideKatakana(text);
                        }
                        addCandidate(key, value + secondKey, text);
                    }
                }
            }
            values = mDictionary.searchUserDic(firstKey);
            if (values != null) {
                for (String value : values) {
                    text = value + secondKey;
                    if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                        text = Converter.toWideKatakana(text);
                    }
                    addCandidate(firstKey, value, text);
                }
            }
        }

        // 入力したものそのままでメイン辞書検索
        values = mDictionary.searchMainDic(key);
        if (values != null) {
            for (String value : values) {
                text = value;
                if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                    text = Converter.toWideKatakana(text);
                }
                addCandidate(key, value, text);
            }
        }
        // 後ろから分割してメイン辞書検索
        for (int pos = len - 1; pos > 0; pos--) {
            firstKey = key.substring(0, pos);
            secondKey = key.substring(pos, len);
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
                // 送りあり
                values = mDictionary.searchMainDic(firstKey + ascii);
                if (values != null) {
                    for (String value : values) {
                        text = value + secondKey;
                        if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                            text = Converter.toWideKatakana(text);
                        }
                        addCandidate(firstKey + okuri, value + okuri, text);
                    }
                }
            }
            // 送りなし
            values = mDictionary.searchMainDic(firstKey);
            if (values != null) {
                for (String value : values) {
                    text = value + secondKey;
                    if (mInputMode == INPUT_MODE_KATAKANA_WIDE) {
                        text = Converter.toWideKatakana(text);
                    }
                    addCandidate(firstKey, value, text);
                }
            }
        }

        text = Converter.toWideKatakana(key);
        addCandidate(0, key, text, text);
        addCandidate(0, key, key, key);

        mCandidateNum = mCandidateText.size();
        mInputView.setCandidates(mCandidateText);

        mCandidateIndex = 0;
        mInputView.selectCandidate(mCandidateIndex);
        icSetComposingText(mCandidateText.get(mCandidateIndex));

        mInputView.setSpaceButtonLabel("選択");
    }
}

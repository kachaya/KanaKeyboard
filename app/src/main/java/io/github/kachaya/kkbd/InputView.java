package io.github.kachaya.kkbd;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class InputView extends LinearLayout {

    private final static int KEYBOARD_QWERTY_HALF_NORMAL = 0;
    private final static int KEYBOARD_QWERTY_HALF_SHIFT = 1;
    private final static int KEYBOARD_QWERTY_WIDE_NORMAL = 2;
    private final static int KEYBOARD_QWERTY_WIDE_SHIFT = 3;
    private final static int KEYBOARD_HIRAGANA_WIDE_NORMAL = 4;
    private final static int KEYBOARD_HIRAGANA_WIDE_SHIFT = 5;
    private final static int KEYBOARD_KATAKANA_WIDE_NORMAL = 6;
    private final static int KEYBOARD_KATAKANA_WIDE_SHIFT = 7;
    private final static int KEYBOARD_KATAKANA_HALF_NORMAL = 8;
    private final static int KEYBOARD_KATAKANA_HALF_SHIFT = 9;
    // ソフトキーボードに表示する文字[10種類][4行][12列]
    private final static int KEYBOARD_ROWS = 4;
    private final static int KEYBOARD_COLS = 12;
    private final String[][][] keyboardLabel = {
            {   // 0:QwertyHalfNormal
                    {"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "-", "^"},
                    {"q", "w", "e", "r", "t", "y", "u", "i", "o", "p", "@", "["},
                    {"a", "s", "d", "f", "g", "h", "j", "k", "l", ";", ":", "]"},
                    {"z", "x", "c", "v", "b", "n", "m", ",", ".", "/", "\\", "\u00A5"},
            },
            {   // 1:QwertyHalfShift
                    {"!", "\"", "#", "$", "%", "&", "'", "(", ")", "", "=", "~"},
                    {"Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P", "`", "{"},
                    {"A", "S", "D", "F", "G", "H", "J", "K", "L", "+", "*", "}"},
                    {"Z", "X", "C", "V", "B", "N", "M", "<", ">", "?", "_", "|"},
            },
            {   // 2:QwertyWideNormal
                    {"１", "２", "３", "４", "５", "６", "７", "８", "９", "０", "－", "＾"},
                    {"ｑ", "ｗ", "ｅ", "ｒ", "ｔ", "ｙ", "ｕ", "ｉ", "ｏ", "ｐ", "＠", "［"},
                    {"ａ", "ｓ", "ｄ", "ｆ", "ｇ", "ｈ", "ｊ", "ｋ", "ｌ", "；", "：", "］"},
                    {"ｚ", "ｘ", "ｃ", "ｖ", "ｂ", "ｎ", "ｍ", "，", "．", "／", "＼", "￥"},
            },
            {   // 3:QwertyWideShift
                    {"！", "”", "＃", "＄", "％", "＆", "’", "（", "）", "", "＝", "～"},
                    {"Ｑ", "Ｗ", "Ｅ", "Ｒ", "Ｔ", "Ｙ", "Ｕ", "Ｉ", "Ｏ", "Ｐ", "｀", "｛"},
                    {"Ａ", "Ｓ", "Ｄ", "Ｆ", "Ｇ", "Ｈ", "Ｊ", "Ｋ", "Ｌ", "＋", "＊", "｝"},
                    {"Ｚ", "Ｘ", "Ｃ", "Ｖ", "Ｂ", "Ｎ", "Ｍ", "＜", "＞", "？", "＿", "｜"},
            },
            {   // 4:HiraganaWideNormal
                    {"ぬ", "ふ", "あ", "う", "え", "お", "や", "ゆ", "よ", "わ", "ほ", "へ"},
                    {"た", "て", "い", "す", "か", "ん", "な", "に", "ら", "せ", "゛", "゜"},
                    {"ち", "と", "し", "は", "き", "く", "ま", "の", "り", "れ", "け", "む"},
                    {"つ", "さ", "そ", "ひ", "こ", "み", "も", "ね", "る", "め", "ろ", "ー"},
            },
            {   // 5:HiraganaWideShift
                    {"", "", "ぁ", "ぅ", "ぇ", "ぉ", "ゃ", "ゅ", "ょ", "を", "", ""},
                    {"", "", "ぃ", "", "ゕ", "", "ゐ", "ゑ", "～", "ゝ", "ゞ", "「"},
                    {"", "", "", "", "", "", "", "", "", "", "ゖ", "」"},
                    {"っ", "", "", "（", "）", "？", "！", "、", "。", "・", "", ""},
            },
            {   // 6:KatakanaWideNormal
                    {"ヌ", "フ", "ア", "ウ", "エ", "オ", "ヤ", "ユ", "ヨ", "ワ", "ホ", "ヘ"},
                    {"タ", "テ", "イ", "ス", "カ", "ン", "ナ", "ニ", "ラ", "セ", "゛", "゜"},
                    {"チ", "ト", "シ", "ハ", "キ", "ク", "マ", "ノ", "リ", "レ", "ケ", "ム"},
                    {"ツ", "サ", "ソ", "ヒ", "コ", "ミ", "モ", "ネ", "ル", "メ", "ロ", "ー"},
            },
            {   // 7:KatakanaWideShift
                    {"", "", "ァ", "ゥ", "ェ", "ォ", "ャ", "ュ", "ョ", "ヲ", "", ""},
                    {"", "", "ィ", "", "ヵ", "", "ヰ", "ヱ", "～", "ヽ", "ヾ", "「"},
                    {"", "", "", "", "", "", "", "", "", "", "ヶ", "」"},
                    {"ッ", "", "", "（", "）", "？", "！", "、", "。", "・", "", ""},
            },
            {   // 8:KatakanaHalfNormal
                    {"ﾇ", "ﾌ", "ｱ", "ｳ", "ｴ", "ｵ", "ﾔ", "ﾕ", "ﾖ", "ﾜ", "ﾎ", "ﾍ"},
                    {"ﾀ", "ﾃ", "ｲ", "ｽ", "ｶ", "ﾝ", "ﾅ", "ﾆ", "ﾗ", "ｾ", "ﾞ", "ﾟ"},
                    {"ﾁ", "ﾄ", "ｼ", "ﾊ", "ｷ", "ｸ", "ﾏ", "ﾉ", "ﾘ", "ﾚ", "ｹ", "ﾑ"},
                    {"ﾂ", "ｻ", "ｿ", "ﾋ", "ｺ", "ﾐ", "ﾓ", "ﾈ", "ﾙ", "ﾒ", "ﾛ", "ｰ"},
            },
            {   // 9:KatakanaHalfShift
                    {"", "", "ｧ", "ｩ", "ｪ", "ｫ", "ｬ", "ｭ", "ｮ", "ｦ", "", ""},
                    {"", "", "ｨ", "", "", "", "", "", "", "", "", "｢"},
                    {"", "", "", "", "", "", "", "", "", "", "", "｣"},
                    {"ｯ", "", "", "", "", "", "", "､", "｡", "･", "", ""},
            }
    };
    private final InputService mInputService;
    private final Context mContext;
    private final LinearLayout mCandidatesLayout;
    private final HorizontalScrollView mCandidatesView;
    private final int mColorCandidateSelect;
    private final int mColorCandidateNormal;
    //
    private final ImageButton mShiftButton;
    private final Button mModeButton;
    private final Button mKeyboardButton;
    private final Button mSpaceButton;
    private final Button mCursorLeftButton;
    private final Button mCursorRightButton;
    private final Button mBackspaceButton;
    private final Button mEnterButton;
    private final Button[][] mCharacterButton;
    //
    private int mKeyboard;
    private Button[] mCandidateButton;

    public InputView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        mInputService = (InputService) context;

        mColorCandidateSelect = ContextCompat.getColor(mContext, R.color.gray_4);
        mColorCandidateNormal = ContextCompat.getColor(mContext, R.color.gray_2);

        mKeyboard = KEYBOARD_QWERTY_HALF_NORMAL;

        View layout = LayoutInflater.from(context).inflate(R.layout.input, this);

        mCandidatesView = layout.findViewById(R.id.candidate_view);
        mCandidatesLayout = layout.findViewById(R.id.candidates_layout);

        LinearLayout[] rows = new LinearLayout[KEYBOARD_ROWS];
        rows[0] = layout.findViewById(R.id.kyeboard_raw0);
        rows[1] = layout.findViewById(R.id.kyeboard_raw1);
        rows[2] = layout.findViewById(R.id.kyeboard_raw2);
        rows[3] = layout.findViewById(R.id.kyeboard_raw3);

        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1.0f);
        int style = R.style.CharacterButton;
        mCharacterButton = new Button[KEYBOARD_ROWS][KEYBOARD_COLS];
        for (int row = 0; row < KEYBOARD_ROWS; row++) {
            for (int col = 0; col < KEYBOARD_COLS; col++) {
                Button b = new Button(new ContextThemeWrapper(mContext, style), null, style);
                b.setLayoutParams(lp);
                b.setOnClickListener(v -> onClickCharacterButton((Button) v));
                b.setText(keyboardLabel[KEYBOARD_QWERTY_HALF_NORMAL][row][col]);
                rows[row].addView(b);
                mCharacterButton[row][col] = b;
            }
        }

        mShiftButton = layout.findViewById(R.id.button_shift);
        mShiftButton.setOnClickListener(this::onClickFunctionButton);
        mKeyboardButton = layout.findViewById(R.id.button_keyboard);
        mKeyboardButton.setOnClickListener(this::onClickFunctionButton);
        mModeButton = layout.findViewById(R.id.button_mode);
        mModeButton.setOnClickListener(this::onClickFunctionButton);
        mSpaceButton = layout.findViewById(R.id.button_space);
        mSpaceButton.setOnClickListener(this::onClickFunctionButton);
        mCursorLeftButton = layout.findViewById(R.id.button_cursor_left);
        mCursorLeftButton.setOnClickListener(this::onClickFunctionButton);
        mCursorRightButton = layout.findViewById(R.id.button_cursor_right);
        mCursorRightButton.setOnClickListener(this::onClickFunctionButton);
        mBackspaceButton = layout.findViewById(R.id.button_backspace);
        mBackspaceButton.setOnClickListener(this::onClickFunctionButton);
        mEnterButton = layout.findViewById(R.id.button_enter);
        mEnterButton.setOnClickListener(this::onClickFunctionButton);
    }

    // 機能ボタンのクリックハンドラ
    public void onClickFunctionButton(View view) {
        int id = view.getId();
        //Log.d("onClickFunctionButton", "id=" + id);

        if (id == R.id.button_backspace) {
            mInputService.handleBackspace();
        } else if (id == R.id.button_enter) {
            mInputService.handleEnter();
        } else if (id == R.id.button_cursor_left) {
            mInputService.handleCursorLeft();
        } else if (id == R.id.button_cursor_right) {
            mInputService.handleCursorRight();
        } else if (id == R.id.button_space) {
            mInputService.handleSpace();
        } else if (id == R.id.button_keyboard) {
            mInputService.handleKeyboard();
        } else if (id == R.id.button_mode) {
            mInputService.handleMode();
        } else if (id == R.id.button_shift) {
            mInputService.handleShift();
        }
    }

    // 文字ボタンのクリックハンドラ
    private void onClickCharacterButton(Button b) {
        CharSequence cs = b.getText();
        for (int i = 0; i < cs.length(); i++) {
            mInputService.processChar(cs.charAt(i));
        }
    }

    public void setKeyboard(int inputMode, int shiftState) {
        switch (inputMode) {
            default:
            case InputService.INPUT_MODE_QWERTY_HALF:
                if (shiftState == InputService.SHIFT_STATE_NONE) {
                    mKeyboard = KEYBOARD_QWERTY_HALF_NORMAL;
                } else {
                    mKeyboard = KEYBOARD_QWERTY_HALF_SHIFT;
                }
                mKeyboardButton.setText("英");
                mModeButton.setText("半");
                break;
            case InputService.INPUT_MODE_QWERTY_WIDE:
                if (shiftState == InputService.SHIFT_STATE_NONE) {
                    mKeyboard = KEYBOARD_QWERTY_WIDE_NORMAL;
                } else {
                    mKeyboard = KEYBOARD_QWERTY_WIDE_SHIFT;
                }
                mKeyboardButton.setText("英");
                mModeButton.setText("全");
                break;
            case InputService.INPUT_MODE_HIRAGANA_WIDE:
                if (shiftState == InputService.SHIFT_STATE_NONE) {
                    mKeyboard = KEYBOARD_HIRAGANA_WIDE_NORMAL;
                } else {
                    mKeyboard = KEYBOARD_HIRAGANA_WIDE_SHIFT;
                }
                mKeyboardButton.setText("日");
                mModeButton.setText("あ");
                break;
            case InputService.INPUT_MODE_KATAKANA_WIDE:
                if (shiftState == InputService.SHIFT_STATE_NONE) {
                    mKeyboard = KEYBOARD_KATAKANA_WIDE_NORMAL;
                } else {
                    mKeyboard = KEYBOARD_KATAKANA_WIDE_SHIFT;
                }
                mKeyboardButton.setText("日");
                mModeButton.setText("ア");
                break;
            case InputService.INPUT_MODE_KATAKANA_HALF:
                if (shiftState == InputService.SHIFT_STATE_NONE) {
                    mKeyboard = KEYBOARD_KATAKANA_HALF_NORMAL;
                } else {
                    mKeyboard = KEYBOARD_KATAKANA_HALF_SHIFT;
                }
                mKeyboardButton.setText("日");
                mModeButton.setText("ｶﾅ");
                break;
        }
        for (int row = 0; row < KEYBOARD_ROWS; row++) {
            for (int col = 0; col < KEYBOARD_COLS; col++) {
                mCharacterButton[row][col].setText(keyboardLabel[mKeyboard][row][col]);
            }
        }

        // Shiftボタンのキートップ
        switch (shiftState) {
            default:
            case InputService.SHIFT_STATE_NONE:
                mShiftButton.setImageResource(R.drawable.ic_shift_none);
                break;
            case InputService.SHIFT_STATE_SINGLE:
                mShiftButton.setImageResource(R.drawable.ic_shift_single);
                break;
            case InputService.SHIFT_STATE_LOCK:
                mShiftButton.setImageResource(R.drawable.ic_shift_lock);
                break;
        }
    }

    // 候補ボタンクリックハンドラ
    private void onClickCandidateButton(Button b) {
        int index = (int) b.getTag();
        mInputService.clickCandidate(index);
    }

    public void clearCandidates() {
        mCandidatesLayout.removeAllViews();
        mCandidatesView.setVisibility(INVISIBLE);
        mCandidateButton = null;
    }

    public void setCandidates(ArrayList<String> candidates) {
        clearCandidates();
        if (candidates == null) {
            return;
        }
        mCandidateButton = new Button[candidates.size()];
        int style = R.style.CandidateButton;
        for (int i = 0; i < candidates.size(); i++) {
            Button b = new Button(new ContextThemeWrapper(mContext, style), null, style);
            b.setOnClickListener(v -> onClickCandidateButton((Button) v));
            b.setTag(i);
            b.setText(candidates.get(i));
            b.setBackgroundColor(mColorCandidateNormal);
            mCandidatesLayout.addView(b);
            mCandidateButton[i] = b;
        }
        mCandidatesView.setVisibility(VISIBLE);     // 候補表示
    }

    // 候補ボタンを選択状態にする
    public void selectCandidate(int index) {
        if (mCandidateButton == null) {
            return;
        }
        Button b;
        int cX = mCandidatesView.getScrollX();
        int cW = mCandidatesView.getWidth();
        for (int i = 0; i < mCandidateButton.length; i++) {
            b = mCandidateButton[i];
            if (i == index) {
                // 見える場所にスクロールする
                int bT = b.getTop();
                int bL = b.getLeft();
                int bR = b.getRight();
                if (bL < cX) {
                    mCandidatesView.scrollTo(bL, bT);
                }
                if (bR > (cX + cW)) {
                    mCandidatesView.scrollTo(bR - cW, bT);
                }
                b.setBackgroundColor(mColorCandidateSelect);
            } else {
                b.setBackgroundColor(mColorCandidateNormal);
            }
        }
    }

    public void setSpaceButtonLabel(String label) {
        mSpaceButton.setText(label);
    }

}

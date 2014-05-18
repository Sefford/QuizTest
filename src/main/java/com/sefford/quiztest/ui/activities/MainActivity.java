package com.sefford.quiztest.ui.activities;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.sefford.quiztest.R;
import com.sefford.quiztest.core.datamodel.JsonSource;
import com.sefford.quiztest.core.datamodel.Question;
import com.sefford.quiztest.core.datamodel.QuestionModel;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends Activity implements Handler.Callback {

    private static final int MSG_CORRECT = 0x1010;
    private static final int MSG_INCORRECT = 0x101;
    private static final int MSG_FETCH = 0x10;
    private static final int MAX_QUESTIONS = 10;
    public static final int TRANSITION_SOLUTION_DURATION = 240;
    public static final int SOLUTION_TRANSITION_DURATION = TRANSITION_SOLUTION_DURATION;
    public static final int SHOW_NEXT_DELAY = 1240;
    public static final int QUESTION_ANIMATION_DURATION = 360;
    public static final int EDGE_LAG_SIMULATOR = 2000;

    private Handler handler = new Handler(this);

    private ProgressBar pbProgress;
    private TextView tvQuestion;
    private LinearLayout llAnswers;
    private ImageView ivIcon;
    private View rlQuestion;
    private MenuItem miScore;

    private int verticalPadding;

    private Random random = new Random();
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TransitionDrawable drawable = (TransitionDrawable) v.getBackground();
            drawable.startTransition(TRANSITION_SOLUTION_DURATION);
            Boolean response = (Boolean) v.getTag();
            if (!response) {
                Vibrator vibrator = (Vibrator) getBaseContext().getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(TRANSITION_SOLUTION_DURATION);
                showCorrectAnswer();
            }
            handler.sendEmptyMessageDelayed(response ? MSG_CORRECT : MSG_INCORRECT, SHOW_NEXT_DELAY);
        }
    };

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *                           previously being shut down then this Bundle contains the data it most
     *                           recently supplied in onSaveInstanceState(Bundle). <b>Note: Otherwise it is null.</b>
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_quiz);
        mapGUI();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (QuestionModel.getInstance().getCurrentQuestion() != null) {
            pbProgress.setIndeterminate(false);
            pbProgress.setProgress(QuestionModel.getInstance().getProgress());
            populateQuestionView(QuestionModel.getInstance().getCurrentQuestion());
            updateScore();
            rlQuestion.setAlpha(1);
        } else {
            handler.sendEmptyMessageDelayed(MSG_FETCH, EDGE_LAG_SIMULATOR);
        }
    }

    private void updateScore() {
        miScore.setTitle(String.format("%d points", QuestionModel.getInstance().getPoints()));
    }

    private void mapGUI() {
        pbProgress = (ProgressBar) findViewById(R.id.pb_progress);
        tvQuestion = (TextView) findViewById(R.id.tv_question);
        llAnswers = (LinearLayout) findViewById(R.id.ll_answers);
        ivIcon = (ImageView) findViewById(R.id.iv_theme);
        rlQuestion = findViewById(R.id.rl_question);

        verticalPadding = getResources().getDimensionPixelSize(R.dimen.answer_vertical_padding);
        pbProgress.setIndeterminate(true);
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_CORRECT:
                QuestionModel.getInstance().setPoints(QuestionModel.getInstance().getPoints() +
                        QuestionModel.getInstance().getCurrentQuestion().getDifficulty() *
                                QuestionModel.getInstance().getCurrentQuestion().getBasePoints());
                updateScore();
            case MSG_INCORRECT:
                prepareProgressAnimation(pbProgress,
                        pbProgress.getProgress() + (pbProgress.getMax() / MAX_QUESTIONS)).start();
                prepareFadeCurrentQuestion(rlQuestion).start();
                break;
            case MSG_FETCH:
                new FetchInfo().execute();
                break;
        }
        return false;
    }


    private void showCorrectAnswer() {
        for (int i = 0; i < llAnswers.getChildCount(); i++) {
            if ((Boolean) llAnswers.getChildAt(i).getTag()) {
                TransitionDrawable drawable = (TransitionDrawable) llAnswers.getChildAt(i).getBackground();
                drawable.startTransition(SOLUTION_TRANSITION_DURATION);
            }
        }
    }

    private void setNextQuestion() {
        populateQuestionView(QuestionModel.getInstance().getNextQuestion(random));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        miScore = menu.getItem(0);
        updateScore();
        return super.onCreateOptionsMenu(menu);
    }

    private void populateQuestionView(Question question) {
        tvQuestion.setText(question.getQuestionText());
        chooseTheme(question.getCategory());
        List<String> answers = new ArrayList<String>(question.getIncorrectAnswers());
        answers.add(question.getCorrectAnswer());
        Collections.shuffle(answers);
        llAnswers.removeAllViews();
        for (String answer : answers) {
            TextView textView = createAnswerButton(question, answer);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
            params.setMargins(0,
                    llAnswers.getChildCount() > 0 ? verticalPadding : 0, 0,
                    llAnswers.getChildCount() > 3 ? 0 : verticalPadding);
            params.weight = 1;

            llAnswers.addView(textView, params);
        }
    }

    private TextView createAnswerButton(Question question, String answer) {
        TextView textView = new TextView(this);
        textView.setTextAppearance(this, android.R.style.TextAppearance_Large);
        textView.setText(answer);
        textView.setGravity(Gravity.CENTER);
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setOnClickListener(listener);
        textView.setTag(answer.equals(question.getCorrectAnswer()));
        textView.setPadding(verticalPadding, 0, verticalPadding, 0);
        textView.setBackgroundResource(answer.equals(QuestionModel.getInstance().getCurrentQuestion().getCorrectAnswer()) ?
                R.drawable.answer_background_correct_transition : R.drawable.answer_background_incorrect_transition);
        return textView;
    }

    private void chooseTheme(int category) {
        int resource = 0;
        switch (category) {
            case Question.GENERAL_KNOWLEDGE:
                resource = R.drawable.general_icon;
                break;
            case Question.ENTERTAINMENT:
                resource = R.drawable.movies_icon;
                break;
            case Question.SPORTS:
                resource = R.drawable.sports_icon;
                break;
        }
        ivIcon.setImageResource(resource);
    }

    private Animator prepareProgressAnimation(View targetView, int targetValue) {
        Animator animation = ObjectAnimator.ofInt(targetView, "progress", pbProgress.getProgress(), targetValue);
        animation.setDuration(QUESTION_ANIMATION_DURATION);
        animation.setInterpolator(new AccelerateDecelerateInterpolator());
        return animation;
    }

    private Animator prepareFadeCurrentQuestion(final View targetView) {
        Animator animation = ObjectAnimator.ofFloat(targetView, View.ALPHA, targetView.getAlpha(), 0);
        animation.setDuration(QUESTION_ANIMATION_DURATION);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                targetView.setTranslationX(targetView.getHeight());
                setNextQuestion();
                prepareShowNextQuestionAnimation(targetView).start();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animation;
    }

    private Animator prepareShowNextQuestionAnimation(final View targetView) {
        Animator animation = ObjectAnimator.ofFloat(targetView, View.TRANSLATION_X, targetView.getWidth(), 0);
        animation.setDuration(480);
        animation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                targetView.setAlpha(1);
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        return animation;
    }

    /**
     * This class will simulate the fetching of the info from the API
     */
    private class FetchInfo extends AsyncTask<Void, Void, List<Question>> {

        @Override
        protected List<Question> doInBackground(Void... params) {
            Type listType = new TypeToken<ArrayList<Question>>() {
            }.getType();
            List<Question> questions = new Gson().fromJson(JsonSource.SOURCE, listType);
            return questions;
        }

        @Override
        protected void onPostExecute(List<Question> questions) {
            QuestionModel.getInstance().setQuestionList(questions);
            pbProgress.setIndeterminate(false);
            prepareProgressAnimation(pbProgress,
                    pbProgress.getProgress() + (pbProgress.getMax() / MAX_QUESTIONS)).start();
            setNextQuestion();
            prepareShowNextQuestionAnimation(rlQuestion).start();
        }
    }
}


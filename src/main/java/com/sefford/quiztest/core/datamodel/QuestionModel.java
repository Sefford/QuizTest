package com.sefford.quiztest.core.datamodel;

import java.util.List;
import java.util.Random;

/**
 * Model for a group of questions
 *
 * @author Saul Diaz <sefford@gmail.com>
 */
public class QuestionModel {

    // Single instance of the model
    public static QuestionModel INSTANCE;
    // List of questions
    private List<Question> questionList;
    // Current questions
    private Question currentQuestion;
    // Progress of the test
    private int progress;
    // Points of the user
    int points;

    private QuestionModel() {
    }

    public static QuestionModel getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new QuestionModel();
        }
        return INSTANCE;
    }

    public List<Question> getQuestionList() {
        return questionList;
    }

    public void setQuestionList(List<Question> questionList) {
        this.questionList = questionList;
    }

    public Question getCurrentQuestion() {
        return currentQuestion;
    }

    public void setCurrentQuestion(Question currentQuestion) {
        this.currentQuestion = currentQuestion;
    }

    public Question getNextQuestion(Random random) {
        setCurrentQuestion(questionList.get(random.nextInt(questionList.size())));
        return getCurrentQuestion();
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}

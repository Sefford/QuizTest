package com.sefford.quiztest.core.datamodel;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Basic question Model
 *
 * @author Saul Diaz <sefford@gmail.com>
 */
public class Question {

    // Existing Categories
    public static final int GENERAL_KNOWLEDGE = 1;
    public static final int ENTERTAINMENT = 2;
    public static final int SPORTS = 3;

    @SerializedName("QuestionID")
    long questionId;
    @SerializedName("Difficulty")
    int difficulty;
    @SerializedName("Points")
    int basePoints;
    @SerializedName("Zone")
    int category;
    @SerializedName("Incorrect")
    List<String> incorrectAnswers;
    @SerializedName("Correct")
    String correctAnswer;
    @SerializedName("Question")
    String questionText;

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getBasePoints() {
        return basePoints;
    }

    public void setBasePoints(int basePoints) {
        this.basePoints = basePoints;
    }

    public int getCategory() {
        return category;
    }

    public void setCategory(int category) {
        this.category = category;
    }

    public List<String> getIncorrectAnswers() {
        return incorrectAnswers;
    }

    public void setIncorrectAnswers(List<String> incorrectAnswers) {
        this.incorrectAnswers = incorrectAnswers;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }
}

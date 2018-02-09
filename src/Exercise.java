import javafx.util.Pair;

import javax.swing.*;
import java.util.List;

/**
 * Klasa przechowująca pytanie wraz z odpowiedziami (może to być pytanie wraz z jedną odpowiedzią oraz pytanie z odpowiedzami do uszegregowania)
 */
public class Exercise {
    private String question;
    private List<Pair<String, Boolean>> answers;

    public Exercise(String question, List<Pair<String, Boolean>> answers) {
        this.question = question;
        this.answers = answers;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<Pair<String, Boolean>> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Pair<String, Boolean>> answers) {
        this.answers = answers;
    }

    /**
     * Metoda sprawdzająca czy kolejność odpowiedzi w JTextFieldach jest prawidłowa
     * @param textFields lista JTextFieldów, w których zapisane są odpowiedzi, do danego pytania
     * @return true, jeśli tak, false, jeśli nie
     */
    public boolean checkQuestionOrder(List<JTextField> textFields){
        for(int i=0;i<answers.size();i++){
            if(!answers.get(i).getKey().equals(textFields.get(i).getText())) return false;
        }
        return true;
    }
}

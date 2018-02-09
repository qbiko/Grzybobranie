import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import javafx.util.Pair;
import org.w3c.dom.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;



/**
 * Główna klasa zawierająca cały proces gry i odpowiadająca za tworzenie wyglądu.
 */
public class MainProcess {
    private static final String pathToResources = "resources/";
    private static final String pathToRanking = pathToResources + "ranking.xml";
    private static List<Exercise> exerciseList = new ArrayList<>();
    private static Set<Integer> questionOrder;
    private static JFrame frame;
    private static JPanel mainPanel;
    private static final int numberOfQuestions = 5;
    private static final Random generator = new Random();
    private static Score score;
    private static List<Score> scoreList = new ArrayList<>();
    private static String name = "";
    private static List<JTextField> answerTextFields;

    public static void main(String[] args) throws Exception {
        loadRanking();
        frame = new JFrame("Grzybobranie");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(800, 600));

        mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
        mainPanel.setMaximumSize(new Dimension(100,100));

        createMenuView();

        frame.add(mainPanel);
        frame.pack();
        frame.setVisible(true);
    }

    /**
     * Metoda tworząca JLabel wraz z odpowiednim obrazkiem, który dodawany jest na górze okna gry.
     * @param target nazwa pliku obrazka, który ma zostać użyty
     * @return JLabel z odpowiednim obrazkiem
     */
    private static JLabel addTopImage(String target){
        ImageIcon targetImage = new ImageIcon(pathToResources + target + ".png");
        JLabel targetLabel = new JLabel(targetImage);

        return targetLabel;
    }

    /**
     * Metoda tworząca widok głównego menu
     */
    private static void createMenuView(){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("menu"));

        JButton quizButton = new JButton("Quiz");
        quizButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createStartGameView("quiz");
            }
        }));

        JButton dragAndDropButton = new JButton("Przeciąganie");
        dragAndDropButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createStartGameView("przeciaganie");
            }
        }));

        JButton rankingButton = new JButton("Ranking");
        rankingButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    createRankingView();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }));
        mainPanel.add(quizButton);
        mainPanel.add(dragAndDropButton);
        mainPanel.add(rankingButton);
        mainPanel.add(createEndGameButton());

        mainPanel.setVisible(true);
    }

    /**
     * Metoda ładująca pytania z pliku xml do odpowiedniej zmiennej
     * @param source nazwa pliku, z którego mają zostać zaczytane pytania
     * @throws Exception
     */
    private static void loadQuestions(String source) throws Exception {
        exerciseList.clear();
        File fXmlFile = new File(pathToResources + source);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("exercise");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String question = eElement.getElementsByTagName("question").item(0).getTextContent();
                NodeList answersXML = eElement.getElementsByTagName("answer");
                List<Pair<String,Boolean>> answers = new ArrayList<>();

                for (int i = 0; i < answersXML.getLength(); i++) {
                    Element answerElement = (Element) answersXML.item(i);
                    String answer = answerElement.getTextContent();
                    Boolean ifCorrect = Boolean.parseBoolean(answerElement.getAttribute("correct"));

                    answers.add(new Pair<>(answer, ifCorrect));
                }
                exerciseList.add(new Exercise(question, answers));
            }
        }
    }

    /**
     * Metoda tworząca widok startu gry, czyli miejsce w którym użytkownik podaje swoje imię.
     * @param topImage Tekst, po którym rozpoznajemy jaki obraz ma być na górze ekranu gry.
     */
    private static void createStartGameView(String topImage){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage(topImage));

        final JTextArea placeForName = new JTextArea();
        placeForName.setMaximumSize(new Dimension(200,30));

        JButton startButton = new JButton("Rozpocznij grę");
        startButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        startButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                name = placeForName.getText();
                if(topImage.equals("quiz")) createChooseLevelView();
                else {
                    try {
                        loadQuestions("przeciaganie.xml");
                        questionOrder = getQuestionOrder();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    score = new Score(4, name);
                    createDragAndDropView();
                }

            }
        }));

        mainPanel.add(placeForName);
        mainPanel.add(startButton);
        mainPanel.setVisible(true);
    }

    /**
     * Metoda tworząca widok wyboru poziomu do quizu.
     */
    private static void createChooseLevelView(){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("quiz"));

        JButton easyLevelButton = new JButton("Łatwy");
        easyLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        easyLevelButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    loadQuestions("latwy.xml");
                    questionOrder = getQuestionOrder();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                score = new Score(1, name);
                createQuestionView();
            }
        }));
        mainPanel.add(easyLevelButton);

        JButton mediumLevelButton = new JButton("Średni");
        mediumLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        mediumLevelButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    loadQuestions("sredni.xml");
                    questionOrder = getQuestionOrder();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                score = new Score(2, name);
                createQuestionView();
            }
        }));
        mainPanel.add(mediumLevelButton);

        JButton difficultLevelButton = new JButton("Trudny");
        difficultLevelButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        difficultLevelButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    loadQuestions("trudny.xml");
                    questionOrder = getQuestionOrder();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
                score = new Score(3, name);
                createQuestionView();
            }
        }));
        mainPanel.add(difficultLevelButton);

        mainPanel.setVisible(true);
    }

    /**
     * Metoda tworząca widok pytania wraz z odpowiedziami do wyboru.
     */
    private static void createQuestionView(){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("quiz"));

        int questionIndex = questionOrder.iterator().next();
        JTextField questionField = new JTextField(exerciseList.get(questionIndex).getQuestion());
        mainPanel.add(questionField);

        answerTextFields = new ArrayList<>();

        Set<Integer> answersOrder = getAnswersOrder(exerciseList.get(questionIndex));

        for(Integer answerIndex : answersOrder){
            Pair<String, Boolean> answer = exerciseList.get(questionIndex).getAnswers().get(answerIndex);
            JButton answerButton = new JButton(answer.getKey());
            answerButton.addActionListener((new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    if(answer.getValue()){
                        score.incrementPoints();
                    }
                    questionOrder.remove(questionIndex);
                    if(questionOrder.size()>0){
                        createQuestionView();
                    }
                    else{
                        scoreList.add(score);
                        try {
                            saveNewRanking();
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                        createEndGameView("quiz");
                    }

                }
            }));
            mainPanel.add(answerButton);
        }
        mainPanel.setVisible(true);
    }

    /**
     * Metoda tworząca widok pytania wraz z odpowiedziami do uszeregowania.
     */
    private static void createDragAndDropView(){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("przeciaganie"));

        int questionIndex = questionOrder.iterator().next();
        JLabel questionField = new JLabel(exerciseList.get(questionIndex).getQuestion());
        mainPanel.add(questionField);

        answerTextFields = new ArrayList<>();

        Set<Integer> answersOrder = getAnswersOrder(exerciseList.get(questionIndex));

        for(Integer answerIndex : answersOrder){
            Pair<String, Boolean> answer = exerciseList.get(questionIndex).getAnswers().get(answerIndex);
            JTextField answerTextField = new JTextField(answer.getKey());
            answerTextField.addMouseListener(new MouseListener() {
                @Override
                public void mouseClicked(MouseEvent e) {}

                @Override
                public void mousePressed(MouseEvent e) {
                    JComponent jc = (JComponent)e.getSource();
                    TransferHandler th = jc.getTransferHandler();
                    th.exportAsDrag(jc, e, TransferHandler.COPY);
                }

                @Override
                public void mouseReleased(MouseEvent e) {}

                @Override
                public void mouseEntered(MouseEvent e) {}

                @Override
                public void mouseExited(MouseEvent e) {}
            });
            answerTextField.setDragEnabled(true);
            answerTextField.setTransferHandler(new CustomTransferHandler());
            mainPanel.add(answerTextField);

            answerTextFields.add(answerTextField);
        }
        JButton nextQuestionButton = new JButton("Dalej");
        nextQuestionButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(exerciseList.get(questionIndex).checkQuestionOrder(answerTextFields)){
                    score.incrementPoints();
                }
                questionOrder.remove(questionIndex);
                if(questionOrder.size()>0){
                    createDragAndDropView();
                }
                else{
                    scoreList.add(score);
                    try {
                        saveNewRanking();
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                    createEndGameView("przeciaganie");
                }
            }
        }));
        mainPanel.add(nextQuestionButton);
        mainPanel.setVisible(true);
    }

    /**
     * Metoda wylosowująca kolejność wyświetlanych pytań.
     * @return set, z pomieszanymi indeksami pytań
     */
    private static Set<Integer> getQuestionOrder(){
        Set<Integer> generated = new LinkedHashSet<Integer>();
        while (generated.size() < numberOfQuestions)
        {
            Integer next = generator.nextInt(exerciseList.size());
            generated.add(next);
        }
        return generated;
    }

    /**
     * Metoda wylosowująca kolejność wyświetlanych odpowiedzi.
     * @param exercise zadanie, do którego ma zostać wylosowana kolejność odpowiedzi
     * @return set, z pomieszanymi indeksami odpowiedzi
     */
    private static Set<Integer> getAnswersOrder(Exercise exercise){
        Set<Integer> generated = new LinkedHashSet<Integer>();
        while (generated.size() < exercise.getAnswers().size())
        {
            Integer next = generator.nextInt(exercise.getAnswers().size());
            generated.add(next);
        }
        return generated;
    }

    /**
     * Metoda tworząca widok końca gry.
     * @param type typ gry, w którą toczyła się rozgrywka
     */
    private static void createEndGameView(String type){
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("quiz"));

        JTextField questionField = new JTextField("Koniec gry! Zdobyłeś " + score.getPoints() + " punkt/y.");
        mainPanel.add(questionField);

        JButton rankingButton = new JButton("Zobacz ranking");
        rankingButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    createRankingView();
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        }));
        mainPanel.add(rankingButton);

        mainPanel.add(createOneMoreTimeButton(type));

        mainPanel.add(createEndGameButton());

        mainPanel.setVisible(true);
    }

    /**
     * Metoda zapisująca do pliku xml, ranking wyników.
     * @throws Exception
     */
    private static void saveNewRanking() throws Exception {
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        Document doc = docBuilder.newDocument();
        Element rootElement = doc.createElement("ranking");
        doc.appendChild(rootElement);

        for (Score oneScore : scoreList) {
            Element scoreElement = doc.createElement("score");
            rootElement.appendChild(scoreElement);

            Element name = doc.createElement("name");
            name.appendChild(doc.createTextNode(oneScore.getName()));
            scoreElement.appendChild(name);

            Element points = doc.createElement("points");
            points.appendChild(doc.createTextNode(String.valueOf(oneScore.getPoints())));
            scoreElement.appendChild(points);
        }


        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(pathToRanking));

        transformer.transform(source, result);
    }

    /**
     * Metoda wczytująca z pliku xml ranking wyników do odpowiedniej zmiennej.
     * @throws Exception
     */
    private static void loadRanking() throws Exception {
        scoreList.clear();
        File fXmlFile = new File(pathToRanking);
        if(!fXmlFile.exists()) return;
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

        doc.getDocumentElement().normalize();
        NodeList nList = doc.getElementsByTagName("score");

        for (int temp = 0; temp < nList.getLength(); temp++) {
            Node nNode = nList.item(temp);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) nNode;
                String name = eElement.getElementsByTagName("name").item(0).getTextContent();
                int points = Integer.parseInt(eElement.getElementsByTagName("points").item(0).getTextContent());

                scoreList.add(new Score(name, points));
            }
        }
    }

    /**
     * Metoda tworząca widok wyświetlający najlepsze zapisane wyniki.
     * @throws Exception
     */
    private static void createRankingView() throws Exception {
        mainPanel.setVisible(false);
        mainPanel.removeAll();

        mainPanel.add(addTopImage("ranking"));

        loadRanking();
        scoreList.sort(new ScoreComparator());
        int position = 1;
        for (Score oneScore : scoreList)  {
            JTextField scoreField = new JTextField(position+ ". " + oneScore.getName() + " " + oneScore.getPoints() + " punkt/y");
            mainPanel.add(scoreField);
            position++;
        }


        mainPanel.add(createBackToMenuButton());

        mainPanel.add(createEndGameButton());

        mainPanel.setVisible(true);
    }

    /**
     * Metoda tworząca button 'Zagraj jeszcze raz' wraz z koniecznymi dla niego akcjami.
     * @return button 'Zagraj jeszcze raz'
     * @param type typ gry, w którą toczyła się rozgrywka
     */
    private static JButton createOneMoreTimeButton(String type){
        JButton oneMoreTimeButton = new JButton("Zagraj jeszcze raz");
        oneMoreTimeButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if(type.equals("quiz"))createChooseLevelView();
                else createDragAndDropView();
            }
        }));
        return oneMoreTimeButton;
    }

    /**
     * Metoda tworząca button 'Wróć do Menu' wraz z koniecznymi dla niego akcjami.
     * @return button 'Wróć do Menu'
     */
    private static JButton createBackToMenuButton(){
        JButton backToMenuButton = new JButton("Wróc do Menu");
        backToMenuButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                createMenuView();
            }
        }));
        return backToMenuButton;
    }

    /**
     * Metoda tworząca button 'Wyjście' wraz z koniecznymi dla niego akcjami.
     * @return button 'Wyjście'
     */
    private static JButton createEndGameButton(){
        JButton endGameButton = new JButton("Wyjście");
        endGameButton.addActionListener((new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        }));
        return endGameButton;
    }
}

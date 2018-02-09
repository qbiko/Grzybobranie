import java.util.Comparator;

/**
 * Klasa implementująca Comparator dla Score, tak by pierwszy w Liście był Score o największym wyniku
 */
public class ScoreComparator implements Comparator<Score> {
    @Override
    public int compare(Score o1, Score o2) {
        return o2.getPoints() - o1.getPoints();
    }
}

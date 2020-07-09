package strategy.table;

/**
 * Strategy factory method for convenient strategy generation.
 */
public class TableStrategyFactory {

    /**
     * Creates strategy object according to given strategy name.
     *
     * @param strategyName e.g "singleField", "multiField"
     * @return newly created {@link TableStrategy} corresponding to strategyName
     */
    public static TableStrategy createStrategy(String strategyName) {
        switch (strategyName) {
            case SingleField.NAME:
                return new SingleField();
            case MultiField.NAME:
                return new MultiField();
            default:
                throw new IllegalArgumentException("No strategy corresponding to " + strategyName);
        }
    }

}

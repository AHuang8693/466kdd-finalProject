package main;

public enum IrisLabel {
    IRIS_SETOSA("Iris-setosa"),
    IRIS_VERSICOLOR("Iris-versicolor"),
    IRIS_VIRGINICA("Iris-virginica");

    private final String label;

    IrisLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}

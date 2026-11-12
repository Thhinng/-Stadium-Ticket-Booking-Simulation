package view;

public abstract class BaseView {
    private final ConsoleView ui;

    protected BaseView(ConsoleView ui) {
        this.ui = ui;
    }

    protected ConsoleView getUi() {
        return this.ui;
    }
}

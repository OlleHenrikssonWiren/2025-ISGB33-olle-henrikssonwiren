package mflix;

public class Controller {
    private Model model;
    private View view;
    public Controller(Model model, View view) {
        this.model = model;
        this.view = view;
    }

    public void Listeners() {
        view.showResults(e -> {
            String inputText = view.getInput(); //ta input
            view.clearText(); // rensa tidigare resultat innan nya
            String result = model.fetch(inputText);
            view.setResultText(result);
        });
    }
}


package mflix;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    static void main(String[] args) {
        Model model = new Model();
        model.initMongo();
        View view = new View();
        Controller control = new Controller(model, view);
        control.Listeners();
    }
}
